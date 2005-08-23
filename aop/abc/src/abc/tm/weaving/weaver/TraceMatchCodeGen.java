package abc.tm.weaving.weaver;

import java.util.*;

import soot.*;
import soot.util.*;
import soot.jimple.*;

import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.UnUsedParams;
import abc.tm.weaving.aspectinfo.*;
import abc.tm.weaving.matching.*;
import abc.weaving.aspectinfo.*;


/**
 * Fills in method stubs for tracematch classes.
 * @author Pavel Avgustinov
 */
public class TraceMatchCodeGen {
    
    // Set this to true to enable debug traces -- print "+" for every time a disjunct
    // is constructed, "-" for every time it's finalized and "*" every time one is
    // discarded for being invalid (those are the default traces).
    // If you pipe the output into a file (by appending '> output_file' to the command
    // line), you can then count the frequency of the respective events with the
    // following command: cat output_file | tr "+" "\n" | wc -l
    // (replacing "+" by "-" or "*" as appropriate).
    private boolean enableDebugTraces = false;
    
    // Class which should be used for weak references. We can't use the default 
    // java.lang.ref.WeakReference, since its equals() method doesn't do what we want.
    // Thus we override it, changing equals method (so that wr.equals(o) is true if
    // o is a weakRef and their referents are the same object, or if wr's referent is o.
    // We also override the hashCode() method to return o.hashCode(), where o is the
    // referent.
    private SootClass myWeakRef = null;
    
    // TODO: Perhaps have a dedicated flag for tracematch codegen
    private static void debug(String message)
    { if (abc.main.Debug.v().aspectCodeGen)
        System.err.println("TCG*** " + message);
    }

    protected String getConstraintClassName(TraceMatch tm) {
        return tm.getPackage() + "Constraint$" + tm.getName();
    }
    
