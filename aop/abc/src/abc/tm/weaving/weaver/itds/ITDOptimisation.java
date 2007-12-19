/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Julian Tibble
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.tm.weaving.weaver.itds;

import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.TMStateMachine;

import soot.BooleanType;
import soot.IntType;
import soot.Modifier;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.VoidType;

import java.util.*;

public class ITDOptimisation
{
    protected TraceMatch tm;
    protected ITDAnalysisResults results;
    protected NameLookup names;
    protected Introductions itds;

    public ITDOptimisation(TraceMatch tm)
    {
        this.tm = tm;
        this.results = tm.getITDAnalysisResults();
        this.names = new NameLookup(tm);
    }

    public void doITDOptimisation()
    {
        SootClass itd_interface = createBindingInterface();
        Type itd_type = itd_interface.getType();
        this.itds = new Introductions(tm, itd_type);
        itds.create(itd_interface);

        new DisjunctUpdates(tm, itds).update();

        for (SootClass sc : results.itdTargets())
        {
            sc.addInterface(itd_interface);
            new Introductions(tm, itd_type).create(sc);
        }
 
        updateLabelsClass(itd_type);
        updateAdvice();
    }

    protected void updateLabelsClass(Type binding_type)
    {
        SootClass labels = tm.getLabelsClass();
        SootMethod constructor =
            names.lookup(labels, VoidType.v(), SootMethod.constructorName);
        JimpleEditor editor = new JimpleEditor(constructor);

        addField(labels, binding_type, names.MODIFIED);

        for (String symbol : tm.getSymbols()) {
            if (needTreeForSymbol(symbol)) {
                String name = mapNameForSymbol(symbol);
                Value depth = editor.getInt(treeDepthForSymbol(symbol));

                addField(labels, names.INDEXTREE.getType(), name);
                editor.initField(name, names.INDEXTREE_INIT, depth);
            }
        }
    }

    protected boolean needTreeForSymbol(String symbol)
    {
        return !tm.getVariableOrder(symbol).contains(results.itdVariable());
    }

    protected String mapNameForSymbol(String symbol)
    {
        return tm.getName() + "bindings_" + symbol;
    }

    protected int treeDepthForSymbol(String symbol)
    {
        return tm.getVariableOrder(symbol).size();
    }

    protected void updateAdvice()
    {
        // per-symbol advice
        for (String symbol : tm.getSymbols()) {
            SootMethod method = tm.getSymbolAdviceMethod(symbol);
            if (needTreeForSymbol(symbol))
                generateTreeLookupAdvice(symbol, method);
            else if (tm.getInitialSymbols().contains(symbol))
                generateInitialSymbolAdvice(symbol, method);
            else
                generateNoLookupAdvice(symbol, method);
        }

        // some-advice
        generateSomeAdvice();
    }

    protected void generateTreeLookupAdvice(String symbol, SootMethod method)
    {
        JimpleGenerator gen = new JimpleGenerator(method);
        Local[] bindings = new Local[method.getParameterCount()];
        for (int i = 0; i < bindings.length; i++)
            bindings[i] = gen.nextParameter();

        // allocate array
        Local array =
            gen.array(names.OBJECT.getType(), gen.getInt(bindings.length));

        // fill array
        int i = 0;
        for (String var : tm.getVariableOrder(symbol)) {
            gen.arrayset(array, gen.getInt(i), bindings[i]);
            i++;
        }

        // get iterator from index tree
        Local labels = getLabelsObject(gen);
        Local map = gen.read(labels, mapNameForSymbol(symbol));
        Local iter = gen.call(map, names.INDEXTREE_GET, array);

        // exit if there is no match with these bindings
        gen.beginIf(gen.equalsTest(iter, gen.getNull()));
            gen.returnVoid();
        gen.endIf();

        // cycle through
        Local hasnext = gen.call(iter, names.ITERATOR_HASNEXT);
        gen.beginWhile(gen.equalsTest(hasnext, gen.getTrue()));
            Local next = gen.call(iter, names.ITERATOR_NEXT);
            // check "next" for nullness
            // (because it may have just been garbage-collected)
            gen.beginIf(gen.notEqualsTest(next, gen.getNull()));
                Local itdobject = gen.cast(itds.getInterfaceType(), next);
	
                // check that this thread owns the itd-object if this is
                // a perthread tracematch
                if (tm.isPerThread()) {
                    Local owner = gen.call(itdobject,
                                    itds.getIsOwnedByCurrentThreadMethod());
                    gen.beginIf(gen.equalsTest(owner, gen.getTrue()));
                }
	
                // transitions
                Local bound = gen.call(itdobject, itds.getIsBoundMethod());
                gen.beginIf(gen.equalsTest(bound, gen.getTrue()));
                    generateITDUpdateCalls(gen, itdobject, symbol, false);
                gen.endIf();
    
                if (tm.isPerThread()) {
                    gen.endIf();
                }
            gen.endIf();
            gen.assign(hasnext, gen.call(iter, names.ITERATOR_HASNEXT));
        gen.endWhile();
        gen.returnVoid();
    }

