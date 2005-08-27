package abc.tm.weaving.weaver;

import java.util.*;

import soot.*;
import soot.util.*;
import soot.coffi.parameter_annotation;
import soot.jimple.*;

import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.UnUsedParams;
import abc.tm.weaving.aspectinfo.*;
import abc.tm.weaving.matching.*;
import abc.weaving.aspectinfo.*;

/**
 * Helps with the generation of the Constraint and Disjunct classes in Jimple.
 * @author Pavel Avgustinov
 */

public class ClassGenHelper {
	///////// flags
    // Set this to true to enable debug traces -- print "D" for every time a disjunct
    // is constructed, "d" for every time it's finalized and "*" every time one is
    // discarded for being invalid; also, "C" when a constraint is created and "c"
	// when one is destroyed (those are the default traces).
    // If you pipe the output into a file (by appending '> output_file' to the command
    // line), you can then count the frequency of the respective events with the
    // following command: cat output_file | tr "D" "\n" | wc -l
    // (replacing "D" by "d" or "C" or "c" or "*" as appropriate).
    private boolean enableDebugTraces = false;

	// Relevant members
	TraceMatch curTraceMatch;
	SootClass constraint, disjunct;
	static SootClass myWeakRef;
	
	private SootClass curClass;
	private SootMethod curMethod;
	private Body curBody;
	private Chain curUnits;
	private LocalGeneratorEx curLGen;
	
	// often needed class and type constants
	static SootClass objectClass;
	static SootClass setClass;
	static SootClass iteratorClass;
	static Type objectType;
	static Type setType;
	static Type iteratorType;
	
	// other often-needed constants
	List emptyList = new LinkedList();
	List singleObjectType = new LinkedList();
	
	/**
	 * The modifiers with which the new classes are created. Modifier.PUBLIC by default. 
	 */
	int classModifiers = Modifier.PUBLIC;
	
	/**
	 * A ClassGenHelper is specific to a tracematch -- a lot of the optimisations rely on knowing information
	 * about the states of the NFA, quite apart from the fact that what methods are generated depends on
	 * the tracematch formals and symbols.
	 * @param tm the relevant tracematch
	 */
	public ClassGenHelper(TraceMatch tm) {
		curTraceMatch = tm;
		
		myWeakRef = Scene.v().getSootClass("org.aspectbench.tm.runtime.internal.MyWeakRef");
		
		// often needed class and type constants
		objectClass = Scene.v().getSootClass("java.lang.Object");
		setClass = Scene.v().getSootClass("java.util.LinkedHashSet");
		iteratorClass = Scene.v().getSootClass("java.util.Iterator");
		objectType = RefType.v("java.lang.Object");
		setType = RefType.v("java.util.LinkedHashSet");
		iteratorType = RefType.v("java.util.Iterator");

		singleObjectType.add(objectType);
	}

	/**
	 * Wrapper for the actual class generation. Creates a Constraint class and a Disjunct class, and sets
	 * the relevant fields of the associated tracematch to the new classes.
	 */
	public void generateClasses() {
    	// the SootClasses for the constraint and the main disjunct class for the tracematch
        constraint = new SootClass(curTraceMatch.getPackage() + "Constraint$" + curTraceMatch.getName(), classModifiers);
        curTraceMatch.setConstraintClass(constraint);

        disjunct = new SootClass(curTraceMatch.getPackage() + "Disjunct$" + curTraceMatch.getName(), classModifiers);
        curTraceMatch.setDisjunctClass(disjunct);

        fillInConstraintClass();
        fillInDisjunctClass();

        Scene.v().addClass(constraint);
        constraint.setApplicationClass();
        constraint.setSuperclass(objectClass);/*
        constraint.setSuperclass(Scene.v().getSootClass("java.lang.Object"));/**/

        Scene.v().addClass(disjunct);
        disjunct.setApplicationClass();
        disjunct.setSuperclass(objectClass);/*
        disjunct.setSuperclass(Scene.v().getSootClass("java.lang.Object"));/**/
	}
	
	//////////////// General Jimple manipulation functions
	
	/**
	 * Marks a SootClass as the current target for startMethod() calls.
	 * @param cl The class for which members will be generated next.
	 */
	protected void startClass(SootClass cl) {
		curClass = cl;
	}
	
	/**
	 * Start the generation of a SootMethod. This method should be called once to set up the internal state
	 * so that further calls to code generation methods generate code in the right place. It creates a method
	 * of the given name with the given parameter types, return type and modifiers and adds it to the 
	 * currently active class (as set with startClass).
	 * @param name The name of the methodd
	 * @param params List containing the Types of parameters
	 * @param returnType Type which the method will return
	 * @param modifiers Modifiers for the method
	 */
	protected void startMethod(String name, List params, Type returnType, int modifiers) {
		curMethod = new SootMethod(name, params, returnType, modifiers);
		curBody = Jimple.v().newBody(curMethod);
		curMethod.setActiveBody(curBody);
		curClass.addMethod(curMethod);
		curLGen = new LocalGeneratorEx(curBody);
		curUnits = curBody.getUnits();
	}
	
	/**
	 * Returns a Jimple null constant
	 */
	protected Value getNull() {
		return NullConstant.v();
	}
	
	/**
	 * Returns a Jimple int constant for the given int value. Also used for Bools -- getInt(1) is true,
	 * getInt(0) is false.
	 */
	protected Value getInt(int n) {
		return IntConstant.v(n);
	}
	
	/**
	 * Returns a Jimple String constant with the given value.
	 */
	protected Value getString(String s) {
		return StringConstant.v(s);
	}
	
