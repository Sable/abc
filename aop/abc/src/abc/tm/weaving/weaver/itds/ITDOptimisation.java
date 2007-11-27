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
import soot.Body;
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
import soot.jimple.Jimple;
import soot.util.Chain;

import java.util.*;

public class ITDOptimisation
{
    protected TraceMatch tm;
    protected ITDAnalysisResults results;

    protected final static String BINDING_INTERFACE = "ITDBinding";
    protected String BINDING_DISJUNCT;
    protected String STATES;
    protected String STATES_POS;
    protected String STATES_NEG;
    protected String OWNER_THREAD;
    protected String NEXT_BINDING;

    protected String MODIFIED;
    protected String ALL_BINDINGS;

    protected SootClass BITSET = lookup("java.util.BitSet");
    protected SootMethod BITSET_INIT =
        lookup(BITSET, VoidType.v(), SootMethod.constructorName);
    protected SootMethod BITSET_GET =
        lookup(BITSET, BooleanType.v(), "get", IntType.v());
    protected SootMethod BITSET_SET =
        lookup(BITSET, VoidType.v(), "set", IntType.v());
    protected SootMethod BITSET_ANDNOT =
        lookup(BITSET, VoidType.v(), "andNot", BITSET.getType());
    protected SootMethod BITSET_OR =
        lookup(BITSET, VoidType.v(), "or", BITSET.getType());
    protected SootMethod BITSET_CLEAR =
        lookup(BITSET, VoidType.v(), "clear");

    protected SootClass THREAD = lookup("java.lang.Thread");
    protected SootMethod CURRENT_THREAD =
        lookup(THREAD, THREAD.getType(), "currentThread");

    protected SootClass MAP = lookup(
        "org.aspectbench.tm.runtime.internal.WeakKeyCollectingIdentityHashMap"
    );
    protected SootMethod MAP_INIT =
        lookup(MAP, VoidType.v(), SootMethod.constructorName);

    protected SootClass DISJUNCT;
    protected SootMethod DISJUNCT_INIT;


    public ITDOptimisation(TraceMatch tm)
    {
        this.tm = tm;
        this.results = tm.getITDAnalysisResults();
        this.BINDING_DISJUNCT = tm.getName() + "disjunct";
        this.STATES = tm.getName() + "states";
        this.STATES_POS = tm.getName() + "states_pos";
        this.STATES_NEG = tm.getName() + "states_neg";
        this.OWNER_THREAD = tm.getName() + "owner_thread";
        this.NEXT_BINDING = tm.getName() + "next_binding";
        this.MODIFIED = tm.getName() + "modified";
        this.ALL_BINDINGS = tm.getName() + "all_bindings";
        this.DISJUNCT = tm.getDisjunctClass();
        this.DISJUNCT_INIT =
            lookup(DISJUNCT, VoidType.v(), SootMethod.constructorName);
    }

    public void doITDOptimisation()
    {
        SootClass binding = createBindingInterface();
        createBindingMethods(binding, binding.getType());

        for (SootClass sc : results.itdTargets())
        {
            sc.addInterface(binding);
            addField(sc, DISJUNCT.getType(), BINDING_DISJUNCT);
            addField(sc, BITSET.getType(), STATES);
            addField(sc, BITSET.getType(), STATES_POS);
            addField(sc, BITSET.getType(), STATES_NEG);
            addField(sc, THREAD.getType(), OWNER_THREAD);
            addField(sc, binding.getType(), NEXT_BINDING);
            createBindingMethods(sc, binding.getType());
        }
        
        updateLabelsClass(binding.getType());
        updateAdvice(binding.getType());
    }

    protected void createBindingMethods(SootClass sc, Type binding_type)
    {
        createInitMethod(sc);
        createIsBoundMethod(sc);
        createTransitionMethod(sc);
        createMergeMethod(sc);
        createSetOwnerThreadMethod(sc);
        createIsOwnedByCurrentThreadMethod(sc);
        createIsInListMethod(sc);
        createAddToListMethod(sc, binding_type);
        createRemoveFromListMethod(sc, binding_type);
        createGetDisjunctMethod(sc);
        createHasMatchedMethod(sc);
        createTerminateMethod(sc);
    }