    protected void generateInitialSymbolAdvice(String symbol, SootMethod method)
    {
        JimpleGenerator gen = new JimpleGenerator(method);
        Local[] bindings = new Local[method.getParameterCount()];
        
        // read parameters
        for (int i = 0; i < bindings.length; i++)
            bindings[i] = gen.nextParameter();
        Local itdobject = getITDObject(gen, symbol, bindings);

        // make disjunct
        Local labels = getLabelsObject(gen);
        Local disjunct = gen.alloc(names.DISJUNCT_INIT);
        gen.write(disjunct, "itdobject", itdobject);

        // make weak references (with disjunct as container) and
        // put weak references into disjunct
        List varnames = tm.getVariableOrder(symbol);
        for (int i = 0; i < varnames.size(); i++)
        {
            Local weakref = gen.alloc(names.WEAKREF_INIT, bindings[i]);
            gen.write(disjunct, "weak$" + varnames.get(i), weakref);
        }

        // Call Init on ITD, which returns the weak reference to use
        // in index trees
        Local treeref = gen.call(itdobject, itds.getInitMethod(), disjunct);
        
        // do transitions on ITD --- (true means only for initial state)
        generateITDUpdateCalls(gen, itdobject, symbol, true);

        // populate maps
        for (String othersymbol : tm.getSymbols()) {
            if (needTreeForSymbol(othersymbol)) {
                String mapname = mapNameForSymbol(othersymbol);
                List<String> othervars = tm.getVariableOrder(othersymbol);
                Local array = gen.array(names.OBJECT.getType(),
                                        gen.getInt(othervars.size()));
                int i = 0;
                for (String var : othervars) {
                    int pos = varnames.indexOf(var);
                    gen.arrayset(array, gen.getInt(i++), bindings[pos]);
                }
                Local map = gen.read(labels, mapname);
                gen.call(map, names.INDEXTREE_INSERT, array, treeref);
            }
        }

        // if the tracematch is "perthread", then the owning thread has
        // to be recorded with each instrumented object (each such object
        // can only be owned by one thread, by freshness)
        if (tm.isPerThread())
            gen.call(itdobject, itds.getSetOwnerThreadMethod());

        gen.returnVoid();
    }

    protected void generateNoLookupAdvice(String symbol, SootMethod method)
    {
        JimpleGenerator gen = new JimpleGenerator(method);
        Local[] bindings = new Local[method.getParameterCount()];

        // read parameters
        for (int i = 0; i < bindings.length; i++)
            bindings[i] = gen.nextParameter();

        // get itd-object
        Local itdobject = getITDObject(gen, symbol, bindings);

        // check itd-object has bindings attached, return if not
        Local bound = gen.call(itdobject, itds.getIsBoundMethod());
        gen.beginIf(gen.equalsTest(bound, gen.getFalse()));
            gen.returnVoid();
        gen.endIf();

        // check IsOwnedByCurrentThread() if perthread tracematch
        if (tm.isPerThread()) {
            Local owner =
                gen.call(itdobject, itds.getIsOwnedByCurrentThreadMethod());
            gen.beginIf(gen.equalsTest(owner, gen.getFalse()));
                gen.returnVoid();
            gen.endIf();
        }

        // check that the (non-itd) parameters are consistent with the
        // bindings found on the itd-object
        generateEqualityChecks(gen, symbol, itdobject, bindings);

        // do transitions -- (false means don't just consider initial state)
        generateITDUpdateCalls(gen, itdobject, symbol, false);
        
        gen.returnVoid();
    }

    protected void generateSomeAdvice()
    {
        SootMethod method = tm.getSomeAdviceMethod();
        JimpleGenerator gen = new JimpleGenerator(method);
        Local labels = getLabelsObject(gen);

        Local modified = gen.read(labels, names.MODIFIED);
        gen.beginWhile(gen.notEqualsTest(modified, gen.getNull()));
            gen.call(modified, itds.getMergeMethod());
            generateMatchedCheck(gen, modified, labels);
            Local next = gen.call(modified, itds.getRemoveFromListMethod());
            gen.call(modified, itds.getTerminateIfPossibleMethod());
            gen.assign(modified, next);
        gen.endWhile();
        gen.write(labels, names.MODIFIED, gen.getNull());

        if (!tm.isPerThread()) {
            // release the TM lock if the TM has not matched
            Local matched = gen.read(labels, tm.getName() + "updated");
            gen.beginIf(gen.equalsTest(matched, gen.getFalse()));
                releaseLock(gen);
            gen.endIf();
        }
        gen.returnVoid();
    }

