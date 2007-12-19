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

import soot.BooleanType;
import soot.IntType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.VoidType;

import java.util.*;

public class NameLookup
{
    protected TraceMatch tm;

    public NameLookup(TraceMatch tm)
    {
        this.tm = tm;

        // interface name
        BINDING_INTERFACE = tm.getPackage() + "ITDBinding$" + tm.getName();

        // itd field names
        BINDING_DISJUNCT = tm.getName() + "disjunct";
        STATES = tm.getName() + "states";
        STATES_POS = tm.getName() + "states_pos";
        OWNER_THREAD = tm.getName() + "owner_thread";
        NEXT_BINDING = tm.getName() + "next_binding";
        MAP_STRENGTH = tm.getName() + "map_strength";
        WEAKEN_MAP_STRENGTH = tm.getName() + "weaken_map_strength";

        // label-class field names
        MODIFIED = tm.getName() + "modified";
        TMREFQUEUE = tm.getName() + "refqueue";

        // disjunct stuff
        DISJUNCT = tm.getDisjunctClass();
        DISJUNCT_TYPE = DISJUNCT.getType();
        DISJUNCT_INIT =
            lookup(DISJUNCT, VoidType.v(), SootMethod.constructorName);
        DISJUNCT_INITCOPY =
            lookup(DISJUNCT, VoidType.v(), SootMethod.constructorName,
                    DISJUNCT.getType());
    }

    public SootClass lookup(String name)
    {
        return soot.Scene.v().getSootClass(name);
    }

    public SootMethod lookup(SootClass sc, Type t, String name, Type... args)
    {
        List<Type> list = Arrays.asList(args);
        return sc.getMethod(name, list, t);
    }

    // interface name
    public final String BINDING_INTERFACE;

    // field names
    public final String BINDING_DISJUNCT;
    public final String STATES;
    public final String STATES_POS;
    public final String OWNER_THREAD;
    public final String NEXT_BINDING;
    public final String MAP_STRENGTH;
    public final String WEAKEN_MAP_STRENGTH;

    // label-class field names
    public final String MODIFIED;
    public final String TMREFQUEUE;

    // disjunct stuff
    public final SootClass DISJUNCT;
    public final Type DISJUNCT_TYPE;
    public final SootMethod DISJUNCT_INIT;
    public final SootMethod DISJUNCT_INITCOPY;

    // useful
    public final SootClass OBJECT = lookup("java.lang.Object");
    public final SootClass SET =
        lookup("java.util.HashSet");
    public final SootClass WEAKREF =
        lookup("org.aspectbench.tm.runtime.internal.MyWeakRef");
    public final SootClass MAYBEWEAKREF =
        lookup("org.aspectbench.tm.runtime.internal.MaybeWeakRef");
    public final SootClass REFQUEUE =
        lookup("java.lang.ref.ReferenceQueue");
    public final SootClass CONTAINER =
        lookup("org.aspectbench.tm.runtime.internal.WeakRefContainer");

    public final SootMethod SET_ADD =
        lookup(SET, BooleanType.v(), "add", OBJECT.getType());

    public final SootMethod WEAKREF_INIT =
        lookup(WEAKREF, VoidType.v(), SootMethod.constructorName,
                OBJECT.getType());
    public final SootMethod WEAKREF_ADDCONTAINER =
        lookup(WEAKREF, VoidType.v(), "addContainer", CONTAINER.getType());
    public final SootMethod WEAKREF_GET =
        lookup(lookup("java.lang.ref.Reference"), OBJECT.getType(), "get");
    public final SootMethod MAYBEWEAKREF_GETWEAKREF =
        lookup(MAYBEWEAKREF, WEAKREF.getType(), "getWeakRef", OBJECT.getType());
    public final SootMethod MAYBEWEAKREF_STRENGTHEN =
        lookup(MAYBEWEAKREF, VoidType.v(), "strengthen", OBJECT.getType());
    public final SootMethod MAYBEWEAKREF_WEAKEN =
        lookup(MAYBEWEAKREF, VoidType.v(), "weaken");
    public final SootMethod WEAKREF_ISEXPIRED =
        lookup(WEAKREF, BooleanType.v(), "isExpired");

    public final SootMethod REFQUEUE_INIT =
        lookup(REFQUEUE, VoidType.v(), SootMethod.constructorName);

    // bitset stuff
    public final SootClass BITSET = lookup("java.util.BitSet");
    public final SootMethod BITSET_INIT =
        lookup(BITSET, VoidType.v(), SootMethod.constructorName);
    public final SootMethod BITSET_GET =
        lookup(BITSET, BooleanType.v(), "get", IntType.v());
    public final SootMethod BITSET_SET =
        lookup(BITSET, VoidType.v(), "set", IntType.v());
    public final SootMethod BITSET_ANDNOT =
        lookup(BITSET, VoidType.v(), "andNot", BITSET.getType());
    public final SootMethod BITSET_OR =
        lookup(BITSET, VoidType.v(), "or", BITSET.getType());
    public final SootMethod BITSET_CLEAR =
        lookup(BITSET, VoidType.v(), "clear");
    public final SootMethod BITSET_CLEARBIT =
        lookup(BITSET, VoidType.v(), "clear", IntType.v());
    public final SootMethod BITSET_NEXTSETBIT =
        lookup(BITSET, IntType.v(), "nextSetBit", IntType.v());

    // thread stuff
    public final SootClass THREAD = lookup("java.lang.Thread");
    public final SootMethod CURRENT_THREAD =
        lookup(THREAD, THREAD.getType(), "currentThread");

    public final SootClass THREADLOCAL = lookup("java.lang.ThreadLocal");
    public final SootMethod THREADLOCAL_GET =
        lookup(THREADLOCAL, OBJECT.getType(), "get");

    // iterators
    public final SootClass ITERATOR = lookup("java.util.Iterator");
    public final SootMethod ITERATOR_HASNEXT =
        lookup(ITERATOR, BooleanType.v(), "hasNext");
    public final SootMethod ITERATOR_NEXT =
        lookup(ITERATOR, OBJECT.getType(), "next");

    // index tree stuff
    public final Type OBJECTARRAY = OBJECT.getType().makeArrayType();
    public final SootClass INDEXTREE =
        lookup("org.aspectbench.tm.runtime.internal.IndexTree");
    public final SootMethod INDEXTREE_INIT =
        lookup(
            lookup("org.aspectbench.tm.runtime.internal.IndexTreeMap"),
            VoidType.v(), SootMethod.constructorName, IntType.v());

    public final SootMethod INDEXTREE_INSERT =
        lookup(INDEXTREE, VoidType.v(), "insert",
                OBJECTARRAY, MAYBEWEAKREF.getType());
    public final SootMethod INDEXTREE_GET =
        lookup(INDEXTREE, ITERATOR.getType(), "get", OBJECTARRAY);
}