    protected void createInitMethod(SootClass sc)
    {
        SootMethod init =
            createTMMethod(sc, VoidType.v(), "Init", DISJUNCT.getType());

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(init);
            gen.write(BINDING_DISJUNCT, gen.nextParameter());

            Local states = gen.alloc(BITSET_INIT);
            Value initial_state = gen.getInt(tm.getInitialStateNumber());
            gen.call(states, BITSET_SET, initial_state);
            
            gen.write(STATES, states);
            gen.write(STATES_POS, gen.alloc(BITSET_INIT));
            gen.write(STATES_NEG, gen.alloc(BITSET_INIT));
            gen.returnVoid();
        }
    }

    protected void createIsBoundMethod(SootClass sc)
    {
        SootMethod is_bound_method =
            createTMMethod(sc, BooleanType.v(), "IsBound");

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(is_bound_method);
            gen.beginIf(gen.equalsTest(gen.read(STATES), gen.getNull()));
                gen.returnValue(gen.getFalse());
            gen.elseBranch();
                gen.returnValue(gen.getTrue());
            gen.endIf();
        }
    }

    protected void createTransitionMethod(SootClass sc)
    {
        Type inttype = IntType.v();
        SootMethod transition_method =
            createTMMethod(sc, VoidType.v(), "Transition", inttype, inttype);

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(transition_method);
            Local from = gen.nextParameter();
            Local to = gen.nextParameter();
            Local states = gen.read(STATES);
            gen.beginIf(gen.call(states, BITSET_GET, from));
                Local states_pos = gen.read(STATES_POS);
                gen.call(states_pos, BITSET_SET, to);
                Local states_neg = gen.read(STATES_NEG);
                gen.call(states_neg, BITSET_SET, from);
            gen.endIf();
            gen.returnVoid();
        }
    }

    protected void createMergeMethod(SootClass sc)
    {
        SootMethod merge_method =
            createTMMethod(sc, VoidType.v(), "Merge");

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(merge_method);
            Value final_state = gen.getInt(tm.getFinalStateNumber());
            Local states = gen.read(STATES);
            Local states_pos = gen.read(STATES_POS);
            Local states_neg = gen.read(STATES_NEG);
            gen.call(states, BITSET_ANDNOT, states_neg);
            gen.call(states, BITSET_OR, states_pos);
            gen.call(states_neg, BITSET_CLEAR);
            gen.call(states_neg, BITSET_SET, final_state);
            gen.returnVoid();
        }
    }

    protected void createSetOwnerThreadMethod(SootClass sc)
    {
        SootMethod set_method =
            createTMMethod(sc, VoidType.v(), "SetOwnerThread");

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(set_method);
            gen.write(OWNER_THREAD, gen.call(CURRENT_THREAD));
            gen.returnVoid();
        }
    }

    protected void createIsOwnedByCurrentThreadMethod(SootClass sc)
    {
        SootMethod owned_method =
            createTMMethod(sc, BooleanType.v(), "IsOwnedByCurrentThread");

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(owned_method);
            Local current = gen.call(CURRENT_THREAD);
            Local owner = gen.read(OWNER_THREAD);
            gen.beginIf(gen.equalsTest(current, owner));
                gen.returnValue(gen.getTrue());
            gen.elseBranch();
                gen.returnValue(gen.getFalse());
            gen.endIf();
            gen.returnVoid();
        }
    }

    protected void createIsInListMethod(SootClass sc)
    {
        SootMethod in_list_method =
            createTMMethod(sc, BooleanType.v(), "IsInList");

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(in_list_method);
            Local next_binding = gen.read(NEXT_BINDING);
            gen.beginIf(gen.equalsTest(next_binding, gen.getNull()));
                gen.returnValue(gen.getFalse());
            gen.elseBranch();
                gen.returnValue(gen.getTrue());
            gen.endIf();
            gen.returnVoid();
        }
    }

    protected void createAddToListMethod(SootClass sc, Type binding_type)
    {
        SootMethod add_to_list_method =
            createTMMethod(sc, VoidType.v(), "AddToList", binding_type);

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(add_to_list_method);
            Local next_binding_param = gen.nextParameter();
            gen.write(NEXT_BINDING, next_binding_param);
            gen.returnVoid();
        }
    }

    protected void createRemoveFromListMethod(SootClass sc, Type binding_type)
    {
        SootMethod remove_method =
            createTMMethod(sc, binding_type, "RemoveFromList");

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(remove_method);
            Local next_binding = gen.read(NEXT_BINDING);
            gen.write(NEXT_BINDING, gen.getNull());
            gen.returnValue(next_binding);
        }
    }

    protected void createGetDisjunctMethod(SootClass sc)
    {
        SootMethod get_disjunct =
            createTMMethod(sc, DISJUNCT.getType(), "GetDisjunct");

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(get_disjunct);
            gen.returnValue(gen.read(BINDING_DISJUNCT));
        }
    }

    protected void createHasMatchedMethod(SootClass sc)
    {
        SootMethod has_matched =
            createTMMethod(sc, BooleanType.v(), "HasMatched");

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(has_matched);
            Value final_state = gen.getInt(tm.getFinalStateNumber());
            Local states = gen.read(STATES);
            Local matched = gen.call(states, BITSET_GET, final_state);
            gen.beginIf(matched);
                gen.returnValue(gen.getTrue());
            gen.elseBranch();
                gen.returnValue(gen.getFalse());
            gen.endIf();
        }
    }

    protected void createTerminateMethod(SootClass sc)
    {
        SootMethod terminate = createTMMethod(sc, VoidType.v(), "Terminate");

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(terminate);
            gen.write(BINDING_DISJUNCT, gen.getNull());
            gen.write(STATES, gen.getNull());
            gen.write(STATES_POS, gen.getNull());
            gen.write(STATES_NEG, gen.getNull());
            gen.write(OWNER_THREAD, gen.getNull());
            gen.write(NEXT_BINDING, gen.getNull());
            gen.returnVoid();
        }
    }

    protected void updateLabelsClass(Type binding_type)
    {
        SootClass labels_class = tm.getLabelsClass();
        addField(labels_class, binding_type, MODIFIED);
        addField(labels_class, MAP.getType(), ALL_BINDINGS);

        SootMethod constructor =
            lookup(labels_class, VoidType.v(), SootMethod.constructorName);
        JimpleEditor editor = new JimpleEditor(constructor);
        editor.initField(ALL_BINDINGS, MAP_INIT);
    }

    protected void updateAdvice(Type binding_type)
    {
        // per-advice
 
        // some-advice
    }



    protected SootClass lookup(String name)
    {
        return soot.Scene.v().getSootClass(name);
    }
    protected SootMethod lookup(SootClass sc, Type t, String name, Type... args)
    {
        ArrayList<Type> list = new ArrayList<Type>(args.length);
        for (int i = 0; i < args.length; i++)
            list.add(args[i]);
        return sc.getMethod(name, list, t);
    }
    protected SootClass createBindingInterface()
    {
        String name = tm.getPackage() + BINDING_INTERFACE + "$" + tm.getName();
        int modifiers = Modifier.PUBLIC | Modifier.INTERFACE;
        SootClass binding = new SootClass(name, modifiers);
        // TODO - add methods
        Scene.v().addClass(binding);
        binding.setApplicationClass();
        binding.setSuperclass(lookup("java.lang.Object"));
        return binding;
    }
    protected void addField(SootClass sc, Type type, String name)
    {
        SootField field =
            new SootField(name, type, Modifier.PUBLIC);
        sc.addField(field);
    }
    protected SootMethod createTMMethod(SootClass sc, Type rettype,
                                        String name, Type... params)
    {
        int modifiers = Modifier.PUBLIC;
        if (sc.isInterface())
            modifiers |= Modifier.ABSTRACT;
        name = tm.getName() + name;
        List<Type> paramlist = new ArrayList<Type>(params.length);
        for (Type paramtype : params)
            paramlist.add(paramtype);
        SootMethod sm = new SootMethod(name, paramlist, rettype, modifiers);
        sc.addMethod(sm);
        return sm;
    }
}
