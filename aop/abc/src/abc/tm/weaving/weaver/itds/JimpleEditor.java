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

import abc.soot.util.LocalGeneratorEx;

import soot.Body;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
import soot.jimple.IdentityStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.util.Chain;

import java.util.*;

public class JimpleEditor
{
    protected SootMethod method;
    protected LocalGeneratorEx local_generator;
    protected Chain units;
    protected Object marker;

    public JimpleEditor(SootMethod method)
    {
        this.method = method;
        Body body = method.getActiveBody();
        this.local_generator = new LocalGeneratorEx(body);
        this.units = body.getUnits().getNonPatchingChain();
        this.marker = units.getFirst();
        while (this.marker instanceof IdentityStmt)
            this.marker = units.getSuccOf(this.marker);
    }

    public void initField(String name, SootMethod constructor, Value... args)
    {
        Local this_local = method.getActiveBody().getThisLocal();
        SootField field = method.getDeclaringClass().getFieldByName(name);
        SootClass allocated = constructor.getDeclaringClass();

        // create local for object
        RefType type = allocated.getType();
        String localname = "obj" + allocated.getName();
        Local local = local_generator.generateLocal(type, name);

        // allocate object
        Value alloc_expr = Jimple.v().newNewExpr(type);
        units.insertBefore(Jimple.v().newAssignStmt(local, alloc_expr), marker);

        // call constructor
        List<Value> arglist = Arrays.asList(args);
        Value constructor_call =
            Jimple.v().newSpecialInvokeExpr(
                local, constructor.makeRef(), arglist);
        units.insertBefore(Jimple.v().newInvokeStmt(constructor_call), marker);

        // assign new object to field
        Value field_ref =
            Jimple.v().newInstanceFieldRef(this_local, field.makeRef());
        units.insertBefore(Jimple.v().newAssignStmt(field_ref, local), marker);
    }

    public Value getInt(int i)
    {
        return IntConstant.v(i);
    }
}
