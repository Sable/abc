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
import soot.BooleanType;
import soot.IntType;
import soot.Modifier;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.VoidType;
import soot.jimple.ConditionExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.util.Chain;

import java.util.*;

public class JimpleGenerator
{
    protected SootMethod method;
    protected int parameters_generated;
    protected LocalGeneratorEx local_generator;
    protected Chain units;
    protected Local this_local;

    public JimpleGenerator(SootMethod method)
    {
        this.method = method;
        this.parameters_generated = 0;
        Body body = Jimple.v().newBody(method);
        method.setActiveBody(body);
        this.local_generator = new LocalGeneratorEx(body);
        this.units = body.getUnits();
        if (!method.isStatic()) {
            RefType this_type = method.getDeclaringClass().getType();
            this_local = local_generator.generateLocal(this_type, "thislocal");
            Value this_val = Jimple.v().newThisRef(this_type);
            units.addLast(Jimple.v().newIdentityStmt(this_local, this_val));
        }
    }

    public Local nextParameter()
    {
        if (parameters_generated >= method.getParameterCount())
            throw new RuntimeException(
                "trying to generate more parameters than the method has");

        String name = "param" + parameters_generated;
        Type type = method.getParameterType(parameters_generated);
        Local paramlocal = local_generator.generateLocal(type, name);
        Value param = Jimple.v().newParameterRef(type, parameters_generated);
        units.addLast(Jimple.v().newIdentityStmt(paramlocal, param));
        parameters_generated++;
        return paramlocal;
    }

    protected Local read(String name)
    {
        SootField field = method.getDeclaringClass().getFieldByName(name);
        Local local = local_generator.generateLocal(field.getType(), name);
        units.addLast(Jimple.v().newAssignStmt(
            local,
            Jimple.v().newInstanceFieldRef(this_local, field.makeRef())
        ));
        return local;
    }

    protected void write(String name, Value val)
    {
        units.addLast(Jimple.v().newAssignStmt(
            Jimple.v().newInstanceFieldRef(
                this_local,
                method.getDeclaringClass().getFieldByName(name).makeRef()
            ),
            val));
    }

    /**
     * Generate an interface or virtual call
     */
    protected Local call(Local base, SootMethod method, Value... args)
    {
        ArrayList<Value> arglist = new ArrayList<Value>(args.length);
        for (int i = 0; i < args.length; i++)
            arglist.add(args[i]);
        InvokeExpr expr;
        if (((RefType)base.getType()).getSootClass().isInterface())
            expr = Jimple.v().newInterfaceInvokeExpr(
                                    base, method.makeRef(), arglist);
        else
            expr = Jimple.v().newVirtualInvokeExpr(
                                    base, method.makeRef(), arglist);

        Type returntype = method.getReturnType();
        if (returntype == VoidType.v()) {
            units.addLast(Jimple.v().newInvokeStmt(expr));
            return null;
        } else {
            String name = method.getName() + "_result";
            Local local = local_generator.generateLocal(returntype, name);
            units.addLast(Jimple.v().newAssignStmt(local, expr));
            return local;
        }
    }

    /**
     * Generate a static call
     */
    protected Local call(SootMethod method, Value... args)
    {
        ArrayList<Value> arglist = new ArrayList<Value>(args.length);
        for (int i = 0; i < args.length; i++)
            arglist.add(args[i]);
        InvokeExpr expr =
            Jimple.v().newStaticInvokeExpr(method.makeRef(), arglist);

        Type returntype = method.getReturnType();
        if (returntype == VoidType.v()) {
            units.addLast(Jimple.v().newInvokeStmt(expr));
            return null;
        } else {
            String name = method.getName() + "_result";
            Local local = local_generator.generateLocal(returntype, name);
            units.addLast(Jimple.v().newAssignStmt(local, expr));
            return local;
        }
    }

    protected Local alloc(SootMethod constructor, Value... args)
    {
        SootClass sc = constructor.getDeclaringClass();

        // create local for object
        RefType type = sc.getType();
        String name = "obj" + sc.getName();
        Local allocated = local_generator.generateLocal(type, name);

        // allocate object
        Value alloc_expr = Jimple.v().newNewExpr(type);
        units.add(Jimple.v().newAssignStmt(allocated, alloc_expr));

        // call constructor
        ArrayList<Value> arglist = new ArrayList<Value>(args.length);
        for (int i = 0; i < args.length; i++)
            arglist.add(args[i]);
        Value constructor_call =
            Jimple.v().newSpecialInvokeExpr(
                allocated, constructor.makeRef(), arglist);
        units.add(Jimple.v().newInvokeStmt(constructor_call));

        return allocated;
    }

    protected Stack<Stmt> if_end_labels = new Stack<Stmt>();

    public void beginIf(Local bool)
    {
        beginIf(Jimple.v().newEqExpr(bool, getFalse()));
    }

    public void beginIf(ConditionExpr condition)
    {
        Stmt label = Jimple.v().newNopStmt();
        if_end_labels.push(label);
        units.addLast(Jimple.v().newIfStmt(condition, label));
    }

    public void elseBranch()
    {
        Stmt label = Jimple.v().newNopStmt();
        units.addLast(Jimple.v().newGotoStmt(label));
        units.addLast(if_end_labels.pop());
        if_end_labels.push(label);
    }

    public void endIf()
    {
        units.addLast(if_end_labels.pop());
    }

    public void returnVoid()
    {
        units.addLast(Jimple.v().newReturnVoidStmt());
    }

    public void returnValue(Value v)
    {
        units.addLast(Jimple.v().newReturnStmt(v));
    }

    public Value getFalse()
    {
        return getInt(0);
    }

    public Value getTrue()
    {
        return getInt(1);
    }

    public Value getNull()
    {
        return NullConstant.v();
    }

    public Value getInt(int i)
    {
        return IntConstant.v(i);
    }

    public ConditionExpr equalsTest(Value a, Value b)
    {
        return Jimple.v().newNeExpr(a, b);
    }
}
