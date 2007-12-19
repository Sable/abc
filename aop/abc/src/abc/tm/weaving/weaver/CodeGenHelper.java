/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Julian Tibble
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

package abc.tm.weaving.weaver;

import soot.*;
import soot.util.*;
import soot.jimple.*;

import abc.weaving.aspectinfo.Formal;
import abc.tm.weaving.aspectinfo.*;
import abc.tm.weaving.matching.*;

import polyglot.util.InternalCompilerError;

import java.util.*;

/**
 * Helps with Jimple generation for updating the
 * constraints on each label of the state machine.
 *
 * @author Julian Tibble
 */
public class CodeGenHelper
{
    public static final int LABEL      = 0;
    public static final int TMP_LABEL  = 1;
    public static final int SKIP_LABEL = 2;

    protected TraceMatch tm;
    protected SootClass thread_local;
    protected SootClass constraint;
    protected SootClass disjunct;
    protected SootClass event;
    protected SootClass lock;
    protected SootClass set;
    protected SootClass cleanup_refs;
    protected Type object;
    protected Type object_array;

    protected String disjuncts_name = null;
    protected String disjuncts_index_name = null;

    protected int local_count; // used to ensure local names are unique

    protected int final_state;

    protected Stmt null_checks_jump_target = null;

    public CodeGenHelper(TraceMatch tm)
    {
        this.tm = tm;
        
        this.thread_local = Scene.v().getSootClass("java.lang.ThreadLocal");
        this.lock = Scene.v().getSootClass(
                        "org.aspectbench.tm.runtime.internal.Lock");
        this.set = Scene.v().getSootClass("java.util.LinkedHashSet");
        this.object = Scene.v().getRefType("java.lang.Object");
        this.object_array = ArrayType.v(object, 1);
        this.cleanup_refs = Scene.v().getSootClass(
                "org.aspectbench.tm.runtime.internal.MyWeakRef");

        this.local_count = 0;
        this.final_state = 0;
    }

    public void setConstraintClass(SootClass constraint)
    {
        this.constraint = constraint;
    }

    public void setDisjunctClass(SootClass disjunct)
    {
        this.disjunct = disjunct;
    }

    public void setEventClass(SootClass event)
    {
        this.event = event;
    }

    public void setFinalState(int final_state)
    {
        this.final_state = final_state;
    }

    protected int nextLocalID()
    {
        return local_count++;
    }

    /**
     * Return the field-name for the constraint label
     * for a particular state and kind of label where:
     *
     * kind 0 - labelX
     *      1 - labelX_tmp
     *      2 - labelX_skip
     */
    protected String getLabelName(int state, int kind)
    {
        String name = tm.getName() + "_label" + state;

        switch (kind)
        {
            case LABEL:
                return name;
            case TMP_LABEL:
                return name + "_tmp";
            case SKIP_LABEL:
                return name + "_skip";
            default:
                throw new
                    InternalCompilerError("getLabelName with invalid kind");
        }
    }

    /**
     * Create a Jimple reference to a static constant on
     * the constraint class - name should be either "trueC"
     * or "falseC".
     */
    protected StaticFieldRef makeConstantRef(String name)
    {
        SootFieldRef ref = 
            Scene.v().makeFieldRef( constraint,
                                    name,
                                    constraint.getType(),
                                    true);

        return Jimple.v().newStaticFieldRef(ref);
    }

    /**
     * Create a Jimple reference to a variable instance field
     * on a disjunct class.
     */
    protected InstanceFieldRef makeDisjunctVarRef(Value base, String name,
                                                    Type type)
    {
        SootFieldRef ref =
            Scene.v().makeFieldRef( disjunct,
                                    "var$" + name,
                                    type,
                                    false);

        return Jimple.v().newInstanceFieldRef(base, ref);
    }

    /**
     * Create a Jimple reference to the disjuncts set on a
     * constraint object.
     */
    protected InstanceFieldRef makeDisjunctsRef(Value base)
    {
        SootFieldRef ref =
            Scene.v().makeFieldRef(constraint, "disjuncts",
                                    set.getType(), false);

        return Jimple.v().newInstanceFieldRef(base, ref);
    }

    protected InstanceFieldRef makeLabelsThreadLocalRef(Value base)
    {
        Type labels_thread_local_type =
            tm.getLabelsThreadLocalClass().getType();

        SootFieldRef ref =
            Scene.v().makeFieldRef( tm.getContainerClass(),
                                    tm.getName() + "labels",
                                    labels_thread_local_type,
                                    false);
                                    
        return Jimple.v().newInstanceFieldRef(base, ref);
    }

    protected void makeLabelsThreadLocalField()
    {
        Type labels_thread_local_type =
            tm.getLabelsThreadLocalClass().getType();

        SootField field = new SootField(
                                tm.getName() + "labels",
                                labels_thread_local_type,
                                Modifier.PRIVATE);

        tm.getContainerClass().addField(field);
    }

    /**
     * Create a Jimple reference to the "lock" field
     * on the tracematch container class.
     */
    protected InstanceFieldRef makeLockRef(Value base)
    {
        SootFieldRef ref =
            Scene.v().makeFieldRef( tm.getContainerClass(),
                                    tm.getName() + "lock",
                                    lock.getType(),
                                    false);

        return Jimple.v().newInstanceFieldRef(base, ref);
    }

    /**
     * Create the "lock" field on the tracematch
     * container class.
     */
    protected void makeLockField()
    {
        SootField field = new SootField(
                                tm.getName() + "lock",
                                lock.getType(),
                                Modifier.PUBLIC);

        tm.getContainerClass().addField(field);
    }

    /**
     * Create a Jimple reference to the "updated" flag
     * on the tracematch container class.
     */
    protected InstanceFieldRef makeUpdatedRef(Value base)
    {
        SootFieldRef ref =
            Scene.v().makeFieldRef( tm.getLabelsClass(),
                                    tm.getName() + "updated",
                                    BooleanType.v(),
                                    false);
 
        return Jimple.v().newInstanceFieldRef(base, ref);
    }

    /**
     * Create the "updated" flag on the tracematch
     * container class.
     */
    protected void makeUpdatedField()
    {
        SootField field = new SootField(
                                tm.getName() + "updated",
                                BooleanType.v(),
                                Modifier.PUBLIC);

        tm.getLabelsClass().addField(field);
    }

    /**
     * Create a Jimple reference to a label instance field,
     * where base is an instance of the tracematch container
     * class.
     *
     * @return the new reference.
     */
    protected InstanceFieldRef makeLabelRef(Value base, int state, int kind)
    {
        SootFieldRef ref =
            Scene.v().makeFieldRef( tm.getLabelsClass(),
                                    getLabelName(state, kind),
                                    constraint.getType(),
                                    false);

        return Jimple.v().newInstanceFieldRef(base, ref);
    }

