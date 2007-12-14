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
        addField(labels, names.REFQUEUE.getType(), names.TMREFQUEUE);
        editor.initField(names.TMREFQUEUE, names.REFQUEUE_INIT);

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
        int params = method.getParameterCount();
        for (int i = 0; i < params; i++)
            gen.nextParameter();
        gen.returnVoid();
    }

    protected void generateInitialSymbolAdvice(String symbol, SootMethod method)
    {
        JimpleGenerator gen = new JimpleGenerator(method);
        List varnames = tm.getVariableOrder(symbol);
        Local[] bindings = new Local[varnames.size()];
        int itdparam = varnames.indexOf(results.itdVariable());
        
        // read parameters
        for (int i = 0; i < bindings.length; i++)
            bindings[i] = gen.nextParameter();
        Local itdobject = gen.cast(itds.getInterfaceType(), bindings[itdparam]);

        // make disjunct
        Local disjunct = gen.alloc(names.DISJUNCT_INIT);
        Local refqueue = gen.read(names.TMREFQUEUE);
        gen.write(disjunct, "itdobject", itdobject);

        // make weak references (with disjunct as container) and
        // put weak references into disjunct
        for (int i = 0; i < bindings.length; i++)
        {
            Local weakref = gen.alloc(names.WEAKREF_INIT,
                                    bindings[0], refqueue, gen.getTrue());
            gen.call(weakref, names.WEAKREF_ADDCONTAINER, disjunct);
            gen.write(disjunct, "weak$" + varnames.get(i), weakref);
        }

        // Call Init on ITD
        gen.call(itdobject, itds.getInitMethod(), disjunct);
        
        // Call Transitions on ITD
        TMStateMachine sm = (TMStateMachine) tm.getStateMachine();
        for (SMNode state : sm.getInitialStates()) {
            Iterator i = state.getOutEdgeIterator();
            while (i.hasNext()) {
                SMEdge edge = (SMEdge) i.next();
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
        Local old_modified = gen.read(labels, names.MODIFIED);
        gen.call(itdobject, itds.getAddToListMethod(), old_modified);
        gen.write(labels, names.MODIFIED, itdobject);

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
                gen.call(map, names.INDEXTREE_INSERT, array, itdobject);
            }
        }

        // Call SetOwnerThread on ITD -- just if PerThread -- TODO

        gen.returnVoid();
    }

    protected void generateNoLookupAdvice(String symbol, SootMethod method)
    {
        JimpleGenerator gen = new JimpleGenerator(method);
        int params = method.getParameterCount();
        for (int i = 0; i < params; i++)
            gen.nextParameter();
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
            gen.assign(modified, next);
        gen.endWhile();
        gen.write(labels, names.MODIFIED, gen.getNull());
        
        Local matched = gen.read(labels, tm.getName() + "updated");
        gen.beginIf(gen.equalsTest(matched, gen.getFalse()));
            releaseLock(gen);
        gen.endIf();
        gen.returnVoid();
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
            gen.call(solutions, names.SET_ADD, solutions);
        gen.endIf();
    }

    protected Local getLabelsObject(JimpleGenerator gen)
    {
        if (tm.isPerThread()) {
            throw new RuntimeException("Have not yet added support for " +
                "perthread tracematches with the ITD optimisation turned on.");
        }

        return gen.getThis();
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
        binding.addInterface(names.ITDBINDING);
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
