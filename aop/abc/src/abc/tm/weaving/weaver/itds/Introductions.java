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

import soot.BooleanType;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.VoidType;

import java.util.*;

public class Introductions
{
    protected TraceMatch tm;
    protected NameLookup names;
    protected Type itd_interface;
 
    public Introductions(TraceMatch tm, Type itd_interface)
    {
        this.tm = tm;
        this.names = new NameLookup(tm);
        this.itd_interface = itd_interface;
    }
    
    public void create(SootClass targetclass)
    {
        if (!targetclass.isInterface())
            createBindingFields(targetclass);

        createBindingMethods(targetclass);
    }

    public SootMethod getInitMethod()
    {
        return init;
    }

    public SootMethod getTerminateIfPossibleMethod()
    {
        return terminate_if_possible;
    }

    public SootMethod getIsBoundMethod()
    {
        return is_bound;
    }

    public SootMethod getTransitionMethod()
    {
        return transition;
    }

    public SootMethod getMergeMethod()
    {
        return merge;
    }

    public SootMethod getSetOwnerThreadMethod()
    {
        return set_owner_thread;
    }

    public SootMethod getIsOwnedByCurrentThreadMethod()
    {
        return is_owned_by_current_thread;
    }

    public SootMethod getIsInListMethod()
    {
        return is_in_list;
    }

    public SootMethod getAddToListMethod()
    {
        return add_to_list;
    }

    public SootMethod getRemoveFromListMethod()
    {
        return remove_from_list;
    }

    public SootMethod getGetDisjunctMethod()
    {
        return get_disjunct;
    }

    public SootMethod getHasMatchedMethod()
    {
        return has_matched;
    }

    public SootMethod getGetFinalDisjunctMethod()
    {
        return get_final_disjunct;
    }

    public Type getInterfaceType()
    {
        return itd_interface;
    }

    SootMethod init;
    SootMethod terminate_if_possible;
    SootMethod is_bound;
    SootMethod strengthen;
    SootMethod transition;
    SootMethod merge;
    SootMethod set_owner_thread;
    SootMethod is_owned_by_current_thread;
    SootMethod is_in_list;
    SootMethod add_to_list;
    SootMethod remove_from_list;
    SootMethod get_disjunct;
    SootMethod has_matched;
    SootMethod get_final_disjunct;

    protected void createBindingFields(SootClass sc)
    {
        addField(sc, names.DISJUNCT_TYPE, names.BINDING_DISJUNCT);
        addField(sc, names.BITSET.getType(), names.STATES);
        addField(sc, names.BITSET.getType(), names.STATES_POS);
        addField(sc, names.THREAD.getType(), names.OWNER_THREAD);
        addField(sc, itd_interface, names.NEXT_BINDING);
        addField(sc, names.MAYBEWEAKREF.getType(), names.MAP_STRENGTH);
        addField(sc, BooleanType.v(), names.WEAKEN_MAP_STRENGTH);
    }

    protected void createBindingMethods(SootClass sc)
    {
        createInitMethod(sc);
        createTerminateIfPossibleMethod(sc);
        createIsBoundMethod(sc);
        createStrengthenMethod(sc);
        createTransitionMethod(sc);
        createMergeMethod(sc);
        createSetOwnerThreadMethod(sc);
        createIsOwnedByCurrentThreadMethod(sc);
        createIsInListMethod(sc);
        createAddToListMethod(sc);
        createRemoveFromListMethod(sc);
        createGetDisjunctMethod(sc);
        createHasMatchedMethod(sc);
        createGetFinalDisjunctMethod(sc);
    }

