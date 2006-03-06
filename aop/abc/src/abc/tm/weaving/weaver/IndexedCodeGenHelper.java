/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Julian Tibble
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
 * This class uses the updated constraint-API
 * which supports indexed constraints.
 *
 * @author Julian Tibble
 */
public class IndexedCodeGenHelper extends CodeGenHelper
{
    public IndexedCodeGenHelper(TraceMatch tm)
    {
        super(tm);
    }

    /**
     * Return the field-name for the constraint label
     * for a particular state.
     *
     * (the new constraint-API does not have different
     * kinds of label)
     */
    protected String getLabelName(int state, int kind)
    {
        return tm.getName() + "_label" + state;
    }

    /**
     * Returns an invoke expression to the static "getTrue" method
     * on the constraint class.
     */
    protected InvokeExpr getTrueMethod(int state)
    {
        List args = new ArrayList(1);
        args.add(getInt(state));

        SootMethodRef ref = constraint.getMethodByName("getTrue").makeRef();

        return Jimple.v().newStaticInvokeExpr(ref, args);
    }

    /**
     * Returns an invoke expression for the constraint constructor
     */
    protected InvokeExpr constraintInitMethod(Local base, int state)
    {
        List arg_types = new ArrayList(1);
        arg_types.add(IntType.v());
        List args = new ArrayList(1);
        args.add(getInt(state));

        SootMethodRef ref =
            Scene.v().makeMethodRef(constraint, SootMethod.constructorName,
                            arg_types, VoidType.v(), false);

        return Jimple.v().newSpecialInvokeExpr(base, ref, args);
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
            args.add(to_state);
            name = "getBindingsForSymbol" + symbol;
        } else {
            name = "queueNegativeBindingsForSymbol" + symbol;
        }

        for (int i = 0; i < params; i++)
            args.add(body.getParameterLocal(i));

        SootMethodRef ref = constraint.getMethodByName(name).makeRef();