    /**
     * Create a private label instance field on the container
     * class of the tracematch.
     */
    protected void makeLabelField(int state, int kind)
    {
        SootField field = new SootField(
                                getLabelName(state, kind),
                                constraint.getType(),
                                Modifier.PUBLIC);

        tm.getLabelsClass().addField(field);
    }

    protected void makeConstructorMethod(SootClass child, SootClass parent)
    {
        SootMethod init = new SootMethod(SootMethod.constructorName,
                                         new ArrayList(),
                                         VoidType.v(),
                                         Modifier.PUBLIC,
                                         new ArrayList());
        child.addMethod(init);

        Body body = Jimple.v().newBody(init);
        init.setActiveBody(body);

        Chain units = body.getUnits();

        genIdentityStmts(init, body, units);
        Local this_local = body.getThisLocal();

        SootMethodRef super_ref =
            Scene.v().makeConstructorRef(parent, new ArrayList());
        InvokeExpr super_call =
            Jimple.v().newSpecialInvokeExpr(this_local, super_ref);

        units.addLast(Jimple.v().newInvokeStmt(super_call));
        units.addLast(Jimple.v().newReturnVoidStmt());
    }

    protected void makeLabelsClass()
    {
        SootClass object_class = Scene.v().getSootClass("java.lang.Object");

        String name = tm.getPackage() + "Labels$" + tm.getName();

        SootClass labels = new SootClass(name, Modifier.PUBLIC);

        tm.setLabelsClass(labels);
        Scene.v().addClass(labels);
        labels.setApplicationClass();
        labels.setSuperclass(object_class);

        makeConstructorMethod(labels, object_class);
    }

    protected void makeLabelsThreadLocalClass()
    {
        String name = tm.getPackage() + "LabelsThreadLocal$" + tm.getName();

        SootClass labels_thread_local = new SootClass(name, Modifier.PUBLIC);

        tm.setLabelsThreadLocalClass(labels_thread_local);
        Scene.v().addClass(labels_thread_local);
        labels_thread_local.setApplicationClass();
        labels_thread_local.setSuperclass(thread_local);

        makeConstructorMethod(labels_thread_local, thread_local);
        makeInitialValueMethod(labels_thread_local);
    }

    protected void makeAndInitThreadLocalField()
    {
        SootClass container = tm.getContainerClass();

        SootMethod init =
            container.getMethodByName(SootMethod.constructorName);
        Body body = init.getActiveBody();

        makeLabelsThreadLocalField();
        makeLabelsThreadLocal(body);
    }

    protected void makeInitialValueMethod(SootClass labels_thread_local)
    {
        SootClass labels_class = tm.getLabelsClass();
        RefType labels_type = labels_class.getType();

        int modifiers = Modifier.PUBLIC | Modifier.SYNCHRONIZED;
        SootMethod init_val = new SootMethod("initialValue",
                                             new ArrayList(),
                                             object,
                                             modifiers);
        labels_thread_local.addMethod(init_val);

        Body body = Jimple.v().newBody(init_val);
        init_val.setActiveBody(body);
        Chain units = body.getUnits();

        Local labels = addLocal(body, "labels", labels_type);

        Expr new_expr = Jimple.v().newNewExpr(labels_type);
        SootMethodRef init =
            Scene.v().makeConstructorRef(labels_class, new ArrayList());
        Expr super_expr =
            Jimple.v().newSpecialInvokeExpr(labels, init, new ArrayList());

        units.addLast(Jimple.v().newAssignStmt(labels, new_expr));
        units.addLast(Jimple.v().newInvokeStmt(super_expr));
        units.addLast(Jimple.v().newReturnStmt(labels));
    }

    /**
     * Creates a new local with a certain name (with the name
     * extended with an ID to ensure uniqueness) and kind, and
     * add it to the body.
     *
     * @return the new local
     */
    protected Local addLocal(Body body, String name, Type kind)
    {
        String local_name = name + "__" + nextLocalID();
        Local new_local = Jimple.v().newLocal(local_name, kind);
        body.getLocals().add(new_local);

        return new_local;
    }

    /**
     * Return an invoke expression to the size() method on a Set.
     */
    protected InvokeExpr sizeMethod(Local base)
    {
        SootMethodRef ref =
            Scene.v().makeMethodRef(set, "size", new LinkedList(),
                                    IntType.v(), false);

        return Jimple.v().newVirtualInvokeExpr(base, ref, new LinkedList());
    }

    /**
     * Return an invoke expression to a lock method ("get"/"own"/"release")
     */
    protected InvokeExpr lockMethod(Local base, String name)
    {
        Type ret_type;
        if (name.equals("own"))
            ret_type = BooleanType.v();
        else
            ret_type = VoidType.v();

        SootMethodRef ref =
            Scene.v().makeMethodRef(lock, name, new LinkedList(),
                                    ret_type, false);

        return Jimple.v().newVirtualInvokeExpr(base, ref, new LinkedList());
    }

    /**
     * Return an invoke expression to a method which adds bindings
     * to a constraint.
     *
     * If positive is true, it generates an addBindingForSymbolx
     * call, otherwise an addNegativeBindingForSymbolx call.
     */
    protected InvokeExpr bindingsMethod(String symbol, Local base,
                                        SootMethod caller, Value from_state,
                                        Value to_state, boolean positive)
    {
        Body body = caller.getActiveBody();
        int params = tm.getVariableOrder(symbol).size();
        String name;
        List args = new ArrayList(params);

        if (positive) {
            args.add(from_state);
            args.add(to_state);
            name = "addBindingsForSymbol" + symbol;
        } else {
            args.add(to_state);
            name = "addNegativeBindingsForSymbol" + symbol;
        }

        for (int i = 0; i < params; i++)
            args.add(body.getParameterLocal(i));

        SootMethodRef ref = constraint.getMethodByName(name).makeRef();

        return Jimple.v().newVirtualInvokeExpr(base, ref, args);
    }

    /**
     * Return an invoke expression for the constraint "or" method.
     */
    protected InvokeExpr orMethod(Local base, Local arg)
    {
        List arg_types = new ArrayList(1);
        List args = new ArrayList(1);

        arg_types.add(constraint.getType());
        args.add(arg);

        SootMethodRef ref =
            Scene.v().makeMethodRef(constraint, "or", arg_types,
                                    constraint.getType(), false);

        return Jimple.v().newVirtualInvokeExpr(base, ref, args);
    }