	/**
	 * This should be called only once per startMethod() call. It returns a local that holds a reference to
	 * 'this', via an Identity statement.
	 */
	protected Local getThisLocal() {
		Local thisLocal = curLGen.generateLocal(curClass.getType(), "thisLocal");
		curUnits.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(curClass.getType())));
		return thisLocal;
	}
	
	/**
	 * This should be called only once per startMethod() call and parameter. It returns a local of the given
	 * type, holding the parameter with the given index.
	 * @param index Index of the required parameter
	 * @param type the type of the parameter and the resulting local -- should match the method declaration
	 * @return
	 */
	protected Local getParamLocal(int index, Type type) {
		Local paramLocal = curLGen.generateLocal(type, "paramLocal" + index);
		curUnits.addLast(Jimple.v().newIdentityStmt(paramLocal, Jimple.v().newParameterRef(type, index)));
		return paramLocal;
	}
	
	/**
	 * Returns a new Nop to be used as a jump label
	 */
	protected Stmt getNewLabel() {
		return Jimple.v().newNopStmt();
	}
    
    /**
     * Returns a new local for the current method body.
     * @param type type of the local
     * @param value initial value to assign to it
     * @param nameHint a hint for the naming of the local -- the name will be nameHint + "$" + <uniqueNumber>.
     */
    protected Local getNewLocal(Type type, Value value, String nameHint) {
        Local result = curLGen.generateLocal(type, nameHint);
        curUnits.addLast(Jimple.v().newAssignStmt(result, value));
        return result;
    }
	
	/**
	 * Returns a local of a given type that contains the value of the variable cast to that type.
	 * @param var the value to cast
	 * @param type the type to cast to
	 */
	protected Local getCastValue(Local var, Type type) {
		Local result = curLGen.generateLocal(type, "castResult");
		curUnits.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newCastExpr(var, type)));
		return result;
	}
	
	/**
	 * Returns a local of type returnType containing the result of the method call. The method is called
	 * with target as the receiver, and the Type of target (assumed to be a RefType) is used to retrieve
	 * the SootClass in which the method is defined. The method is assumed to have no parameters.
	 * @param target The receiver of the call
	 * @param name The name of the method
	 * @param returnType Return type of the method -- also type of the local to be returned
	 * @return A local of type returnType containing the return value of the method call.
	 */
	protected Local getMethodCallResult(Local target, String name, Type returnType) {
		Local result = curLGen.generateLocal(returnType, name + "$result");
		SootClass cl = ((RefType)target.getType()).getSootClass();
		if(cl.isInterface()) {
			curUnits.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newInterfaceInvokeExpr(target,
					Scene.v().makeMethodRef(cl, name, emptyList, returnType, false))));
		} else {
			curUnits.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newVirtualInvokeExpr(target,
					Scene.v().makeMethodRef(cl, name, emptyList, returnType, false))));
		}
		return result; 
	}
	
	/**
	 * Returns a local of type returnType containing the result of the method call. The method is called
	 * with target as the receiver, and the Type of target (assumed to be a RefType) is used to retrieve
	 * the SootClass in which the method is defined.
	 * @param target The receiver of the call
	 * @param name The name of the method
	 * @param formals List of types of the formal parameters -- should contain one member
	 * @param returnType Return type of the method -- also type of the local to be returned
	 * @param arg The local to be passed as a parameter
	 * @return A local of type returnType containing the return value of the method call.
	 */
	protected Local getMethodCallResult(Local target, String name, List formals, Type returnType, Local arg) {
		Local result = curLGen.generateLocal(returnType, name + "$result");
		SootClass cl = ((RefType)target.getType()).getSootClass();
		if(cl.isInterface()) {
			curUnits.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newInterfaceInvokeExpr(target,
					Scene.v().makeMethodRef(cl, name, formals, returnType, false), arg)));
		} else {
			curUnits.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newVirtualInvokeExpr(target,
					Scene.v().makeMethodRef(cl, name, formals, returnType, false), arg)));
		}
		return result;
	}
	
	/**
	 * Returns a local of type returnType containing the result of the method call. The method is called
	 * with target as the receiver, and the Type of target (assumed to be a RefType) is used to retrieve
	 * the SootClass in which the method is defined.
	 * @param target The receiver of the call
	 * @param name The name of the method
	 * @param formals List of types of the formal parameters
	 * @param returnType Return type of the method -- also type of the local to be returned
	 * @param actuals List containing locals with the parameter values that should be passed to the method.
	 * @return A local of type returnType containing the return value of the method call.
	 */
	protected Local getMethodCallResult(Local target, String name, List formals, Type returnType, List actuals) {
		Local result = curLGen.generateLocal(returnType, name + "$result");
		SootClass cl = ((RefType)target.getType()).getSootClass();
		if(cl.isInterface()) {
			curUnits.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newInterfaceInvokeExpr(target,
					Scene.v().makeMethodRef(cl, name, formals, returnType, false), actuals)));
		} else {
			curUnits.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newVirtualInvokeExpr(target,
					Scene.v().makeMethodRef(cl, name, formals, returnType, false), actuals)));
		}
		return result;
	}
	
	/**
	 * Returns a local containing a reference to a newly constructed object of the given class.
	 */
	protected Local getNewObject(SootClass cl) {
		Local result = curLGen.generateLocal(cl.getType(), "newObject");
		curUnits.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newNewExpr(cl.getType())));
		doConstructorCall(result, cl);
		return result;
	}
	
	/**
	 * Returns a local containing a reference to a newly constructed object of the given class. The constructor
	 * with parameter list 'formals' is used (this should contain one type only), and 'arg' is passed as an
	 * actual parameter
	 * @param cl type to construct
	 * @param formals list of one type -- the type of the single parameter in the constructor signature
	 * @param arg actual value to pass to the constructor
	 */
	protected Local getNewObject(SootClass cl, List formals, Value arg) {
		Local result = curLGen.generateLocal(cl.getType(), "newObject");
		curUnits.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newNewExpr(cl.getType())));
		doConstructorCall(result, cl, formals, arg);
		return result;
	}
	
	/**
	 * Returns a local containing a reference to a newly constructed object of the given class. The constructor
	 * with parameter list 'formals' is used, and 'actuals' is passed as parameter values.
	 * @param cl type to construct
	 * @param formals list of parameter types
	 * @param actuals list of actual parameter values
	 */
	protected Local getNewObject(SootClass cl, List formals, List actuals) {
		Local result = curLGen.generateLocal(cl.getType(), "newObject");
		curUnits.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newNewExpr(cl.getType())));
		doConstructorCall(result, cl, formals, actuals);
		return result;
	}
	
	/**
	 * Returns a local containing a reference to an instance field with target 'target'. The class in which the
	 * field is declared is determined from the type of the 'target' local.
	 * @param target the receiver of the field access
	 * @param name the name of the field
	 * @param type the type of the field
	 * @return
	 */
	protected Local getFieldLocal(Local target, String name, Type type) {
		Local result = curLGen.generateLocal(type, name + "$local");
		SootFieldRef fieldRef = Scene.v().makeFieldRef(((RefType)target.getType()).getSootClass(), name, type, false);
		curUnits.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newInstanceFieldRef(target, fieldRef)));
		return result;
	}
	
	/**
	 * Returns a local containing a reference to a static field of the class cl.
	 * @param cl the class in which the field is declared
	 * @param name the name of the field
	 * @param type the type of the field
	 */
	protected Local getStaticFieldLocal(SootClass cl, String name, Type type) {
		Local result = curLGen.generateLocal(type, name + "$local");
		SootFieldRef fieldRef = Scene.v().makeFieldRef(cl, name, type, true);
		curUnits.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newStaticFieldRef(fieldRef)));
		return result;
	}
    
    /**
     * Arithmetic helper function -- adds 'value' to the (primitive numeric type'd) 'local' and stores the
     * result in 'local'
     * @param local variable to add to
     * @param value value to add
     */
    protected void doAddToLocal(Local local, Value value) {
        curUnits.addLast(Jimple.v().newAssignStmt(local, Jimple.v().newAddExpr(local, value)));
    }
	
	/**
	 * Call a method with given name and return type on target. The method is assumed to have no arguments.
	 * Any return value is discarded.
	 * @param target The receiver of the call
	 * @param name The name of the method
	 * @param returnType The return type of the method
	 */
	protected void doMethodCall(Local target, String name, Type returnType) {
		SootClass cl = ((RefType)target.getType()).getSootClass();
		if(cl.isInterface()) {
			curUnits.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(target,
					Scene.v().makeMethodRef(cl, name, emptyList, returnType, false))));
		} else {
			curUnits.addLast(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(target,
					Scene.v().makeMethodRef(cl, name, emptyList, returnType, false))));
		}
	}
	
	/**
	 * Call a method with given name and return type on target. The method is assumed to have exactly one
	 * argument, arg. Any return value is discarded.
	 * @param target The receiver of the call
	 * @param name The name of the method
	 * @param formals List of types for formal parameters -- assumed to contain one element
	 * @param returnType The return type of the method
	 * @param arg The single parameter that is passed to the method
	 */
	protected void doMethodCall(Local target, String name, List formals, Type returnType, Value arg) {
		SootClass cl = ((RefType)target.getType()).getSootClass();
		if(cl.isInterface()) {
			curUnits.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(target,
					Scene.v().makeMethodRef(cl, name, formals, returnType, false), arg)));
		} else {
			curUnits.addLast(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(target,
					Scene.v().makeMethodRef(cl, name, formals, returnType, false), arg)));
		}
	}
	
	/**
	 * Call a method with given name and return type on target. The method is assumed to have parameters of
	 * types contained in formals, actuals is the list of actual parameters. Any return value is discarded.
	 * @param target The receiver of the call
	 * @param name The name of the method
	 * @param formals List of types for formal parameters
	 * @param returnType The return type of the method
	 * @param actuals The list of values to pass to the method
	 */
	protected void doMethodCall(Local target, String name, List formals, Type returnType, List actuals) {
		SootClass cl = ((RefType)target.getType()).getSootClass();
		if(cl.isInterface()) {
			curUnits.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(target,
					Scene.v().makeMethodRef(cl, name, formals, returnType, false), actuals)));
		} else {
			curUnits.addLast(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(target,
					Scene.v().makeMethodRef(cl, name, formals, returnType, false), actuals)));
		}
	}
	
	/**
	 * Does a specialinvoke of the constructor with no parameters of class 'cl', using 'target' as the receiver.
	 * Useful for calling superconstructors in class initialisers.
	 */
	protected void doConstructorCall(Local target, SootClass cl) {
		curUnits.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(target,
				Scene.v().makeConstructorRef(cl, emptyList))));
	}
	
	/**
	 * Does a specialinvoke of the constructor with parameters 'formals' of class 'cl', using 'target' as the receiver.
	 * Useful for calling superconstructors in class initialisers. 'formals' should be a list containing a single type,
	 * 'arg' should be a value of that type -- it is passed as an actual parameter.
	 */
	protected void doConstructorCall(Local target, SootClass cl, List formals, Value arg) {
		curUnits.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(target,
				Scene.v().makeConstructorRef(cl, formals), arg)));
	}
	
	/**
	 * Does a specialinvoke of the constructor with parameters 'formals' of class 'cl', using 'target' as the receiver.
	 * Useful for calling superconstructors in class initialisers. 'actuals' is a list of values passed as actual
	 * parameters to the constructor.
	 */
	protected void doConstructorCall(Local target, SootClass cl, List formals, List actuals) {
		curUnits.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(target,
				Scene.v().makeConstructorRef(cl, formals), actuals)));
	}
	
	/**
	 * Inserts a jump to the given label at the end of the current method body.
	 */
	protected void doJump(Stmt label) {
		curUnits.addLast(Jimple.v().newGotoStmt(label));
	}
	
	/**
	 * Inserts a conditional jump to the given label at the end of the current method body -- the jump is performed
	 * if val1 and val2 are equal (w.r.t. ==).
	 */
	protected void doJumpIfEqual(Value val1, Value val2, Stmt label) {
		curUnits.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(val1, val2), label));
	}
    
	/**
	 * Inserts a conditional jump to the given label at the end of the current method body -- the jump is performed
	 * if val1 and val2 are NOT equal (w.r.t. ==).
	 */
    protected void doJumpIfNotEqual(Value val1, Value val2, Stmt label) {
        curUnits.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(val1, val2), label));
    }
	
	/**
	 * Inserts a conditional jump to the given label at the end of the current method body -- the jump is performed
	 * if val is null.
	 */
    protected void doJumpIfNull(Value val, Stmt label) {
        doJumpIfEqual(val, NullConstant.v(), label);
    }
    
	/**
	 * Inserts a conditional jump to the given label at the end of the current method body -- the jump is performed
	 * if val is NOT null.
	 */
    protected void doJumpIfNotNull(Value val, Stmt label) {
        doJumpIfNotEqual(val, NullConstant.v(), label);
    }
    
	/**
	 * Inserts a conditional jump to the given label at the end of the current method body -- the jump is performed
	 * if var is an instance of type.
	 */
    protected void doJumpIfInstanceOf(Local var, Type type, Stmt label) {
        Local booleanLocal = curLGen.generateLocal(BooleanType.v(), "booleanLocal");
        curUnits.addLast(Jimple.v().newAssignStmt(booleanLocal, Jimple.v().newInstanceOfExpr(var, type)));
        curUnits.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(booleanLocal, IntConstant.v(1)), label));
    }
    
	/**
	 * Inserts a conditional jump to the given label at the end of the current method body -- the jump is performed
	 * if var is NOT an instance of type.
	 */
    protected void doJumpIfNotInstanceOf(Local var, Type type, Stmt label) {
        Local booleanLocal = curLGen.generateLocal(BooleanType.v(), "booleanLocal");
        curUnits.addLast(Jimple.v().newAssignStmt(booleanLocal, Jimple.v().newInstanceOfExpr(var, type)));
        curUnits.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(booleanLocal, IntConstant.v(0)), label));
    }
    
	/**
	 * Inserts a conditional jump to the given label at the end of the current method body -- the jump is performed
	 * if bool (which should be a boolean local) contains true.
	 */
	protected void doJumpIfTrue(Local bool, Stmt label) {
		curUnits.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(bool, getInt(1)), label));
	}
	
	/**
	 * Inserts a conditional jump to the given label at the end of the current method body -- the jump is performed
	 * if bool (which should be a boolean local) contains false.
	 */
	protected void doJumpIfFalse(Local bool, Stmt label) {
		curUnits.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(bool, getInt(0)), label));
	}
    
    /**
     * Inserts a lookup switch at the end of the current method body. The switch is on the value of 'key', with 
     * comparison values 'values' and corresponding jumps to 'labels'. If no value matches, jump to defaultLabel.
     */
    protected void doLookupSwitch(Local key, List values, List labels, Stmt defaultLabel) {
        curUnits.addLast(Jimple.v().newLookupSwitchStmt(key, values, labels, defaultLabel));
    }
	
	/**
	 * Set the value of an instance field.
	 * @param target receiver of the field access -- type determines in which class the field is searched
	 * @param name the name of the field
	 * @param type the type of the field
	 * @param value the value to assign to the field
	 */
	protected void doSetField(Local target, String name, Type type, Value value) {
		SootFieldRef fieldRef = Scene.v().makeFieldRef(((RefType)target.getType()).getSootClass(), name, type, false);
		curUnits.addLast(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(target, fieldRef), value));
	}
	
	/**
	 * Set the value of a static field.
	 * @param cl the class which declares the field
	 * @param name the name of the field
	 * @param type the type of the field
	 * @param value the value to assign to the field
	 */
	protected void doSetStaticField(SootClass cl, String name, Type type, Value value) {
		SootFieldRef fieldRef = Scene.v().makeFieldRef(cl, name, type, true);
		curUnits.addLast(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(fieldRef), value));
	}
	
	/**
	 * Inserts the given label at the end of the current method body.
	 */
	protected void doAddLabel(Stmt label) {
		curUnits.addLast(label);
	}
	
	/**
	 * Inserts a return statement at the end of the current method body -- returns 'val'.
	 */
	protected void doReturn(Value val) {
		curUnits.addLast(Jimple.v().newReturnStmt(val));
	}
	
	/**
	 * Inserts a 'void return' statement at the end of the current method body (for methods of void return type).
	 */
	protected void doReturnVoid() {
		curUnits.addLast(Jimple.v().newReturnVoidStmt());
	}
    
    /**
     * Inserts code to throw a run-time exception with message s at the end of the current method body.
     */
    protected void doThrowException(String s) {
        // exception = new RuntimeException("Attempt to get an unbound variable");
        List singleString = new LinkedList();
        singleString.add(RefType.v("java.lang.String"));
        curUnits.addLast(Jimple.v().newThrowStmt(getNewObject(
                Scene.v().getSootClass("java.lang.RuntimeException"), singleString, StringConstant.v(s))));
    }
	
	/**
	 * Inserts code to print s to stdout at the end of the current method body.
	 */
	protected void doPrintString(String s) {
		if(enableDebugTraces) {
			List singleString = new LinkedList();
			singleString.add(RefType.v("java.lang.String"));
			Local out = getStaticFieldLocal(Scene.v().getSootClass("java.lang.System"), "out", 
					RefType.v("java.io.PrintStream"));
			doMethodCall(out, "print", singleString, VoidType.v(), getString(s));
		}
	}
	
	/**
	 * Hook to perform 'raw' Jimple code -- i.e. a fragment of code that doesn't justify a higher-level
	 * method. Just appends it to the current method's body.
	 * @param stmt The Jimple statement to be appended.
	 */
	protected void doRawJimple(Stmt stmt) {
		curUnits.addLast(stmt);
	}
	
	//////////////// Generation of the Constraint class
	/**
	 * General ideas for the constraint class are as follows:
	 * - A constraint gives a set of (positive or negative) bindings which need to hold for the constraint
	 *   to be true. The constraint is stored in disjunctive normal form, i.e. as a disjunction of "disjuncts".
	 *   Thus, a constraint holds a set of disjuncts and some methods to manipulate them.
	 * - The constraint has two static fields -- trueC and falseC -- which represent the 'true' and 'false'
	 *   constraints.
	 *   
	 * This method fills in the members and methods of the constraint class.
	 */
	protected void fillInConstraintClass() {
		startClass(constraint);
		addConstraintClassMembers();
        addConstraintInitialiser();
        addConstraintStaticInitialiser();
        addConstraintFinalizeMethod();
		addConstraintOrMethod();
		addConstraintCopyMethod();
        addConstraintGetDisjunctArrayMethod();
        addConstraintAddBindingsMethods();
        addConstraintAddNegativeBindingsMethods();
	}
	
	/**
	 * Add fields to the Constraint class. As it stands, there are only three fields: trueC and 
	 * falseC, the public static singleton true and false constraints, and disjuncts, a 
	 * LinkedHashSet storing the disjuncts in a constraint.
	 * 
	 * I have a feeling that using static singletons for true and false constraints isn't optimal,
	 * one could, say, use flags.
	 */
	protected void addConstraintClassMembers() {
        SootField trueConstraint = new SootField("trueC", constraint.getType(), Modifier.PUBLIC | 
                Modifier.FINAL | Modifier.STATIC);
		constraint.addField(trueConstraint);
		SootField falseConstraint = new SootField("falseC", constraint.getType(), Modifier.PUBLIC |
		                Modifier.FINAL | Modifier.STATIC);
		constraint.addField(falseConstraint);
		SootField disjuncts = new SootField("disjuncts", setType,
		                Modifier.PUBLIC);
		constraint.addField(disjuncts);
	}
	
	/**
	 * We provide just a basic constructor that allocates the disjuncts set at the moment.
	 * 
	 * Also, there is a constructor that takes a set and uses that set as the disjunct set.
	 * 
	 * If debugging is enabled, constructors also print a single 'C' character to stdout.
	 */
	protected void addConstraintInitialiser() {
		
		////////// no-argument constructor
		startMethod(SootMethod.constructorName, emptyList, VoidType.v(), Modifier.PUBLIC);
		
		Local thisLocal = getThisLocal();
		
		// call super() -- TODO - do we need to do this?
		doConstructorCall(thisLocal, objectClass);
		
		// Construct the new set and store it in this.disjuncts
		doSetField(thisLocal, "disjuncts", setType, getNewObject(setClass));
		
		// For debugging -- print a "C" for constraint construction
		doPrintString("C");
		
		doReturnVoid();
		
		
		////////// single-argument constructor (taking a set)
		List singleSet = new LinkedList();
		singleSet.add(setType);
		startMethod(SootMethod.constructorName, singleSet, VoidType.v(), Modifier.PUBLIC);
		
		thisLocal = getThisLocal();
		doConstructorCall(thisLocal, objectClass);
		doSetField(thisLocal, "disjuncts", setType, getParamLocal(0, setType));
		doPrintString("C");
		doReturnVoid();
	}
	
	/**
	 * The static initialiser of the constraint class simply sets the static fields (trueC, falseC), and
	 * ensures that trueC contains an empty disjunct -- this is necessary to make the methods work correctly. 
	 */
	protected void addConstraintStaticInitialiser() {
		startMethod(SootMethod.staticInitializerName, emptyList, VoidType.v(), Modifier.PUBLIC);
		
		List singleSet = new LinkedList();
		singleSet.add(setType);

		// Need to initialise static members -- trueC and falseC.
		// trueC should contain a single empty disjunct
		Local tempSet = getNewObject(setClass);
		doMethodCall(tempSet, "add", singleObjectType, BooleanType.v(), getNewObject(disjunct));
		doSetStaticField(constraint, "trueC", constraint.getType(), getNewObject(constraint, singleSet, tempSet));
		doSetStaticField(constraint, "falseC", constraint.getType(), getNewObject(constraint));
		doReturnVoid();
	}
	
	/**
	 * If debugging is enabled, finalizing a constraint prints a single 'c' character on stdout.
	 */
	protected void addConstraintFinalizeMethod() {
		startMethod("finalize", emptyList, VoidType.v(), Modifier.PROTECTED);
		// For debugging -- print a "c" for constraint destruction
		doPrintString("c");
		doReturnVoid();
	}
	
	/**
	 * Add the method Constraint or(Constraint arg);
	 * 
	 * The intended behaviour is this:
	 * - if either this or arg is trueC, the result is true.
	 * - if this is falseC, the result is a copy of arg.
	 * - if arg is falseC, the result is this.
	 * - Otherwise, the result is this, after the disjuncts from arg have been added to the disjunct set.
	 * 
	 * Note that this update is DESTRUCTIVE -- we deviate from the 'functional' modification of constraints
	 * for the or() method. The reason is rather subtle and follows from the fact that or() is only ever 
	 * called on temporary constraint labels, so that it doesn't matter if we modify them destructively --
	 * only after the temporary labels become permanent do we need to worry about doing things functionally.
	 */
	protected void addConstraintOrMethod() {
		List singleConstraint = new LinkedList();
		singleConstraint.add(constraint.getType());
		startMethod("or", singleConstraint, constraint.getType(), Modifier.PUBLIC);
		
		Local thisLocal = getThisLocal();
		Local paramLocal = getParamLocal(0, constraint.getType());
		Local trueC = getStaticFieldLocal(constraint, "trueC", constraint.getType());
		Local falseC = getStaticFieldLocal(constraint, "falseC", constraint.getType());
		
		Stmt labelReturnTrue = getNewLabel();
		Stmt labelReturnThis = getNewLabel();
		Stmt labelReturnParamCopy = getNewLabel();
		
		doJumpIfEqual(thisLocal, trueC, labelReturnTrue);
		doJumpIfEqual(paramLocal, trueC, labelReturnTrue);
		doJumpIfEqual(thisLocal, falseC, labelReturnParamCopy);
		doJumpIfEqual(paramLocal, falseC, labelReturnThis);
		
		// if we're here -- we need to add paramLocal's disjuncts to this.disjuncts
		List singleCollection = new LinkedList();
		singleCollection.add(RefType.v("java.util.Collection"));
		Local thisSet = getFieldLocal(thisLocal, "disjuncts", setType);
		Local paramSet = getFieldLocal(paramLocal, "disjuncts", setType);
		doMethodCall(thisSet, "addAll", singleCollection, BooleanType.v(), paramSet);
		doReturn(thisLocal);
		
		// the jump labels:
		doAddLabel(labelReturnTrue);
		doReturn(trueC);
		
		doAddLabel(labelReturnThis);
		doReturn(thisLocal);
		
		doAddLabel(labelReturnParamCopy);
		doReturn(getMethodCallResult(paramLocal, "copy", constraint.getType()));
	}
	
	/**
	 * Add a method with signature Constraint copy(Constraint arg);
	 * 
	 * That method returns a copy of arg. It reuses the same disjuncts, but constructs a new set to hold them, and
	 * a new Constraint object with that new set as its disjuncts set.
	 */
	protected void addConstraintCopyMethod() {
		startMethod("copy", emptyList, constraint.getType(), Modifier.PUBLIC);
		
		List singleCollection = new LinkedList();
		singleCollection.add(RefType.v("java.util.Collection"));
		List singleSet = new LinkedList();
		singleSet.add(setType);
		
		Local thisLocal = getThisLocal();
		
		// resultSet = new linkedHashSet(this.disjuncts);
		Local resultSet = getNewObject(setClass, singleCollection, getFieldLocal(thisLocal, "disjuncts", setType));
		
		// return new Constraint(resultSet);
		doReturn(getNewObject(constraint, singleSet, resultSet));
	}
	
    /**
     * Add a method with signature Object[] getDisjunctArray();
     * 
     *  Needed by the advice -- it is used to iterate over all solutions to a constraint. Simply uses Set.toArray().
     */
    protected void addConstraintGetDisjunctArrayMethod() {
        Type arrayType = ArrayType.v(objectType, 1);
        startMethod("getDisjunctArray", emptyList, arrayType, Modifier.PUBLIC);
        
        // return this.disjuncts.toArray();
        doReturn(getMethodCallResult(getFieldLocal(getThisLocal(), "disjuncts", setType), "toArray", arrayType));
    }

	/**
	 * Add methods with signature Constraint addBindingsForSymbolX(bindings..) for each symbol X of the tracematch.
	 * 
	 * The Constraint class will contain, for each tracematch symbol X, a method called
	 * addBindingsForSymbolX(). The parameters for this method are two ints (the numbers of
	 * the 'from' and 'to' states of the transition that is being taken at run-time), 
	 * followed by an Object parameter for every tracematch variable bound by the symbol.
	 * 
	 * The idea is that these methods record new bindings obtained by following a transition in the NFA triggered
	 * by an event in the program, and return falseC if the new bindings are incompatible with this constarint, 
	 * or a new constraint that incorporates the new bindings if they *are* compatible.
	 */
	protected void addConstraintAddBindingsMethods() {
		List singleInt = new LinkedList();
		singleInt.add(IntType.v());
		List singleSet = new LinkedList();
		singleSet.add(setType);
		
		Iterator symbolIt = curTraceMatch.getSymbols().iterator();
		String symbol;
		while(symbolIt.hasNext()) {
			symbol = (String) symbolIt.next();
			List variables = curTraceMatch.getVariableOrder(symbol);
			List parameterTypes = new LinkedList();
			parameterTypes.add(IntType.v()); // number of originating state of the transition
			parameterTypes.add(IntType.v()); // number of the target state of the transition
			int varCount = variables.size();
			for(int i = 0; i < varCount; i++) {
				parameterTypes.add(objectType); // one Object parameter for each bound variable
			}
			startMethod("addBindingsForSymbol" + symbol, parameterTypes, constraint.getType(), Modifier.PUBLIC);
			if(varCount == 0) {
				// if we have no variables, we just return this
				doReturn(getThisLocal());
			} else {
				Local thisLocal = getThisLocal();
				
				Local falseC = getStaticFieldLocal(constraint, "falseC", constraint.getType());
				
				// We'll need these labels..
				Stmt labelReturnFalse = getNewLabel();
				Stmt labelLoopBegin = getNewLabel();
				Stmt labelLoopEnd = getNewLabel();
				Stmt labelDisjunctValid = getNewLabel();
				
				// if(this == falseC) return false;
				doJumpIfEqual(thisLocal, falseC, labelReturnFalse);

				// Store all parameters in local variables, put those variables in a list
				List parameterLocals = new LinkedList();
				
				int parameterIndex = 0;
				Local stateFrom = getParamLocal(parameterIndex++, IntType.v());
				parameterLocals.add(stateFrom);
				
				Local stateTo = getParamLocal(parameterIndex++, IntType.v());
				parameterLocals.add(stateTo);
				
				for(Iterator varIt = variables.iterator(); varIt.hasNext(); varIt.next()) {
					parameterLocals.add(getParamLocal(parameterIndex++, objectType));
				}
				
				Local thisDisjuncts = getFieldLocal(thisLocal, "disjuncts", setType);
				// new disjunct set for the result, as we don't change things in-place
				Local resultDisjuncts = getNewObject(setClass);
				
				Local disjunctIterator = getMethodCallResult(thisDisjuncts, "iterator", iteratorType);
				
				// we have to emulate loops with jumps. This is meant to be
				// while(disjunctIterator.hasNext());
				doAddLabel(labelLoopBegin);
				doJumpIfFalse(getMethodCallResult(disjunctIterator, "hasNext", BooleanType.v()), labelLoopEnd);
				
				Local curDisjunct = getCastValue(getMethodCallResult(disjunctIterator, "next", objectType), disjunct.getType());
				
		        ////////// Cleanup of invalid disjuncts -- if the current disjunct isn't valid,
		        // just remove it from the disjunct set and continue with the next.
		        // if(!curDisjunct.validateDisjunct(stateTo) { it.remove(); goto labelLoopBegin; }
				doJumpIfTrue(getMethodCallResult(curDisjunct, "validateDisjunct", singleInt, BooleanType.v(), stateTo), 
						labelDisjunctValid);
				doMethodCall(disjunctIterator, "remove", VoidType.v());
				doJump(labelLoopBegin);
				
				doAddLabel(labelDisjunctValid);
				////////// end cleanup code
				
				Local resultDisjunct = getMethodCallResult(curDisjunct, "addBindingsForSymbol" + symbol, 
						parameterTypes, disjunct.getType(), parameterLocals);
				doMethodCall(resultDisjuncts, "add", singleObjectType, BooleanType.v(), resultDisjunct);
				
				doJump(labelLoopBegin);
				// end of loop
				
				doAddLabel(labelLoopEnd);
				
				// We remove the false disjunct, then, if the disjunct set is empty, we return the
		        // false constraint falseC, otherwise we return a new constraint with the 
		        // appropriate disjunct set.
				Local falseD = getStaticFieldLocal(disjunct, "falseD", disjunct.getType());
				doMethodCall(resultDisjuncts, "remove", singleObjectType, BooleanType.v(), falseD);
				doJumpIfTrue(getMethodCallResult(resultDisjuncts, "isEmpty", BooleanType.v()), labelReturnFalse);
				
				// non-empty result set -- construct new constraint and return it
				doReturn(getNewObject(constraint, singleSet, resultDisjuncts));
				
				doAddLabel(labelReturnFalse);
				doReturn(falseC);
			}
		}
	}
	
	/**
	 * Add methods with signature Constraint addNegativeBindingsForSymbolX(bindings..) for each symbol X of the tracematch.
	 * 
	 * The Constraint class will contain, for each tracematch symbol X, a method called
	 * addNegativeBindingsForSymbolX(). The parameters for this method are an int (the number of
	 * the 'to' state of the transition that is being taken at run-time), 
	 * followed by an Object parameter for every tracematch variable bound by the symbol.
	 * 
	 * The idea is that these methods record new bindings obtained by following a transition in the NFA triggered
	 * by an event in the program, and return falseC if the new bindings are incompatible with this constarint, 
	 * or a new constraint that incorporates the new bindings if they *are* compatible.
	 */
	protected void addConstraintAddNegativeBindingsMethods() {
		List singleInt = new LinkedList();
		singleInt.add(IntType.v());
		List singleSet = new LinkedList();
		singleSet.add(setType);
		List singleCollection = new LinkedList();
		singleCollection.add(RefType.v("java.util.Collection"));
		
		Iterator symbolIt = curTraceMatch.getSymbols().iterator();
		String symbol;
		while(symbolIt.hasNext()) {
			symbol = (String) symbolIt.next();
			List variables = curTraceMatch.getVariableOrder(symbol);
			List parameterTypes = new LinkedList();
			parameterTypes.add(IntType.v()); // number of originating state of the transition
			int varCount = variables.size();
			for(int i = 0; i < varCount; i++) {
				parameterTypes.add(objectType); // one Object parameter for each bound variable
			}
			startMethod("addNegativeBindingsForSymbol" + symbol, parameterTypes, constraint.getType(), Modifier.PUBLIC);
			if(varCount == 0) {
				// if we have no variables, we just return false
				doReturn(getStaticFieldLocal(constraint, "falseC", constraint.getType()));
			} else {
				Local thisLocal = getThisLocal();

				Local falseC = getStaticFieldLocal(constraint, "falseC", constraint.getType());
				// We'll need these labels..
				Stmt labelReturnFalse = getNewLabel();
				Stmt labelLoopBegin = getNewLabel();
				Stmt labelLoopEnd = getNewLabel();
				Stmt labelDisjunctValid = getNewLabel();
				
				// if(this == falseC) return false;
				doJumpIfEqual(thisLocal, falseC, labelReturnFalse);

				// Store all parameters in local variables, put those variables in a list
				List parameterLocals = new LinkedList();
				
				int parameterIndex = 0;
				Local stateTo = getParamLocal(parameterIndex++, IntType.v());
				parameterLocals.add(stateTo);
				
				for(Iterator varIt = variables.iterator(); varIt.hasNext(); varIt.next()) {
					parameterLocals.add(getParamLocal(parameterIndex++, objectType));
				}
				
				Local thisDisjuncts = getFieldLocal(thisLocal, "disjuncts", setType);
				// new disjunct set for the result, as we don't change things in-place
				Local resultDisjuncts = getNewObject(setClass);
				
				Local disjunctIterator = getMethodCallResult(thisDisjuncts, "iterator", iteratorType);
				
				// we have to emulate loops with jumps. This is meant to be
				// while(disjunctIterator.hasNext());
				doAddLabel(labelLoopBegin);
				doJumpIfFalse(getMethodCallResult(disjunctIterator, "hasNext", BooleanType.v()), labelLoopEnd);
				
				Local curDisjunct = getCastValue(getMethodCallResult(disjunctIterator, "next", objectType), disjunct.getType());
				
		        ////////// Cleanup of invalid disjuncts -- if the current disjunct isn't valid,
		        // just remove it from the disjunct set and continue with the next.
		        // if(!curDisjunct.validateDisjunct(stateTo) { it.remove(); goto labelLoopBegin; }
				doJumpIfTrue(getMethodCallResult(curDisjunct, "validateDisjunct", singleInt, BooleanType.v(), stateTo), 
						labelDisjunctValid);
				doMethodCall(disjunctIterator, "remove", VoidType.v());
				doJump(labelLoopBegin);
				
				doAddLabel(labelDisjunctValid);
				////////// end cleanup code
				
				// addNegativeBindingsForSymbolX returns a Set if the symbol binds more than one variable,
				// and a single disjunct otherwise.
				if(varCount < 2) {
					// resultDisjunct = addNegativeBindingsForSymbolX(stateTo, [bindings]);
					Local resultDisjunct = getMethodCallResult(curDisjunct, "addNegativeBindingsForSymbol" + symbol, 
							parameterTypes, disjunct.getType(), parameterLocals);
					doMethodCall(resultDisjuncts, "add", singleObjectType, BooleanType.v(), resultDisjunct);
				} else {
					// resultDisjunctSet = addNegativeBindingsForSymbolX(stateTo, [bindings]);
					Local resultDisjunctSet = getMethodCallResult(curDisjunct, "addNegativeBindingsForSymbol" + symbol, 
							parameterTypes, setType, parameterLocals);
					doMethodCall(resultDisjuncts, "addAll", singleCollection, BooleanType.v(), resultDisjunctSet);
				}
				
				doJump(labelLoopBegin);
				// end of loop
				
				doAddLabel(labelLoopEnd);
				
				// We remove the false disjunct, then, if the disjunct set is empty, we return the
		        // false constraint falseC, otherwise we return a new constraint with the 
		        // appropriate disjunct set.
				Local falseD = getStaticFieldLocal(disjunct, "falseD", disjunct.getType());
				doMethodCall(resultDisjuncts, "remove", singleObjectType, BooleanType.v(), falseD);
				doJumpIfTrue(getMethodCallResult(resultDisjuncts, "isEmpty", BooleanType.v()), labelReturnFalse);
				
				// non-empty result set -- construct new constraint and return it
				doReturn(getNewObject(constraint, singleSet, resultDisjuncts));
				
				doAddLabel(labelReturnFalse);
				doReturn(falseC);
			}
		}
	}
	
	//////////////// Generation of the Disjunct class
	/**
	 * Fills in the fields and methods of the disjunct class. A disjunct represents a (potentially incomplete)
	 * solution of a constraint, i.e. a (potentially incomplete) set of bindings that would make the constraint
	 * true. For every tracematch formal variable X, the Disjunct class has a field var$X which records the 
	 * current binding of X (possibly weakly, using MyWeakRef and depending on the state that the disjunct is
	 * associated with), a boolean flag X$isWeak (indicating whether the binding is weak or not), and a set 
	 * not$X of negative bindings for X. There is also a method get$X() which returns the value bound by X, 
	 * whether X is weak or strong. var$X == null if X is not bound. not$X == null iff var$X != null, as we
	 * don't need to keep track of negative bindings after X is bound.
	 * 
	 * There are two static fields, trueD and falseD, representing the true and false disjunct respectively.
	 */
	protected void fillInDisjunctClass() {
		startClass(disjunct);
		addDisjunctClassMembers();
        addDisjunctInitialiser();
        addDisjunctStaticInitialiser();
        addDisjunctFinalizeMethod();
		addDisjunctAddNegativeBindingsForVariableMethods();
        addDisjunctGetVarMethods();
        addDisjunctAddBindingsForSymbolMethods();
        addDisjunctAddNegBindingsForSymbolMethods();
        addDisjunctEqualsMethod();
        addDisjunctHashCodeMethod();
        // addDisjunctCopyMethod(); -- replaced by copy constructor
        addDisjunctValidateDisjunctMethod();
	}
	
	protected void addDisjunctClassMembers() {
		// static singleton fields for true/false disjuncts
        SootField trueDisjunct= new SootField("trueD", disjunct.getType(), Modifier.PUBLIC | 
                Modifier.FINAL | Modifier.STATIC);
		disjunct.addField(trueDisjunct);
		SootField falseDisjunct = new SootField("falseD", disjunct.getType(), Modifier.PUBLIC |
		                Modifier.FINAL | Modifier.STATIC);
		disjunct.addField(falseDisjunct);
		
		// the remainin fields depend upon the tracematch and its variables. For each tracematch
		// formal X, X$isWeak (is the reference to X weak?), not$X (which values X mustn't take 
        // -- a set, and null if X is bound) and var$X (the value X is bound to, or null if X is
        // unbound).
        List varNames = curTraceMatch.getFormalNames();
        Iterator varIt = varNames.iterator();
        while(varIt.hasNext()) {
            String varName = (String)varIt.next();
            SootField curField = new SootField("var$" + varName, objectType,
                    Modifier.PUBLIC);
            disjunct.addField(curField);
            curField = new SootField("not$" + varName, setType,
                    Modifier.PUBLIC);
            disjunct.addField(curField);
            curField = new SootField(varName + "$isWeak", BooleanType.v(),
                    Modifier.PUBLIC);
            disjunct.addField(curField);
        }
	}
    
    /**
     * Provide two constructors for Disjuncts -- a 'default' constructor and a 'copy' constructor, the second
     * taking a Disjunct as a parameter.
     * 
     * Also, if debugging is enabled, print the character 'D' to stdout whenever a disjunct is constructed.
     */
    protected void addDisjunctInitialiser() {
        // no-argument constructor
        startMethod(SootMethod.constructorName, emptyList, VoidType.v(), Modifier.PUBLIC);

        Local thisLocal = getThisLocal();
        
        // call super() -- TODO - do we need to do this?
        doConstructorCall(thisLocal, objectClass);
        
        // Initialise each negative bindings set to a new set.
        // TODO -- possible optimisation -- spot which variables are bound after every initial transition
        // and don't allocate negative bindings sets for those. Probably small effect, though.
        List varNames = curTraceMatch.getFormalNames();
        Iterator varIt = varNames.iterator();
        while(varIt.hasNext()) {
            String varName = (String)varIt.next();
            doSetField(thisLocal, "not$" + varName, setType, getNewObject(setClass));
        }
        
        // For debugging -- print a 'D' whenever a disjunct is constructed
        doPrintString("D");
        
        doReturnVoid();
        
        
        // Single-disjunct-argument copy constructor
        List singleDisjunct = new LinkedList();
        singleDisjunct.add(disjunct.getType());
        List singleCollection = new LinkedList();
        singleCollection.add(RefType.v("java.util.Collection"));
        startMethod(SootMethod.constructorName, singleDisjunct, VoidType.v(), Modifier.PUBLIC);
        
        thisLocal = getThisLocal();
        Local paramLocal = getParamLocal(0, disjunct.getType());
        
        // call super() -- TODO - do we need to do this?
        doConstructorCall(thisLocal, objectClass);
        
        // Initialise each negative bindings set to a new set.
        // TODO -- possible optimisation -- spot which variables are bound after every initial transition
        // and don't allocate negative bindings sets for those. Probably small effect, though.
        varNames = curTraceMatch.getFormalNames();
        varIt = varNames.iterator();
        Local curBinding, curSet;
        while(varIt.hasNext()) {
            String varName = (String)varIt.next();
            Stmt labelSkipNegBindingsSet = getNewLabel();
            curBinding = getFieldLocal(paramLocal, "var$" + varName, objectType);
            doSetField(thisLocal, "var$" + varName, objectType, curBinding);
            doSetField(thisLocal, varName + "$isWeak", BooleanType.v(), 
                    getFieldLocal(paramLocal, varName + "$isWeak", BooleanType.v()));
            doJumpIfNotNull(curBinding, labelSkipNegBindingsSet);
            curSet = getNewObject(setClass);
            doMethodCall(curSet, "addAll", singleCollection, BooleanType.v(), 
                    getFieldLocal(paramLocal, "not$" + varName, setType));
            doSetField(thisLocal, "not$" + varName, setType, curSet);
            
            doAddLabel(labelSkipNegBindingsSet);
        }
        
        // For debugging -- print a 'D' whenever a disjunct is constructed
        doPrintString("D");
        
        doReturnVoid();
    }
    
    /**
     * Initialise the two static fields, trueD and falseD.
     */
    protected void addDisjunctStaticInitialiser() {
        startMethod(SootMethod.staticInitializerName, emptyList, VoidType.v(), Modifier.PUBLIC);
        
        // Need to initialise static members -- trueD and falseD.
        doSetStaticField(disjunct, "trueD", disjunct.getType(), getNewObject(disjunct));
        doSetStaticField(disjunct, "falseD", disjunct.getType(), getNewObject(disjunct));
        doReturnVoid();
    }
    
    /**
     * If debug tracing is enabled, print 'd' to stdout whenever a disjunct is finalized.
     */
    protected void addDisjunctFinalizeMethod() {
        startMethod("finalize", emptyList, VoidType.v(), Modifier.PROTECTED);
        // For debugging -- print a "d" for disjunct destruction
        doPrintString("d");
        doReturnVoid();
    }
    
    /**
     * Add a method of signature Disjunct addNegativeBindingsForVariableX(Object binding) for each formal
     * tracematch parameter X.
     * 
     * The method adds a negative binding for a single variable to the current disjunct. there are three cases:
     * 1. The variable is bound to the same value as the new binding -- return falseD.
     * 2. The variable is bound to a different value -- return this.copy() (no need to record new negative binding).
     * 3. The variable is not bound -- return this.copy().not$var.add(new MyWeakRef(binding)).
     */
    protected void addDisjunctAddNegativeBindingsForVariableMethods() {
        List singleDisjunct = new LinkedList();
        singleDisjunct.add(disjunct.getType());
        List varNames = curTraceMatch.getFormalNames();
        Iterator varIt = varNames.iterator();
        while(varIt.hasNext()) {
            String varName = (String)varIt.next();
            startMethod("addNegativeBindingForVariable" + varName, singleObjectType, disjunct.getType(), Modifier.PUBLIC);

            Stmt labelReturnFalse = getNewLabel();
            Stmt labelVarNotBound = getNewLabel();
            
            Local thisLocal = getThisLocal();
            Local paramLocal = getParamLocal(0, objectType);
            
            doJumpIfNull(getFieldLocal(thisLocal, "var$" + varName, objectType), labelVarNotBound);
            
            // variable bound -- return false if bound to same value
            doJumpIfEqual(getMethodCallResult(thisLocal, "get$" + varName, objectType), paramLocal, labelReturnFalse);
            
            // bound to a different value -- return new Disjunct(this); (i.e. this.copy())
            doReturn(getNewObject(disjunct, singleDisjunct, thisLocal));
            
            // variable isn't bound -- return this.copy().not$var.add(new WeakRef(paramLocal))
            doAddLabel(labelVarNotBound);
            Local result = getNewObject(disjunct, singleDisjunct, thisLocal);
            Local targetSet = getFieldLocal(result, "not$" + varName, setType);
            doMethodCall(targetSet, "add", singleObjectType, BooleanType.v(), paramLocal);
            doReturn(result);
            
            doAddLabel(labelReturnFalse);
            doReturn(getStaticFieldLocal(disjunct, "falseD", disjunct.getType()));
        }
    }
    
    /**
     * Add a method Object get$X() for each formal tracematch variable X.
     * 
     * Returns the value X is bound to, dereferencing a weak reference if necessary. Throws a runtime
     * exception if X is not bound.
     */
    protected void addDisjunctGetVarMethods() {
        List varNames = curTraceMatch.getFormalNames();
        Iterator varIt = varNames.iterator();
        while(varIt.hasNext()) {
            String varName = (String)varIt.next();
            startMethod("get$" + varName, emptyList, objectType, Modifier.PUBLIC);
            Stmt labelThrowException = getNewLabel();
            Stmt labelBindingIsWeak = getNewLabel();
            
            Local thisLocal = getThisLocal();
            Local var = getFieldLocal(thisLocal, "var$" + varName, objectType);
            doJumpIfNull(var, labelThrowException);
            doJumpIfInstanceOf(var, myWeakRef.getType(), labelBindingIsWeak);
            
            // binding is strong -- just return it
            doReturn(var);
            
            // binding is weak -- return ((MyWeakRef)var).get();
            doAddLabel(labelBindingIsWeak);
            doReturn(getMethodCallResult(getCastValue(var, myWeakRef.getType()), "get", objectType));
            
            // attempt to get an unbound variable -- throw an exception
            doAddLabel(labelThrowException);
            doThrowException("Attempt to get an unbound variable: " + varName);
        }
    }
    
    /**
     * Add methods addBindingsForSymbolX(int from, int to, Objects bindings..) for each tracematch symbol X.
     * 
     * This method should be called when a transition in the NFA is taken after being triggered by an event
     * in the observed program. It checks whether the new bindings are compatible with this disjunct, and 
     * returns falseD if they're not and a new disjunct which records any new information if they are.
     */
    protected void addDisjunctAddBindingsForSymbolMethods() {
        List singleDisjunct = new LinkedList();
        singleDisjunct.add(disjunct.getType());
        
        Iterator symbolIt = curTraceMatch.getSymbols().iterator();
        String symbol;
        while(symbolIt.hasNext()) {
            symbol = (String) symbolIt.next();
            List variables = curTraceMatch.getVariableOrder(symbol);
            List parameterTypes = new LinkedList();
            parameterTypes.add(IntType.v()); // number of originating state of the transition
            parameterTypes.add(IntType.v()); // number of the target state of the transition
            int varCount = variables.size();
            for(int i = 0; i < varCount; i++) {
                parameterTypes.add(objectType); // one Object parameter for each bound variable
            }
            
            startMethod("addBindingsForSymbol" + symbol, parameterTypes, disjunct.getType(), Modifier.PUBLIC);
            Stmt labelReturnFalse = getNewLabel();
            Stmt labelReturnThis = getNewLabel();
            
            // the first part of this method just checks if the new bindings are compatible.
            Local thisLocal = getThisLocal();
            int parameterIndex = 0;
            Local stateFrom = getParamLocal(parameterIndex++, IntType.v());
            Local stateTo = getParamLocal(parameterIndex++, IntType.v());
            
            // we store all bindings in a list
            List/*<Local>*/ bindings = new LinkedList();
            Iterator varIt = variables.iterator();
            while(varIt.hasNext()) {
                varIt.next();
                bindings.add(getParamLocal(parameterIndex++, objectType));
            }
            
            Iterator bindIt = bindings.iterator();
            varIt = variables.iterator();
            while(bindIt.hasNext()) {
                String varName = (String)varIt.next();
                Local curVar = (Local)bindIt.next();
                Stmt labelCurVarNotBound = getNewLabel();
                Stmt labelCheckNextVar = getNewLabel();
                Local curThisVar = getFieldLocal(thisLocal, "var$" + varName, objectType);
                doJumpIfNull(curThisVar, labelCurVarNotBound);
                
                Local curThisVarVal = getMethodCallResult(thisLocal, "get$" + varName, objectType);
                // return false if already incompatible
                doJumpIfNotEqual(curThisVarVal, curVar, labelReturnFalse);
                // since the current variable is bound, we skip the check of the negative binding sets
                doJump(labelCheckNextVar);
                
                doAddLabel(labelCurVarNotBound);
                // compare negative binding sets
                // The check we actually do is if(this.not$var.contains(new MyWeakRef(curVar))), since only
                // weak bindings are stored in the negative bindings sets. This relies on MyWeakRef.equals()
                // returning true if and only if there is reference equality between the two referents.
                doJumpIfTrue(getMethodCallResult(getFieldLocal(thisLocal, "not$" + varName, setType),
                        "contains", singleObjectType, BooleanType.v(), 
                                getNewObject(myWeakRef, singleObjectType, curVar)), labelReturnFalse);
                
                doAddLabel(labelCheckNextVar);
            }
            
            // OK, if we fall through here then this disjunct is compatible with the new bindings,
            // otherwise we would have jumped to labelReturnFalse. Now we have a lookup switch to
            // distinguish between different states and hence perform the required weak/strong
            // behaviour.
            
            // To construct the LookupSwitch, we need two lists of equal length -- a list of values
            // to compare to (those will be state numbers of states that have appropriate transitions)
            // and a list of labels to jump to.
            List switchValues = new LinkedList();
            List switchLabels = new LinkedList();
            List incomingNodes = new LinkedList();
            Iterator stateIt = ((TMStateMachine)curTraceMatch.getState_machine()).getStateIterator();
            while(stateIt.hasNext()) {
                SMNode state = (SMNode)stateIt.next();
                if(state.hasInEdgeWithLabel(symbol)) {
                    switchValues.add(getInt(state.getNumber()));
                    switchLabels.add(getNewLabel());
                    incomingNodes.add(state);
                }
            }
            
            Stmt labelThrowException = getNewLabel();
            
            doLookupSwitch(stateTo, switchValues, switchLabels, labelThrowException);
            
            Iterator labelIt = switchLabels.iterator();
            stateIt = incomingNodes.iterator();
            while(labelIt.hasNext()) {
                Stmt curLabel = (Stmt)labelIt.next();
                SMNode curState = (SMNode)stateIt.next();
                
                doAddLabel(curLabel);
                
                // Optimisation: Only construct new disjuncts when necessary
                // =============
                // Observe that once a disjunct is fully bound, the only way in which it can change
                // further (since we don't keep track of negative bindings) is if it has to
                // strengthen a reference that was hitherto weak. Thus, if the current state is
                // guaranteed to have bound all tracematch variables, we want to return 'this' rather
                // than a new disjunct object if we are coming from a state that also has all variables
                // bound, and which agrees with the current state regarding needStrongRefs.
                if(curState.boundVars.equals(new LinkedHashSet(curTraceMatch.getFormalNames()))) {
                    // the current state binds all variables.
                    // Great -- construct a list of all states S such that there is a transition labelled
                    // with 'symbol' from S to curState, where S binds all variables and has the same
                    // needStrongRefs set as curState.
                    List skipStates = new LinkedList();
                    List jumpLabels = new LinkedList();
                    Stmt labelContinueNormally = getNewLabel();
                    Iterator incomingIt = curState.getInEdgeIterator();
                    while(incomingIt.hasNext()) {
                        SMEdge incoming = (SMEdge)incomingIt.next();
                        if(incoming.getLabel().equals(symbol)) {
                            SMNode predecessor = incoming.getSource();
                            if(predecessor.boundVars.equals(curState.boundVars) 
                                    && predecessor.needStrongRefs.equals(curState.needStrongRefs)) {
                            	int index; // lookupswitch values list must be sorted
                            	for(index = 0; index < skipStates.size(); index++) {
                            		if(((IntConstant)(skipStates.get(index))).value > predecessor.getNumber()) break;
                            	}
                                skipStates.add(index, getInt(predecessor.getNumber()));
                                jumpLabels.add(labelReturnThis);
                            }
                        }
                    }
                    
                    // If there are any states that allow the optimisation, do it:
                    if(!skipStates.isEmpty()) {
                        doLookupSwitch(stateFrom, skipStates, jumpLabels, labelContinueNormally);
                        doAddLabel(labelContinueNormally);
                    }
                }

                // We create a copy of this, add the new bindings and return it
                Local result = getNewObject(disjunct, singleDisjunct, thisLocal);
                varIt = variables.iterator();
                bindIt = bindings.iterator();
                while(varIt.hasNext()) {
                    String varName = (String)varIt.next();
                    Local binding = (Local)bindIt.next();
                    
                    if(curState.needStrongRefs.contains(varName)) {
                        // result.var$isWeak = false;
                        doSetField(result, varName + "$isWeak", BooleanType.v(), getInt(0));
                        // result.var = binding;
                        doSetField(result, "var$" + varName, objectType, binding);
                    } else {
                        // result.var$isWeak = true;
                        doSetField(result, varName + "$isWeak", BooleanType.v(), getInt(0));
                        // result.var = new MyWeakRef(binding);
                        doSetField(result, "var$" + varName, objectType, getNewObject(myWeakRef, singleObjectType, binding));
                    }
                    
                    // set negative binding set to null -- we don't need it any more
                    doSetField(result, "not$" + varName, setType, NullConstant.v());
                }
                
                // new bindings are recorded -- return result;
                doReturn(result);
            }
            
            // unfinished business -- the labels
            doAddLabel(labelReturnFalse);
            doReturn(getStaticFieldLocal(disjunct, "falseD", disjunct.getType()));
            
            doAddLabel(labelReturnThis);
            // For debugging purposes -- print an 'x' when returning this, as it shows that the optimisation applies
            doPrintString("x");
            doReturn(thisLocal);
            
            doAddLabel(labelThrowException);
            doThrowException("Disjunct.addBindingsForSymbol" + symbol + " got an invalid state number: " + stateTo);
        }
    }
    
    /**
     * Add methods addNegativeBindingsForSymbolX(int to, Objects bindings..) for each tracematch symbol X.
     * 
     * Depending on how many variables are bound by this symbol, one of three things can happen:
     * - if no variables are bound, the result is falseD.
     * - if one variable is bound, the result is a single disjunct.
     * - if more than one variable is bound, the result is a set of disjuncts, since
     *    addNegBindings on a disjunct D is meant to return
     *           D && !(x1 == v1 && x2 == v2 && ...)
     *       <=> D && (x1 != v1 || x2 != v2 || ...)
     *       <=> (D && (x1 != v1)) || (D && (x2 != v2)) || ...
     *       
     * Because of this, the methods return a single disjunct for symbols that bind 0 or 1 variable, and a set
     * of disjuncts for other symbols. Note that all the elements of the set are easily computed by calling
     * addNegativeBindingsForVariableV().
     */
    protected void addDisjunctAddNegBindingsForSymbolMethods() {
        List singleDisjunct = new LinkedList();
        singleDisjunct.add(disjunct.getType());
        
        Iterator symbolIt = curTraceMatch.getSymbols().iterator();
        String symbol;
        while(symbolIt.hasNext()) {
            symbol = (String) symbolIt.next();
            List variables = curTraceMatch.getVariableOrder(symbol);
            List parameterTypes = new LinkedList();
            parameterTypes.add(IntType.v()); // number of the target state of the transition
            int varCount = variables.size();
            for(int i = 0; i < varCount; i++) {
                parameterTypes.add(objectType); // one Object parameter for each bound variable
            }
            
            boolean returnSet = (varCount > 1);
            Type returnType = (returnSet) ? setType : disjunct.getType();
            
            startMethod("addNegativeBindingsForSymbol" + symbol, parameterTypes, returnType, Modifier.PUBLIC);
            
            if(varCount == 0) {
                doReturn(getStaticFieldLocal(disjunct, "falseD", disjunct.getType()));
                continue;
            }
            
            Local thisLocal = getThisLocal();
            int parameterIndex = 0;
            Local stateTo = getParamLocal(parameterIndex++, IntType.v());
            
            // we store all bindings in a list
            List/*<Local>*/ bindings = new LinkedList();
            Iterator varIt = variables.iterator();
            while(varIt.hasNext()) {
                varIt.next();
                bindings.add(getParamLocal(parameterIndex++, objectType));
            }

            // We implement the following optimisation:
            // If we are currently in a state that is guaranteed to bind all variables, then the actual underlying
            // disjuncts will never be changed by addNegativeBindings -- all that can happen is that the bindings
            // are incompatible, in which case we return falseD, or that the bindings *are* compatible, in which
            // case we can return this rather than this.copy();
            // This relies on the fact that addNegativeBindings is only called on skip looks; thus the 'from' and
            // the 'to' states of the transition always have the same strong-references behaviour, since they're
            // the same node, and therefore we would never have to 'strengthen' previously weak bindings when
            // doing this.
            Stmt labelComputeResultNormally = Jimple.v().newNopStmt();
            Stmt labelCheckBindingsOnly = Jimple.v().newNopStmt();

            List jumpToLabels = new LinkedList(); // list of labels for the switch statement
            List jumpOnValues = new LinkedList(); // list of IntConstants for the switch statement
            
            Iterator stateIt = ((TMStateMachine)curTraceMatch.getState_machine()).getStateIterator();
            while(stateIt.hasNext()) {
                SMNode curNode = (SMNode)stateIt.next();
                // if all variables are bound and the state has a state loop -- we want to optimise it.
                if(curNode.hasEdgeTo(curNode, "") 
                        && curNode.boundVars.equals(new LinkedHashSet(curTraceMatch.getFormalNames()))) {
                    jumpToLabels.add(labelCheckBindingsOnly);
                    jumpOnValues.add(IntConstant.v(curNode.getNumber()));
                }
            }
            
            Local resultSet = (returnSet ? getNewObject(setClass) : null);
            Local result = null;

            // If we have found any states that allow the optimisation, then do it.
            if(!jumpToLabels.isEmpty()) {
                doLookupSwitch(stateTo, jumpOnValues, jumpToLabels, labelComputeResultNormally);
                doAddLabel(labelComputeResultNormally);
            }

            // "normal" result computation: call Disjunct.addNegativeBindingForVariable with each var/binding pair
            // and accumulate results in a set if needed, then return.
            varIt = variables.iterator();
            Iterator bindIt = bindings.iterator();
            while(varIt.hasNext()) {
                String varName = (String)varIt.next();
                Local binding = (Local)bindIt.next();
                
                result = getMethodCallResult(thisLocal, "addNegativeBindingForVariable" + varName,
                        singleObjectType, disjunct.getType(), binding);
                
                if(returnSet) {
                    doMethodCall(resultSet, "add", singleObjectType, BooleanType.v(), result);
                }
            }
            
            if(returnSet) {
                doReturn(resultSet);
            } else {
                doReturn(result);
            }
            
            // if the optimisation above applied -- suppose the disjunct is fully bound. Adding negative bindings
            // won't change it. They'll either be incompatible (result falseD) or compatible (result this).
            if(!jumpToLabels.isEmpty()) {
                doAddLabel(labelCheckBindingsOnly);
                Stmt labelReturnThis = getNewLabel();
                varIt = variables.iterator();
                bindIt = bindings.iterator();
                while(varIt.hasNext()) {
                    String varName = (String)varIt.next();
                    Local binding = (Local)bindIt.next();
                    
                    // if there's even just one variable whose binding doesn't contradict the new set of bindings,
                    // then the resulting set of disjuncts would contain 'this', so we just return it.
                    doJumpIfNotEqual(getMethodCallResult(thisLocal, "get$" + varName, objectType),
                            binding, labelReturnThis);
                }
                
                // if we fall through here, all bindings were incompatible, so we return false
                result = getStaticFieldLocal(disjunct, "falseD", disjunct.getType());
                if(returnSet) {
                    doMethodCall(resultSet, "add", singleObjectType, BooleanType.v(), result);
                    doReturn(resultSet);
                } else {
                    doReturn(result);
                }
                
                doAddLabel(labelReturnThis);
                if(returnSet) {
                    doMethodCall(resultSet, "add", singleObjectType, BooleanType.v(), thisLocal);
                    doReturn(resultSet);
                } else {
                    doReturn(thisLocal);
                }
            }
        }
    }
    
    /**
     * This method is very important, as the default Set implementations contain elements
     * unique with respect to the equals() method. Thus, with a sensible implementation of
     * this method, we don't need to worry about removing duplicate disjuncts from the
     * disjunct sets in the constraint() class.
     * 
     * Two disjuncts are considered equal iff either they are the same object or, for every
     * variable X, either both bind X to the same value or neither binds X, and the set of
     * negative bindings for X is the same.
     */
    protected void addDisjunctEqualsMethod() {
        startMethod("equals", singleObjectType, BooleanType.v(), Modifier.PUBLIC);
        
        Stmt labelReturnTrue = getNewLabel();
        Stmt labelReturnFalse = getNewLabel();
        
        Local thisLocal = getThisLocal();
        Local paramLocal = getParamLocal(0, objectType);
        
        // if objects are identical, return true
        doJumpIfEqual(thisLocal, paramLocal, labelReturnTrue);
        
        // if type is wrong, return false
        doJumpIfNotInstanceOf(paramLocal, disjunct.getType(), labelReturnFalse);
        
        // it's the right type -- cast it
        Local paramDisjunct = getCastValue(paramLocal, disjunct.getType());
        
        Iterator varIt = curTraceMatch.getFormalNames().iterator();
        while(varIt.hasNext()) {
            String varName = (String)varIt.next();
            
            Stmt labelThisNull = getNewLabel();
            Stmt labelCheckNextVar = getNewLabel();
            
            doJumpIfNull(getFieldLocal(thisLocal, "var$" + varName, objectType), labelThisNull);
            // the variable is bound by this -- if it isn't bound by the disjunct, return false
            doJumpIfNull(getFieldLocal(thisLocal, "var$" + varName, objectType), labelReturnFalse);
            // both disjuncts bind the variable -- check reference equality of the bindings
            doJumpIfEqual(getMethodCallResult(thisLocal, "get$" + varName, objectType),
                    getMethodCallResult(paramDisjunct, "get$" + varName, objectType), labelCheckNextVar);
            // bindings not equal -- so disjuncts not equal
            doJump(labelReturnFalse);
            
            doAddLabel(labelThisNull);
            // the variable is not bound by this -- if it's bound by the disjunct, return false
            doJumpIfNotNull(getFieldLocal(thisLocal, "var$" + varName, objectType), labelReturnFalse);
            // we now have to check whether the negative binding sets agree:
            // if(!this.not$var.equals(paramDisjunct.not$var)) return false;
            doJumpIfFalse(getMethodCallResult(getFieldLocal(thisLocal, "not$" + varName, setType), "equals",
                    singleObjectType, BooleanType.v(), getFieldLocal(paramDisjunct, "not$" + varName, setType)),
                    labelReturnFalse);
            
            doAddLabel(labelCheckNextVar);
        }
        
        // we have now checked all variables -- if we haven't branched off, return true
        doAddLabel(labelReturnTrue);
        doReturn(getInt(1));
        
        doAddLabel(labelReturnFalse);
        doReturn(getInt(0));
    }
    
    /**
     * This method is very important, as the default hashCode() method doesn't fulfill its contract
     * with the modified Disjunct.equals() method, and inconsistent hashCodes may bugger up the
     * behaviour of HashSets and other things relying on them.
     * 
     * A hash code for the disjunct is obtained by adding up the hash codes of all bound variables and
     * all negative bindings sets for unbound variables, then taking the hashCode of that.
     */
    protected void addDisjunctHashCodeMethod() {
        startMethod("hashCode", emptyList, IntType.v(), Modifier.PUBLIC);
        Local thisLocal = getThisLocal();
        Local hashCode = getNewLocal(IntType.v(), getInt(0), "hashCode");
        
        Iterator varIt = curTraceMatch.getFormalNames().iterator();
        while(varIt.hasNext()) {
            String varName = (String)varIt.next();
            Stmt labelVarUnbound = getNewLabel();
            Stmt labelHandleNextVar = getNewLabel();
            Local curVar = getFieldLocal(thisLocal, "var$" + varName, objectType);
            doJumpIfNull(curVar, labelVarUnbound);
            doAddToLocal(hashCode, getMethodCallResult(curVar, "hashCode", IntType.v()));
            doJump(labelHandleNextVar);
            
            doAddLabel(labelVarUnbound);
            doAddToLocal(hashCode, getMethodCallResult(getFieldLocal(thisLocal, "not$" + varName, setType), 
                    "hashCode", IntType.v()));
            
            doAddLabel(labelHandleNextVar);
        }
        
        doReturn(hashCode);
    }
    
    /**
     * Adds a method with the signature "public boolean validateDisjunct(int state);".
     * The idea is that the return value is false if one of the collectableWeakRefs of
     * the disjuct has expired, and true otherwise. Validating a disjunct before calling 
     * add[Neg]Bindings on it will enable clean-up of unneeded disjuncts 
     */
    protected void addDisjunctValidateDisjunctMethod() {
        List singleInt = new LinkedList();
        singleInt.add(IntType.v());
        startMethod("validateDisjunct", singleInt, BooleanType.v(), Modifier.PUBLIC);
        
        Local thisLocal = getThisLocal();
        Local stateTo = getParamLocal(0, IntType.v());

        Stmt labelReturnTrue = getNewLabel();
        Stmt labelReturnFalse = getNewLabel();
        Stmt labelThrowException = getNewLabel();
        
        // We distinguish which state we're in by doing a lookup switch
        List switchValues = new LinkedList();
        List switchLabels = new LinkedList();
        Iterator stateIt = ((TMStateMachine)curTraceMatch.getState_machine()).getStateIterator();
        while(stateIt.hasNext()) {
            SMNode state = (SMNode)stateIt.next();
            switchValues.add(getInt(state.getNumber()));
            switchLabels.add(getNewLabel());
        }
        
        doLookupSwitch(stateTo, switchValues, switchLabels, labelThrowException);
        
        Iterator labelIt = switchLabels.iterator();
        stateIt = ((TMStateMachine)curTraceMatch.getState_machine()).getStateIterator();
        while(stateIt.hasNext()) {
            SMNode state = (SMNode)stateIt.next();
            Stmt label = (Stmt)labelIt.next();
            
            doAddLabel(label);
            //////// Cleaning up invalidated collectableWeakRefs
            // Each state is labelled with a set collectableWeakRefs. If one of these
            // becomes invalid, the disjunct can never lead to a successful match and,
            // accordingly, can/must be discarded.
            //
            // Since we don't allow null bindings and WeakRefs become null when they are
            // invalidated, we check each bound variable in the collectableWeakRefs set
            // for the current state for null-ness, and if it's null return false.
            Iterator collectableWeakRefIt = state.collectableWeakRefs.iterator();
            while(collectableWeakRefIt.hasNext()) {
                String varName = (String)collectableWeakRefIt.next();
                Stmt labelCurVarNotBound = getNewLabel();
                
                doJumpIfNull(getFieldLocal(thisLocal, "var$" + varName, objectType), labelCurVarNotBound);
                
                // variable is bound -- check it
                doJumpIfNull(getMethodCallResult(thisLocal, "get$" + varName, objectType), labelReturnFalse);
                
                doAddLabel(labelCurVarNotBound);
            }
            doJump(labelReturnTrue);
        }
        // if we don't branch out, the disjunct is valid
        doAddLabel(labelReturnTrue);
        doReturn(getInt(1));
        
        doAddLabel(labelReturnFalse);
        // Debug -- print out "*" for this kind of cleanup
        doPrintString("*");
        doReturn(getInt(0));
        
        doAddLabel(labelThrowException);
        doThrowException("Disjunct.validateDisjunct() called with an invalid state number");

    }
}