        return Jimple.v().newVirtualInvokeExpr(base, ref, args);
    }

    /**
     * Return an invoke expression for the constraint "queue" method.
     * The number of arguments to queue varies according to how many
     * indices the state has, and if they can be used for this a
     * particular call to queue.
     */
    protected InvokeExpr queueMethod(Local base, List args)
    {
        int num_args = args.size();
        List arg_types = new ArrayList(num_args);

        arg_types.add(set.getType());
        for (int i = 1; i < num_args; i++)
            arg_types.add(object);
 
        SootMethodRef ref =
            Scene.v().makeMethodRef(constraint, "queue", arg_types,
                                    VoidType.v(), false);

        return Jimple.v().newVirtualInvokeExpr(base, ref, args);
    }

    /**
     * Return an invoke expression to a method which merges queued
     * positive and negative bindings into a constraint.
     */
    protected InvokeExpr mergeMethod(Local base)
    {
        SootMethodRef ref =
            Scene.v().makeMethodRef(constraint, "merge", new LinkedList(),
                                    VoidType.v(), false);

        return Jimple.v().newVirtualInvokeExpr(base, ref, new LinkedList());
    }

    ////////////// ACTUAL CODE GENERATION METHODS ////////////////////////

    /**
     * This method is not needed for the new constraint-API,
     * which does not have canonical representations of
     * true and false constraints.
     */
    protected Local getConstant(Body body, Chain units, String name)
    {
        throw new InternalCompilerError(
                "Called getConstant, but using indexed constraints");
    }

    /**
     * Creates a new constraint which represents false for all
     * non-zero states, and true for the inital state (zero)
     */
    protected Local makeNewConstraint(Body body, Chain units, int state)
    {
        Local new_constraint =
            addLocal(body, "new_constraint", constraint.getType());

        if (state == 0) {
            Value call = getTrueMethod(state);
            units.addLast(Jimple.v().newAssignStmt(new_constraint, call));
        } else {
            Value new_expr = Jimple.v().newNewExpr(constraint.getType());
            Value call = constraintInitMethod(new_constraint, state);

            units.addLast(Jimple.v().newAssignStmt(new_constraint, new_expr));
            units.addLast(Jimple.v().newInvokeStmt(call));
        }

        return new_constraint;
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
        Local result = null;

        if (positive_bindings) {
            result = addLocal(body, "bind_result", set.getType());
            units.addLast(Jimple.v().newAssignStmt(result, call));
        } else {
            units.addLast(Jimple.v().newInvokeStmt(call));
        }

        return result;
    }

    /**
     * This method is not needed for the new constraint-API,
     * which uses "queue" methods to support more efficient
     * destructive updates.
     */
    protected Local callOrMethod(Body body, Chain units, Local base, Local arg)
    {
        throw new InternalCompilerError(
                "Called callOrMethod, but using indexed constraints");
    }

    /**
     * Call the "queue" method on a constraint.
     * The number of arguments to queue varies according to how many
     * indices the state has, and if they can be used for this a
     * particular call to queue.
     */
    protected void callQueueMethod(Body body, Chain units, Local base,
                                   List args)
    {
        Value call = queueMethod(base, args);
        units.addLast(Jimple.v().newInvokeStmt(call));
    }

    /**
     * Call the "merge" method, which merges queued positive and
     * negative bindings into a constraint.
     */
    protected void callMergeMethod(Body body, Chain units, Local base)
    {
        Value call = mergeMethod(base);
        units.addLast(Jimple.v().newInvokeStmt(call));
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

        makeUpdatedField();
        setUpdated(units, this_local, IntConstant.v(0));

        while (states.hasNext()) {
            SMNode state = (SMNode) states.next();
            int s_num = state.getNumber();

            makeLabelField(s_num, LABEL);
            Local con = makeNewConstraint(body, units, s_num);
            assignToLabel(body, units, this_local, s_num, LABEL, con);
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

        null_checks_jump_target = newPlaceHolder();
        insertNullChecks(method, units, null_checks_jump_target);
        
        insertBeforeReturn(units, body.getUnits());
    }

    /**
     * Generate the target for the null-check jump
     */
    public void genNullChecksJumpTarget(SootMethod method)
    {
        Body body = method.getActiveBody();
        Chain units = newChain();
        
        insertPlaceHolder(units, null_checks_jump_target);
        null_checks_jump_target = null;

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
        Local updated = getUpdated(body, units, updated_base);
        setUpdated(units, updated_base, IntConstant.v(0));

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

        setUpdated(units, label_base, IntConstant.v(1));

        Value from_state = getInt(from);
        Value to_state = getInt(to);
        Local lab_from = getLabel(body, units, label_base, from, LABEL);
        Local bind_result =
            callBindingsMethod(body, units, symbol, lab_from,
                                method, from_state, to_state, true);

        Local lab_to = getLabel(body, units, label_base, to, TMP_LABEL);
        List args = new ArrayList(1);
        args.add(bind_result);
        callQueueMethod(body, units, lab_to, args);

        insertBeforeReturn(units, body.getUnits());
    }

    /**
     * Generate code to update a label with the constraint for
     * a skip transition.
     */
    public void genSkipLabelUpdate(int to, String symbol, SootMethod method)
    {
        Body body = method.getActiveBody();

        Local this_local = body.getThisLocal();

        Chain units = newChain();
        Local label_base = getLabelBase(body, units, this_local);

        setUpdated(units, label_base, IntConstant.v(1));

        Value to_state = getInt(to);
        Local lab = getLabel(body, units, label_base, to, SKIP_LABEL);
        callBindingsMethod(body, units, symbol, lab, method,
                           to_state, to_state, false);

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

        Local label = getLabel(body, units, label_base, state, LABEL);
        callMergeMethod(body, units, label);

        if (is_final && !tm.isPerThread())
            genLockRelease(body, units, label, true);

        insertBeforeReturn(units, body.getUnits());
    }
}