    /**
     * Return an invoke expression for the constraint
     * "getDisjunctArray" method.
     */
    protected InvokeExpr disjunctsMethod(Local base)
    {
        List arg_types = new ArrayList(0);
        List args = arg_types;

        SootMethodRef ref =
            Scene.v().makeMethodRef(constraint, "getDisjunctArray",
                                    arg_types, object_array, false);
        
        return Jimple.v().newVirtualInvokeExpr(base, ref, args);
    }

    /**
     * Return an invoke expression for the TraceMatch
     * body method.
     */
    protected InvokeExpr bodyMethod(SootMethod method, Body body,
                                    Local base, List args)
    {
        SootMethodRef ref = method.makeRef();

        int num_args = ref.parameterTypes().size();
        List new_args = new ArrayList(num_args);
        new_args.addAll(args);

        for (int i = new_args.size(); i < num_args; i++)
            new_args.add(body.getParameterLocal(i));

        if (ref.isStatic())
            return Jimple.v().newStaticInvokeExpr(ref, new_args);
        else
            return Jimple.v().newVirtualInvokeExpr(base, ref, new_args);
    }

    protected VirtualInvokeExpr realBodyMethod(Body body, Local disjuncts,
                                                Local offset)
    {
        SootMethodRef ref = tm.getRealBodyMethod().makeRef();
        int num_args = ref.parameterTypes().size();

        List args = new ArrayList(num_args);

        for (int i = 0; i < num_args - 2; i++)
            args.add(body.getParameterLocal(i));

        args.add(disjuncts);
        args.add(offset);

        Local base = body.getThisLocal();

        return Jimple.v().newVirtualInvokeExpr(base, ref, args);
    }

    /**
     * Finds a method call to a method with a given name, on the
     * tracematch container class, starting searching units
     * beginning at current.
     */
    protected Stmt findMethodCall(Chain units, Stmt current, String name)
    {
        while (current != units.getLast()) {
            InvokeExpr invoke_expr = null;

            if (current instanceof AssignStmt) {
                AssignStmt assign = (AssignStmt) current;

                if (assign.getRightOp() instanceof InvokeExpr)
                    invoke_expr = (InvokeExpr) assign.getRightOp();
            }
            else if (current instanceof InvokeStmt) {
                InvokeStmt call = (InvokeStmt) current;

                if (call.getInvokeExpr() instanceof InvokeExpr)
                    invoke_expr = (InvokeExpr) call.getInvokeExpr();
            }

            if (invoke_expr != null) {
                SootMethodRef called_ref = invoke_expr.getMethodRef();

                if (   called_ref.name().equals(name)
                    && called_ref.declaringClass() == tm.getContainerClass())
                    return current;
            }

            current = (Stmt) units.getSuccOf(current);
        }

        return null;
    }

    ////////////// ACTUAL CODE GENERATION METHODS ////////////////////////

    /**
     * Create a Jimple local to use as a counter.
     */
    protected Local newCounter(Body body, Chain units)
    {
        Local counter = addLocal(body, "i", IntType.v());
        units.addLast(Jimple.v().newAssignStmt(counter, getInt(0)));

        return counter;
    }

    protected Local copyCounter(Body body, Chain units, Local counter)
    {
        Local new_counter = newCounter(body, units);
        units.addLast(Jimple.v().newAssignStmt(new_counter, counter));

        return new_counter;
    }

    protected void incCounter(Chain units, Local counter)
    {
        Expr sum = Jimple.v().newAddExpr(counter, IntConstant.v(1));

        units.addLast(Jimple.v().newAssignStmt(counter, sum));
    }

    protected void decCounter(Chain units, Local counter)
    {
        Expr diff = Jimple.v().newSubExpr(counter, IntConstant.v(1));

        units.addLast(Jimple.v().newAssignStmt(counter, diff));
    }

    protected Local getArrayLength(Body body, Chain units, Local array)
    {
        Local result = addLocal(body, "i", IntType.v());

        Expr length = Jimple.v().newLengthExpr(array);
        units.addLast(Jimple.v().newAssignStmt(result, length));

        return result;
    }

    /**
     * Creates a Jimple no-op statement, which can be used
     * as a place holder to jump to.
     */
    protected Stmt newPlaceHolder()
    {
        return Jimple.v().newNopStmt();
    }


    protected Local insertDisjunctsLocal(Body body, Chain units)
    {
        Local disjuncts = addLocal(body, "disjuncts", object_array);
        Expr new_array = Jimple.v().newNewArrayExpr(object, IntConstant.v(1));

        units.addLast(Jimple.v().newAssignStmt(disjuncts, new_array));

        return disjuncts;
    }

    /**
     * Insert the place holder into the statement chain.
     */
    protected void insertPlaceHolder(Chain units, Stmt placeholder)
    {
        units.addLast(placeholder);
    }

    /**
     * Creates a new empty patching chain.
     */
    protected Chain newChain()
    {
        return new PatchingChain(new HashChain());
    }

    /**
     * Inserts the proceed() call, if any, and the return
     * statement.
     */
    protected void insertBeforeReturn(Chain new_units, Chain units)
    {
        Object insertion_point = units.getLast();
        if (insertion_point instanceof ReturnStmt)
            insertion_point = units.getPredOf(insertion_point);

        insertion_point = units.getPredOf(insertion_point);
        units.insertAfter(new_units, insertion_point);
    }

    /**
     * Get the proceed() call, if any, or the return statement.
     * (to provide a target for jumps)
     */
    protected Stmt getReturn(Chain units)
    {
        Object stmt = units.getLast();
        if (stmt instanceof ReturnStmt)
                stmt = units.getPredOf(stmt);

        return (Stmt) stmt;
    }

    /**
     * Insert a new return call returning value.
     */
    protected void insertReturn(Chain units, Value val)
    {
        if (val != null)
            units.addLast(Jimple.v().newReturnStmt(val));
        else
            units.addLast(Jimple.v().newReturnVoidStmt());
    }

    protected void insertGoto(Chain units, Stmt placeholder)
    {
        units.addLast(Jimple.v().newGotoStmt(placeholder));
    }

    protected void insertIf(Chain units, Local bool, Stmt jump_to)
    {
        EqExpr test = Jimple.v().newEqExpr(bool, IntConstant.v(0));

        units.addLast(Jimple.v().newIfStmt(test, jump_to));
    }

    protected void insertIfNot(Chain units, Local bool, Stmt jump_to)
    {
        EqExpr test = Jimple.v().newEqExpr(bool, IntConstant.v(1));

        units.addLast(Jimple.v().newIfStmt(test, jump_to));
    }

    protected void insertIf(Chain units, Expr expr, Stmt jump_to)
    {
        units.addLast(Jimple.v().newIfStmt(expr, jump_to));
    }