    protected void createInitMethod(SootClass sc)
    {
        Type weakreftype = names.MAYBEWEAKREF.getType();
        init = createTMMethod(sc, weakreftype, "Init", names.DISJUNCT_TYPE);

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(init);
            Local disjunct = gen.nextParameter();

            SootMethod update_ref_kinds =
                names.lookup(names.DISJUNCT, VoidType.v(), "updateRefKinds");
            gen.call(disjunct, update_ref_kinds);
            gen.write(names.BINDING_DISJUNCT, disjunct);

            Local states = gen.alloc(names.BITSET_INIT);
            Value initial_state = gen.getInt(tm.getInitialStateNumber());
            gen.call(states, names.BITSET_SET, initial_state);
            
            gen.write(names.STATES, states);
            gen.write(names.STATES_POS, gen.alloc(names.BITSET_INIT));

            // return a maybe-weak-ref, which is used in index trees
            // which refer to this itd-object
            Local weakref_to_this = generateInitMapStrengthCode(gen);
            gen.returnValue(weakref_to_this);
        }
    }

    protected void createTerminateIfPossibleMethod(SootClass sc)
    {
        terminate_if_possible =
            createTMMethod(sc, VoidType.v(), "TerminateIfPossible");

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(terminate_if_possible);
            SootMethod has_expired =
                names.lookup(names.DISJUNCT, BooleanType.v(), "hasExpired");
            
            Local states = gen.read(names.STATES);
            Local setbit =
                gen.call(states, names.BITSET_NEXTSETBIT, gen.getInt(0));

            gen.beginIf(gen.notEqualsTest(setbit, gen.getInt(-1)));
                Local disjunct = gen.read(names.BINDING_DISJUNCT);
                Local expired = gen.call(disjunct, has_expired);
                gen.beginIf(gen.equalsTest(expired, gen.getFalse()));
                    gen.returnVoid();
                gen.endIf();
            gen.endIf();

            gen.write(names.BINDING_DISJUNCT, gen.getNull());
            gen.write(names.STATES, gen.getNull());
            gen.write(names.STATES_POS, gen.getNull());
            gen.write(names.OWNER_THREAD, gen.getNull());
            gen.write(names.NEXT_BINDING, gen.getNull());
            gen.returnVoid();
        }
    }

    protected void createIsBoundMethod(SootClass sc)
    {
        is_bound = createTMMethod(sc, BooleanType.v(), "IsBound");

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(is_bound);
            gen.beginIf(gen.equalsTest(gen.read(names.STATES), gen.getNull()));
                gen.returnValue(gen.getFalse());
            gen.elseBranch();
                gen.returnValue(gen.getTrue());
            gen.endIf();
        }
    }

    protected void createStrengthenMethod(SootClass sc)
    {
        strengthen =
            createTMMethod(sc, VoidType.v(), "Strengthen", IntType.v());

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(strengthen);
            Local current_state = gen.nextParameter();
            Local disjunct = gen.read(names.BINDING_DISJUNCT);

            int num_states = tm.getStateMachine().getNumberOfStates();
            gen.beginSwitch(current_state, num_states);

            Iterator states = tm.getStateMachine().getStateIterator();
            while (states.hasNext()) {
                SMNode state = (SMNode) states.next();

                gen.beginCase(state.getNumber());
                for (String varname : state.needStrongRefs) {
                    SootMethod strengthen_var =
                        names.lookup(names.DISJUNCT, VoidType.v(),
                                        "strengthen" + varname);
                    gen.call(disjunct, strengthen_var);

                    generateStrengthenMapStrengthCode(gen, varname);
                }
                for (String varname : state.weakRefs) {
                    // also need to keep a strong reference from the leaves
                    // of index trees to an itd-object, if the variable for
                    // the itd-object is weak (but not collectable)
                    generateStrengthenMapStrengthCode(gen, varname);
                }

                gen.exitSwitch();
            }
            gen.endSwitch();
            gen.returnVoid();
        }
    }

    protected void createTransitionMethod(SootClass sc)
    {
        Type inttype = IntType.v();
        transition =
            createTMMethod(sc, VoidType.v(), "Transition", inttype, inttype);

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(transition);
            Local from = gen.nextParameter();
            Local to = gen.nextParameter();
            Local states = gen.read(names.STATES);
            gen.beginIf(gen.call(states, names.BITSET_GET, from));
                Local states_pos = gen.read(names.STATES_POS);
                gen.call(states_pos, names.BITSET_SET, to);
                gen.call(gen.getThis(), strengthen, to);
            gen.endIf();
            gen.returnVoid();
        }
    }

    protected void createMergeMethod(SootClass sc)
    {
        merge = createTMMethod(sc, VoidType.v(), "Merge");

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(merge);
            SootMethod update_ref_kinds =
                names.lookup(names.DISJUNCT, VoidType.v(), "updateRefKinds");

            Local states = gen.read(names.STATES);
            Local states_pos = gen.read(names.STATES_POS);
            gen.call(states, names.BITSET_CLEAR);
            gen.write(names.STATES, states_pos);
            gen.write(names.STATES_POS, states);

            Local disjunct = gen.read(names.BINDING_DISJUNCT);
            gen.call(disjunct, update_ref_kinds);

            generateWeakenMapStrengthCode(gen);

            gen.returnVoid();
        }
    }

    protected void createSetOwnerThreadMethod(SootClass sc)
    {
        set_owner_thread = createTMMethod(sc, VoidType.v(), "SetOwnerThread");

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(set_owner_thread);
            gen.write(names.OWNER_THREAD, gen.call(names.CURRENT_THREAD));
            gen.returnVoid();
        }
    }

    protected void createIsOwnedByCurrentThreadMethod(SootClass sc)
    {
        is_owned_by_current_thread =
            createTMMethod(sc, BooleanType.v(), "IsOwnedByCurrentThread");

        if (!sc.isInterface()) {
            JimpleGenerator gen =
                new JimpleGenerator(is_owned_by_current_thread);
            Local current = gen.call(names.CURRENT_THREAD);
            Local owner = gen.read(names.OWNER_THREAD);
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
        is_in_list = createTMMethod(sc, BooleanType.v(), "IsInList");

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(is_in_list);
            Local next_binding = gen.read(names.NEXT_BINDING);
            gen.beginIf(gen.equalsTest(next_binding, gen.getNull()));
                gen.returnValue(gen.getFalse());
            gen.elseBranch();
                gen.returnValue(gen.getTrue());
            gen.endIf();
            gen.returnVoid();
        }
    }

    protected void createAddToListMethod(SootClass sc)
    {
        add_to_list =
            createTMMethod(sc, VoidType.v(), "AddToList", itd_interface);

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(add_to_list);
            Local next_binding_param = gen.nextParameter();
            gen.write(names.NEXT_BINDING, next_binding_param);
            gen.returnVoid();
        }
    }

    protected void createRemoveFromListMethod(SootClass sc)
    {
        remove_from_list = createTMMethod(sc, itd_interface, "RemoveFromList");

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(remove_from_list);
            Local next_binding = gen.read(names.NEXT_BINDING);
            gen.write(names.NEXT_BINDING, gen.getNull());
            gen.returnValue(next_binding);
        }
    }

    protected void createGetDisjunctMethod(SootClass sc)
    {
        get_disjunct = createTMMethod(sc, names.DISJUNCT_TYPE, "GetDisjunct");

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(get_disjunct);
            gen.returnValue(gen.read(names.BINDING_DISJUNCT));
        }
    }

    protected void createHasMatchedMethod(SootClass sc)
    {
        has_matched = createTMMethod(sc, BooleanType.v(), "HasMatched");

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(has_matched);
            Value final_state = gen.getInt(tm.getFinalStateNumber());
            Local states = gen.read(names.STATES);
            Local matched = gen.call(states, names.BITSET_GET, final_state);
            gen.beginIf(matched);
                gen.returnValue(gen.getTrue());
            gen.elseBranch();
                gen.returnValue(gen.getFalse());
            gen.endIf();
        }
    }

    protected void createGetFinalDisjunctMethod(SootClass sc)
    {
        get_final_disjunct =
            createTMMethod(sc, names.DISJUNCT_TYPE, "GetFinalDisjunct");

        if (!sc.isInterface()) {
            JimpleGenerator gen = new JimpleGenerator(get_final_disjunct);
            SootMethod update_ref_kinds =
                names.lookup(names.DISJUNCT, VoidType.v(), "updateRefKinds");

            Value final_state = gen.getInt(tm.getFinalStateNumber());
            Local states = gen.read(names.STATES);
            gen.call(states, names.BITSET_CLEARBIT, final_state);

            Local disjunct = gen.read(names.BINDING_DISJUNCT);
            Local disjunct_copy = gen.alloc(names.DISJUNCT_INITCOPY, disjunct);
            Local statenum =
                gen.call(states, names.BITSET_NEXTSETBIT, gen.getInt(0));
            gen.beginWhile(gen.notEqualsTest(statenum, gen.getInt(-1)));
                gen.call(gen.getThis(), strengthen, statenum);
                gen.increment(statenum);
                gen.assign(statenum,
                gen.call(states, names.BITSET_NEXTSETBIT, statenum));
            gen.endWhile();
            gen.call(disjunct, update_ref_kinds);
            gen.returnValue(disjunct_copy);
        }
    }

    public Local generateInitMapStrengthCode(JimpleGenerator gen)
    {
        Local map_strength =
            gen.cast(names.MAYBEWEAKREF.getType(),
                gen.call(names.MAYBEWEAKREF_GETWEAKREF, gen.getThis()));
        gen.write(names.MAP_STRENGTH, map_strength);
        gen.write(names.WEAKEN_MAP_STRENGTH, gen.getTrue());

        return map_strength;
    }

    public void generateStrengthenMapStrengthCode(JimpleGenerator gen,
                                                    String var)
    {
        if (!var.equals(tm.getITDAnalysisResults().itdVariable()))
            return;

        Local map_strength = gen.read(names.MAP_STRENGTH);
        gen.call(map_strength, names.MAYBEWEAKREF_STRENGTHEN, gen.getThis());
        gen.write(names.WEAKEN_MAP_STRENGTH, gen.getFalse());
    }

    public void generateWeakenMapStrengthCode(JimpleGenerator gen)
    {
            Local weaken = gen.read(names.WEAKEN_MAP_STRENGTH);
            gen.beginIf(gen.equalsTest(weaken, gen.getTrue()));
                // need to weaken map-strength
                Local map_strength = gen.read(names.MAP_STRENGTH);
                gen.call(map_strength, names.MAYBEWEAKREF_WEAKEN);
            gen.elseBranch();
                // reset flag for next round of transitions
                gen.write(names.WEAKEN_MAP_STRENGTH, gen.getTrue());
            gen.endIf();
    }

    public void addField(SootClass sc, Type type, String name)
    {
        SootField field =
            new SootField(name, type, Modifier.PUBLIC);
        sc.addField(field);
    }

    public SootMethod createTMMethod(SootClass sc, Type rettype,
                                     String name, Type... params)
    {
        name = tm.getName() + name;
        int modifiers = Modifier.PUBLIC;
        if (sc.isInterface())
            modifiers |= Modifier.ABSTRACT;
        List<Type> paramlist = Arrays.asList(params);
        SootMethod sm = new SootMethod(name, paramlist, rettype, modifiers);
        sc.addMethod(sm);
        return sm;
    }
}
