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
        assign(local,
               Jimple.v().newInstanceFieldRef(this_local, field.makeRef())
        );
        return local;
    }

    protected Local read(Local base, String name)
    {
        SootClass base_class = ((RefType) base.getType()).getSootClass();
        SootField field = base_class.getFieldByName(name);
        Local local = local_generator.generateLocal(field.getType(), name);
        assign(local,
            Jimple.v().newInstanceFieldRef(base, field.makeRef())
        );
        return local;
    }

    protected void write(String name, Value val)
    {
        assign(
            Jimple.v().newInstanceFieldRef(
                this_local,
                method.getDeclaringClass().getFieldByName(name).makeRef()
            ),
            val);
    }

    protected void write(Local base, String name, Value val)
    {
        SootClass base_class = ((RefType) base.getType()).getSootClass();
        assign(
            Jimple.v().newInstanceFieldRef(base,
                base_class.getFieldByName(name).makeRef()),
            val);
    }

    protected Local array(Type basetype, Value size)
    {
        Type type = basetype.makeArrayType();
        Local local = local_generator.generateLocal(type, "array");
        assign(local, Jimple.v().newNewArrayExpr(basetype, size));
        return local;
    }

    protected void arrayset(Local array, Value index, Local val)
    {
        assign(Jimple.v().newArrayRef(array, index), val);
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
        if (method.getDeclaringClass().isInterface())
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
            assign(local, expr);
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
            assign(local, expr);
            return local;
        }
    }

    protected Local alloc(SootMethod constructor, Value... args)
    {
        SootClass sc = constructor.getDeclaringClass();

        // create local for object
        RefType type = sc.getType();
        String name = "obj" + sc.getShortName();
        Local allocated = local_generator.generateLocal(type, name);

        // allocate object
        Value alloc_expr = Jimple.v().newNewExpr(type);
        assign(allocated, alloc_expr);

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

    protected Local cast(Type to_type, Local local)
    {
        Local casted = local_generator.generateLocal(to_type, "cast_result");
        assign(casted, Jimple.v().newCastExpr(local, to_type));
        return casted;
    }

    protected void assign(Value assigned, Value val)
    {
        units.addLast(Jimple.v().newAssignStmt(assigned, val));
    }

    protected void increment(Local local)
    {
        assign(local, Jimple.v().newAddExpr(local, getInt(1)));
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

    protected Stack<Stmt> while_start_labels = new Stack<Stmt>();
    protected Stack<Stmt> while_end_labels = new Stack<Stmt>();

    public void beginWhile(ConditionExpr condition)
    {
        Stmt start_label = Jimple.v().newNopStmt();
        Stmt end_label = Jimple.v().newNopStmt();

        while_start_labels.push(start_label);
        while_end_labels.push(end_label);
    
        units.addLast(start_label);
        units.addLast(Jimple.v().newIfStmt(condition, end_label));
    }

    public void continueWhile()
    {
        Stmt start_label = while_start_labels.peek();
        units.addLast(Jimple.v().newGotoStmt(start_label));
    }

    public void endWhile()
    {
        Stmt start_label = while_start_labels.pop();
        Stmt end_label = while_end_labels.pop();

        units.addLast(Jimple.v().newGotoStmt(start_label));
        units.addLast(end_label);
    }
    

    /**
     * For each switch, an array, a, is pushed onto this stack.
     * For a switch with N cases, a[0]..a[N-1] are the labels
     * for each case, a[N] is the label for the default case,
     * and a[N+1] is the label for the end of the switch.
     */
    protected Stack<Stmt[]> switch_case_labels = new Stack<Stmt[]>();

    public void beginSwitch(Local switchvar, int cases)
    {
        Stmt[] labels = new Stmt[cases+2];
        List<Value> switchvals = new ArrayList<Value>();
        List<Stmt> switchlabels = new ArrayList<Stmt>();
        for (int i = 0; i < cases; i++) {
            Stmt label = Jimple.v().newNopStmt();
            labels[i] = label;
            switchvals.add(getInt(i));
            switchlabels.add(label);
        }
        Stmt defaultlabel = Jimple.v().newNopStmt();
        labels[cases] = defaultlabel;
        labels[cases+1] = Jimple.v().newNopStmt(); // end of switch block

        units.addLast(Jimple.v().newLookupSwitchStmt(
            switchvar, switchvals, switchlabels, defaultlabel));
        switch_case_labels.push(labels);
    }

    public void beginCase(int casenumber)
    {
        Stmt[] labels = switch_case_labels.peek();
        Stmt caselabel = labels[casenumber];
        units.addLast(labels[casenumber]);
        // record that this case has been generated
        labels[casenumber] = null;
    }

    public void beginDefaultCase()
    {
        Stmt[] labels = switch_case_labels.peek();
        units.addLast(labels[labels.length - 2]);
        // record that this case has been generated
        labels[labels.length - 2] = null;
    }

    public void exitSwitch()
    {
        Stmt[] labels = switch_case_labels.peek();
        units.addLast(Jimple.v().newGotoStmt(labels[labels.length - 1]));
    }

    public void endSwitch()
    {
        Stmt[] labels = switch_case_labels.pop();
        // generate any missing cases
        for (int i = 0; i < labels.length; i++) {
            Stmt label = labels[i];
            if (label != null)
                units.addLast(label);
        }
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

    public Local getThis()
    {
        return this_local;
    }

    public ConditionExpr equalsTest(Value a, Value b)
    {
        return Jimple.v().newNeExpr(a, b);
    }

    public ConditionExpr notEqualsTest(Value a, Value b)
    {
        return Jimple.v().newEqExpr(a, b);
    }

    public ConditionExpr instanceOfTest(Type type, Value tested)
    {
        Local answer =
            local_generator.generateLocal(BooleanType.v(), "isinstanceof");
        assign(answer, Jimple.v().newInstanceOfExpr(tested, type));
        return equalsTest(answer, getFalse());
    }

    public Local land(Value a, Value b)
    {
        Local and = local_generator.generateLocal(IntType.v(), "and");
        assign(and, Jimple.v().newAndExpr(a, b));
        return and;
    }

    public Local lor(Value a, Value b)
    {
        Local or = local_generator.generateLocal(IntType.v(), "or");
        assign(or, Jimple.v().newOrExpr(a, b));
        return or;
    }

    public Local rightShift(Value a, Value b)
    {
        Local shift = local_generator.generateLocal(IntType.v(), "shift");
        assign(shift, Jimple.v().newShrExpr(a, b));
        return shift;
    }
}