    protected void insertNullChecks(SootMethod m, Chain units)
    {
        Body body = m.getActiveBody();
        int params = m.getParameterCount();
        Stmt return_label = newPlaceHolder();
        Stmt finish_label = newPlaceHolder();

        for (int i = 0; i < params; i++) {
            Local param = body.getParameterLocal(i);

            // skip this test for primitive bindings
            if (! (param.getType() instanceof RefLikeType))
                continue;

            EqExpr test = Jimple.v().newEqExpr(param, NullConstant.v());

            units.addLast(Jimple.v().newIfStmt(test, return_label));
        }

        insertGoto(units, finish_label);
        insertPlaceHolder(units, return_label);
        insertReturn(units, null);
        insertPlaceHolder(units, finish_label);
        Local this_local = body.getThisLocal();
        Local updated_base = getLabelBase(body, units, this_local);
        setUpdated(units, updated_base, IntConstant.v(1));
    }

    /**
     * Create a Jimple local containing an integer constant
     */
    protected Value getInt(int n)
    {
        return IntConstant.v(n);
    }

    /**
     * Create a Jimple local containing the value of a
     * static constant on the constraint class of the
     * same type as the constraint class.
     */
    protected Local getConstant(Body body, Chain units, String name)
    {
        Ref ref = makeConstantRef(name);
        Local constant = addLocal(body, name, constraint.getType());
        units.addLast(Jimple.v().newAssignStmt(constant, ref));

        return constant;
    }

    /**
     * Get the number of disjuncts that a constraint has.
     */
    protected Local getNumberOfDisjuncts(Body body, Chain units,
                                         Local constraint)
    {
        Local disjuncts = addLocal(body, "disjuncts", set.getType());
        Local solutions = addLocal(body, "solutions", IntType.v());

        Ref disjuncts_ref = makeDisjunctsRef(constraint);
        Expr size_call = sizeMethod(disjuncts);

        units.addLast(Jimple.v().newAssignStmt(disjuncts, disjuncts_ref));
        units.addLast(Jimple.v().newAssignStmt(solutions, size_call));

        return solutions;
    }

    /**
     * Assign a variable field on a disjunct object to a local
     * of the same name.
     */
    protected void getDisjunctVar(Body body, Chain units,
                                  Local disjunct, Local local)
    {
        String name = local.getName();
        Type type = local.getType();
        Ref ref = makeDisjunctVarRef(disjunct, name, type);

        units.addLast(Jimple.v().newAssignStmt(local, ref));
    }

    protected void makeLabelsThreadLocal(Body body)
    {
        SootClass labels_thread_local = tm.getLabelsThreadLocalClass();
        Chain units = newChain();

        Local this_local = body.getThisLocal();
        Local new_obj =
            addLocal(body, "labels_per_thread", labels_thread_local.getType());

        SootMethodRef construct_ref =
            Scene.v().makeConstructorRef(labels_thread_local, new ArrayList());

        Expr new_expr =
            Jimple.v().newNewExpr(labels_thread_local.getType());
        Expr construct_expr =
            Jimple.v().newSpecialInvokeExpr(new_obj, construct_ref);

        Ref field_ref = makeLabelsThreadLocalRef(this_local);

        units.addLast(Jimple.v().newAssignStmt(new_obj, new_expr));
        units.addLast(Jimple.v().newInvokeStmt(construct_expr));
        units.addLast(Jimple.v().newAssignStmt(field_ref, new_obj));

        insertBeforeReturn(units, body.getUnits());
    }

    /**
     * Create a new org.aspectbench.tm.runtime.internal.Lock object
     * assign it to the lock field on the tracematch container.
     */
    protected void makeLock(Body body, Chain units)
    {
        Local this_local = body.getThisLocal();
        Local new_lock = addLocal(body, "lock", lock.getType());

        SootMethodRef construct_ref =
            Scene.v().makeConstructorRef(lock, new LinkedList());
        Expr new_expr =
            Jimple.v().newNewExpr(lock.getType());
        Expr construct_expr =
            Jimple.v().newSpecialInvokeExpr(new_lock, construct_ref);

        Ref lock_ref = makeLockRef(this_local);

        units.addLast(Jimple.v().newAssignStmt(new_lock, new_expr));
        units.addLast(Jimple.v().newInvokeStmt(construct_expr));
        units.addLast(Jimple.v().newAssignStmt(lock_ref, new_lock));
    }

    /**
     * Acquire the tracematch's lock.
     */
    protected void getLock(Body body, Chain units)
    {
        callLockMethod(body, units, "get");
    }

    /**
     * Release the tracematch's lock.
     */
    protected void releaseLock(Body body, Chain units)
    {
        callLockMethod(body, units, "release");
    }

    /**
     * Return a boolean expression which is true if the
     * current thread does not own the tracematch's lock.
     */
    protected Expr doNotOwnLock(Body body, Chain units)
    {
        Local result = callLockMethod(body, units, "own");

        return Jimple.v().newEqExpr(result, getInt(0));
    }

    /**
     * Create a Jimple local and assign it the value of the
     * "updated" flag on the tracematch container class.
     */
    protected Local getUpdated(Body body, Chain units, Local base)
    {
        Ref ref = makeUpdatedRef(base);

        Local updated = addLocal(body, "updated", BooleanType.v());
        units.addLast(Jimple.v().newAssignStmt(updated, ref));
        
        return updated;
    }

    /**
     * Assign a Jimple local to the "updated" flag on the
     * tracematch container class.
     */
    protected void setUpdated(Chain units, Local base, Value val)
    {
        Ref ref = makeUpdatedRef(base);
        units.addLast(Jimple.v().newAssignStmt(ref, val));
    }
 
    /**
     * Generate a call to the static method which cleans up
     * the indexing maps if collectable weak references have
     * expired.
     */
    protected void cleanupMaps(Chain units)
    {
        SootMethodRef ref =
            cleanup_refs.getMethodByName("checkExpired").makeRef();
        List args = new LinkedList();
        InvokeExpr cleanup_call = Jimple.v().newStaticInvokeExpr(ref, args);
        units.addLast(Jimple.v().newInvokeStmt(cleanup_call));
    }

    protected Local getLabelBase(Body body, Chain units, Local base)
    {
        if (! tm.isPerThread())
            return base;

        SootClass labels_tl_class = tm.getLabelsThreadLocalClass();
        SootClass labels_class = tm.getLabelsClass();

        Local labels_tl = addLocal(body, "labels_tl",
                                    labels_tl_class.getType());
        Local labels_obj = addLocal(body, "labels_obj", object);
        Local labels = addLocal(body, "labels", labels_class.getType());

        SootMethodRef get = thread_local.getMethodByName("get").makeRef();

        Ref labels_tl_ref = makeLabelsThreadLocalRef(base);
        InvokeExpr get_call =
            Jimple.v().newVirtualInvokeExpr(labels_tl, get, new ArrayList());
        CastExpr cast =
            Jimple.v().newCastExpr(labels_obj, labels.getType());

        units.addLast(Jimple.v().newAssignStmt(labels_tl, labels_tl_ref));
        units.addLast(Jimple.v().newAssignStmt(labels_obj, get_call));
        units.addLast(Jimple.v().newAssignStmt(labels, cast));
        
        return labels;
    }