    protected String getDisjunctClassName(TraceMatch tm) {
        return tm.getPackage() + "Disjunct$" + tm.getName();
    }
    
       
    protected void printString(Body b, String s) {
        if(!enableDebugTraces) return;
        
        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        Chain units = b.getUnits();
        Local out = lgen.generateLocal(RefType.v("java.io.PrintStream"), "out");
        units.addLast(Jimple.v().newAssignStmt(out, Jimple.v().newStaticFieldRef(
                Scene.v().makeFieldRef(Scene.v().getSootClass("java.lang.System"), "out", 
                        RefType.v("java.io.PrintStream"), true))));
        List parameters = new LinkedList();
        parameters.add(RefType.v("java.lang.String"));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(out,
                Scene.v().makeMethodRef(Scene.v().getSootClass("java.io.PrintStream"), "print",
                        parameters, VoidType.v(), false), StringConstant.v(s))));
    }
    
    protected void throwException(Body b, String s) {
        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        List parameters = new LinkedList();
        parameters.add(RefType.v("java.lang.String"));
        Local throwable = lgen.generateLocal(RefType.v("java.lang.RuntimeException"), "exception");
        
        Chain units = b.getUnits();
        units.addLast(Jimple.v().newAssignStmt(throwable, Jimple.v().newNewExpr(
                RefType.v("java.lang.RuntimeException"))));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(throwable, 
                Scene.v().makeConstructorRef(Scene.v().getSootClass("java.lang.RuntimeException"),
                parameters), StringConstant.v(s))));
        units.addLast(Jimple.v().newThrowStmt(throwable));
    }
    
    protected Local getWeakRefLocal(Body b, Local target, SootClass myWeakRef) {
    	LocalGeneratorEx lgen = new LocalGeneratorEx(b);
    	Local result = lgen.generateLocal(myWeakRef.getType(), "weakRefLocal");
    	
    	List singleObjectTypeParameter = new LinkedList();
    	singleObjectTypeParameter.add(RefType.v("java.lang.Object"));
    	
    	Chain units = b.getUnits();
    	units.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newNewExpr(myWeakRef.getType())));
    	units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(result, 
    			Scene.v().makeConstructorRef(myWeakRef, singleObjectTypeParameter), target)));
    	
    	return result;
    }
    
    // codegen helper methods
    protected void addReturnThisMethod(SootClass addToClass, String methodName, List parameters) {
        SootMethod method = new SootMethod(methodName, parameters, addToClass.getType(),
                Modifier.PUBLIC);
        Body b = Jimple.v().newBody(method);
        method.setActiveBody(b);
        addToClass.addMethod(method);
        
        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        Local thisLocal = lgen.generateLocal(addToClass.getType(), "thisLocal");
        
        Chain units = b.getUnits();
        units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(addToClass.getType())));
        units.addLast(Jimple.v().newReturnStmt(thisLocal));
    }
    
    // returns a static field member of the same type as addToClass
    protected void addReturnFieldMethod(SootClass addToClass, String methodName,
            FieldRef fieldToReturn, List parameters) {
        SootMethod method = new SootMethod(methodName, parameters, addToClass.getType(),
                Modifier.PUBLIC);
        Body b = Jimple.v().newBody(method);
        method.setActiveBody(b);
        addToClass.addMethod(method);
        
        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        Local result = lgen.generateLocal(addToClass.getType(), "result");
        
        Chain units = b.getUnits();
        units.addLast(Jimple.v().newAssignStmt(result, fieldToReturn));
        units.addLast(Jimple.v().newReturnStmt(result));
    }
    
    /**
     * Implements the simple optimisation described below. Applicable in add[Neg]Bindings on Disjunct.
     * @param tm the tracematch we're considering
     * @param curState the state we're entering 
     * @param symbolName the name of the symbol which we are considering
     * @param units the PatchingChain to which to append the checking code
     * @param stateNumberFrom the Local holding the state number of the state we're entering from
     * @param labelReturnThis the label to jump to if we discover we can 'return this;'
     */
    protected void returnThisIfPossible(TraceMatch tm, SMNode curState, String symbolName, Chain units, Local stateNumberFrom, Stmt labelReturnThis) {
        // Optimisation: Only construct new disjuncts when necessary
        // =============
        // Observe that once a disjunct is fully bound, the only way in which it can change
        // further (since we don't keep track of negative bindings) is if it has to
        // strengthen a reference that was hitherto weak. Thus, if the current state is
        // guaranteed to have bound all tracematch variables, we want to return 'this' rather
        // than a new disjunct object if we are coming from a state that also has all variables
        // bound, and which agrees with the current state regarding needStrongRefs.
        //
        if(curState.boundVars.equals(new LinkedHashSet(tm.getFormalNames()))) {
        	// the current state binds all variables.
        	// Great -- construct a list of all states S such that there is a transition labelled
        	// with symbolName from S to curState, where S binds all variables and has the same
        	// needStrongRefs set as curState.
        	List skipStates = new LinkedList();
        	Iterator incomingIt = curState.getInEdgeIterator();
        	while(incomingIt.hasNext()) {
        		SMEdge incoming = (SMEdge)incomingIt.next();
        		if(incoming.getLabel().equals(symbolName)) {
        			SMNode predecessor = incoming.getSource();
        			if(predecessor.boundVars.equals(curState.boundVars) && predecessor.needStrongRefs.equals(curState.needStrongRefs)) {
        				skipStates.add(new Integer(predecessor.getNumber()));
        			}
        		}
        	}
        	
        	// now check if we're coming from one of the skippable states, and if we are, return this.
        	Iterator skipIt = skipStates.iterator();
        	while(skipIt.hasNext()) {
        		int number = ((Integer)skipIt.next()).intValue();
        		units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(stateNumberFrom, IntConstant.v(number)), labelReturnThis));
        	}
        }
    }
    
    /**
     * Create the classes needed to keep constraints for a given tracematch. Classes are
     * the Constraint set of disjuncts and the disjunct class, plus any helper classes.
     * Could, at some point, specialise the constraints to each FSA state.
     * @param tm The relevant tracematch
     */
    protected void createConstraintClasses(TraceMatch tm) {
        // Create the MyWeakReference class. Since it doesn't depend on the tracematch, we
        // only create it once.
        if(myWeakRef == null) {
        	// myWeakRef = Scene.v().getSootClass("java.lang.ref.WeakReference");/*
        	myWeakRef = new SootClass(tm.getPackage()+"MyWeakReference");
        	addMWRInitialiserMethod();
        	addMWREqualsMethod();
        	addMWRHashCodeMethod();
        	Scene.v().addClass(myWeakRef);
        	myWeakRef.setApplicationClass();
        	myWeakRef.setSuperclass(Scene.v().getSootClass("java.lang.ref.WeakReference"));/**/
        }
        
    	// the SootClasses for the constraint and the main disjunct class for the tracematch
        SootClass object = Scene.v().getSootClass("java.lang.Object");
        SootClass constraint = new SootClass(getConstraintClassName(tm));
        SootClass disjunct = new SootClass(getDisjunctClassName(tm));
        tm.setConstraintClass(constraint);
        tm.setDisjunctClass(disjunct);

        fillInConstraintClass(tm, constraint, disjunct);
        fillInDisjunctClass(tm, disjunct);
        Scene.v().addClass(constraint);
        Scene.v().addClass(disjunct);
        constraint.setApplicationClass();
        disjunct.setApplicationClass();
        constraint.setSuperclass(object);
        disjunct.setSuperclass(object);
    }
    
    /**
     * Fills in the constraint class for a given tracematch.
     * @param constraint the SootClass to add methods and fields to
     * @param disjunct the SootClass for the disjuncts -- to call methods on
     */
    protected void fillInConstraintClass(TraceMatch tm, SootClass constraint, SootClass disjunct) {
        /*
         * General ideas are as follows:
         * - We need a class Constraint$TraceMatchName which holds a set of disjuncts and
         *   provides operations to form conjunctions and disjunctions. This may or may not
         *   need to be specialised to the tracematch.
         * - A class Disjunct$TraceMatchName, which has fields for each tracematch variable
         *   that contain bindings (if appropriate), flags indicating if each variable is
         *   bound, and sets of negative bindings for each variable. Provides methods to
         *   form conjunctions and disjunctions with other disjuncts, get the values of
         *   the variables, and introduce new (positive or negative) bindings.
         * - Classes Disjunct$TraceMatchName$<StateNumber> extending Disjunct$TraceMatchName
         *   which override certain parts of the behaviour by specialising it to the current
         *   state. In particular, they determine whether weak or strong references to 
         *   bound objects need to be kept for each variable, and whether there is a need to
         *   store negative bindings or not.
         */
        
        // Declare classes
        Type setType = RefType.v("java.util.Set");
        SootField trueConstraint = new SootField("trueC", constraint.getType(), Modifier.PUBLIC | 
                            Modifier.FINAL | Modifier.STATIC);
        constraint.addField(trueConstraint);
        SootField falseConstraint = new SootField("falseC", constraint.getType(), Modifier.PUBLIC |
                            Modifier.FINAL | Modifier.STATIC);
        constraint.addField(falseConstraint);
        SootField disjuncts = new SootField("disjuncts", setType,
                            Modifier.PUBLIC);
        constraint.addField(disjuncts);
        
        addConstraintOrMethod(constraint, disjunct);
        
        // addConstraintAndMethod(constraint, disjunct); // not needed with addBindings
        
        addConstraintCopyMethod(constraint, disjunct);
        
        // list needed for symbols that bind no variables -- then the addNegBindings methods
        // take a single int, addBindings take two ints.
        List parametersOneInt = new LinkedList();
        parametersOneInt.add(IntType.v());
        List parametersTwoInts = new LinkedList();
        parametersTwoInts.add(IntType.v());
        parametersTwoInts.add(IntType.v());
        
        Iterator symbolIt = tm.getSym_to_vars().keySet().iterator();
        String symbol;
        while(symbolIt.hasNext()) {
            symbol = (String)symbolIt.next();
            
            /////////// handling for variable-less symbols/tracematches
            if(tm.getVariableOrder(symbol).isEmpty()) { 
                // if this symbol has no vars -- return this from addBindingsForSymbol...
                addReturnThisMethod(constraint, "addBindingsForSymbol" + symbol, parametersTwoInts);
                // ... and falseC from addNegBindingsForSymbol
                FieldRef falseC = Jimple.v().newStaticFieldRef(Scene.v().makeFieldRef(constraint, 
                        "falseC", constraint.getType(), true));
                addReturnFieldMethod(constraint, "addNegativeBindingsForSymbol" + symbol,
                        falseC, parametersOneInt);
                continue;
            }

            addAddBindingsDispatchMethod(constraint, disjunct, 
                    "addBindingsForSymbol" + symbol, tm.getVariableOrder(symbol));
            addAddNegativeBindingsDispatchMethod(constraint, disjunct, 
                    "addNegativeBindingsForSymbol" + symbol, tm.getVariableOrder(symbol));
        }
        
        addConstraintGetDisjunctArrayMethod(constraint, disjunct);
        
        addConstraintInitialiser(constraint);
        
        addConstraintStaticInitialiser(constraint, disjunct);
    }
    
    protected SootMethod addAddBindingsDispatchMethod(SootClass constraint, SootClass disjunct,
            String methodName, List/*<String>*/ variables) {
        int varCount = variables.size();
        List parameterTypes = new LinkedList();
        parameterTypes.add(IntType.v()); // originating state number
        parameterTypes.add(IntType.v()); // target state number
        for(int i = 0; i < varCount; i++) {
            parameterTypes.add(RefType.v("java.lang.Object"));
        }
        SootMethod symbolMethod = new SootMethod(methodName, 
                parameterTypes, constraint.getType(), Modifier.PUBLIC);
        Body b = Jimple.v().newBody(symbolMethod);
        symbolMethod.setActiveBody(b);
        constraint.addMethod(symbolMethod);
        
        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        // generate 'standard' locals
        SootClass setClass = Scene.v().getSootClass("java.util.Set");
        RefType setType = RefType.v("java.util.Set");
        RefType iteratorType = RefType.v("java.util.Iterator");
        SootClass hashSet = Scene.v().getSootClass("java.util.LinkedHashSet");
        SootClass iteratorClass = Scene.v().getSootClass("java.util.Iterator");
        Local resultSet = lgen.generateLocal(hashSet.getType(), "resultSet");
        Local localSet = lgen.generateLocal(setType, "localSet");
        Local result = lgen.generateLocal(constraint.getType(), "result");
        Local thisLocal = lgen.generateLocal(constraint.getType(), "this");
        Local disjunctThis = lgen.generateLocal(disjunct.getType(), "disjunctThis");
        Local disjunctIt = lgen.generateLocal(iteratorType, "disjunctIt");
        Local disjunctResult = lgen.generateLocal(disjunct.getType(), "disjunctResult");

        Chain units = b.getUnits();
        // Add identity statements for this local
        units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(constraint.getType())));
        
        // add symbol-dependent parameters and identity statements
        List parameterLocals = new LinkedList();
        Local parameterLocal;
        int parameterIndex = 0;
        
        parameterLocal = lgen.generateLocal(IntType.v(), "symbolNumberFrom");
        parameterLocals.add(parameterLocal);
        units.addLast(Jimple.v().newIdentityStmt(parameterLocal, 
                Jimple.v().newParameterRef(IntType.v(), parameterIndex++)));

        parameterLocal = lgen.generateLocal(IntType.v(), "symbolNumberTo");
        parameterLocals.add(parameterLocal);
        units.addLast(Jimple.v().newIdentityStmt(parameterLocal, 
                Jimple.v().newParameterRef(IntType.v(), parameterIndex++)));
        
        for(Iterator it = variables.iterator(); it.hasNext(); ) {
            parameterLocal = lgen.generateLocal(RefType.v("java.lang.Object"), (String)it.next());
            parameterLocals.add(parameterLocal);
            units.addLast(Jimple.v().newIdentityStmt(parameterLocal,
                    Jimple.v().newParameterRef(RefType.v("java.lang.Object"), parameterIndex++)));
        }

        Stmt labelReturnFalseC = Jimple.v().newNopStmt();
        StaticFieldRef falseC = Jimple.v().newStaticFieldRef(
                Scene.v().makeFieldRef(constraint, "falseC", constraint.getType(), true));
        Local falseConstraint = lgen.generateLocal(constraint.getType(), "falseConstraint");
        units.addLast(Jimple.v().newAssignStmt(falseConstraint, falseC));

        // First off -- if this is falseC, then the result of addBindings is false.
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(thisLocal, falseConstraint), labelReturnFalseC));
        
        // Store this.disjuncts in a local
        units.addLast(Jimple.v().newAssignStmt(localSet, 
                Jimple.v().newInstanceFieldRef(
                        thisLocal, 
                        Scene.v().makeFieldRef(constraint, "disjuncts", setType, false))));
        // Create a new HashSet for the result, as we're not changing things in-place
        units.addLast(Jimple.v().newAssignStmt(resultSet,
                Jimple.v().newNewExpr(hashSet.getType())));
        // do specialinvoke of constructor
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(resultSet, 
                Scene.v().makeConstructorRef(hashSet, new LinkedList()))));

        // Get an iterator for this constraint's disjuncts
        units.addLast(Jimple.v().newAssignStmt(disjunctIt, Jimple.v().newInterfaceInvokeExpr(
                localSet, Scene.v().makeMethodRef(setClass, "iterator", new LinkedList(), 
                        iteratorType, false))));
        
        // Have to emulate loops with jumps: while(disjunctIt.hasNext()) { ... }
        Stmt labelLoopBegin = Jimple.v().newNopStmt();
        Stmt labelLoopEnd = Jimple.v().newNopStmt();
        units.addLast(labelLoopBegin);
        // if(!it1.hasNext()) goto labelLoopEnd; <code for loop>; <label>:
        Local booleanLocal = lgen.generateLocal(BooleanType.v(), "booleanLocal");
        units.addLast(Jimple.v().newAssignStmt(booleanLocal,
                Jimple.v().newInterfaceInvokeExpr(disjunctIt, 
                        Scene.v().makeMethodRef(iteratorClass, "hasNext", new LinkedList(), BooleanType.v(), false))));
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(booleanLocal, IntConstant.v(0)),
                        labelLoopEnd));
        // disjunctThis = (Disjunct)disjunctIt.next();
        Local tmpObject = lgen.generateLocal(RefType.v("java.lang.Object"), "tmpObject");
        units.addLast(Jimple.v().newAssignStmt(tmpObject, Jimple.v().newInterfaceInvokeExpr(disjunctIt, 
                                Scene.v().makeMethodRef(iteratorClass, "next", new LinkedList(), 
                                        RefType.v("java.lang.Object"), false))));
        units.addLast(Jimple.v().newAssignStmt(disjunctThis, 
                Jimple.v().newCastExpr(tmpObject, disjunct.getType())));
        
        ////////// Cleanup of invalid disjuncts -- if the current disjunct isn't valid,
        // just remove it from the disjunct set and continue with the next.
        // if(!disjunctThis.validateDisjunct(state) { it.remove(); goto labelLoopBegin; }
        List singleIntParameter = new LinkedList();
        singleIntParameter.add(IntType.v());
        Local isValidDisjunct = lgen.generateLocal(BooleanType.v(), "isValidDisjunct");
        Stmt labelDisjunctValid = Jimple.v().newNopStmt();
        units.addLast(Jimple.v().newAssignStmt(isValidDisjunct, Jimple.v().newVirtualInvokeExpr(disjunctThis,
                Scene.v().makeMethodRef(disjunct, "validateDisjunct", singleIntParameter, BooleanType.v(),
                        false), (Local)parameterLocals.get(0))));
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(isValidDisjunct, IntConstant.v(1)), labelDisjunctValid));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(disjunctIt, 
                Scene.v().makeMethodRef(iteratorClass, "remove", new LinkedList(), VoidType.v(), false))));
        units.addLast(Jimple.v().newGotoStmt(labelLoopBegin));
        
        units.addLast(labelDisjunctValid);
        
        // disjunctResult = disjunct.addBindingsForSymbolX(...);
        units.addLast(Jimple.v().newAssignStmt(disjunctResult,
                Jimple.v().newVirtualInvokeExpr(disjunctThis, 
                        Scene.v().makeMethodRef(disjunct, methodName,
                                parameterTypes, disjunct.getType(), false), parameterLocals)));
        // resultSet.add(disjunctResult);
        List parameters = new LinkedList();
        parameters.add(RefType.v("java.lang.Object"));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(resultSet,
                Scene.v().makeMethodRef(hashSet, "add", parameters, BooleanType.v(), false), disjunctResult)));
        // goto beginning of inner loop
        units.addLast(Jimple.v().newGotoStmt(labelLoopBegin));
        units.addLast(labelLoopEnd);

        // We remove the false disjunct, then, if the disjunct set is empty, we return the
        // false constraint falseC, otherwise we return a new constraint with the 
        // appropriate disjunct set.
        parameters.clear();
        parameters.add(RefType.v("java.lang.Object"));
        // resultSet.remove(falseD);
        StaticFieldRef falseD = Jimple.v().newStaticFieldRef(
                Scene.v().makeFieldRef(disjunct, "falseD", disjunct.getType(), true));
        Local falseDisjunct = lgen.generateLocal(disjunct.getType(), "falseDisjunct");
        units.addLast(Jimple.v().newAssignStmt(falseDisjunct, falseD));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(resultSet,
                Scene.v().makeMethodRef(hashSet, "remove", parameters, BooleanType.v(), false), falseDisjunct)));
        // if(resultSet.isEmpty) goto label;
        units.addLast(Jimple.v().newAssignStmt(booleanLocal, Jimple.v().newVirtualInvokeExpr(resultSet,
                Scene.v().makeMethodRef(hashSet, "isEmpty", new LinkedList(), BooleanType.v(), false))));
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(booleanLocal, IntConstant.v(1)),
                labelReturnFalseC));
        // Set is nonempty -- construct a new constraint to return
        units.addLast(Jimple.v().newAssignStmt(result, 
                Jimple.v().newNewExpr(constraint.getType())));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(result,
                Scene.v().makeConstructorRef(constraint, new LinkedList()))));
        // result.disjuncts = resultSet;
        units.addLast(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(result,
                    Scene.v().makeFieldRef(constraint, "disjuncts", setType, false)), 
                resultSet));
        // return result;
        units.addLast(Jimple.v().newReturnStmt(result));
        // Label
        units.addLast(labelReturnFalseC);
        // return falseC;
        units.addLast(Jimple.v().newReturnStmt(falseConstraint));
        
        return symbolMethod;
    }
    
    /**
     * Add the addNegativeBindings() method to the Constraint class
     * @param constraint
     * @param disjunct
     * @param methodName
     * @param variables
     * @return
     */
    protected SootMethod addAddNegativeBindingsDispatchMethod(SootClass constraint, SootClass disjunct,
            String methodName, List/*<String>*/ variables) {
        int varCount = variables.size();
        List parameterTypes = new LinkedList();
        parameterTypes.add(IntType.v());
        for(int i = 0; i < varCount; i++) {
            parameterTypes.add(RefType.v("java.lang.Object"));
        }
        SootMethod symbolMethod = new SootMethod(methodName, 
                parameterTypes, constraint.getType(), Modifier.PUBLIC);
        Body b = Jimple.v().newBody(symbolMethod);
        symbolMethod.setActiveBody(b);
        constraint.addMethod(symbolMethod);
        
        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        // generate 'standard' locals
        SootClass setClass = Scene.v().getSootClass("java.util.Set");
        RefType setType = RefType.v("java.util.Set");
        RefType iteratorType = RefType.v("java.util.Iterator");
        SootClass hashSet = Scene.v().getSootClass("java.util.LinkedHashSet");
        SootClass iteratorClass = Scene.v().getSootClass("java.util.Iterator");
        Local resultSet = lgen.generateLocal(hashSet.getType(), "resultSet");
        Local localSet = lgen.generateLocal(setType, "localSet");
        Local tmpSet = lgen.generateLocal(setType, "tmpSet");
        Local result = lgen.generateLocal(constraint.getType(), "result");
        Local thisLocal = lgen.generateLocal(constraint.getType(), "this");
        Local disjunctThis = lgen.generateLocal(disjunct.getType(), "disjunctThis");
        Local disjunctIt = lgen.generateLocal(iteratorType, "disjunctIt");
        Local disjunctResult = lgen.generateLocal(disjunct.getType(), "disjunctResult");

        Chain units = b.getUnits();
        // Add identity statements for this local
        units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(constraint.getType())));
        
        // add symbol-dependent parameters and identity statements
        List parameterLocals = new LinkedList();
        Local parameterLocal;
        int parameterIndex = 0;
        parameterLocal = lgen.generateLocal(IntType.v(), "symbolNumber");
        parameterLocals.add(parameterLocal);
        units.addLast(Jimple.v().newIdentityStmt(parameterLocal, 
                Jimple.v().newParameterRef(IntType.v(), parameterIndex++)));
        
        for(Iterator it = variables.iterator(); it.hasNext(); ) {
            parameterLocal = lgen.generateLocal(RefType.v("java.lang.Object"), (String)it.next());
            parameterLocals.add(parameterLocal);
            units.addLast(Jimple.v().newIdentityStmt(parameterLocal,
                    Jimple.v().newParameterRef(RefType.v("java.lang.Object"), parameterIndex++)));
        }
        
        // If this is falseC, then the result is falseC
        Stmt labelReturnFalseC = Jimple.v().newNopStmt();

        StaticFieldRef falseC = Jimple.v().newStaticFieldRef(
                Scene.v().makeFieldRef(constraint, "falseC", constraint.getType(), true));
        Local falseConstraint = lgen.generateLocal(constraint.getType(), "falseConstraint");
        units.addLast(Jimple.v().newAssignStmt(falseConstraint, falseC));

        units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(thisLocal, falseConstraint), labelReturnFalseC));
        
        // Store this.disjuncts in a local
        units.addLast(Jimple.v().newAssignStmt(localSet, 
                Jimple.v().newInstanceFieldRef(
                        thisLocal, 
                        Scene.v().makeFieldRef(constraint, "disjuncts", setType, false))));
        // Create a new HashSet for the result, as we're not changing things in-place
        units.addLast(Jimple.v().newAssignStmt(resultSet,
                Jimple.v().newNewExpr(hashSet.getType())));
        // do specialinvoke of constructor
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(resultSet, 
                Scene.v().makeConstructorRef(hashSet, new LinkedList()))));

        // Get an iterator for this constraint's disjuncts
        units.addLast(Jimple.v().newAssignStmt(disjunctIt, Jimple.v().newInterfaceInvokeExpr(
                localSet, Scene.v().makeMethodRef(setClass, "iterator", new LinkedList(), 
                        iteratorType, false))));
        
        // Have to emulate loops with jumps: while(disjunctIt.hasNext()) { ... }
        Stmt labelLoopBegin = Jimple.v().newNopStmt();
        Stmt labelLoopEnd = Jimple.v().newNopStmt();
        units.addLast(labelLoopBegin);
        // if(!it1.hasNext()) goto labelLoopEnd; <code for loop>; <label>:
        Local booleanLocal = lgen.generateLocal(BooleanType.v(), "booleanLocal");
        units.addLast(Jimple.v().newAssignStmt(booleanLocal,
                Jimple.v().newInterfaceInvokeExpr(disjunctIt, 
                        Scene.v().makeMethodRef(iteratorClass, "hasNext", new LinkedList(), BooleanType.v(), false))));
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(booleanLocal, IntConstant.v(0)),
                        labelLoopEnd));
        // disjunctThis = (Disjunct)disjunctIt.next();
        Local tmpObject = lgen.generateLocal(RefType.v("java.lang.Object"), "tmpObject");
        units.addLast(Jimple.v().newAssignStmt(tmpObject, Jimple.v().newInterfaceInvokeExpr(disjunctIt, 
                                Scene.v().makeMethodRef(iteratorClass, "next", new LinkedList(), 
                                        RefType.v("java.lang.Object"), false))));
        units.addLast(Jimple.v().newAssignStmt(disjunctThis, 
                Jimple.v().newCastExpr(tmpObject, disjunct.getType())));
        
        ////////// Cleanup of invalid disjuncts -- if the current disjunct isn't valid,
        // just remove it from the disjunct set and continue with the next.
        // if(!disjunctThis.validateDisjunct(state) { it.remove(); goto labelLoopBegin; }
        List singleIntParameter = new LinkedList();
        singleIntParameter.add(IntType.v());
        Local isValidDisjunct = lgen.generateLocal(BooleanType.v(), "isValidDisjunct");
        Stmt labelDisjunctValid = Jimple.v().newNopStmt();
        units.addLast(Jimple.v().newAssignStmt(isValidDisjunct, Jimple.v().newVirtualInvokeExpr(disjunctThis,
                Scene.v().makeMethodRef(disjunct, "validateDisjunct", singleIntParameter, BooleanType.v(),
                        false), (Local)parameterLocals.get(0))));
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(isValidDisjunct, IntConstant.v(1)), labelDisjunctValid));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(disjunctIt, 
                Scene.v().makeMethodRef(iteratorClass, "remove", new LinkedList(), VoidType.v(), false))));
        units.addLast(Jimple.v().newGotoStmt(labelLoopBegin));
        
        units.addLast(labelDisjunctValid);
        

        List parameters = new LinkedList();
        
        // disjunct.addBindingsForSymbolX() returns a ***Set*** for symbols that bind more than one variable, and
        // a single disjunct for all others. Thus...
        if(varCount < 2) {
        	// disjunctResult = disjunct.addBindingsForSymbolX(...);
        	units.addLast(Jimple.v().newAssignStmt(disjunctResult,
        			Jimple.v().newVirtualInvokeExpr(disjunctThis, 
        					Scene.v().makeMethodRef(disjunct, methodName,
        							parameterTypes, disjunct.getType(), false), parameterLocals)));
        	// resultSet.add(disjunctResult);
        	parameters.add(RefType.v("java.lang.Object"));
        	units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(resultSet,
        			Scene.v().makeMethodRef(hashSet, "add", parameters, BooleanType.v(), false), disjunctResult)));
        } else {
        	// if this symbol binds >= 2 variables, the result is a set... handle accordingly
        	units.addLast(Jimple.v().newAssignStmt(tmpSet,
        			Jimple.v().newVirtualInvokeExpr(disjunctThis, 
        					Scene.v().makeMethodRef(disjunct, methodName,
        							parameterTypes, setType, false), parameterLocals)));
        	// resultSet.addAll(tmpSet);
        	parameters.add(RefType.v("java.util.Collection"));
        	units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(resultSet,
        			Scene.v().makeMethodRef(hashSet, "addAll", parameters, BooleanType.v(), false), tmpSet)));
        }
        // goto beginning of inner loop
        units.addLast(Jimple.v().newGotoStmt(labelLoopBegin));
        units.addLast(labelLoopEnd);

        // We remove the false disjunct, then, if the disjunct set is empty, we return the
        // false constraint falseC, otherwise we return a new constraint with the 
        // appropriate disjunct set.
        parameters.clear();
        parameters.add(RefType.v("java.lang.Object"));
        // resultSet.remove(falseD);
        StaticFieldRef falseD = Jimple.v().newStaticFieldRef(
                Scene.v().makeFieldRef(disjunct, "falseD", disjunct.getType(), true));
        Local falseDisjunct = lgen.generateLocal(disjunct.getType(), "falseDisjunct");
        units.addLast(Jimple.v().newAssignStmt(falseDisjunct, falseD));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(resultSet,
                Scene.v().makeMethodRef(hashSet, "remove", parameters, BooleanType.v(), false), falseDisjunct)));
        // if(resultSet.isEmpty) goto label;
        units.addLast(Jimple.v().newAssignStmt(booleanLocal, Jimple.v().newVirtualInvokeExpr(resultSet,
                Scene.v().makeMethodRef(hashSet, "isEmpty", new LinkedList(), BooleanType.v(), false))));
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(booleanLocal, IntConstant.v(1)),
                labelReturnFalseC));
        // Set is nonempty -- construct a new constraint to return
        units.addLast(Jimple.v().newAssignStmt(result, 
                Jimple.v().newNewExpr(constraint.getType())));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(result,
                Scene.v().makeConstructorRef(constraint, new LinkedList()))));
        // result.disjuncts = resultSet;
        units.addLast(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(result,
                    Scene.v().makeFieldRef(constraint, "disjuncts", setType, false)), 
                resultSet));
        // return result;
        units.addLast(Jimple.v().newReturnStmt(result));
        // Label
        units.addLast(labelReturnFalseC);
        // return falseC;
        units.addLast(Jimple.v().newReturnStmt(falseConstraint));
        return symbolMethod;
    }
    
    
    protected void addConstraintOrMethod(SootClass constraint, SootClass disjunct) {
        SootClass setClass = Scene.v().getSootClass("java.util.Set");
        RefType setType = RefType.v("java.util.Set");
        SootClass hashSet = Scene.v().getSootClass("java.util.LinkedHashSet");
        
        LinkedList parameters = new LinkedList();
        parameters.add(constraint.getType());
        SootMethod orMethod = new SootMethod("or", parameters, constraint.getType(), 
                Modifier.PUBLIC);
        Body b = Jimple.v().newBody(orMethod);
        orMethod.setActiveBody(b);
        
        constraint.addMethod(orMethod);
        
        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        Local localSet = lgen.generateLocal(setType, "localSet");
        Local remoteSet = lgen.generateLocal(setType, "remoteSet");
        Local result = lgen.generateLocal(constraint.getType(), "result");
        Local thisLocal = lgen.generateLocal(constraint.getType(), "this");
        Local paramLocal = lgen.generateLocal(constraint.getType(), "param");
        
        Chain units = b.getUnits();
        // Add identity statements for this and parameter locals
        units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(constraint.getType())));
        units.addLast(Jimple.v().newIdentityStmt(paramLocal, 
            Jimple.v().newParameterRef(constraint.getType(), 0)));
        // If one of the constraints is trueC, the result is trueC
        // Define labels
        Stmt labelThisNotTrue = Jimple.v().newNopStmt();
        Stmt labelBothNotTrue = Jimple.v().newNopStmt();
        Stmt labelCheckParameter = Jimple.v().newNopStmt();
        Stmt labelComputeResult = Jimple.v().newNopStmt();
        // if(this != trueC && parameter1 != trueC) goto label; return trueC; label:
        StaticFieldRef trueC = Jimple.v().newStaticFieldRef(
            Scene.v().makeFieldRef(constraint, "trueC", constraint.getType(), true));
        StaticFieldRef falseC = Jimple.v().newStaticFieldRef(
            Scene.v().makeFieldRef(constraint, "falseC", constraint.getType(), true));
        Local trueLocal = lgen.generateLocal(constraint.getType(), "trueLocal");
        Local falseLocal = lgen.generateLocal(constraint.getType(), "falseLocal");
        units.addLast(Jimple.v().newAssignStmt(trueLocal, trueC));
        units.addLast(Jimple.v().newAssignStmt(falseLocal, falseC));
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(thisLocal, trueLocal), labelThisNotTrue));
        units.addLast(Jimple.v().newReturnStmt(trueLocal));
        units.addLast(labelThisNotTrue);
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(paramLocal, trueLocal), labelBothNotTrue));
        units.addLast(Jimple.v().newReturnStmt(trueLocal));
        units.addLast(labelBothNotTrue);
        // If one of the constraints is false, the result is (a copy of) the other one.
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(thisLocal, falseLocal), 
            labelCheckParameter));
        // this is falseC -- return parameter copy
        units.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newVirtualInvokeExpr(paramLocal,
                    Scene.v().makeMethodRef(constraint, "copy", new LinkedList(), constraint.getType(), false))));
        units.addLast(Jimple.v().newReturnStmt(result));
        // else -- check parameter
        units.addLast(labelCheckParameter);
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(
            paramLocal, falseLocal), labelComputeResult));
        units.addLast(Jimple.v().newReturnStmt(thisLocal));
        // Store this.disjuncts in a local
        units.addLast(labelComputeResult);
        units.addLast(Jimple.v().newAssignStmt(localSet, 
            Jimple.v().newInstanceFieldRef(
                    thisLocal, 
                    Scene.v().makeFieldRef(constraint, "disjuncts", setType, false))));
        // Store the parameter's disjuncts in a local
        units.addLast(Jimple.v().newAssignStmt(remoteSet,
            Jimple.v().newInstanceFieldRef(paramLocal, 
                            Scene.v().makeFieldRef(constraint, "disjuncts", setType, false))));
        parameters.clear();
        RefType collection = RefType.v("java.util.Collection");
        parameters.add(collection);
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(localSet,
            Scene.v().makeMethodRef(setClass, "addAll", parameters, BooleanType.v(), false),
            remoteSet)));
        // return this;
        units.addLast(Jimple.v().newReturnStmt(thisLocal));
    }
    
