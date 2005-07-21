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
    protected SootClass constraint;
    protected SootClass disjunct;
    protected SootClass iterator;
    protected Type object;

    protected int local_count; // used to ensure local names are unique

    public CodeGenHelper(TraceMatch tm)
    {
        this.tm = tm;
        this.constraint = tm.getConstraintClass();
        this.disjunct = tm.getDisjunctClass();

        this.iterator = Scene.v().getSootClass("java.util.Iterator");
        this.object = Scene.v().getRefType("java.lang.Object");

        this.local_count = 0;
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
     * Create a Jimple reference to the "updated" flag
     * on the tracematch container class.
     */
    protected InstanceFieldRef makeUpdatedRef(Value base)
    {
        SootFieldRef ref =
            Scene.v().makeFieldRef( tm.getContainerClass(),
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
                                Modifier.PRIVATE);

        tm.getContainerClass().addField(field);
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
            Scene.v().makeFieldRef( tm.getContainerClass(),
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
                                Modifier.PRIVATE);

        tm.getContainerClass().addField(field);
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
     * Return an invoke expression to a method which adds bindings
     * to a constraint.
     *
     * If state is non-null, it generates an addBindingForSymbolx
     * call, otherwise an addNegativeBindingForSymbolx call.
     */
    protected Value bindingsMethod(String symbol, Local base,
                                    SootMethod caller, Value state)
    {
        Body body = caller.getActiveBody();
        int params = caller.getParameterCount();
        String name;

        List arg_types = new ArrayList(params);
        List args = new ArrayList(params);

        if (state != null) {
            arg_types.add(IntType.v());
            args.add(state);
            name = "addBindingsForSymbol" + symbol;
        } else {
            name = "addNegativeBindingsForSymbol" + symbol;
        }

        for (int i = 0; i < params; i++)
        {
            arg_types.add(object);
            args.add(body.getParameterLocal(i));
        }

        SootMethodRef ref =
            Scene.v().makeMethodRef(constraint, name, arg_types,
                                    constraint.getType(), false);

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
     * "getDisjunctIterator" method.
     */
    protected InvokeExpr disjunctsMethod(Local base)
    {
        List arg_types = new ArrayList(0);
        List args = arg_types;

        SootMethodRef ref =
            Scene.v().makeMethodRef(constraint, "getDisjunctIterator",
                                    arg_types, iterator.getType(), false);
        
        return Jimple.v().newVirtualInvokeExpr(base, ref, args);
    }

    /**
     * Return an invoke expression for the Iterator
     * "hasNext()" or "next()" method (depending on
     * whether query is true or false).
     */
    protected InvokeExpr iteratorMethod(Local base, boolean query)
    {
        String name   = query ? "hasNext"       : "next";
        Type ret_type = query ? BooleanType.v() : object;

        List arg_types = new ArrayList(0);
        List args = arg_types;

        SootMethodRef ref = Scene.v().makeMethodRef(iterator, name, arg_types,
                                                    ret_type, false);

        return Jimple.v().newInterfaceInvokeExpr(base, ref, args);
    }

    /**
     * Return an invoke expression for the TraceMatch
     * body method.
     */
    protected VirtualInvokeExpr bodyMethod(Local base, Local disjunct,
                                            List arg_types, List args,
                                            Type ret_type)
    {
        SootMethodRef ref =
            Scene.v().makeMethodRef(tm.getContainerClass(),
                                    tm.getBodyMethod().getName(),
                                    arg_types, ret_type, false);

        return Jimple.v().newVirtualInvokeExpr(base, ref, args);
    }


    ////////////// ACTUAL CODE GENERATION METHODS ////////////////////////

    /**
     * Creates a Jimple no-op statement, which can be used
     * as a place holder to jump to.
     */
    protected Stmt newPlaceHolder()
    {
        return Jimple.v().newNopStmt();
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
        units.addLast(Jimple.v().newReturnStmt(val));
    }

    protected void insertGoto(Chain units, Stmt placeholder)
    {
        units.addLast(Jimple.v().newGotoStmt(placeholder));
    }

    protected void insertIf(Chain units, Value bool, Stmt placeholder)
    {
        EqExpr test = Jimple.v().newEqExpr(bool, IntConstant.v(0));

        units.addLast(Jimple.v().newIfStmt(test, placeholder));
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
     * Create a Jimple local and assign it the value of a
     * variable field on a disjunct object.
     */
    protected Local getDisjunctVar(Body body, Chain units, Local base,
                                    String name, Type type)
    {
        Ref ref = makeDisjunctVarRef(base, name, object);
        Local var_object = addLocal(body, name + "_object", object);
        units.addLast(Jimple.v().newAssignStmt(var_object, ref));

        Local var = addLocal(body, name, type);
        CastExpr cast = Jimple.v().newCastExpr(var_object, type);
        units.addLast(Jimple.v().newAssignStmt(var, cast));

        return var;
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
     * Create a Jimple local and assign it the value of a label
     * field on an instance of the constraint class.
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
     * Assign a Jimple local to a label field on a constraint class.
     */
    protected void assignToLabel(Chain units, Local base,
                                    int state, int kind, Local val)
    {
        Ref ref = makeLabelRef(base, state, kind);
        units.addLast(Jimple.v().newAssignStmt(ref, val));
    }
 
    /**
     * Create a Jimple local containing the result of casting
     * the argument to the disjunct type.
     */
    protected Local castDisjunct(Body body, Chain units, Local arg)
    {
        CastExpr cast_val = Jimple.v().newCastExpr(arg, disjunct.getType());

        Local result = addLocal(body, "disjunct", disjunct.getType());
        units.addLast(Jimple.v().newAssignStmt(result, cast_val));

        return result;
    }

    /**
     * Call a bindings method (addBindingsForSymbolx, or
     * addNegativeBindigsForSymbolx if state is null).
     * Return the result as a Jimple local.
     */
    protected Local callBindingsMethod(Body body, Chain units,
                                        String symbol, Local base,
                                        SootMethod caller, Value state)
    {
        Value call = bindingsMethod(symbol, base, caller, state);
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
     * Return the result as a Jimple local.
     */
    protected Local callDisjunctsMethod(Body body, Chain units, Local base)
    {
        Value call = disjunctsMethod(base);
        Local result = addLocal(body, "disjuncts", iterator.getType());
        units.addLast(Jimple.v().newAssignStmt(result, call));

        return result;
    }

    /**
     * Call the hasNext() or next() method on an Iterator,
     * depending on whether query is true or false.
     * Return the result as a Jimple local.
     */
    protected Local callIteratorMethod(Body body, Chain units, Local base,
                                        boolean query)
    {
        String result_name = query ? "has_next"      : "next";
        Type ret_type      = query ? BooleanType.v() : object;

        Value call = iteratorMethod(base, query);
        Local result = addLocal(body, result_name, ret_type);
        units.addLast(Jimple.v().newAssignStmt(result, call));

        return result;
    }

    /**
     * Call the TraceMatch body method, return a Jimple local
     * for the result, or null if it is a void method.
     */
    protected Local callBodyMethod(Body body, Chain units, Local base,
                                    Local disjunct, List formals)
    {
        List arg_types = new ArrayList(formals.size());
        List args = new ArrayList(formals.size());
        Iterator i = formals.iterator();

        while (i.hasNext()) {
            Formal f = (Formal) i.next();
            Type type = f.getType().getSootType();

            arg_types.add(type);
            args.add(getDisjunctVar(body, units, disjunct, f.getName(), type));
        }

        Type ret_type = tm.getBodyMethod().getReturnType();
        Value call = bodyMethod(base, disjunct, arg_types, args, ret_type);

        if (ret_type == VoidType.v()) {
            units.addLast(Jimple.v().newInvokeStmt(call));
            return null;
        } else {
            Local result = addLocal(body, "result", ret_type);
            units.addLast(Jimple.v().newAssignStmt(result, call));
            return result;
        }
    }

    /**
     * Generates all the label fields on the tracematch container
     * class and modifies the <init> method to initialise them.
     */
    public void makeAndInitLabelFields()
    {
        SootClass container = tm.getContainerClass();
        TMStateMachine sm = (TMStateMachine) tm.getState_machine();
        Iterator states = sm.getStateIterator();

        SootMethod init =
            container.getMethodByName(SootMethod.constructorName);
        Body body = init.getActiveBody();

        // local for `this'
        Local this_local = body.getThisLocal();

        Chain units = newChain();

        Local trueC = getConstant(body, units, "trueC");
        Local falseC = getConstant(body, units, "falseC");

        makeUpdatedField();
        setUpdated(units, this_local, IntConstant.v(0));

        while (states.hasNext()) {
            SMNode state = (SMNode) states.next();
            int s_num = state.getNumber();

            if (state.isInitialNode()) {
                makeLabelField(s_num, LABEL);
                assignToLabel(units, this_local, s_num, LABEL, trueC);
            } else {
                makeLabelField(s_num, LABEL);
                makeLabelField(s_num, TMP_LABEL);
                makeLabelField(s_num, SKIP_LABEL);
                assignToLabel(units, this_local, s_num, LABEL, falseC);
                assignToLabel(units, this_local, s_num, TMP_LABEL, falseC);
                assignToLabel(units, this_local, s_num, SKIP_LABEL, falseC);
            }
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

        setUpdated(units, this_local, IntConstant.v(1));

        Value state = getInt(to);
        Local lab_from = getLabel(body, units, this_local, from, LABEL);
        Local bind_result =
            callBindingsMethod(body, units, symbol, lab_from, method, state);

        Local lab_to = getLabel(body, units, this_local, to, TMP_LABEL);
        Local or_result =
            callOrMethod(body, units, lab_to, bind_result);
        assignToLabel(units, this_local, to, TMP_LABEL, or_result);

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

        Local lab = getLabel(body, units, this_local, to, SKIP_LABEL);
        Local result =
            callBindingsMethod(body, units, symbol, lab, method, null);

        assignToLabel(units, this_local, to, SKIP_LABEL, result);

        insertBeforeReturn(units, body.getUnits());
    }

    /**
     * Generate code that tests the "updated" flag on the
     * tracematch container class, and jumps to the return
     * statement if it is false.
     */
    protected void genTestAndResetUpdated(SootMethod method)
    {
        Body body = method.getActiveBody();
        Local this_local = body.getThisLocal();
        Chain units = newChain();

        Stmt end = getReturn(body.getUnits());

        Local updated = getUpdated(body, units, this_local);
        insertIf(units, updated, end);
        setUpdated(units, this_local, IntConstant.v(0));

        insertBeforeReturn(units, body.getUnits());
    }

    /**
     * Generate the code for master-updating labels - i.e. the
     * assignments from labelX_tmp to labelX etc.  that get
     * performed in the some() advice. (non-skip case)
     */
    protected void genLabelMasterUpdate(boolean skip_loop, int state,
                                        SootMethod method)
    {
        Body body = method.getActiveBody();

        Local this_local = body.getThisLocal();

        Chain units = newChain();

        // Assign TMP_LABEL to LABEL
        // (combining with SKIP_LABEL if appropriate)
        Local new_label;
        new_label = getLabel(body, units, this_local, state, TMP_LABEL);

        if (skip_loop) {
            Local skip_label =
                getLabel(body, units, this_local, state, SKIP_LABEL);
            new_label = callOrMethod(body, units, new_label, skip_label);
        }

        // update LABEL
        assignToLabel(units, this_local, state, LABEL, new_label);

        // reset TMP_LABEL
        Local falseC = getConstant(body, units, "falseC");
        assignToLabel(units, this_local, state, TMP_LABEL, falseC);

        // reset SKIP_LABEL
        assignToLabel(units, this_local, state, SKIP_LABEL, new_label);

        insertBeforeReturn(units, body.getUnits());
    }

    /**
     * Generate code to run the tracematch body for each solution
     */
    protected void genRunSolutions(int state, SootMethod method, List formals)
    {
        boolean is_around = method.getReturnType() != VoidType.v();

        Body body = method.getActiveBody();

        Local this_local = body.getThisLocal();

        // remove return statement, add new statements,
        // then put the return statement back.
        Chain units = newChain();

        // Get the disjunct iterator
        Local lab_final = getLabel(body, units, this_local, state, LABEL);
        Local disjuncts = callDisjunctsMethod(body, units, lab_final);

        Stmt loop = newPlaceHolder();
        Stmt end  = getReturn(body.getUnits());

        // Marker for beginning of loop
        insertPlaceHolder(units, loop);

        // loop test
        Local has_next = callIteratorMethod(body, units, disjuncts, true);
        insertIf(units, has_next, end);

        Local next = callIteratorMethod(body, units, disjuncts, false);
        Local disjunct = castDisjunct(body, units, next);

        // returns null if the body method is not an around method
        Local result =
            callBodyMethod(body, units, this_local, disjunct, formals);

        if (is_around)
            insertReturn(units, result);           
        else
            insertGoto(units, loop);

        insertBeforeReturn(units, body.getUnits());
    }
}