    /**
     * Create a Jimple local and assign it the value of a label
     * field.
     */
    protected Local getLabel(Body body, Chain units, Local base,
                                int state, int kind)
    {
        Ref ref = makeLabelRef(base, state, kind);

        Local label = addLocal(body, "label", constraint.getType());
        units.addLast(Jimple.v().newAssignStmt(label, ref));

        return label;
    }

    /**
     * Assign a Jimple value to a label field.
     */
    protected void assignToLabel(Body body, Chain units, Local base,
                                    int state, int kind, Immediate val)
    {
        Ref ref = makeLabelRef(base, state, kind);
        units.addLast(Jimple.v().newAssignStmt(ref, val));
    }
 
    /**
     * Create a Jimple local containing the result of casting
     * the argument to the disjunct type.
     */
    protected Local castDisjunct(Body body, Chain units, Value arg)
    {
        CastExpr cast_val = Jimple.v().newCastExpr(arg, disjunct.getType());

        Local result = addLocal(body, "disjunct", disjunct.getType());
        units.addLast(Jimple.v().newAssignStmt(result, cast_val));

        return result;
    }

    /**
     * Create a Jimple local containing a `fake' disjunct (null).
     */
    protected Local fakeDisjunct(Body body, Chain units)
    {
        Local result = addLocal(body, "disjunct", disjunct.getType());
        units.addLast(Jimple.v().newAssignStmt(result, NullConstant.v()));

        return result;
    }

    /**
     * Call a bindings method (addBindingsForSymbolx or
     * addNegativeBindingsForSymbolx, depending on positive_bindings).
     *
     * Return the result as a Jimple local.
     */
    protected Local callBindingsMethod(Body body, Chain units, String symbol,
                                        Local base, SootMethod caller,
                                        Value from_state, Value to_state,
                                        boolean positive_bindings)
    {
        Value call = bindingsMethod(symbol, base, caller, from_state,
                                    to_state, positive_bindings);
        Local result = addLocal(body, "bind_result", constraint.getType());
        units.addLast(Jimple.v().newAssignStmt(result, call));

        return result;
    }

    /**
     * Call the or method on a constraint. Return the
     * result as a Jimple local.
     */
    protected Local callOrMethod(Body body, Chain units, Local base, Local arg)
    {
        Value call = orMethod(base, arg);
        Local result = addLocal(body, "or_result", constraint.getType());
        units.addLast(Jimple.v().newAssignStmt(result, call));

        return result;
    }

    /**
     * Call the getDisjunctIterator method on a constraint.
     * Assign the result to the passed disjuncts local.
     */
    protected void callDisjunctsMethod(Body body, Chain units, Local base,
                                        Local disjuncts)
    {
        Value call = disjunctsMethod(base);
        units.addLast(Jimple.v().newAssignStmt(disjuncts, call));
    }

    /**
     * Assign an empty Object array to the passed local.
     */
    protected void makeEmptyArray(Chain units, Local result)
    {
        Expr new_array = Jimple.v().newNewArrayExpr(object, getInt(0));
        units.addLast(Jimple.v().newAssignStmt(result, new_array));
    }

    /**
     * return true if the offset is not passed the
     * end of the array, otherwise false
     */
    protected Expr arrayAtEnd(Body body, Chain units, Local base, Local offset)
    {
        Local length = addLocal(body, "length", IntType.v());

        Expr array_length = Jimple.v().newLengthExpr(base);

        units.addLast(Jimple.v().newAssignStmt(length, array_length));

        return Jimple.v().newGeExpr(offset, length);
    }

    /**
     * return base[offset++] as a new Jimple local
     * (that is, the increment is done afterwards)
     */
    protected Local arrayGet(Body body, Chain units,
                             Local base, Local offset)
    {
        Local result = addLocal(body, "next", object);

        Ref result_ref = Jimple.v().newArrayRef(base, offset);

        units.addLast(Jimple.v().newAssignStmt(result, result_ref));

        return result;
    }

    protected void swapAssignForIdentity(Chain units, String local_name,
                                            int param)
    {
        Object current = units.getFirst();
        boolean found = false;

        while (!found) {
            if (! (current instanceof AssignStmt)) {
                current = units.getSuccOf(current);
                continue;
            }

            AssignStmt assign = (AssignStmt) current;

            if (! (assign.getLeftOp() instanceof Local)) {
                current = units.getSuccOf(current);
                continue;
            }

            Local assigned = (Local) assign.getLeftOp();

            if (! assigned.getName().equals(local_name)) {
                current = units.getSuccOf(current);
                continue;
            }

            ParameterRef param_ref =
                Jimple.v().newParameterRef(assigned.getType(), param);
            IdentityStmt id_stmt =
                Jimple.v().newIdentityStmt(assigned, param_ref);

            units.swapWith(assign, id_stmt);
            found = true;
        }
    }

    protected Local callLockMethod(Body body, Chain units, String name)
    {
        Local this_local = body.getThisLocal();
        Local lock_local = addLocal(body, "lock", lock.getType());
        Local result = null;

        Ref lock_ref = makeLockRef(this_local);
        Expr call = lockMethod(lock_local, name);

        units.addLast(Jimple.v().newAssignStmt(lock_local, lock_ref));

        if (name.equals("own")) {
            result = addLocal(body, "own", BooleanType.v());
            units.addLast(Jimple.v().newAssignStmt(result, call));
        } else {
            units.addLast(Jimple.v().newInvokeStmt(call));
        }

        return result;
    }

    protected Local callDummyProceedMethod(Body body, Chain units)
    {
        SootClass container = tm.getContainerClass();
        String proceed_name = tm.getDummyProceedName();
        SootMethod proceed = container.getMethodByName(proceed_name);
        Type ret_type = proceed.getReturnType();

        Local this_local = body.getThisLocal();
        Local result = null;
     
        Value call = bodyMethod(proceed, body, this_local, new LinkedList());

        if (ret_type == VoidType.v()) {
            units.addLast(Jimple.v().newInvokeStmt(call));
        } else {
            result = addLocal(body, "ret_val", ret_type);
            units.addLast(Jimple.v().newAssignStmt(result, call));
        }

        return result;
    }

