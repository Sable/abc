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
import soot.Local;
import soot.Modifier;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.VoidType;

import java.util.*;

public class DisjunctUpdates
{
    protected TraceMatch tm;
    protected Introductions itds;
    protected NameLookup names;

    public DisjunctUpdates(TraceMatch tm, Introductions itds)
    {
        this.tm = tm;
        this.itds = itds;
        this.names = new NameLookup(tm);
    }

    protected SootMethod update_ref_kinds;
    protected SootMethod has_expired;

    protected void update()
    {
        addField(names.DISJUNCT, itds.getInterfaceType(), "itdobject");
        for (String varname : tm.getNonPrimitiveFormalNames()) {
            addField(names.DISJUNCT, BooleanType.v(), "weaken$" + varname);
            addField(names.DISJUNCT, IntType.v(), "collectable$" + varname);
            createStrengthenMethod(varname);
            createUncollectableMethod(varname);
        }
        createUpdateRefKindsMethod();
        createHasExpiredMethod();
        updateEqualsMethod();
    }

    protected void createUpdateRefKindsMethod()
    {
        update_ref_kinds =
            createMethod(names.DISJUNCT, VoidType.v(), "updateRefKinds");
        JimpleGenerator gen = new JimpleGenerator(update_ref_kinds);

        for (String varname : tm.getNonPrimitiveFormalNames()) {
            // weak/strong
            Local weaken = gen.read("weaken$" + varname);
            gen.beginIf(gen.equalsTest(weaken, gen.getTrue()));
                gen.write("var$" + varname, gen.getNull());
            gen.endIf();
            gen.write("weaken$" + varname, gen.getTrue());
            // (un)collectable
            Local flags = gen.read("collectable$" + varname);
            flags = gen.lor(
                        gen.rightShift(flags, gen.getInt(1)),
                        gen.getInt(2));
            gen.write("collectable$" + varname, flags);
        }
        gen.returnVoid();
    }

    protected void createHasExpiredMethod()
    {
        has_expired =
            createMethod(names.DISJUNCT, BooleanType.v(), "hasExpired");
        JimpleGenerator gen = new JimpleGenerator(has_expired);

        for (String varname : tm.getNonPrimitiveFormalNames()) {
            Local val = gen.read("var$" + varname);
            gen.beginIf(gen.equalsTest(val, gen.getNull()));
                Local flags = gen.read("collectable$" + varname);
                flags = gen.land(flags, gen.getInt(1));
                gen.beginIf(gen.equalsTest(flags, gen.getInt(1)));
                    Local weak = gen.read("weak$" + varname);
                    Local expired = gen.call(weak, names.WEAKREF_ISEXPIRED);
                    gen.beginIf(gen.equalsTest(expired, gen.getTrue()));
                        gen.returnValue(gen.getTrue());
                    gen.endIf();
                gen.endIf();
            gen.endIf();
        }
        gen.returnValue(gen.getFalse());
    }

    protected void createStrengthenMethod(String varname)
    {
        SootMethod strengthen =
            createMethod(names.DISJUNCT, VoidType.v(), "strengthen" + varname);
        JimpleGenerator gen = new JimpleGenerator(strengthen);

        Type vartype = tm.bindingType(varname);

        gen.beginIf(gen.equalsTest(gen.read("var$" + varname),
                                   gen.getNull()));
            Local weakref = gen.read("weak$" + varname);
            Local strongref = gen.call(weakref, names.WEAKREF_GET);
            gen.write("var$" + varname, gen.cast(vartype, strongref));
        gen.endIf();
        gen.write("weaken$" + varname, gen.getFalse());
        gen.returnVoid();
    }

    protected void createUncollectableMethod(String varname)
    {
        SootMethod uncollectable =
            createMethod(names.DISJUNCT, VoidType.v(),
                            "uncollectable" + varname);
        JimpleGenerator gen = new JimpleGenerator(uncollectable);

        Local flags = gen.read("collectable$" + varname);
        flags = gen.land(flags, gen.getInt(~2));
        gen.write("collectable$" + varname, flags);

        gen.returnVoid();
    }

    protected void updateEqualsMethod()
    {
        SootMethod equals = names.lookup(names.DISJUNCT, BooleanType.v(),
                                         "equals", names.OBJECT.getType());
        JimpleGenerator gen = new JimpleGenerator(equals);

        gen.nextParameter();
        gen.returnValue(gen.getFalse());
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