/*    protected void addConstraintAndMethod(SootClass constraint, SootClass disjunct) {
        RefType setType = RefType.v("java.util.Set");
        SootClass hashSet = Scene.v().getSootClass("java.util.LinkedHashSet");
        
        List parameters = new LinkedList();
        parameters.add(constraint.getType());
        SootMethod andMethod = new SootMethod("and", parameters, constraint.getType(), 
                Modifier.PUBLIC);
        
        constraint.addMethod(andMethod);
        
        // b is the body we're working with
        Body b = Jimple.v().newBody(andMethod);
        andMethod.setActiveBody(b);
        
        // declare locals
        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        Local resultSet = lgen.generateLocal(setType, "resultSet");
        Local localSet = lgen.generateLocal(setType, "localSet");
        Local remoteSet = lgen.generateLocal(setType, "remoteSet");
        Local result = lgen.generateLocal(constraint.getType(), "result");
        Local thisLocal = lgen.generateLocal(constraint.getType(), "this");
        Local paramLocal = lgen.generateLocal(constraint.getType(), "param");
        Local it1 = lgen.generateLocal(RefType.v("java.util.Iterator"), "iterator");
        Local it2 = lgen.generateLocal(RefType.v("java.util.Iterator"), "iterator");
        Local disjunctThis = lgen.generateLocal(disjunct.getType(), "disjunctThis");
        Local disjunctParam = lgen.generateLocal(disjunct.getType(), "disjunctParamm");
        Local disjunctResult = lgen.generateLocal(disjunct.getType(), "disjunctResult");

        Chain units = b.getUnits();
        // Add identity statements for this and parameter locals
        units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(constraint.getType())));
        units.addLast(Jimple.v().newIdentityStmt(paramLocal, 
                Jimple.v().newParameterRef(constraint.getType(), 0)));
        
        Value falseC = Jimple.v().newStaticFieldRef(Scene.v().makeFieldRef(constraint,
                "falseC", constraint.getType(), true));
        Value trueC = Jimple.v().newStaticFieldRef(Scene.v().makeFieldRef(constraint,
                "trueC", constraint.getType(), true));
        
        // If one of the constraints is falseC, the result is falseC
        // Define labels
        Stmt labelBothNotFalse = Jimple.v().newNopStmt();
        Stmt labelCheckParameter = Jimple.v().newNopStmt();
        Stmt labelComputeResult = Jimple.v().newNopStmt();
        // if(this != falseC && parameter1 != falseC) goto label; return falseC; label:
        units.addLast(Jimple.v().
            newIfStmt(Jimple.v().newAndExpr(
                    Jimple.v().newNeExpr(thisLocal, falseC),
                    Jimple.v().newNeExpr(paramLocal, falseC)),
                    labelBothNotFalse));
        units.addLast(Jimple.v().newReturnStmt(falseC));
        units.addLast(labelBothNotFalse);
        // If one of the constraints is true, the result is (a copy of) the other one.
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(thisLocal, trueC), 
                labelCheckParameter));
        // this is trueC -- return parameter copy
        units.addLast(Jimple.v().newReturnStmt(
                Jimple.v().newVirtualInvokeExpr(paramLocal,
                        Scene.v().makeMethodRef(constraint, "copy", new LinkedList(), constraint.getType(), false))));
        // else -- check parameter
        units.addLast(labelCheckParameter);
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(
                paramLocal, trueC), labelComputeResult));
        units.addLast(Jimple.v().newReturnStmt(Jimple.v().newVirtualInvokeExpr(thisLocal, 
                        Scene.v().makeMethodRef(constraint, "copy", new LinkedList(), constraint.getType(), false))));
        
        // Bugger -- need to do the cross-product thing.
        
        // Store this.disjuncts in a local
        units.addLast(Jimple.v().newAssignStmt(localSet, 
                Jimple.v().newInstanceFieldRef(
                        thisLocal, 
                        Scene.v().makeFieldRef(constraint, "disjuncts", setType, false))));
        // Store the parameter's disjuncts in a local
        units.addLast(Jimple.v().newAssignStmt(remoteSet,
                Jimple.v().newInstanceFieldRef(Jimple.v().newParameterRef(constraint.getType(), 1), 
                                Scene.v().makeFieldRef(constraint, "disjuncts", setType, false))));

        // Create a new HashSet for the result, as we're not changing things in-place
        units.addLast(Jimple.v().newAssignStmt(resultSet,
                Jimple.v().newNewExpr(hashSet.getType())));
        // do specialinvoke of constructor
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(resultSet, 
                Scene.v().makeConstructorRef(hashSet, new LinkedList()))));
        
        Type iteratorType = RefType.v("java.util.Iterator");
        SootClass iteratorClass = Scene.v().getSootClass("java.util.Iterator");
        // Get an iterator for this constraint's disjuncts
        units.addLast(Jimple.v().newAssignStmt(it1, Jimple.v().newInterfaceInvokeExpr(
                localSet, Scene.v().makeMethodRef(hashSet, "iterator", new LinkedList(), 
                        iteratorType, false))));
        
        // Have to emulate loops with jumps. Outer loop: while(it1.hasNext()) { ... }
        Stmt labelOuterLoopBegin = Jimple.v().newNopStmt();
        Stmt labelOuterLoopEnd = Jimple.v().newNopStmt();
        Stmt labelInnerLoopBegin = Jimple.v().newNopStmt();
        Stmt labelInnerLoopEnd = Jimple.v().newNopStmt();
        units.addLast(labelOuterLoopBegin);
        // if(!it1.hasNext()) goto labelOuterLoopEnd; <code for outer loop>; <label>:
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newNegExpr(
                Jimple.v().newVirtualInvokeExpr(it1, 
                        Scene.v().makeMethodRef(iteratorClass, "hasNext", new LinkedList(), BooleanType.v(), false))),
                        labelOuterLoopEnd));
        // disjunctThis = (Disjunct)it1.next();
        units.addLast(Jimple.v().newAssignStmt(disjunctThis, 
                Jimple.v().newCastExpr(
                        Jimple.v().newVirtualInvokeExpr(it1, 
                                Scene.v().makeMethodRef(iteratorClass, "next", new LinkedList(), 
                                        RefType.v("java.lang.Object"), false)),
                        disjunct.getType())));
        // it2 = param.disjuncts.iterator();
        units.addLast(Jimple.v().newAssignStmt(it2, Jimple.v().newInterfaceInvokeExpr(
                remoteSet, Scene.v().makeMethodRef(hashSet, "iterator", new LinkedList(),
                        iteratorType, false))));
        // begin inner loop
        units.addLast(labelInnerLoopBegin);
        // if(!it2.hasNext()) goto labelInnerLoopEnd; <code for inner loop>; <label>:
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newNegExpr(
                Jimple.v().newVirtualInvokeExpr(it2, 
                        Scene.v().makeMethodRef(iteratorClass, "hasNext", new LinkedList(), BooleanType.v(), false))),
                        labelInnerLoopEnd));
        // disjunctParam = (Disjunct)it2.next();
        units.addLast(Jimple.v().newAssignStmt(disjunctParam, 
                Jimple.v().newCastExpr(
                        Jimple.v().newVirtualInvokeExpr(it2, 
                                Scene.v().makeMethodRef(iteratorClass, "next", new LinkedList(), 
                                        RefType.v("java.lang.Object"), false)),
                        disjunct.getType())));
        // disjunctResult = disjunctLocal.and(disjunctRemote);
        parameters.clear();
        parameters.add(disjunct.getType());
        units.addLast(Jimple.v().newAssignStmt(disjunctResult,
                Jimple.v().newVirtualInvokeExpr(disjunctThis, 
                        Scene.v().makeMethodRef(disjunct, "and", parameters, disjunct.getType(), false),
                        disjunctParam)));
        // resultSet.add(disjunctResult);
        parameters.clear();
        parameters.add(RefType.v("java.lang.Object"));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(resultSet,
                Scene.v().makeMethodRef(hashSet, "add", parameters, BooleanType.v(), false), disjunctResult)));
        // goto beginning of inner loop
        units.addLast(Jimple.v().newGotoStmt(labelInnerLoopBegin));
        // end of inner loop
        units.addLast(labelInnerLoopEnd);
        // goto beginning of outer loop
        units.addLast(Jimple.v().newGotoStmt(labelOuterLoopBegin));
        // end of outer loop
        units.addLast(labelOuterLoopEnd);
        // now the cross-product of conjunctions of disjuncts is stored in resultSet.
        // We remove the false disjunct, then, if the disjunct set is empty, we return the
        // false constraint falseC, otherwise we return a new constraint with the 
        // appropriate disjunct set.
        parameters.clear();
        parameters.add(RefType.v("java.lang.Object"));
        StaticFieldRef falseD = Jimple.v().newStaticFieldRef(
                Scene.v().makeFieldRef(disjunct, "falseD", disjunct.getType(), true));
        // resultSet.remove(falseD);
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(resultSet,
                Scene.v().makeMethodRef(hashSet, "remove", parameters, BooleanType.v(), false), falseD)));
        Stmt labelReturnFalseC = Jimple.v().newNopStmt();
        // if(resultSet.isEmpty) goto label;
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newInterfaceInvokeExpr(resultSet,
                Scene.v().makeMethodRef(hashSet, "isEmpty", new LinkedList(), BooleanType.v(), false)),
                labelReturnFalseC));
        // Set is nonempty -- construct a new constraint to return
        units.addLast(Jimple.v().newAssignStmt(result, 
                Jimple.v().newNewExpr(constraint.getType())));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(result,
                Scene.v().makeConstructorRef(constraint, new LinkedList()))));
        // result.disjuncts = resultSet;
        units.addLast(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(result,
                    Scene.v().makeFieldRef(constraint, "disjuncts", setType, false)), 
                resultSet));
        // return result;
        units.addLast(Jimple.v().newReturnStmt(result));
        // Label
        units.addLast(labelReturnFalseC);
        // return falseC;
        units.addLast(Jimple.v().newReturnStmt(falseC));
        
    }*/
    
    protected void addConstraintCopyMethod(SootClass constraint, SootClass disjunct) {
        SootClass setClass = Scene.v().getSootClass("java.util.Set");
        RefType setType = RefType.v("java.util.Set");
        SootClass hashSet = Scene.v().getSootClass("java.util.LinkedHashSet");
        
        SootMethod copyMethod = new SootMethod("copy", new LinkedList(), constraint.getType(), 
                Modifier.PUBLIC);
        Body b = Jimple.v().newBody(copyMethod);
        copyMethod.setActiveBody(b);
        
        constraint.addMethod(copyMethod);
        
        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        Local resultSet = lgen.generateLocal(setType, "resultSet");
        Local localSet = lgen.generateLocal(setType, "localSet");
        Local result = lgen.generateLocal(constraint.getType(), "result");
        Local thisLocal = lgen.generateLocal(constraint.getType(), "this");
        
        Value falseC = Jimple.v().newStaticFieldRef(Scene.v().makeFieldRef(constraint,
                "falseC", constraint.getType(), true));
        Value trueC = Jimple.v().newStaticFieldRef(Scene.v().makeFieldRef(constraint,
                "trueC", constraint.getType(), true));
        
        Chain units = b.getUnits();
        // Add identity statements for this local
        units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(constraint.getType())));
        // if this is falseC or trueC, return this -- XXX doesn't sound right at a second reading
        Stmt labelReturnThis = Jimple.v().newNopStmt();
        Local trueLocal = lgen.generateLocal(constraint.getType(), "trueLocal");
        Local falseLocal = lgen.generateLocal(constraint.getType(), "falseLocal");
        units.addLast(Jimple.v().newAssignStmt(trueLocal, trueC));
        units.addLast(Jimple.v().newAssignStmt(falseLocal, falseC));
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(trueLocal, thisLocal),
                labelReturnThis));
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(falseLocal, thisLocal),
                labelReturnThis));
        // Store this.disjuncts in a local
        units.addLast(Jimple.v().newAssignStmt(localSet, 
                Jimple.v().newInstanceFieldRef(
                        thisLocal, 
                        Scene.v().makeFieldRef(constraint, "disjuncts", setType, false))));
        // Create a new Constraint object to store the result in
        units.addLast(Jimple.v().newAssignStmt(result, 
                Jimple.v().newNewExpr(constraint.getType())));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(result,
                Scene.v().makeConstructorRef(constraint, new LinkedList()))));
        // add all disjuncts to resultSet -- disjunctst has been initialised in the constructor
        units.addLast(Jimple.v().newAssignStmt(resultSet, Jimple.v().newInstanceFieldRef(result, 
                Scene.v().makeFieldRef(constraint, "disjuncts", setType, false))));
        List parameters = new LinkedList();
        parameters.add(RefType.v("java.util.Collection"));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(resultSet,
                Scene.v().makeMethodRef(setClass, "addAll", parameters, BooleanType.v(), false),
                localSet)));
        // return result;
        units.addLast(Jimple.v().newReturnStmt(result));
        // if this is trueC or falseC, return this
        units.addLast(labelReturnThis);
        units.addLast(Jimple.v().newReturnStmt(thisLocal));
        
    }
    
    protected void addConstraintInitialiser(SootClass constraint) {
        SootMethod init = new SootMethod(SootMethod.constructorName, new LinkedList(), 
                VoidType.v(), Modifier.PUBLIC);
        Body b = Jimple.v().newBody(init);
        init.setActiveBody(b);
        constraint.addMethod(init);

        SootClass hashSet = Scene.v().getSootClass("java.util.LinkedHashSet");

        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        Local tempSet = lgen.generateLocal(hashSet.getType(), "tempSet");
        Local thisLocal = lgen.generateLocal(constraint.getType(), "thisLocal");
        Chain units = b.getUnits();

        units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(constraint.getType())));

        // call super()
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(thisLocal,
                Scene.v().makeConstructorRef(Scene.v().getSootClass("java.lang.Object"),
                        new LinkedList()))));

        units.addLast( Jimple.v().newAssignStmt(tempSet, 
                Jimple.v().newNewExpr(hashSet.getType())));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(tempSet, 
                Scene.v().makeConstructorRef(hashSet, new LinkedList()))));
        units.addLast(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(thisLocal,
                Scene.v().makeFieldRef(constraint, "disjuncts", RefType.v("java.util.Set"), false)),
                tempSet));
        units.addLast(Jimple.v().newReturnVoidStmt());
    }
    
    protected void addConstraintStaticInitialiser(SootClass constraint, SootClass disjunct) {
        SootMethod clinit = new SootMethod(SootMethod.staticInitializerName, new LinkedList(), 
                VoidType.v(), Modifier.STATIC);
        Body b = Jimple.v().newBody(clinit);
        clinit.setActiveBody(b);
        constraint.addMethod(clinit);

        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        Local tempConstraint = lgen.generateLocal(constraint.getType(), "tempConstraint");
        // trueC = new Constraint();
        Chain units = b.getUnits();
        StaticFieldRef trueConstraintField = Jimple.v().newStaticFieldRef(
                Scene.v().makeFieldRef(constraint, "trueC", constraint.getType(), true));
        StaticFieldRef falseConstraintField = Jimple.v().newStaticFieldRef(
                Scene.v().makeFieldRef(constraint, "falseC", constraint.getType(), true));
        units.addLast( Jimple.v().newAssignStmt(tempConstraint, 
                Jimple.v().newNewExpr(constraint.getType())));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(tempConstraint, 
                Scene.v().makeConstructorRef(constraint, new LinkedList()))));
        
        // trueC should contain an empty disjunct
        Local disjuncts = lgen.generateLocal(RefType.v("java.util.Set"), "trueCDisjuncts");
        Local emptyDisjunct = lgen.generateLocal(disjunct.getType(), "emptyDisjunct");
        units.addLast(Jimple.v().newAssignStmt(emptyDisjunct, Jimple.v().newNewExpr(disjunct.getType())));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(emptyDisjunct,
                Scene.v().makeConstructorRef(disjunct, new LinkedList()))));
        units.addLast(Jimple.v().newAssignStmt(disjuncts, Jimple.v().newInstanceFieldRef(tempConstraint,
                Scene.v().makeFieldRef(constraint, "disjuncts", RefType.v("java.util.Set"), false))));
        List parameters = new LinkedList();
        parameters.add(RefType.v("java.lang.Object"));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(disjuncts,
                Scene.v().makeMethodRef(Scene.v().getSootClass("java.util.Set"), "add", parameters,
                        BooleanType.v(), false), emptyDisjunct)));
        units.addLast(Jimple.v().newAssignStmt(trueConstraintField, tempConstraint));

        units.addLast(Jimple.v().newAssignStmt(tempConstraint, 
                Jimple.v().newNewExpr(constraint.getType())));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(tempConstraint, 
                Scene.v().makeConstructorRef(constraint, new LinkedList()))));
        units.addLast(Jimple.v().newAssignStmt(falseConstraintField, tempConstraint));
        units.addLast(Jimple.v().newReturnVoidStmt());
    }
    
    protected void addConstraintGetDisjunctArrayMethod(SootClass constraint, SootClass disjunct) {
        Type arrayType =  ArrayType.v(disjunct.getType(), 1);
        RefType setType = RefType.v("java.util.Set");
        
        SootMethod getIterator = new SootMethod("getDisjunctArray", new LinkedList(), 
                arrayType, Modifier.PUBLIC);
        Body b = Jimple.v().newBody(getIterator);
        getIterator.setActiveBody(b);
        constraint.addMethod(getIterator);
        
        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        Local set = lgen.generateLocal(setType, "result");
        Local thisLocal = lgen.generateLocal(constraint.getType(), "this");
        
        Chain units = b.getUnits();
        // Add identity statements for this local
        units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(constraint.getType())));
        
        // store the disjunct set in a local
        units.addLast(Jimple.v().newAssignStmt(set, Jimple.v().newInstanceFieldRef(thisLocal, 
                Scene.v().makeFieldRef(constraint, "disjuncts", setType, false))));
        
        // return set.toArray();
        Local arrayLocal = lgen.generateLocal(ArrayType.v(disjunct.getType(), 1), "array");
        units.addLast(Jimple.v().newAssignStmt(arrayLocal, Jimple.v().newInterfaceInvokeExpr(set, 
                Scene.v().makeMethodRef(Scene.v().getSootClass("java.util.Set"), "toArray",
                        new LinkedList(), arrayType, false))));
        units.addLast(Jimple.v().newReturnStmt(arrayLocal));
    }
    
    /**
     * Fills in the disjunct class, adding fields and methods.
     * @param tm the tracematch this class should be specialised to
     * @param disjunct the SootClass -- already created.
     */
    protected void fillInDisjunctClass(TraceMatch tm, SootClass disjunct) {
        // add the fields to the class
        SootField curField = new SootField("trueD", disjunct.getType(), Modifier.PUBLIC |
                Modifier.FINAL | Modifier.STATIC);
        disjunct.addField(curField);
        curField = new SootField("falseD", disjunct.getType(), Modifier.PUBLIC |
                Modifier.FINAL | Modifier.STATIC);
        disjunct.addField(curField);
        // the remaining fields depend on the tracematch declared variables -- for each
        // variable X, we want a field 'Object var$X', a field 'boolean X$isBound', a field
        // 'Set not$X' (initialised in the <clinit>) and a field 'boolean X$isWeak'.
        RefType objectType = RefType.v("java.lang.Object");
        RefType setType = RefType.v("java.util.Set");
        String varName;
        List varNames = tm.getFormalNames();
        Iterator varIt = varNames.iterator();
        while(varIt.hasNext()) {
            varName = (String)varIt.next();
            curField = new SootField("var$" + varName, objectType,
                    Modifier.PUBLIC);
            disjunct.addField(curField);
            curField = new SootField(varName + "$isBound", BooleanType.v(),
                    Modifier.PUBLIC);
            disjunct.addField(curField);
            curField = new SootField("not$" + varName, setType,
                    Modifier.PUBLIC);
            disjunct.addField(curField);
            curField = new SootField(varName + "$isWeak", BooleanType.v(),
                    Modifier.PUBLIC);
            disjunct.addField(curField);
            
            // the following method(s) are needed by addNegativeBindingsForSymbol()
            addDisjunctAddNegBindingForVariable(varName, disjunct, myWeakRef);
        }
        addDisjunctEqualsMethod(disjunct, varNames);
        addDisjunctHashCodeMethod(disjunct, varNames);
        
        
        // now -- where the bulk of the work happens, addBindingsForSymbolX and the state-
        // specific versions, addBindingsForSymbolXInState<Number>, also negative bindings-
        // versions of these.
        addDisjunctAddBindingsForSymbolMethods(tm, disjunct, myWeakRef);
        addDisjunctAddNegBindingsForSymbolMethods(tm, disjunct, myWeakRef);
        
        // We need a copy() method..
        addDisjunctCopyMethod(tm, disjunct);
        
        // The variable getters
        addDisjunctGetVarMethods(tm, disjunct, myWeakRef);
        
        addDisjunctInitialiser(tm, disjunct);
        
        addDisjunctStaticInitialiser(disjunct);
        
        addDisjunctValidateDisjunctMethod(tm, disjunct);
    }
    
    protected void addDisjunctEqualsMethod(SootClass disjunct, List varNames) {
        ///////////// Disjunct.equals() method ///////////////////////////////////////
        /* This method is very important, as the default Set implementations contain elements
         * unique with respect to the equals() method. Thus, with a sensible implementation of
         * this method, we don't need to worry about removing duplicate disjuncts from the
         * disjunct sets in the constraint() class.
         * 
         * Two disjuncts are considered equal iff either they are the same object or, for every
         * variable X, either both bind X to the same value or neither binds X, and the set of
         * negative bindings for X is the same.
         */
        RefType objectType = RefType.v("java.lang.Object");
        RefType setType = RefType.v("java.util.Set");
        List singleObjectParameter = new LinkedList();
        singleObjectParameter.add(objectType);
        SootMethod equalsMethod = new SootMethod("equals", singleObjectParameter, BooleanType.v(), Modifier.PUBLIC);
        Body b = Jimple.v().newBody(equalsMethod);
        equalsMethod.setActiveBody(b);
        disjunct.addMethod(equalsMethod);
        
        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        Local thisLocal = lgen.generateLocal(disjunct.getType(), "thisLocal");
        Local compareTo = lgen.generateLocal(RefType.v("java.lang.Object"), "compareTo");
        Local compareToDisjunct = lgen.generateLocal(disjunct.getType(), "compareToDisjunct");
        Local localSet = lgen.generateLocal(setType, "localSet");
        Local remoteSet = lgen.generateLocal(setType, "remoteSet");
        Local booleanLocal = lgen.generateLocal(BooleanType.v(), "booleanLocal");
        
        // first things first -- identity statements
        Chain units = b.getUnits();
        units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(disjunct.getType())));
        units.addLast(Jimple.v().newIdentityStmt(compareTo, Jimple.v().newParameterRef(
                objectType, 0)));
        
        // label to jump to as soon as we know we can return false (or true)
        Stmt labelReturnFalse = Jimple.v().newNopStmt();
        Stmt labelReturnTrue = Jimple.v().newNopStmt();
        
        // if the type is wrong, return false
        units.addLast(Jimple.v().newAssignStmt(booleanLocal, 
                Jimple.v().newInstanceOfExpr(compareTo, disjunct.getType())));
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(booleanLocal, 
                IntConstant.v(1)), labelReturnFalse));
        
        // if the objects are identical, return true
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(thisLocal, compareTo), 
                labelReturnTrue));
        
        // cast to the right type for further comparisons
        units.addLast(Jimple.v().newAssignStmt(compareToDisjunct, Jimple.v().newCastExpr(
                compareTo, disjunct.getType())));
        
        // if either this or the parameter is trueD or falseD (or however true and false disjuncts
        // are flagged) then we can probably return false here... think about it.
        
        // for now, just check the two disjuncts agree on all variables
        Iterator varIt = varNames.iterator();
        while(varIt.hasNext()) {
            String varName = (String)varIt.next();
            // if both bind the variable but differ in the value they assign to it, return false.
            Stmt labelCheckNegSets = Jimple.v().newNopStmt();
            Local thisBound = lgen.generateLocal(BooleanType.v(), "thisBound");
            Local paramBound = lgen.generateLocal(BooleanType.v(), "paramBound");
            Local varThis = lgen.generateLocal(objectType, "varThis");
            Local varParam = lgen.generateLocal(objectType, "varParam");
            Local weakRef = lgen.generateLocal(myWeakRef.getType(), "weakRef");
          
            units.addLast(Jimple.v().newAssignStmt(varThis, Jimple.v().newInstanceFieldRef(thisLocal,
                    Scene.v().makeFieldRef(disjunct, "var$" + varName, objectType, false))));
            units.addLast(Jimple.v().newAssignStmt(varParam, Jimple.v().newInstanceFieldRef(compareToDisjunct,
                    Scene.v().makeFieldRef(disjunct, "var$" + varName, objectType, false))));

            units.addLast(Jimple.v().newAssignStmt(thisBound, Jimple.v().newInstanceFieldRef(thisLocal,
                    Scene.v().makeFieldRef(disjunct, varName + "$isBound", BooleanType.v(), false))));
            units.addLast(Jimple.v().newAssignStmt(paramBound, Jimple.v().newInstanceFieldRef(compareToDisjunct,
                    Scene.v().makeFieldRef(disjunct, varName + "$isBound", BooleanType.v(), false))));
            
            // If this and the parameter differ in the binding state of the current variable, they aren't equal.
            units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(thisBound, paramBound),
                    labelReturnFalse));
            
            // Otherwise, if thisBound == false (and so paramBound == false too), compare the neg binding sets
            units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(thisBound, IntConstant.v(1)),
                    labelCheckNegSets));

            // If we fall through here, both disjuncts bind the current variable.
            // If either varThis or varParam is a weak reference, we need to use that weak reference's .equals() 
            // method to check for equality... otherwise we're interested in reference equality (this relies
            // on the fact that MyWeakReference.equals() *is* reference equality for the underlying referents).
            Stmt labelCheckParamIsWeak = Jimple.v().newNopStmt();
            Stmt labelCheckStrongEquality = Jimple.v().newNopStmt();
            Stmt labelCheckNextVar = Jimple.v().newNopStmt();
            
            // if the varThis reference is weak, store it in weakRef and check for weak equality
            Local thisWeak = lgen.generateLocal(BooleanType.v(), "thisWeak");
            units.addLast(Jimple.v().newAssignStmt(thisWeak, Jimple.v().newInstanceFieldRef(thisLocal,
                    Scene.v().makeFieldRef(disjunct, varName + "$isWeak", BooleanType.v(), false))));
            units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(thisWeak, IntConstant.v(1)), labelCheckParamIsWeak));
            units.addLast(Jimple.v().newAssignStmt(weakRef, Jimple.v().newCastExpr(varThis, myWeakRef.getType())));
            units.addLast(Jimple.v().newAssignStmt(booleanLocal, Jimple.v().newVirtualInvokeExpr(weakRef, 
            		Scene.v().makeMethodRef(myWeakRef, "equals", singleObjectParameter, BooleanType.v(), false),
            		varParam)));
            units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(booleanLocal, IntConstant.v(1)), labelReturnFalse));
            units.addLast(Jimple.v().newGotoStmt(labelCheckNextVar));
            
            // if the varParam reference is weak, store it in weakRef and check for weak equality
            units.addLast(labelCheckParamIsWeak);
            Local paramWeak = lgen.generateLocal(BooleanType.v(), "paramWeak");
            units.addLast(Jimple.v().newAssignStmt(paramWeak, Jimple.v().newInstanceFieldRef(compareToDisjunct,
                    Scene.v().makeFieldRef(disjunct, varName + "$isWeak", BooleanType.v(), false))));
            units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(paramWeak, IntConstant.v(1)), labelCheckStrongEquality));
            units.addLast(Jimple.v().newAssignStmt(weakRef, Jimple.v().newCastExpr(varParam, myWeakRef.getType())));
            units.addLast(Jimple.v().newAssignStmt(booleanLocal, Jimple.v().newVirtualInvokeExpr(weakRef, 
            		Scene.v().makeMethodRef(myWeakRef, "equals", singleObjectParameter, BooleanType.v(), false),
            		varThis)));
            units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(booleanLocal, IntConstant.v(1)), labelReturnFalse));
            units.addLast(Jimple.v().newGotoStmt(labelCheckNextVar));
            
            // if neither binding is weak, we just check reference equality
            units.addLast(labelCheckStrongEquality);
            units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(varThis, varParam),
                    labelReturnFalse));
            units.addLast(Jimple.v().newGotoStmt(labelCheckNextVar)); // don't check sets -- they're null

            units.addLast(labelCheckNegSets);
            // if (!this.not_X.equals(parameter.not_X)) return false;
            // need to store both sets in locals before making the method call
            units.addLast(Jimple.v().newAssignStmt(localSet, 
                    Jimple.v().newInstanceFieldRef(thisLocal, 
                            Scene.v().makeFieldRef(disjunct, "not$" + varName, setType, false))));
            units.addLast(Jimple.v().newAssignStmt(remoteSet, 
                    Jimple.v().newInstanceFieldRef(compareToDisjunct, 
                            Scene.v().makeFieldRef(disjunct, "not$" + varName, setType, false))));
            booleanLocal = lgen.generateLocal(BooleanType.v(), "booleanLocal");
            units.addLast(Jimple.v().newAssignStmt(booleanLocal, Jimple.v().newInterfaceInvokeExpr(localSet,
                    Scene.v().makeMethodRef(Scene.v().getSootClass("java.util.Set"),
                            "equals", singleObjectParameter, BooleanType.v(), false),
                    remoteSet)));
            units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(booleanLocal, IntConstant.v(1)),
                    labelReturnFalse));
            
            units.addLast(labelCheckNextVar);
        }
        // We have now checked all variables -- if we haven't jumped to labelReturnFalse, return true
        units.addLast(labelReturnTrue);
        units.addLast(Jimple.v().newReturnStmt(IntConstant.v(1)));
        
        // now for the unfinished business
        units.addLast(labelReturnFalse);
        units.addLast(Jimple.v().newReturnStmt(IntConstant.v(0)));  
    }
    
    protected void addDisjunctHashCodeMethod(SootClass disjunct, List varNames) {
        ///////////// Disjunct.hashCode() method ///////////////////////////////////////
        /* This method is very important, as the default hashCode() method doesn't fulfill its contract
         * with the modified Disjunct.equals() method, and inconsistent hashCodes may bugger up the
         * behaviour of HashSets and other things relying on them.
         * 
         * A hash code for the disjunct is obtained by adding up the hash codes of all bound variables and
         * all negative bindings sets for unbound variables, then taking the hashCode of that.
         */
        RefType objectType = RefType.v("java.lang.Object");
        RefType setType = RefType.v("java.util.Set");
        SootMethod hashCodeMethod = new SootMethod("hashCode", new LinkedList(), IntType.v(), Modifier.PUBLIC);
        Body b = Jimple.v().newBody(hashCodeMethod);
        hashCodeMethod.setActiveBody(b);
        disjunct.addMethod(hashCodeMethod);
        
        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        Local thisLocal = lgen.generateLocal(disjunct.getType(), "thisLocal");
        Local curVar = lgen.generateLocal(objectType, "curVar");
        Local curSet = lgen.generateLocal(setType, "curSet");
        Local result = lgen.generateLocal(IntType.v(), "result");
        Local tmpHash = lgen.generateLocal(IntType.v(), "tmpHash");
        Local tmpBool = lgen.generateLocal(BooleanType.v(), "tmpBool");
        
        // first things first -- identity statements
        Chain units = b.getUnits();
        units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(disjunct.getType())));
        
        // result = 0;
        units.addLast(Jimple.v().newAssignStmt(result, IntConstant.v(0)));
        
        // for now, just check the two disjuncts agree on all variables
        Iterator varIt = varNames.iterator();
        while(varIt.hasNext()) {
            String varName = (String)varIt.next();
            // if this variable is bound, add its hash code to the result
            Stmt labelVarNotBound = Jimple.v().newNopStmt();
            Stmt labelAddToResult = Jimple.v().newNopStmt();
            units.addLast(Jimple.v().newAssignStmt(tmpBool, Jimple.v().newInstanceFieldRef(thisLocal, 
            		Scene.v().makeFieldRef(disjunct, varName + "$isBound", BooleanType.v(), false))));
            units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(tmpBool, IntConstant.v(1)), labelVarNotBound));
            units.addLast(Jimple.v().newAssignStmt(curVar, Jimple.v().newInstanceFieldRef(thisLocal, 
            		Scene.v().makeFieldRef(disjunct, "var$" + varName, objectType, false))));
            units.addLast(Jimple.v().newAssignStmt(tmpHash, Jimple.v().newVirtualInvokeExpr(curVar, 
            		Scene.v().makeMethodRef(Scene.v().getSootClass("java.lang.Object"), "hashCode", new LinkedList(),
            				IntType.v(), false))));
            units.addLast(Jimple.v().newGotoStmt(labelAddToResult));
            
            // else add the hash code of the negative binding set to the result
            units.addLast(labelVarNotBound);
            units.addLast(Jimple.v().newAssignStmt(curSet, Jimple.v().newInstanceFieldRef(thisLocal,
            		Scene.v().makeFieldRef(disjunct, "not$" + varName, setType, false))));
            units.addLast(Jimple.v().newAssignStmt(tmpHash, Jimple.v().newVirtualInvokeExpr(curSet, 
            		Scene.v().makeMethodRef(Scene.v().getSootClass("java.lang.Object"), "hashCode", new LinkedList(),
            				IntType.v(), false))));
            
            // do the addition
            units.addLast(labelAddToResult);
            units.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newAddExpr(result, tmpHash)));
        }

        units.addLast(Jimple.v().newReturnStmt(result));
    }
    
    protected void addDisjunctAddBindingsForSymbolMethods(TraceMatch tm, SootClass disjunct, SootClass myWeakRef) {
        RefType objectType = RefType.v("java.lang.Object");
        RefType setType = RefType.v("java.util.Set");
        List singleObjectParameter = new LinkedList();
        singleObjectParameter.add(objectType);
        Iterator symbolIt = tm.getSym_to_vars().keySet().iterator();
        while(symbolIt.hasNext()) {
            String symbolName = (String)symbolIt.next();

            List variableList = tm.getVariableOrder(symbolName);
            List parameters = new LinkedList();
            parameters.add(IntType.v()); // state number of the state where the transition originates
            parameters.add(IntType.v()); // state number of the state where the transition ends
            
            /////////// handling for variable-less symbols/tracematches
            // if this symbol has no vars -- return this
            if(variableList.isEmpty()) { 
                addReturnThisMethod(disjunct, "addBindingsForSymbol" + symbolName, parameters);
                continue;
            }

            for(int i = 0; i < variableList.size(); i++) parameters.add(objectType);
            SootMethod symbolMethod = new SootMethod("addBindingsForSymbol" + symbolName,
                    parameters, disjunct.getType(), Modifier.PUBLIC);
            Body b = Jimple.v().newBody(symbolMethod);
            symbolMethod.setActiveBody(b);
            disjunct.addMethod(symbolMethod);
            
            LocalGeneratorEx lgen = new LocalGeneratorEx(b);
            Local thisLocal = lgen.generateLocal(disjunct.getType(), "thisLocal");
            Local stateNumberFrom = lgen.generateLocal(IntType.v(), "stateNumberFrom");
            Local stateNumberTo = lgen.generateLocal(IntType.v(), "stateNumberTo");
            Local curVarNegBindings = lgen.generateLocal(setType, "curNegBindings");
            Local result = lgen.generateLocal(disjunct.getType(), "result");
            Local varLocal = lgen.generateLocal(objectType, "varLocal");
            
            Chain units = b.getUnits();
            units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(disjunct.getType())));
            units.addLast(Jimple.v().newIdentityStmt(stateNumberFrom,
                    Jimple.v().newParameterRef(IntType.v(), 0)));
            units.addLast(Jimple.v().newIdentityStmt(stateNumberTo,
                    Jimple.v().newParameterRef(IntType.v(), 1)));
            
            List variableLocalsList = new LinkedList();
            int parameterIndex = 2;
            Iterator varIt = variableList.iterator();
            while(varIt.hasNext()) {
                String varName = (String)varIt.next();
                // add local for this variable and identity statement to store the parameter
                Local curVar = lgen.generateLocal(objectType, varName);
                units.addLast(Jimple.v().newIdentityStmt(curVar, 
                        Jimple.v().newParameterRef(objectType, parameterIndex)));
                variableLocalsList.add(curVar);
                parameterIndex++;
            }
            
            // Label needed later -- return this;
            Stmt labelReturnThis = Jimple.v().newNopStmt();
            Stmt labelPrintXReturnThis = Jimple.v().newNopStmt();
            // label to jump to if new bindings are incompatible
            Stmt labelReturnFalse = Jimple.v().newNopStmt();
            Stmt labelPrintStarReturnFalse = Jimple.v().newNopStmt();
            
            // now we have all the locals. Generate the code for this method.
            varIt = variableList.iterator();
            Iterator localIt = variableLocalsList.iterator();
            while(varIt.hasNext()) {
                String varName = (String)varIt.next();
                Local curVar = (Local)localIt.next();
                Stmt labelCurVarNotBound = Jimple.v().newNopStmt();
                Stmt labelCheckNextVar = Jimple.v().newNopStmt();
                
                Local curVarBound = lgen.generateLocal(BooleanType.v(), "curVarBound");
                units.addLast(Jimple.v().newAssignStmt(curVarBound, Jimple.v().newInstanceFieldRef(thisLocal, 
                                Scene.v().makeFieldRef(disjunct, varName + "$isBound", BooleanType.v(), false))));
                units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(curVarBound, IntConstant.v(1)),
                        labelCurVarNotBound));
                // TODO -- implicit assumption that the name and variable lists are ordered identically.
                // make sure it's valid.
                Local curThisVar = lgen.generateLocal(objectType, "curThisVar");
                units.addLast(Jimple.v().newAssignStmt(curThisVar, Jimple.v().newVirtualInvokeExpr(thisLocal, 
                                Scene.v().makeMethodRef(disjunct, "get$" + varName, new LinkedList(),
                                        objectType, false))));
                units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(curThisVar,
                        curVar), labelReturnFalse));
                // If the variable is bound, we don't keep negative bindings for it, so skip the check
                units.addLast(Jimple.v().newGotoStmt(labelCheckNextVar));
                units.addLast(labelCurVarNotBound);
                // if(this.not_var.contains(new MyWeakRef(var1))) return false;
                // XXX Note that this relies on MyWeakRef.equals() returning true if it has a weak
                // reference to var1.
                units.addLast(Jimple.v().newAssignStmt(curVarNegBindings,
                        Jimple.v().newInstanceFieldRef(thisLocal, 
                                Scene.v().makeFieldRef(disjunct, "not$" + varName, setType, false))));
                Local booleanLocal = lgen.generateLocal(BooleanType.v(), "booleanLocal");
                Local weakRef = getWeakRefLocal(b, curVar, myWeakRef);
                units.addLast(Jimple.v().newAssignStmt(booleanLocal, Jimple.v().newInterfaceInvokeExpr(curVarNegBindings,
                        Scene.v().makeMethodRef(Scene.v().getSootClass("java.util.Set"), "contains",
                                singleObjectParameter, BooleanType.v(), false), weakRef)));
                units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(booleanLocal, IntConstant.v(1)), 
                        labelReturnFalse));
                units.addLast(labelCheckNextVar);
            }
            
            // OK, if we fall through here then this disjunct is compatible with the new bindings,
            // otherwise we would have jumped to labelReturnFalse. Now dispatch to the state-
            // specialised method that records the bindings with weak or strong references.
            
            // to construct the LookupSwitch, we need two lists of equal length -- a list of values
            // to compare to (those will be state numbers, 0 .. (#states - 1)) and a list of labels
            // to jump to.
            Iterator stateIt = ((TMStateMachine)tm.getState_machine()).getStateIterator();
            List switchValues = new LinkedList();
            List labels = new LinkedList();
            while(stateIt.hasNext()) {
                switchValues.add(IntConstant.v(((SMNode)stateIt.next()).getNumber()));
                labels.add(Jimple.v().newNopStmt());
            }
            // if the number passed is none of the state numbers, use this as the default jump
            Stmt labelThrowException = Jimple.v().newNopStmt();
            // the switch:
            units.addLast(Jimple.v().newLookupSwitchStmt(stateNumberTo, switchValues, labels, labelThrowException));
            
            // we now need to generate a call to the correct method.
            // Change of plan: inline the methods here. Shouldn't be an issue unless we hit the
            // size limit for methods, which is unlikely -- the code below grows in proportion to
            // the product of number of states and number of variables bound by the current
            // symbol, so it *may* be an issue with exceedingly complex regexes and symbols...
            // If it is, split it up into specialised addBindingsForSymbolXInState<Number> methods.
            // TODO: Best solution would be if the method we generate has more than 65K bytecodes
            // and go back to regenerate it differently if it does. Not sure if it's feasible though.
            Iterator labelIt = labels.iterator();
            stateIt = ((TMStateMachine)tm.getState_machine()).getStateIterator();
            while(stateIt.hasNext()) {
                SMNode curState = (SMNode)stateIt.next();
                Stmt curLabel = (Stmt)labelIt.next();
                units.addLast(curLabel);
                
                returnThisIfPossible(tm, curState, symbolName, units, stateNumberFrom, labelPrintXReturnThis);
                
                // result = copy();
                units.addLast(Jimple.v().newAssignStmt(result, 
                        Jimple.v().newVirtualInvokeExpr(thisLocal, 
                                Scene.v().makeMethodRef(disjunct, "copy", new LinkedList(), 
                                        disjunct.getType(), false))));
                // for each variable..
                varIt = variableList.iterator();
                // IMPORTANT ASSUMPTION -- variableLocalsList is iterated in the order in which
                // elements are added and variableList is iterated in the same order as last time,
                // so that the correspondence between variable name and local variable storing 
                // the relevant parameter is preserved.
                localIt = variableLocalsList.iterator();
                while(varIt.hasNext()) {
                    String varName = (String)varIt.next();
                    Local curVar = (Local)localIt.next();
                    // result.var$isBound = true;
                    units.addLast(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(result,
                            Scene.v().makeFieldRef(disjunct, varName + "$isBound", BooleanType.v(),
                                    false)),
                            IntConstant.v(1)));
                    if(curState.needStrongRefs.contains(varName)) {
                        // result.var$isWeak = false;
                        units.addLast(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(result,
                                Scene.v().makeFieldRef(disjunct, varName + "$isWeak", BooleanType.v(),
                                        false)),
                                IntConstant.v(0)));
                        // result.var$var = var;
                        units.addLast(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(result,
                                Scene.v().makeFieldRef(disjunct, "var$" + varName, objectType,
                                        false)),
                                curVar));
                    } else {
                        // result.var$isWeak = true;
                        units.addLast(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(result,
                                Scene.v().makeFieldRef(disjunct, varName + "$isWeak", BooleanType.v(),
                                        false)),
                                IntConstant.v(1)));
                        // result.var$var = new MyWeakRef(var);
                        Local weakRef = getWeakRefLocal(b, curVar, myWeakRef);
                        units.addLast(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(result,
                                Scene.v().makeFieldRef(disjunct, "var$" + varName, objectType,
                                        false)),
                                weakRef));
                    }
                    // result.not$var = null; -- don't need to keep negative bindings for bound vars
                    units.addLast(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(result,
                            Scene.v().makeFieldRef(disjunct, "not$" + varName, setType,
                                    false)),
                            NullConstant.v()));
                }
                // now all the new bindings are recorded -- return the result
                units.addLast(Jimple.v().newReturnStmt(result));
            }
            // unfinished business -- still have some labels we haven't inserted:
            units.addLast(labelPrintStarReturnFalse);
            printString(b, "*");
            
            units.addLast(labelReturnFalse);
            
            Local falseDisjunct = lgen.generateLocal(disjunct.getType(), "falseDisjunct");
            units.addLast(Jimple.v().newAssignStmt(falseDisjunct, Jimple.v().newStaticFieldRef(
                    Scene.v().makeFieldRef(disjunct, "falseD", disjunct.getType(), true))));
            units.addLast(Jimple.v().newReturnStmt(falseDisjunct));
            
            
            units.addLast(labelPrintXReturnThis);
            printString(b, "x");
            
            units.addLast(labelReturnThis);
            units.addLast(Jimple.v().newReturnStmt(thisLocal));
            
            
            // we really shouldn't need this label -- it's there for the default jump of the switch.
            // For now, it just returns false.
            units.addLast(labelThrowException);
            
            // throw new RuntimeException("AddDisjunctAddBindings got invalid state number N");
            throwException(b, "AddDisjunctAddBindings got invalid state number " + stateNumberTo);
        }
    }
    
    protected void addDisjunctAddNegBindingForVariable(String varName, SootClass disjunct, SootClass myWeakRef) {
        RefType objectType = RefType.v("java.lang.Object");
        RefType setType = RefType.v("java.util.Set");
    	List singleObjectParameter = new LinkedList();
    	singleObjectParameter.add(objectType);
    	SootMethod varMethod = new SootMethod("addNegativeBindingForVariable" + varName,
    			singleObjectParameter, disjunct.getType(), Modifier.PUBLIC);
    	Body b = Jimple.v().newBody(varMethod);
    	varMethod.setActiveBody(b);
    	disjunct.addMethod(varMethod);
    	
    	LocalGeneratorEx lgen = new LocalGeneratorEx(b);
    	Local thisLocal = lgen.generateLocal(disjunct.getType(), "thisLocal");
    	Local paramLocal = lgen.generateLocal(objectType, "paramLocal");
    	Local curVar = lgen.generateLocal(objectType, "curVar");
    	Local curVarNegBindings = lgen.generateLocal(setType, "curNegBindings");
    	Local result = lgen.generateLocal(disjunct.getType());
    	Local booleanLocal = lgen.generateLocal(BooleanType.v(), "booleanLocal");
    	
    	Chain units = b.getUnits();
    	units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(disjunct.getType())));
    	units.addLast(Jimple.v().newIdentityStmt(paramLocal, Jimple.v().newParameterRef(objectType, 0)));
    	
    	// We're trying to add a negative binding for a single variable to the current disjunct. There are
    	// three cases:
    	// 1. The variable is bound to the same value as the negative binding -- return falseD.
    	// 2. The variable is bound to a different value -- return this.copy().
    	// 3. The variable is not bound -- return this.copy().not$var.add(new WeakRef(paramLocal)).
    	Stmt labelReturnFalse = Jimple.v().newNopStmt();
    	Stmt labelVarNotBound = Jimple.v().newNopStmt();
    	
    	units.addLast(Jimple.v().newAssignStmt(booleanLocal, Jimple.v().newInstanceFieldRef(thisLocal,
    			Scene.v().makeFieldRef(disjunct, varName + "$isBound", BooleanType.v(), false))));
    	units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(booleanLocal, IntConstant.v(1)), labelVarNotBound));
    	
    	// variable is bound
    	units.addLast(Jimple.v().newAssignStmt(curVar, Jimple.v().newVirtualInvokeExpr(thisLocal,
    			Scene.v().makeMethodRef(disjunct, "get$" + varName, new LinkedList(), objectType, false))));
    	units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(curVar, paramLocal), labelReturnFalse));
    	
    	// variable bound to a different value -- return this.copy();
    	units.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newVirtualInvokeExpr(thisLocal, 
    			Scene.v().makeMethodRef(disjunct, "copy", new LinkedList(), disjunct.getType(), false))));
    	units.addLast(Jimple.v().newReturnStmt(result));
    	
    	// Handle the case of the variable not being bound
    	units.addLast(labelVarNotBound);
    	units.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newVirtualInvokeExpr(thisLocal, 
    			Scene.v().makeMethodRef(disjunct, "copy", new LinkedList(), disjunct.getType(), false))));
    	units.addLast(Jimple.v().newAssignStmt(curVarNegBindings, Jimple.v().newInstanceFieldRef(result,
    			Scene.v().makeFieldRef(disjunct, "not$" + varName, setType, false))));
    	Local weakRef = getWeakRefLocal(b, paramLocal, myWeakRef);
    	units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(curVarNegBindings,
    			Scene.v().makeMethodRef(Scene.v().getSootClass("java.util.Set"), "add", singleObjectParameter, 
    					BooleanType.v(), false),
    			weakRef)));
    	units.addLast(Jimple.v().newReturnStmt(result));
    	
    	// Final case -- return false
    	units.addLast(labelReturnFalse);
        Local falseDisjunct = lgen.generateLocal(disjunct.getType(), "falseDisjunct");
        units.addLast(Jimple.v().newAssignStmt(falseDisjunct, Jimple.v().newStaticFieldRef(
                Scene.v().makeFieldRef(disjunct, "falseD", disjunct.getType(), true))));
        units.addLast(Jimple.v().newReturnStmt(falseDisjunct));
    	
    }
    
    protected void addDisjunctAddNegBindingsForSymbolMethods(TraceMatch tm, SootClass disjunct, SootClass myWeakRef) {
    	boolean returnSet = false; // do we need to return a set, or can we return just a single disjunct?
        RefType objectType = RefType.v("java.lang.Object");
        RefType setType = RefType.v("java.util.Set");
        RefType linkedHashSetType = RefType.v("java.util.LinkedHashSet");
        SootClass linkedHashSet = Scene.v().getSootClass("java.util.LinkedHashSet");
        List singleObjectParameter = new LinkedList();
        singleObjectParameter.add(objectType);
        Iterator symbolIt = tm.getSym_to_vars().keySet().iterator();
        FieldRef falseD = Jimple.v().newStaticFieldRef(Scene.v().makeFieldRef(disjunct, 
                "falseD", disjunct.getType(), true));
        while(symbolIt.hasNext()) {
            String symbolName = (String)symbolIt.next();
            List parameters = new LinkedList();
            // take state number for legacy reasons..
            parameters.add(IntType.v());
            
            List variableList = tm.getVariableOrder(symbolName);

            //////////// handling of variable-less symbols/tracematches -- return falseD
            if(variableList.isEmpty()) {
                addReturnFieldMethod(disjunct, "addNegativeBindingsForSymbol" + symbolName,
                        falseD, parameters);
                continue;
            }
            
            //////////// handling of symbols that only bind one variable -- return a single Disjunct
            if(variableList.size() == 1) returnSet = false;
            else returnSet = true; // if more than one variable bound, must return a set.
            
            ////////// If more than one variable are bound, we must return a set, since
            // addNegBindings on a disjunct D is meant to return
            //           D && !(x1 == v1 && x2 == v2 && ...)
            //     <=>   D && (x1 != v1 || x2 != v2 || ..)
            //     <=>   (D && (x1 != v1)) || (D && (x2 != v2)) || ...
            //
            // Thus we return a set of disjuncts which should be added to the constraint's disjunct set.
            
            for(int i = 0; i < variableList.size(); i++) parameters.add(objectType);
            SootMethod symbolMethod = new SootMethod("addNegativeBindingsForSymbol" + symbolName,
                    parameters, (returnSet ? setType : disjunct.getType()), Modifier.PUBLIC);
            Body b = Jimple.v().newBody(symbolMethod);
            symbolMethod.setActiveBody(b);
            disjunct.addMethod(symbolMethod);
            
            LocalGeneratorEx lgen = new LocalGeneratorEx(b);
            Local thisLocal = lgen.generateLocal(disjunct.getType(), "thisLocal");
            Local stateNumber = lgen.generateLocal(IntType.v(), "stateNumber");
            Local result = lgen.generateLocal(disjunct.getType(), "result");
            Local resultSet = (returnSet ? lgen.generateLocal(linkedHashSetType, "resultSet") : null);
            
            Chain units = b.getUnits();
            units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(disjunct.getType())));
            
            List variableLocalsList = new LinkedList();
            int parameterIndex = 0;
            // First parameter is the state number
            units.addLast(Jimple.v().newIdentityStmt(stateNumber, 
                    Jimple.v().newParameterRef(IntType.v(), parameterIndex)));

            // Remaining parameters are negative bindings to be added/checked
            parameterIndex = 1;
            Iterator varIt = variableList.iterator();
            while(varIt.hasNext()) {
                String varName = (String)varIt.next();
                // add local for this variable and identity statement to store the parameter
                Local curVar = lgen.generateLocal(objectType, varName);
                units.addLast(Jimple.v().newIdentityStmt(curVar, 
                        Jimple.v().newParameterRef(objectType, parameterIndex)));
                variableLocalsList.add(curVar);
                parameterIndex++;
            }
            
            if(returnSet) {
	            // For each variableX that's being bound by this symbol, we add this.addNegativeBindingForVariableX(V)
	            // to the result set, where V is the appropriate binding value.
	            units.addLast(Jimple.v().newAssignStmt(resultSet, Jimple.v().newNewExpr(linkedHashSetType)));
	            units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(resultSet,
	            		Scene.v().makeConstructorRef(linkedHashSet, new LinkedList()))));
            }
            
            // now we have all the locals. 
            
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
            
            Iterator stateIt = ((TMStateMachine)tm.getState_machine()).getStateIterator();
            while(stateIt.hasNext()) {
            	SMNode curNode = (SMNode)stateIt.next();
            	// if all variables are bound and the state has a state loop -- we want to optimise it.
            	if(curNode.hasEdgeTo(curNode, "") && curNode.boundVars.equals(new LinkedHashSet(tm.getFormalNames()))) {
            		jumpToLabels.add(labelCheckBindingsOnly);
            		jumpOnValues.add(IntConstant.v(curNode.getNumber()));
            	}
            }
            
            // If we have found any states that allow the optimisation, then do it.
            if(!jumpToLabels.isEmpty()) {
            	units.addLast(Jimple.v().newLookupSwitchStmt(stateNumber, jumpOnValues, jumpToLabels, labelComputeResultNormally));
            	units.addLast(labelComputeResultNormally);
            }
            
            varIt = variableList.iterator();
            Iterator localIt = variableLocalsList.iterator();
            while(varIt.hasNext()) {
                String varName = (String)varIt.next();
                Local curVar = (Local)localIt.next();
                
                // result = this.addNegativeBindingForVariableX(V);
                units.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newVirtualInvokeExpr(thisLocal,
                		Scene.v().makeMethodRef(disjunct, "addNegativeBindingForVariable" + varName, singleObjectParameter,
                				disjunct.getType(), false), curVar)));
                
                if(!returnSet) {
                	units.addLast(Jimple.v().newReturnStmt(result));
                	break; // not really needed, since returnSet is only false if the list has exactly one element
                } else {
                	units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(resultSet, 
                			Scene.v().makeMethodRef(Scene.v().getSootClass("java.util.Set"), "add", singleObjectParameter,
                					BooleanType.v(), false), result)));
                }
            }
            
            if(returnSet) {
            	// OK, we have constructed the result set -- now return it
            	units.addLast(Jimple.v().newReturnStmt(resultSet));
            }
            
            // Jump target for the optimisation -- if the current state guarantees the disjunct to be fully bound, we simply
            // need to check compatibility of bindings and return this or falseD.
            if(!jumpToLabels.isEmpty()) {
            	Stmt labelReturnThis = Jimple.v().newNopStmt();
            	Local curThisVar = lgen.generateLocal(objectType, "curThisVar");
            	units.addLast(labelCheckBindingsOnly);
                printString(b, "X");
            	varIt = variableList.iterator();
            	localIt = variableLocalsList.iterator();
            	while(varIt.hasNext()) {
            		String varName = (String)varIt.next();
            		Local curVar = (Local)localIt.next();
            		units.addLast(Jimple.v().newAssignStmt(curThisVar, Jimple.v().newVirtualInvokeExpr(thisLocal,
            				Scene.v().makeMethodRef(disjunct, "get$" + varName, new LinkedList(), objectType, false))));
            		// If there is even just one negative binding that doesn't contradict this disjunct, then that would
            		// make 'this' a disjunct in the result.
            		units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(curVar, curThisVar), labelReturnThis));
            	}
            	
            	// if we haven't jumped out, then each binding was identical to what we have bound in this disjunct,
            	// i.e. the new bindings are incompatible -- return falseD.
            	units.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newStaticFieldRef(
            			Scene.v().makeFieldRef(disjunct, "falseD", disjunct.getType(), true))));
            	if(returnSet) {
                	units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(resultSet, 
                			Scene.v().makeMethodRef(Scene.v().getSootClass("java.util.Set"), "add", singleObjectParameter,
                					BooleanType.v(), false), result)));
                	// OK, we have constructed the result set -- now return it
                	units.addLast(Jimple.v().newReturnStmt(resultSet));
            	} else {
            		units.addLast(Jimple.v().newReturnStmt(result));
            	}
            	
            	// if the bindings are compatible -- return this
            	units.addLast(labelReturnThis);
            	if(returnSet) {
                	units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(resultSet, 
                			Scene.v().makeMethodRef(Scene.v().getSootClass("java.util.Set"), "add", singleObjectParameter,
                					BooleanType.v(), false), thisLocal)));
                	// OK, we have constructed the result set -- now return it
                	units.addLast(Jimple.v().newReturnStmt(resultSet));
            	} else {
            		units.addLast(Jimple.v().newReturnStmt(thisLocal));
            	}
            }
        }
    }
    
    /**
     * Createas a copy() method on the disjunct class that duplicates the current disjunct,
     * preserving all positive and negative bindings.
     * @param tm the tracematch to specialise to
     * @param disjunct the SootClass that the method should be added to
     */
    protected void addDisjunctCopyMethod(TraceMatch tm, SootClass disjunct) {
        RefType objectType = RefType.v("java.lang.Object");
        RefType setType = RefType.v("java.util.Set");
        Type boolType = BooleanType.v();
        
        SootMethod copyMethod = new SootMethod("copy",
                new LinkedList(), disjunct.getType(), Modifier.PUBLIC);
        Body b = Jimple.v().newBody(copyMethod);
        copyMethod.setActiveBody(b);
        disjunct.addMethod(copyMethod);
        
        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        Local thisLocal = lgen.generateLocal(disjunct.getType(), "thisLocal");
        Local result = lgen.generateLocal(disjunct.getType(), "result");
        Local curNegBindings = lgen.generateLocal(setType, "curNegBindings");
        
        Chain units = b.getUnits();
        units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(disjunct.getType())));
        
        // result = new Disjunct();
        units.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newNewExpr(disjunct.getType())));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(result, 
                Scene.v().makeConstructorRef(disjunct, new LinkedList()))));
        
        // For each variable X...
        List varNames = tm.getFormalNames();
        Iterator varIt = varNames.iterator();
        while(varIt.hasNext()) {
            String varName = (String)varIt.next();
            // result.X = this.X;
            Local tmpVar = lgen.generateLocal(objectType, "tmpVar");
            units.addLast(Jimple.v().newAssignStmt(tmpVar, Jimple.v().newInstanceFieldRef(thisLocal,
                    Scene.v().makeFieldRef(disjunct, "var$" + varName, objectType, false))));
            units.addLast(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(result, 
                    Scene.v().makeFieldRef(disjunct, "var$" + varName, objectType, false)),
                tmpVar));
            // result.X$isBound = this.X$isBound;
            Local tmpBound = lgen.generateLocal(BooleanType.v(), "tmpBound");
            units.addLast(Jimple.v().newAssignStmt(tmpBound, Jimple.v().newInstanceFieldRef(thisLocal,
                    Scene.v().makeFieldRef(disjunct, varName + "$isBound", boolType, false))));
            units.addLast(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(result, 
                    Scene.v().makeFieldRef(disjunct, varName + "$isBound", boolType, false)),
                tmpBound));
            // resuult.X$isWeak = this.X$isWeak;
            Local tmpWeak = lgen.generateLocal(BooleanType.v(), "tmpWeak");
            units.addLast(Jimple.v().newAssignStmt(tmpWeak, Jimple.v().newInstanceFieldRef(thisLocal,
                    Scene.v().makeFieldRef(disjunct, varName + "$isWeak", boolType, false))));
            units.addLast(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(result, 
                    Scene.v().makeFieldRef(disjunct, varName + "$isWeak", boolType, false)),
                tmpWeak));
            // If variable isn't bound, we need to keep track of negative bindings
            Stmt labelNextVar = Jimple.v().newNopStmt();
            units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(tmpBound, IntConstant.v(1)),
                    labelNextVar));
            // not_X has been set to a new LinkedHashSet in the constructor -- potentially we
            // could consider keeping it null by only initialising it here.. TODO
            units.addLast(Jimple.v().newAssignStmt(curNegBindings, Jimple.v().newInstanceFieldRef(result,
                    Scene.v().makeFieldRef(disjunct, "not$" + varName, setType, false))));
            Local tmpSet = lgen.generateLocal(setType, "tmpSet");
            units.addLast(Jimple.v().newAssignStmt(tmpSet, Jimple.v().newInstanceFieldRef(thisLocal, 
                    Scene.v().makeFieldRef(disjunct, "not$" + varName, setType, false))));
            List parameters = new LinkedList();
            parameters.add(RefType.v("java.util.Collection"));
            units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(curNegBindings, 
                    Scene.v().makeMethodRef(Scene.v().getSootClass("java.util.Set"), "addAll", 
                            parameters, BooleanType.v(), false), 
                    tmpSet)));
            // the label
            units.addLast(labelNextVar);
        }
        // return result;
        units.add(Jimple.v().newReturnStmt(result));
    }
    
    /**
     * Creates getter methods 'Object get$X()' for each variable X. These methods throw a runtime
     * exception if they're called on an unbound variable, and return the bound value, taking into
     * account whether the reference is weak or strong.
     * @param tm the tracematch for the variables of which we're generating methods
     * @param disjunc the class we're adding methods to
     */
    protected void addDisjunctGetVarMethods(TraceMatch tm, SootClass disjunct, SootClass myWeakRef) {
        RefType objectType = RefType.v("java.lang.Object");
        // For each variable X...
        List varNames = tm.getFormalNames();
        Iterator varIt = varNames.iterator();
        while(varIt.hasNext()) {
            String varName = (String)varIt.next();
        
            SootMethod getMethod = new SootMethod("get$" + varName, new LinkedList(),
                    objectType, Modifier.PUBLIC);
            
            Body b = Jimple.v().newBody(getMethod);
            getMethod.setActiveBody(b);
            disjunct.addMethod(getMethod);
            
            LocalGeneratorEx lgen = new LocalGeneratorEx(b);
            Local thisLocal = lgen.generateLocal(disjunct.getType(), "thisLocal");
            Local result = lgen.generateLocal(objectType, "result");
            Local weakRef = lgen.generateLocal(myWeakRef.getType(), "weakRef");
            Local exception = lgen.generateLocal(RefType.v("java.lang.RuntimeException"), "exception");
            
            Chain units = b.getUnits();
            units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(disjunct.getType())));
            Stmt labelThrowException = Jimple.v().newNopStmt();
            // if(!this.X$isBound) goto throwException;
            Local thisBound = lgen.generateLocal(BooleanType.v(), "thisBound");
            units.addLast(Jimple.v().newAssignStmt(thisBound, Jimple.v().newInstanceFieldRef(thisLocal,
                    Scene.v().makeFieldRef(disjunct, varName + "$isBound", BooleanType.v(), false))));
            units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(thisBound, IntConstant.v(1)),
                    labelThrowException));
            // if(this.X$isWeak) goto returnWeak
            Stmt labelReturnWeak = Jimple.v().newNopStmt();
            Local thisWeak = lgen.generateLocal(BooleanType.v(), "thisWeak");
            units.addLast(Jimple.v().newAssignStmt(thisWeak, Jimple.v().newInstanceFieldRef(thisLocal,
                    Scene.v().makeFieldRef(disjunct, varName + "$isWeak", BooleanType.v(), false))));
            units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(thisWeak, IntConstant.v(1)),
                    labelReturnWeak));
            
            // if we're here, then we have bound the object strongly -- just return it.
            
            units.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newInstanceFieldRef(thisLocal,
                    Scene.v().makeFieldRef(disjunct, "var$" + varName, objectType, false))));
            units.addLast(Jimple.v().newReturnStmt(result));
            
            units.addLast(labelReturnWeak);
            // To return the referent of a weak reference, store it in a local and call its get() method
            units.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newInstanceFieldRef(thisLocal,
                    Scene.v().makeFieldRef(disjunct, "var$" + varName, objectType, false))));
            units.addLast(Jimple.v().newAssignStmt(weakRef,
                                        Jimple.v().newCastExpr(result, myWeakRef.getType())));
            units.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newVirtualInvokeExpr(weakRef,
                    Scene.v().makeMethodRef(myWeakRef, "get", new LinkedList(), objectType, false))));
            units.addLast(Jimple.v().newReturnStmt(result));
            
            // finally, the exception throwing code:
            units.addLast(labelThrowException);
            // exception = new RuntimeException("Attempt to get an unbound variable");
            List parameters = new LinkedList();
            parameters.add(RefType.v("java.lang.String"));
            units.addLast(Jimple.v().newAssignStmt(exception, Jimple.v().newNewExpr(
                    RefType.v("java.lang.RuntimeException"))));
            units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(exception, 
                    Scene.v().makeConstructorRef(Scene.v().getSootClass("java.lang.RuntimeException"),
                    parameters), StringConstant.v("Attempt to get an unbound variable: " + varName))));
            units.addLast(Jimple.v().newThrowStmt(exception));
        }
    }

    protected void addDisjunctInitialiser(TraceMatch tm, SootClass disjunct) {
        SootMethod init = new SootMethod(SootMethod.constructorName, new LinkedList(), 
                VoidType.v(), Modifier.PUBLIC);
        Body b = Jimple.v().newBody(init);
        init.setActiveBody(b);
        disjunct.addMethod(init);

        RefType hashSet = RefType.v("java.util.LinkedHashSet");

        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        Local tempSet = lgen.generateLocal(hashSet, "tempSet");
        Local thisLocal = lgen.generateLocal(disjunct.getType(), "thisLocal");
        Chain units = b.getUnits();
        units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(disjunct.getType())));
 
        // call super()
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(thisLocal,
                Scene.v().makeConstructorRef(Scene.v().getSootClass("java.lang.Object"),
                        new LinkedList()))));

        // For now -- initialise each not$X set with a new set on construction.
        List varNames = tm.getFormalNames();
        Iterator varIt = varNames.iterator();
        while(varIt.hasNext()) {
            String varName = (String)varIt.next();
            units.addLast(Jimple.v().newAssignStmt(tempSet, Jimple.v().newNewExpr(hashSet)));
            units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(tempSet,
                    Scene.v().makeConstructorRef(Scene.v().getSootClass("java.util.LinkedHashSet"),
                            new LinkedList()))));
            units.addLast(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(thisLocal,
                    Scene.v().makeFieldRef(disjunct, "not$" + varName, 
                            RefType.v("java.util.Set"), false)), tempSet));
        }
        // For debugging purposes -- print out a + any time a disjunct is created
        printString(b, "D");
        units.addLast(Jimple.v().newReturnVoidStmt());
        
        // For debugging purposes -- print out a - any time a disjunct is finalised
        SootMethod finalize = new SootMethod("finalize", new LinkedList(), VoidType.v(), Modifier.PROTECTED);
        b = Jimple.v().newBody(finalize);
        finalize.setActiveBody(b);
        disjunct.addMethod(finalize);
        units = b.getUnits();
        printString(b, "d");
        units.addLast(Jimple.v().newReturnVoidStmt());
    }
    
    protected void addDisjunctStaticInitialiser(SootClass disjunct) {
        SootMethod clinit = new SootMethod(SootMethod.staticInitializerName, new LinkedList(), 
                VoidType.v(), Modifier.STATIC);
        Body b = Jimple.v().newBody(clinit);
        clinit.setActiveBody(b);
        disjunct.addMethod(clinit);

        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        Local tempConstraint = lgen.generateLocal(disjunct.getType(), "tempConstraint");
        // trueC = new Constraint();
        Chain units = b.getUnits();
        StaticFieldRef trueConstraintField = Jimple.v().newStaticFieldRef(
                Scene.v().makeFieldRef(disjunct, "trueD", disjunct.getType(), true));
        StaticFieldRef falseConstraintField = Jimple.v().newStaticFieldRef(
                Scene.v().makeFieldRef(disjunct, "falseD", disjunct.getType(), true));
        units.addLast( Jimple.v().newAssignStmt(tempConstraint, 
                Jimple.v().newNewExpr(disjunct.getType())));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(tempConstraint, 
                Scene.v().makeConstructorRef(disjunct, new LinkedList()))));
        units.addLast(Jimple.v().newAssignStmt(trueConstraintField, tempConstraint));
        units.addLast(Jimple.v().newAssignStmt(tempConstraint, 
                Jimple.v().newNewExpr(disjunct.getType())));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(tempConstraint, 
                Scene.v().makeConstructorRef(disjunct, new LinkedList()))));
        units.addLast(Jimple.v().newAssignStmt(falseConstraintField, tempConstraint));
        units.addLast(Jimple.v().newReturnVoidStmt());
    }
    
    /**
     * Adds a method with the signature "public boolean validateDisjunct(int state);".
     * The idea is that the return value is false if one of the collectableWeakRefs of
     * the disjuct has expired, and true otherwise. Validating a disjunct before calling 
     * add[Neg]Bindings on it will enable clean-up of unneeded disjuncts 
     * @param tm the tracematch from which to take the states
     * @param disjuct the class to add the method to.
     */
    protected void addDisjunctValidateDisjunctMethod(TraceMatch tm, SootClass disjunct) {
        List parameters = new LinkedList();
        parameters.add(IntType.v());
        SootMethod validate = new SootMethod("validateDisjunct", parameters, BooleanType.v(),
                Modifier.PUBLIC);
        Body b = Jimple.v().newBody(validate);
        validate.setActiveBody(b);
        disjunct.addMethod(validate);

        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        Local thisLocal = lgen.generateLocal(disjunct.getType(), "thisLocal");
        Local stateNumber = lgen.generateLocal(IntType.v(), "stateNumber");
        Local varLocal = lgen.generateLocal(RefType.v("java.lang.Object"), "varLocal");
        
        Chain units = b.getUnits();
        units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(disjunct.getType())));
        units.addLast(Jimple.v().newIdentityStmt(stateNumber, Jimple.v().newParameterRef(IntType.v(), 0)));
        
        // to construct the LookupSwitch, we need two lists of equal length -- a list of values
        // to compare to (those will be state numbers, 0 .. (#states - 1)) and a list of labels
        // to jump to.
        Iterator stateIt = ((TMStateMachine)tm.getState_machine()).getStateIterator();
        List switchValues = new LinkedList();
        List labels = new LinkedList();
        while(stateIt.hasNext()) {
            switchValues.add(IntConstant.v(((SMNode)stateIt.next()).getNumber()));
            labels.add(Jimple.v().newNopStmt());
        }
        
        Stmt labelThrowException = Jimple.v().newNopStmt();
        Stmt labelReturnFalse = Jimple.v().newNopStmt();
        Stmt labelReturnTrue = Jimple.v().newNopStmt();
        // the switch:
        units.addLast(Jimple.v().newLookupSwitchStmt(stateNumber, switchValues, labels, labelThrowException));

        Iterator labelIt = labels.iterator();
        stateIt = ((TMStateMachine)tm.getState_machine()).getStateIterator();
        while(stateIt.hasNext()) {
            SMNode curState = (SMNode)stateIt.next();
            Stmt curLabel = (Stmt)labelIt.next();
            units.addLast(curLabel);
            
            //////// Cleaning up invalidated collectableWeakRefs
            // Each state is labelled with a set collectableWeakRefs. If one of these
            // becomes invalid, the disjunct can never lead to a successful match and,
            // accordingly, can/must be discarded.
            //
            // Since we don't allow null bindings and WeakRefs become null when they are
            // invalidated, we check each bound variable in the collectableWeakRefs set
            // for the current state for null-ness, and if it's null return falseD.
            Iterator collectableWeakRefIt = curState.collectableWeakRefs.iterator();
            while(collectableWeakRefIt.hasNext()) {
                String varName = (String)collectableWeakRefIt.next();
                Stmt labelCurVarNotBound = Jimple.v().newNopStmt();
                Local booleanLocal = lgen.generateLocal(BooleanType.v(), "isVarBound");
                units.addLast(Jimple.v().newAssignStmt(booleanLocal, Jimple.v().newInstanceFieldRef(thisLocal,
                        Scene.v().makeFieldRef(disjunct, varName + "$isBound", BooleanType.v(), false))));
                units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(booleanLocal, IntConstant.v(1)), labelCurVarNotBound));
                // variable is bound -- check if it's invalid
                units.addLast(Jimple.v().newAssignStmt(varLocal, Jimple.v().newVirtualInvokeExpr(thisLocal,
                        Scene.v().makeMethodRef(disjunct, "get$" + varName, new LinkedList(), RefType.v("java.lang.Object"), false))));
                units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(varLocal, NullConstant.v()), labelReturnFalse));
                
                units.addLast(labelCurVarNotBound);
            }
            
            units.addLast(Jimple.v().newGotoStmt(labelReturnTrue));
        }

        units.addLast(labelReturnTrue);
        units.addLast(Jimple.v().newReturnStmt(IntConstant.v(1)));
        
        units.addLast(labelReturnFalse);
        printString(b, "*");
        units.addLast(Jimple.v().newReturnStmt(IntConstant.v(0)));
        
        units.addLast(labelThrowException);
        throwException(b, "Disjunct.validateDisjunct() called with an invalid state number");
    }

    /**
     * Fills in the method stubs that have been generated for this tracematch.
     * @param tm the tracematch in question
     */
    protected void fillInAdviceBodies(TraceMatch tm, Collection unused)
    {
        List body_formals = traceMatchBodyParameters(unused, tm);

        CodeGenHelper helper = new CodeGenHelper(tm);
        helper.makeAndInitLabelFields();

        Iterator syms = tm.getSymbols().iterator();

        while (syms.hasNext()) {
            String symbol = (String) syms.next();
            SootMethod advice_method = tm.getSymbolAdviceMethod(symbol);

            fillInSymbolAdviceBody(symbol, advice_method, tm, helper);
        }

        Iterator kinds = tm.getKinds().iterator();
        while (kinds.hasNext()) {
            String kind = (String) kinds.next();
            SootMethod advice_method = tm.getSomeAdviceMethod(kind);

            fillInSomeAdviceBody(kind, advice_method, tm, helper, body_formals);
        }
    }
    
    protected void addMWRInitialiserMethod() {
    	List singleObjectTypeParameter = new LinkedList();
    	singleObjectTypeParameter.add(RefType.v("java.lang.Object"));
    	SootMethod init = new SootMethod(SootMethod.constructorName, singleObjectTypeParameter, VoidType.v(), Modifier.PUBLIC);
    	Body b = Jimple.v().newBody(init);
    	init.setActiveBody(b);
    	myWeakRef.addMethod(init);
    	
    	LocalGeneratorEx lgen = new LocalGeneratorEx(b);
    	Local thisLocal = lgen.generateLocal(myWeakRef.getType(), "thisLocal");
    	Local param = lgen.generateLocal(RefType.v("java.lang.Object"), "paramLocal");
    	
    	Chain units = b.getUnits();
    	units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(myWeakRef.getType())));
    	units.addLast(Jimple.v().newIdentityStmt(param, Jimple.v().newParameterRef(RefType.v("java.lang.Object"), 0)));
    	
    	units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(thisLocal, 
    			Scene.v().makeConstructorRef(Scene.v().getSootClass("java.lang.ref.WeakReference"), singleObjectTypeParameter),
    			param)));
    	
    	units.addLast(Jimple.v().newReturnVoidStmt());
    	
    	SootMethod clinit = new SootMethod(SootMethod.staticInitializerName, new LinkedList(), VoidType.v(), Modifier.STATIC);
    	b = Jimple.v().newBody(clinit);
    	clinit.setActiveBody(b);
    	myWeakRef.addMethod(clinit);
    	
    	units = b.getUnits();
    	units.addLast(Jimple.v().newReturnVoidStmt());
    }
    
    protected void addMWREqualsMethod() {
    	SootClass weakRef = Scene.v().getSootClass("java.lang.ref.WeakReference");
    	RefType objectType = RefType.v("java.lang.Object");
    	List singleObjectTypeParameter = new LinkedList();
    	singleObjectTypeParameter.add(objectType);
    	SootMethod equals = new SootMethod("equals", singleObjectTypeParameter, BooleanType.v(), Modifier.PUBLIC);
    	Body b = Jimple.v().newBody(equals);
    	equals.setActiveBody(b);
    	myWeakRef.addMethod(equals);
    	
    	LocalGeneratorEx lgen = new LocalGeneratorEx(b);
    	Local thisLocal = lgen.generateLocal(myWeakRef.getType(), "thisLocal");
    	Local param = lgen.generateLocal(objectType, "paramLocal");
    	Local paramWR = lgen.generateLocal(myWeakRef.getType(), "paramWeakRef");
    	Local thisObject = lgen.generateLocal(objectType, "thisObject");
    	Local paramObject = lgen.generateLocal(objectType, "paramObject");
    	
    	Chain units = b.getUnits();
    	units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(myWeakRef.getType())));
    	units.addLast(Jimple.v().newIdentityStmt(param, Jimple.v().newParameterRef(RefType.v("java.lang.Object"), 0)));
    	
    	Stmt labelParamIsWeakRef = Jimple.v().newNopStmt();
    	Stmt labelReturnTrue = Jimple.v().newNopStmt();

    	units.addLast(Jimple.v().newAssignStmt(thisObject, Jimple.v().newVirtualInvokeExpr(thisLocal, 
    			Scene.v().makeMethodRef(weakRef, "get", new LinkedList(), objectType, false))));
    	// if param is a weakRef, we need to do param.get();
        Local booleanLocal = lgen.generateLocal(BooleanType.v(), "booleanLocal");
        units.addLast(Jimple.v().newAssignStmt(booleanLocal, Jimple.v().newInstanceOfExpr(param, myWeakRef.getType())));
    	units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(booleanLocal, IntConstant.v(1)), labelParamIsWeakRef));
    	// otherwise we can just compare:
    	units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(thisObject, param), labelReturnTrue));
    	// return false;
    	units.addLast(Jimple.v().newReturnStmt(IntConstant.v(0)));
    	
    	units.addLast(labelParamIsWeakRef);
    	// if param is a weakRef, cast to myWeakRef:
    	units.addLast(Jimple.v().newAssignStmt(paramWR, Jimple.v().newCastExpr(param, myWeakRef.getType())));
    	// then store its referent in a local:
    	units.addLast(Jimple.v().newAssignStmt(paramObject, Jimple.v().newVirtualInvokeExpr(paramWR, 
    			Scene.v().makeMethodRef(myWeakRef, "get", new LinkedList(), objectType, false))));
    	// and compare the two:
    	units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(thisObject, paramObject), labelReturnTrue));
    	// return false;
    	units.addLast(Jimple.v().newReturnStmt(IntConstant.v(0)));

    	units.addLast(labelReturnTrue);
    	units.addLast(Jimple.v().newReturnStmt(IntConstant.v(1)));
    }
    
    protected void addMWRHashCodeMethod() {
    	SootClass weakRef = Scene.v().getSootClass("java.lang.ref.WeakReference");
    	RefType objectType = RefType.v("java.lang.Object");
    	SootMethod hashCode = new SootMethod("hashCode", new LinkedList(), IntType.v(), Modifier.PUBLIC);
    	Body b = Jimple.v().newBody(hashCode);
    	hashCode.setActiveBody(b);
    	myWeakRef.addMethod(hashCode);
    	
    	LocalGeneratorEx lgen = new LocalGeneratorEx(b);
    	Local thisLocal = lgen.generateLocal(myWeakRef.getType(), "thisLocal");
    	Local thisObject = lgen.generateLocal(objectType, "thisObject");
    	Local result = lgen.generateLocal(IntType.v(), "result");
    	
    	Chain units = b.getUnits();
    	units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(myWeakRef.getType())));
    	
    	units.addLast(Jimple.v().newAssignStmt(thisObject, Jimple.v().newVirtualInvokeExpr(thisLocal, 
    			Scene.v().makeMethodRef(weakRef, "get", new LinkedList(), objectType, false))));

    	// if thisObject == null return 0;
    	Stmt labelReturnZero = Jimple.v().newNopStmt();
    	units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(thisObject, NullConstant.v()), labelReturnZero));
    	units.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newVirtualInvokeExpr(thisObject, 
    			Scene.v().makeMethodRef(Scene.v().getSootClass("java.lang.Object"), "hashCode", 
    					new LinkedList(), IntType.v(), false))));
    	
    	units.addLast(Jimple.v().newReturnStmt(result));
    	
    	units.addLast(labelReturnZero);
    	units.addLast(Jimple.v().newReturnStmt(IntConstant.v(0)));
    }

    protected void fillInSymbolAdviceBody(String symbol, SootMethod method,
                                            TraceMatch tm, CodeGenHelper helper)
    {
        TMStateMachine sm = (TMStateMachine) tm.getState_machine();
        Iterator to_states = sm.getStateIterator();

        while (to_states.hasNext()) {
            SMNode to = (SMNode) to_states.next();
            Iterator edges = to.getInEdgeIterator();

            // we don't accumulate useless constraints for
            // initial states
            if (to.isInitialNode())
                continue;

            helper.genNullChecks(method);

            while (edges.hasNext()) {
                SMEdge edge = (SMEdge) edges.next();

                if (edge.getLabel().equals(symbol)) {
                    SMNode from = (SMNode) edge.getSource();

                    helper.genLabelUpdate(from.getNumber(), to.getNumber(),
                                            edge.getLabel(), method);
                }
            }

            helper.genNullChecksJumpTarget(method);

            if (to.hasEdgeTo(to, "")) // (skip-loop)
                helper.genSkipLabelUpdate(to.getNumber(), symbol, method);
        }
    }

    protected void fillInSomeAdviceBody(String kind, SootMethod method,
                                        TraceMatch tm, CodeGenHelper helper,
                                        List body_formals)
    {
        TMStateMachine sm = (TMStateMachine) tm.getState_machine();
        Iterator states = sm.getStateIterator();
        SMNode final_state = null;

        helper.genTestAndResetUpdated(method);

        while (states.hasNext()) {
            SMNode state = (SMNode) states.next();
            boolean skip_loop;

            // there is only one final state, and we remember it
            // in order to generate solution code later
            if (state.isFinalNode())
                final_state = state;

            // we don't want to accumulate useless constraints
            // for initial states
            if (state.isInitialNode())
                continue;

            if (state.hasEdgeTo(state, "")) // (skip-loop)
                skip_loop = true;
            else
                skip_loop = false;

            helper.genLabelMasterUpdate(skip_loop, state.getNumber(), method);
        }

        if (!tm.isAround() || kind.equals("around"))
            helper.genRunSolutions(final_state.getNumber(),
                                    method, body_formals);
    }
 
    protected List traceMatchBodyParameters(Collection unused, TraceMatch tm)
    {
        List formals = new ArrayList(tm.getFormals().size());
        Iterator i = tm.getFormals().iterator();

        while (i.hasNext()) {
            Formal f = (Formal) i.next();

            if (!unused.contains(f.getName()))
                formals.add(f);
        }

        return formals;
    }

    protected void prepareAdviceBody(SootMethod sm, List names, Collection unused) {
    	List paramTypes = new LinkedList();
    	List paramNames = new LinkedList();
    	Iterator ptIter = sm.getParameterTypes().iterator();
    	Iterator nameIter = names.iterator();
    	while (ptIter.hasNext()) {
    		Type pt = (Type) ptIter.next();
    		String name = (String) nameIter.next();
    		if (!unused.contains(name)){
    			paramTypes.add(pt); 
    			paramNames.add(name);
    		}
    	}
    	
    	// renumber the identity statements
    	Body body = sm.getActiveBody();
    	Unit u = null;
    	Chain units = body.getUnits();
    	for (Iterator unitIter = units.iterator(); unitIter.hasNext(); ) {
    		u = (Unit) unitIter.next();
    		if (u instanceof IdentityStmt) {
    			IdentityStmt is = (IdentityStmt) u;
    			Value rhs = is.getRightOp();
    			if (rhs instanceof ParameterRef) {
    				ParameterRef pr = (ParameterRef) rhs;
    				String oldName = (String) names.get(pr.getIndex());
    				if (paramNames.contains(oldName)) {
    				   int newIndex = paramNames.indexOf(oldName);
    				   pr.setIndex(newIndex);
    			    } else 
    			    	unitIter.remove();
    			}
    		}
    	}
    	
    	sm.setParameterTypes(paramTypes);
 /*   
    	if (u == null) return;
		
		SootClass scIter = Scene.v().getSootClass("java.util.Iterator");
		paramTypes.add(scIter.getType());
		
		
    	Local iterLocal = body.getParameterLocal(paramTypes.size()-1);
    	LocalGeneratorEx lgen = new LocalGeneratorEx(body);
    	
    	// is there still a binding remaining?
    	Local hasNext = lgen.generateLocal(BooleanType.v(),"hasNext");
    	SootMethodRef smrHasNext = scIter.getMethod("hasNext",new LinkedList()).makeRef();
    	InvokeExpr e = Jimple.v().newVirtualInvokeExpr(iterLocal,smrHasNext,new LinkedList());
		AssignStmt ass = Jimple.v().newAssignStmt(hasNext,e);
		units.insertBefore(u,ass);
		u.redirectJumpsToThisTo(ass);
		
		// if not, proceed
		EqExpr ce = Jimple.v().newEqExpr(hasNext,IntConstant.v(0));
    	Stmt stmtIfHasNext = Jimple.v().newIfStmt(ce,u);
    	units.insertBefore(u,stmtIfHasNext);
    
    	// otherwise recursively call the body with another binding
        // FIXME: this is missing!

        // jump over the normal proceed
		Unit elsetarget = (Unit) units.getSuccOf(u);
		Stmt stmtJump = Jimple.v().newGotoStmt(elsetarget);
    	units.insertBefore(u,elsetarget);
  
		System.out.println(body.toString());		
		*/
    }
    /**
     * Fills in the method stubs generated by the frontend for a given tracematch.
     * @param tm the tracecmatch to deal with.
     */
    public void fillInTraceMatch(TraceMatch tm) {
        TMStateMachine tmsm = (TMStateMachine)tm.getState_machine();

		Collection unused = UnUsedParams.unusedFormals(tm.getBodyMethod(),tm.getFormalNames());
        
        tmsm.prepareForMatching(tm.getSymbols(), tm.getFormalNames(), tm.getSym_to_vars(), 
                                                UnUsedParams.unusedFormals(tm.getBodyMethod(),tm.getFormalNames()),
                                                tm.getPosition());
        
        
        prepareAdviceBody(tm.getBodyMethod(),tm.getFormalNames(),unused);
        
        // Create the constraint class(es). A constraint is represented in DNF as a set of
        // disjuncts, which are conjuncts of positive or negative bindings. For now, we 
        // only create one kind of disjunct class for each tracematch, specialised to have
        // fields for the tracecmatch variables. A potential optimisation is to specialise
        // the disjunct class to each state, as negative bindings needn't be kept for all
        // states in general -- this may/will be done in time.
        createConstraintClasses(tm);
        //ClassGenHelper cgh = new ClassGenHelper(tm);
        //cgh.generateClasses();
        
        // Fill in the advice bodies. The method stubs have been created by the frontend and
        // can be obtained from the TraceMatch object; code to keep track of changing 
        // constraints and to run the tracematch advice when appropriate, with the necessary
        // bindings, should be added.
        fillInAdviceBodies(tm, unused);
    }
}