    /**
     * Call the TraceMatch body method.
     */
    protected void callBodyMethod(Body body, Chain units,
                                    List args, Local result)
    {
        SootMethod method = tm.getBodyMethod();
        Local this_local = body.getThisLocal();

        Value call = bodyMethod(method, body, this_local, args);

        if (result == null)
            units.addLast(Jimple.v().newInvokeStmt(call));
        else
            units.addLast(Jimple.v().newAssignStmt(result, call));
    }

    protected Stmt updateBodyCall(SootMethod method, Chain units, Stmt call)
    {
        Body body = method.getActiveBody();
        Local this_local = body.getThisLocal();
        InvokeExpr call_expr;
        Local result;

        if (call instanceof InvokeStmt) {
            call_expr = ((InvokeStmt) call).getInvokeExpr();
            result = null;
        } else {
            AssignStmt assign = (AssignStmt) call;
            call_expr = (InvokeExpr) assign.getRightOp();
            result = (Local) assign.getLeftOp();
        }

        List args = call_expr.getArgs();

        // We are planning to re-write body(...) -> real_body(...)
        //
        // BUT, if this call to body(...) has the correct number
        // of arguments then it is not one of the proceed() calls
        // we re-wrote - it is a woven advice-call and should
        // be left alone!
 
        if (args.size() == tm.getBodyMethod().getParameterCount())
            return (Stmt) units.getSuccOf(call);


        Value new_call_expr = bodyMethod(method, body, this_local, args);
        Stmt new_call;

        if (result == null)
            new_call = Jimple.v().newInvokeStmt(new_call_expr);
        else
            new_call = Jimple.v().newAssignStmt(result, new_call_expr);

        units.swapWith(call, new_call);

        return new_call;
    }

    protected Local callRealBodyMethod(Body body, Chain units,
                                        Local disjuncts, Local offset)
    {
        Type ret_type = tm.getRealBodyMethod().getReturnType();

        Value call = realBodyMethod(body, disjuncts, offset);
        Local result = null;

        if (ret_type != VoidType.v()) {
            result = addLocal(body, "result", ret_type);
            units.addLast(Jimple.v().newAssignStmt(result, call));
        } else {
            units.addLast(Jimple.v().newInvokeStmt(call));
        }

        return result;
    }

    /**
     * Generates all the label fields modifies the <init> method
     * to initialise them. If the tracematch is not per-thread,
     * these are generated on the tracematch container class,
     * otherwise they are generated on a separate labels-class
     * which is accessed from the container class by a
     * ThreadLocal.
     */
    public void makeAndInitLabelFields()
    {
        if (tm.isPerThread()) {
            makeLabelsClass();
            makeLabelsThreadLocalClass();
            makeAndInitThreadLocalField();
        }

        SootClass container = tm.getLabelsClass();
        TMStateMachine sm = (TMStateMachine) tm.getStateMachine();
        Iterator states = sm.getStateIterator();

        SootMethod init =
            container.getMethodByName(SootMethod.constructorName);
        Body body = init.getActiveBody();

        Local this_local = body.getThisLocal();
        Chain units = newChain();

        if (! tm.isPerThread()) {
            makeLockField();
            makeLock(body, units);
        }

        Local trueC = getConstant(body, units, "trueC");
        Local falseC = getConstant(body, units, "falseC");

        makeUpdatedField();
        setUpdated(units, this_local, IntConstant.v(0));

        while (states.hasNext()) {
            SMNode state = (SMNode) states.next();
            int s_num = state.getNumber();

            if (state.isInitialNode()) {
                makeLabelField(s_num, LABEL);
                assignToLabel(body, units, this_local, s_num, LABEL, trueC);
            } else {
                makeLabelField(s_num, LABEL);
                makeLabelField(s_num, TMP_LABEL);
                makeLabelField(s_num, SKIP_LABEL);
                assignToLabel(body, units, this_local,
                                s_num, LABEL, falseC);
                assignToLabel(body, units, this_local,
                                s_num, TMP_LABEL, falseC);
                assignToLabel(body, units, this_local,
                                s_num, SKIP_LABEL, falseC);
            }
        }

        insertBeforeReturn(units, body.getUnits());
    }

    /**
     * Generate code to jump over some updates if any of the bindings
     * are null.
     */
    public void genNullChecks(SootMethod method)
    {
        Body body = method.getActiveBody();
        Chain units = newChain();

        insertNullChecks(method, units);
        insertBeforeReturn(units, body.getUnits());
    }

    /**
     * Generate code to acquire a tracematch lock while the
     * labels are being updated.
     */
    public void genAcquireLock()
    {
        SootMethod method = tm.getSynchAdviceMethod();
        Body body = method.getActiveBody();
        Local this_local = body.getThisLocal();

        Chain units = newChain();

        // acquire lock if the tracematch is not per-thread
        if (! tm.isPerThread())
            getLock(body, units);

        // reset updated flag
        Local updated_base = getLabelBase(body, units, this_local);
        setUpdated(units, updated_base, IntConstant.v(0));

        // generate call to clean up the indexing maps
        // if any collectable weak references have expired
        cleanupMaps(units);

        insertBeforeReturn(units, body.getUnits());
    }

    /**
     * Generate code to update a label with the constraint
     * for performing a "from --->[symbol] to" transition.
     */
    public void genLabelUpdate(int from, int to, String symbol,
                                        SootMethod method)
    {
        Body body = method.getActiveBody();

        Local this_local = body.getThisLocal();

        Chain units = newChain();
        Local label_base = getLabelBase(body, units, this_local);

        Value from_state = getInt(from);
        Value to_state = getInt(to);
        Local lab_from = getLabel(body, units, label_base, from, LABEL);
        Local bind_result =
            callBindingsMethod(body, units, symbol, lab_from,
                                method, from_state, to_state, true);

        Local lab_to = getLabel(body, units, label_base, to, TMP_LABEL);
        Local or_result =
            callOrMethod(body, units, lab_to, bind_result);
        assignToLabel(body, units, label_base, to, TMP_LABEL, or_result);

        insertBeforeReturn(units, body.getUnits());
    }

    /**
     * temporary --- this should be removed when the
     *               old-style codegen is removed
     */
    public void genRegisterSymbolBindings(String symbol, SootMethod method)
    {
        // do nothing
    }

    /**
     * Generate code to update a label with the constraint for
     * a skip transition.
     */
    public void genSkipLabelUpdate(SMNode to, String symbol, SootMethod method)
    {
        Body body = method.getActiveBody();

        Local this_local = body.getThisLocal();

        Chain units = newChain();
        Local label_base = getLabelBase(body, units, this_local);

        int to_number = to.getNumber();
		Value to_state = getInt(to_number);
        Local lab = getLabel(body, units, label_base, to_number, SKIP_LABEL);
        Local result =
            callBindingsMethod(body, units, symbol, lab, method,
                                to_state, to_state, false);

        assignToLabel(body, units, label_base, to_number, SKIP_LABEL, result);

        insertBeforeReturn(units, body.getUnits());
    }

