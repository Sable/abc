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
    protected InvokeExpr queueMethod(Local base, List args,
                                     int state, String symbol)
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
     * Create a box for a primitive binding which we need to use
     * as an index.
     */
    protected Local makeNewBox(Body body, Chain units, SootClass box_class,
                                Local value)
    {
        List arg_types = new ArrayList(1);
        arg_types.add(value.getType());

        List args = new ArrayList(1);
        args.add(value);

        SootMethodRef ref = Scene.v().makeMethodRef(
                                        box_class,
                                        SootMethod.constructorName,
                                        arg_types,
                                        VoidType.v(),
                                        false);

        Local box = addLocal(body, "box", box_class.getType());
        Value new_expr = Jimple.v().newNewExpr(box_class.getType());
        Value init_call = Jimple.v().newSpecialInvokeExpr(box, ref, args);

        units.addLast(Jimple.v().newAssignStmt(box, new_expr));
        units.addLast(Jimple.v().newInvokeStmt(init_call));
        
        return box;
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
                                   List args, int state, String symbol)
    {
        // if this state is indexed, and the symbol that
        // triggered this method call binds all the indices,
        // then add them to the "queue" call
        TMStateMachine sm = (TMStateMachine) tm.getStateMachine();
        SMNode node = sm.getStateByNumber(state);
        List sym_binds = tm.getVariableOrder(symbol);

        if (sym_binds.containsAll(node.indices)) {
            Local[] keys = new Local[node.indices.size()];

            for (int i = 0; i < keys.length; i++) {
                String var = (String) node.indices.get(i);
                int param_num = sym_binds.indexOf(var);
                Local param = body.getParameterLocal(param_num);

                if (tm.isPrimitive(var)) {
                    SootClass box_class = tm.weakBindingClass(var);
                    Type box_type = box_class.getType();
                    param = makeNewBox(body, units, box_class, param);
                }

                args.add(param);
            }
        }

        Value call = queueMethod(base, args, state, symbol);
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
        List args = new LinkedList();
        args.add(bind_result);
        callQueueMethod(body, units, lab_to, args, to, symbol);

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

    /**
     * Erase the final state by assigning a new label to it,
     * not writing null to the field as we did before with
     * the old constraint-API
     */
    protected void eraseLabelOnFinalState(Body body, Chain units,
                                          Local label_base)
    {
        Local con = makeNewConstraint(body, units, final_state);
        assignToLabel(body, units, label_base, final_state, LABEL, con);
    }
}