    /**
     * check that the (non-itd) parameters are consistent with the
     * bindings found on the itd-object
     */
    protected void generateEqualityChecks(JimpleGenerator gen, String symbol,
                                          Local itdobject, Local[] bindings)
    {
        Local disjunct = gen.call(itdobject, itds.getGetDisjunctMethod());
        List<String> varnames = tm.getVariableOrder(symbol);
        int itdindex = getITDObjectParameterNumber(symbol);
        for (int i = 0; i < bindings.length; i++) {
            if (i != itdindex) {
                String var = varnames.get(i);
                Type vartype = tm.bindingType(var);

                SootMethod get_method =
                    names.lookup(names.DISJUNCT, vartype, "get$" + var);
                Local disjunct_val = gen.call(disjunct, get_method);
                gen.beginIf(gen.equalsTest(bindings[i], disjunct_val));
                    gen.returnVoid();
                gen.endIf();
            }
        }
    }

    protected void generateITDUpdateCalls(JimpleGenerator gen, Local itdobject,
                                          String symbol, boolean onlyinitial)
    {
        TMStateMachine sm = (TMStateMachine) tm.getStateMachine();
        Iterator<SMNode> states;

        if (onlyinitial)
            states = sm.getInitialStates().iterator();
        else
            states = sm.getStateIterator();

        while (states.hasNext()) {
            SMNode state = states.next();
            Iterator edges = state.getOutEdgeIterator();
            while (edges.hasNext()) {
                SMEdge edge = (SMEdge) edges.next();
                if (!edge.isSkipEdge() && edge.getLabel().equals(symbol))
                {
                    Value from = gen.getInt(state.getNumber());
                    Value to = gen.getInt(edge.getTarget().getNumber());
                    gen.call(itdobject, itds.getTransitionMethod(), from, to);
                }
            }
        }

        // Call AddToList on ITD and update 'modified' field on labels class
        Local labels = getLabelsObject(gen);
        Local inlist = gen.call(itdobject, itds.getIsInListMethod());
        gen.beginIf(gen.equalsTest(inlist, gen.getFalse()));
            Local old_modified = gen.read(labels, names.MODIFIED);
            gen.call(itdobject, itds.getAddToListMethod(), old_modified);
            gen.write(labels, names.MODIFIED, itdobject);
        gen.endIf();
    }

    protected void generateMatchedCheck(JimpleGenerator gen,
                                        Local itdobject, Local labels)
    {
        int final_state = tm.getFinalStateNumber();
        String final_constraint = tm.getName() + "_label" + final_state;

        Local matched = gen.call(itdobject, itds.getHasMatchedMethod());
        gen.beginIf(gen.equalsTest(matched, gen.getTrue()));
            gen.write(labels, tm.getName() + "updated", gen.getTrue());
            Local solutions =
                gen.read(gen.read(labels, final_constraint), "disjuncts");
            Local disjunct =
                gen.call(itdobject, itds.getGetFinalDisjunctMethod());
            gen.call(solutions, names.SET_ADD, disjunct);
        gen.endIf();
    }

    protected Local getITDObject(JimpleGenerator gen, String symbol,
                                    Local[] parameters)
    {
        int pos = getITDObjectParameterNumber(symbol);
        Type itdtype = itds.getInterfaceType();

        if (!tm.getInitialSymbols().contains(symbol)) {
            // argument may not have ITD on it, in which case the
            // symbol does not match
            gen.beginIf(gen.instanceOfTest(itdtype, parameters[pos]));
                gen.returnVoid();
            gen.endIf();
        }
        return gen.cast(itds.getInterfaceType(), parameters[pos]);
    }

    protected int getITDObjectParameterNumber(String symbol)
    {
        return tm.getVariableOrder(symbol).indexOf(results.itdVariable());
    }

    protected Local getLabelsObject(JimpleGenerator gen)
    {
        if (!tm.isPerThread())
            return gen.getThis();
        
        Local labelsfield = gen.read(tm.getName() + "labels");
        Local labelsobject = gen.call(labelsfield, names.THREADLOCAL_GET);
        return gen.cast(tm.getLabelsClass().getType(), labelsobject);
    }

    protected void releaseLock(JimpleGenerator gen)
    {
        SootClass lock =
            names.lookup("org.aspectbench.tm.runtime.internal.Lock");
        SootMethod release = names.lookup(lock, VoidType.v(), "release");

        Local lockobj = gen.read(getLabelsObject(gen), tm.getName() + "lock");
        gen.call(lockobj, release);
    }

    protected SootClass createBindingInterface()
    {
        String name = names.BINDING_INTERFACE;
        int modifiers = Modifier.PUBLIC | Modifier.INTERFACE;
        SootClass binding = new SootClass(name, modifiers);
        Scene.v().addClass(binding);
        binding.setApplicationClass();
        binding.setSuperclass(names.OBJECT);
        return binding;
    }

    protected void addField(SootClass sc, Type type, String name)
    {
        SootField field =
            new SootField(name, type, Modifier.PUBLIC);
        sc.addField(field);
    }

    protected SootMethod createMethod(SootClass sc, Type rettype,
                                      String name, Type... params)
    {
        int modifiers = Modifier.PUBLIC;
        if (sc.isInterface())
            modifiers |= Modifier.ABSTRACT;
        List<Type> paramlist = Arrays.asList(params);
        SootMethod sm = new SootMethod(name, paramlist, rettype, modifiers);
        sc.addMethod(sm);
        return sm;
    }
}