    /**
     * Generate code that tests the "updated" flag on the
     * labels class. If it is not set, code is generated to
     * release the tracematch lock and return.
     */
    protected void genReturnIfNotUpdated(SootMethod method)
    {
        Body body = method.getActiveBody();
        Local this_local = body.getThisLocal();
        Chain units = newChain();
        Local updated_base = getLabelBase(body, units, this_local);

        Stmt rest = newPlaceHolder();

        Local updated = getUpdated(body, units, updated_base);
        insertIfNot(units, updated, rest);

        if (! tm.isPerThread())
            releaseLock(body, units);
        insertReturn(units, null);
        insertPlaceHolder(units, rest);

        insertBeforeReturn(units, body.getUnits());
    }

    /**
     * Generate the code for master-updating labels - i.e. the
     * assignments from labelX_tmp to labelX etc.  that get
     * performed in the some() advice. (non-skip case)
     */
    protected void genLabelMasterUpdate(boolean skip_loop, int state,
                                        SootMethod method, boolean is_final)
    {
        Body body = method.getActiveBody();

        Local this_local = body.getThisLocal();

        Chain units = newChain();
        Local label_base = getLabelBase(body, units, this_local);

        // Assign TMP_LABEL to LABEL
        // (combining with SKIP_LABEL if appropriate)
        Local new_label;
        new_label = getLabel(body, units, label_base, state, TMP_LABEL);

        if (skip_loop) {
            Local skip_label =
                getLabel(body, units, label_base, state, SKIP_LABEL);
            new_label = callOrMethod(body, units, new_label, skip_label);
        }

        // update LABEL
        assignToLabel(body, units, label_base, state, LABEL, new_label);

        // reset TMP_LABEL
        Local falseC = getConstant(body, units, "falseC");
        assignToLabel(body, units, label_base, state, TMP_LABEL, falseC);

        // reset SKIP_LABEL
        assignToLabel(body, units, label_base, state, SKIP_LABEL, new_label);

        if (is_final && !tm.isPerThread())
            genLockRelease(body, units, new_label, true);

        insertBeforeReturn(units, body.getUnits());
    }

    /**
     * Generate code to release the tracematch lock (if we don't own
     * the lock, then the release-method does nothing)
     *
     * If "check" is true, the lock is only released if there are
     * no solutions on the constraint object for the final state.
     */
    protected void genLockRelease(Body body, Chain units, Local label,
                                    boolean check)
    {
        Local solutions = getNumberOfDisjuncts(body, units, label);

        Stmt end = newPlaceHolder();

        if (check) {
            Expr condition = Jimple.v().newNeExpr(solutions, getInt(0));
            insertIf(units, condition, end);
        }

        releaseLock(body, units);
        insertPlaceHolder(units, end);
    }

    public void transformBodyMethod()
    {
        transformBodyMethodSignature();
        transformBodyMethodLocalAssignments();
    }

    /**
     * Remove all tracematch formal parameters (apart from any around
     * formals) from the tracematch body method signature, so that the
     * advice which refers to it can be woven correctly.
     */
    protected void transformBodyMethodSignature()
    {
        SootMethod method = tm.getBodyMethod();

        // change signature
        List new_types = new ArrayList();
        Iterator i = tm.getNewAdviceBodyFormals().iterator();
        
        while (i.hasNext()) {
            Formal f = (Formal) i.next();

            new_types.add(f.getType().getSootType());
        }

        method.setParameterTypes(new_types);
    }

    /**
     * Re-number local assignments to match the new signature,
     * assigning null to any locals for parameters the method
     * no longer has.
     */
    protected void transformBodyMethodLocalAssignments()
    {
        SootMethod method = tm.getBodyMethod();
        Body body = method.getActiveBody();
        Chain units = body.getUnits();
        Chain assignments = newChain();

        Object current = units.getFirst();

        while(current instanceof IdentityStmt)
        {
            IdentityStmt id_stmt = (IdentityStmt) current;
            current = units.getSuccOf(current);

            Local local = (Local) id_stmt.getLeftOp();
            Value rhs = id_stmt.getRightOp();

            if (! (rhs instanceof ParameterRef))
                continue;

            ParameterRef param_ref = (ParameterRef) rhs;
            int new_index = tm.getBodyParameterIndex(param_ref.getIndex());

            if (new_index != -1) {
                Ref new_param_ref =
                    Jimple.v().newParameterRef(param_ref.getType(), new_index);
                Stmt new_id_stmt =
                    Jimple.v().newIdentityStmt(local, new_param_ref);

                units.swapWith(id_stmt, new_id_stmt);
            } else {
                units.remove(id_stmt);
                assignments.add(
                    Jimple.v().newAssignStmt(local, NullConstant.v()));
            }
        }

        units.insertBefore(assignments, current);
    }

    public void transformParametersAndProceeds()
    {
        Stmt assignments_end = transformParameterAssignments();
        transformProceedCalls(assignments_end);
    }

    public Stmt transformParameterAssignments()
    {
        SootMethod method = tm.getBodyMethod();
        Body body = method.getActiveBody();

        Chain assignments = newChain();
 
        Local disjuncts = insertDisjunctsLocal(body, assignments);
        Local index = getArrayLength(body, assignments, disjuncts);
        incCounter(assignments, index);
        Local current_index = copyCounter(body, assignments, index);
        decCounter(assignments, current_index);

        if (tm.isAround()) {
            Stmt tm_body = newPlaceHolder();
            Stmt proceed_call = newPlaceHolder();

            // call proceed() if there are no more solutions
            Expr at_end = arrayAtEnd(body, assignments,
                                     disjuncts, current_index);

            insertIf(assignments, at_end, proceed_call);
            insertGoto(assignments, tm_body);

            insertPlaceHolder(assignments, proceed_call);
            Local result = callDummyProceedMethod(body, assignments);

            insertReturn(assignments, result);
            insertPlaceHolder(assignments, tm_body);
        }

        Value disjunct_obj = arrayGet(body, assignments,
                                        disjuncts, current_index);
        Local disjunct = castDisjunct(body, assignments, disjunct_obj);

        // we'll need these later...
        this.disjuncts_name = disjuncts.getName();
        this.disjuncts_index_name = index.getName();

        // find the end of the identity statements in the current method
        Chain units = body.getUnits();
        Object current = units.getFirst();

        while (current instanceof IdentityStmt)
            current = units.getSuccOf(current);

        int num_assigns = tm.nonAroundFormals();

        for (int i = 0; i < num_assigns; i++)
        {
            AssignStmt assign = (AssignStmt) current;
            Local local = (Local) assign.getLeftOp();

            Object next = units.getSuccOf(current);

            if (! tm.getUnusedFormals().contains(local.getName())) {
                getDisjunctVar(body, assignments, disjunct, local);
                units.remove(current);
            }

            current = next;
        }

        units.insertAfter(assignments, units.getPredOf(current));

        return (Stmt) current;
    }

    public void transformProceedCalls(Stmt search_start)
    {
        SootMethod method = tm.getBodyMethod();
        Body body = method.getActiveBody();
        Chain units = body.getUnits();

        String proceed = tm.getDummyProceedName();

        Stmt proceed_call =
                findMethodCall(units, search_start, proceed);

        while (proceed_call != null) {
            InvokeExpr call_expr;
            Local result;

            if (proceed_call instanceof InvokeStmt) {
                call_expr = ((InvokeStmt) proceed_call).getInvokeExpr();
                result = null;
            } else {
                AssignStmt assign = (AssignStmt) proceed_call;
                result = (Local) assign.getLeftOp();
                call_expr = (InvokeExpr) assign.getRightOp();
            }

            Chain new_stmt = newChain();
            callBodyMethod(body, new_stmt, call_expr.getArgs(), result);
            Stmt new_call = (Stmt) new_stmt.getFirst();

            units.swapWith(proceed_call, new_call);

            proceed_call = findMethodCall(units, new_call, proceed);
        }
    }

    public void extractBodyMethod()
    {
        SootClass container = tm.getContainerClass();
        SootMethod old_body_method = tm.getBodyMethod();

        String new_name = old_body_method.getName() + "_real";
        List types = new ArrayList(old_body_method.getParameterTypes());
        Type ret_type = old_body_method.getReturnType();
        int modifiers = old_body_method.getModifiers();
        List exceptions = old_body_method.getExceptions();

        SootMethod real_body_method =
            new SootMethod(new_name, types, ret_type, modifiers, exceptions);

        real_body_method.setActiveBody(old_body_method.getActiveBody());
        Body new_body = Jimple.v().newBody(old_body_method);
        old_body_method.setActiveBody(new_body);

        container.addMethod(real_body_method);
    }

    public void transformRealBodyMethod()
    {
        transformRealBodyMethodParameters();
        transformRealBodyMethodSelfCalls();
    }

    protected void transformRealBodyMethodParameters()
    {
        SootMethod method = tm.getRealBodyMethod();
        List types = new ArrayList(method.getParameterTypes());

        types.add(object_array);
        types.add(IntType.v());

        method.setParameterTypes(types);

        Body body = method.getActiveBody();
        Chain units = body.getUnits();

        int disjunct_param = types.size() - 2;
        int index_param = types.size() - 1;

        swapAssignForIdentity(units, disjuncts_name, disjunct_param);
        swapAssignForIdentity(units, disjuncts_index_name, index_param);
    }

    protected void transformRealBodyMethodSelfCalls()
    {
        SootMethod real_body_method = tm.getRealBodyMethod();
        Body body = real_body_method.getActiveBody();
        Chain units = body.getUnits();

        Stmt first = (Stmt) units.getFirst();
        String body_name = tm.getBodyMethod().getName();

        Stmt body_call = findMethodCall(units, first, body_name);

        while (body_call != null) {
            Stmt new_call = updateBodyCall(real_body_method, units, body_call);

            body_call = findMethodCall(units, new_call, body_name);
        }
    }

    /**
     * Generate code to run the tracematch body for each solution
     */
    protected void genRunSolutions()
    {
        SootMethod method = tm.getBodyMethod();
        Body body = method.getActiveBody();

        Chain units = body.getUnits();

        // generate identity statements;
        genIdentityStmts(method, body, units);

        Local this_local = body.getThisLocal();
        Local label_base = getLabelBase(body, units, this_local);

        Stmt fake_init = newPlaceHolder();
        Stmt init = newPlaceHolder();
        Stmt init_end = newPlaceHolder();
        Stmt loop = newPlaceHolder();
        Stmt end  = newPlaceHolder();

        Local disjuncts = addLocal(body, "disjuncts", object_array);

        // we initialise the disjunct array with dummy values in
        // two situations:
        //   (1) this is not a per-thread tracematch, but we
        //       don't own the lock (i.e. there were no solutions)
        //   (2) the updated flag is false

        if (! tm.isPerThread()) {
            Expr do_not_own_lock = doNotOwnLock(body, units);

            insertIf(units, do_not_own_lock, fake_init);
        }

        Local updated = getUpdated(body, units, label_base);
        insertIfNot(units, updated, init);

        // fake initialisation of disjunct array
        insertPlaceHolder(units, fake_init);
        makeEmptyArray(units, disjuncts);
        insertGoto(units, init_end);

        // real initialisation of disjunct array
        insertPlaceHolder(units, init);
        Local lab_final =
            getLabel(body, units, label_base, final_state, LABEL);
        callDisjunctsMethod(body, units, lab_final, disjuncts);
        eraseLabelOnFinalState(body, units, label_base);

        if (! tm.isPerThread())
            genLockRelease(body, units, lab_final, false);
        insertPlaceHolder(units, init_end);

        // initialise loop counter
        Local offset = newCounter(body, units);

        // Marker for beginning of loop
        insertPlaceHolder(units, loop);

        if (! tm.isAround()) {
            // loop test
            Expr at_end = arrayAtEnd(body, units, disjuncts, offset);
            insertIf(units, at_end, end);
        }

        Local result = callRealBodyMethod(body, units, disjuncts, offset);
        incCounter(units, offset);

        if (! tm.isAround()) {
            insertGoto(units, loop);
        }

        insertPlaceHolder(units, end);
        insertReturn(units, result);           
    }

    protected void eraseLabelOnFinalState(Body body, Chain units,
                                          Local label_base)
    {
        assignToLabel(body, units, label_base, final_state,
                        LABEL, NullConstant.v());
    }

    protected void genIdentityStmts(SootMethod method, Body body, Chain units)
    {
        RefType this_type = method.getDeclaringClass().getType();
        List types = method.getParameterTypes();

        Ref this_ref = Jimple.v().newThisRef(this_type);
        Local this_local = addLocal(body, "this", this_type);
        units.addLast(Jimple.v().newIdentityStmt(this_local, this_ref));

        Iterator i = types.iterator();
        int arg = 0;

        while (i.hasNext()) {
            Type arg_type = (Type) i.next();

            Ref arg_ref = Jimple.v().newParameterRef(arg_type, arg++);
            Local arg_local = addLocal(body, "arg", arg_type);
            units.addLast(Jimple.v().newIdentityStmt(arg_local, arg_ref));
        }
    }
}
