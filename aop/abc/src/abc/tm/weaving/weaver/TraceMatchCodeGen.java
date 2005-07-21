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
    // TODO: Perhaps have a dedicated flag for tracematch codegen
    private static void debug(String message)
    { if (abc.main.Debug.v().aspectCodeGen)
        System.err.println("TCG*** " + message);
    }

    protected String getConstraintClassName(TraceMatch tm) {
        return "Constraint$" + tm.getName();
    }
    
    protected String getDisjunctClassName(TraceMatch tm) {
        return "Disjunct$" + tm.getName();
    }
    
    /**
     * Create the classes needed to keep constraints for a given tracematch. Classes are
     * the Constraint set of disjuncts and the disjunct class, plus any helper classes.
     * Could, at some point, specialise the constraints to each FSA state.
     * @param tm The relevant tracematch
     */
    protected void createConstraintClasses(TraceMatch tm) {
        // the SootClasses for the constraint and the main disjunct class for the tracematch 
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
        
        Iterator symbolIt = tm.getSym_to_vars().keySet().iterator();
        String symbol;
        while(symbolIt.hasNext()) {
            symbol = (String)symbolIt.next();
            addAddBindingsDispatchMethod(constraint, disjunct, 
                    "addBindingsForSymbol" + symbol, tm.getVariableOrder(symbol));
            addAddBindingsDispatchMethod(constraint, disjunct, 
                    "addNegativeBindingsForSymbol" + symbol, tm.getVariableOrder(symbol));
        }
        
        addConstraintGetDisjunctIteratorMethod(constraint, disjunct);
        
        addConstraintInitialiser(constraint);
        
        addConstraintStaticInitialiser(constraint, disjunct);
    }
    
    protected SootMethod addAddBindingsDispatchMethod(SootClass constraint, SootClass disjunct,
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
        RefType setType = RefType.v("java.util.Set");
        RefType iteratorType = RefType.v("java.util.Iterator");
        SootClass hashSet = Scene.v().getSootClass("java.util.HashSet");
        SootClass iteratorClass = Scene.v().getSootClass("java.util.Iterator");
        Local resultSet = lgen.generateLocal(setType, "resultSet");
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
                localSet, Scene.v().makeMethodRef(hashSet, "iterator", new LinkedList(), 
                        iteratorType, false))));
        
        // Have to emulate loops with jumps: while(disjunctIt.hasNext()) { ... }
        Stmt labelLoopBegin = Jimple.v().newNopStmt();
        Stmt labelLoopEnd = Jimple.v().newNopStmt();
        units.addLast(labelLoopBegin);
        // if(!it1.hasNext()) goto labelLoopEnd; <code for loop>; <label>:
        Local booleanLocal = lgen.generateLocal(BooleanType.v(), "booleanLocal");
        units.addLast(Jimple.v().newAssignStmt(booleanLocal,
                Jimple.v().newVirtualInvokeExpr(disjunctIt, 
                        Scene.v().makeMethodRef(iteratorClass, "hasNext", new LinkedList(), BooleanType.v(), false))));
        units.addLast(Jimple.v().newAssignStmt(booleanLocal,
                Jimple.v().newNegExpr(booleanLocal)));
        units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(booleanLocal, IntConstant.v(1)),
                        labelLoopEnd));
        // disjunctThis = (Disjunct)disjunctIt.next();
        Local tmpObject = lgen.generateLocal(RefType.v("java.lang.Object"), "tmpObject");
        units.addLast(Jimple.v().newAssignStmt(tmpObject, Jimple.v().newVirtualInvokeExpr(disjunctIt, 
                                Scene.v().makeMethodRef(iteratorClass, "next", new LinkedList(), 
                                        RefType.v("java.lang.Object"), false))));
        units.addLast(Jimple.v().newAssignStmt(disjunctThis, 
                Jimple.v().newCastExpr(tmpObject, disjunct.getType())));
        // disjunctResult = disjunct.addBindingsForSymbolX(...);
        units.addLast(Jimple.v().newAssignStmt(disjunctResult,
                Jimple.v().newVirtualInvokeExpr(disjunctThis, 
                        Scene.v().makeMethodRef(disjunct, methodName,
                                parameterTypes, disjunct.getType(), false), parameterLocals)));
        // resultSet.add(disjunctResult);
        List parameters = new LinkedList();
        parameters.add(RefType.v("java.lang.Object"));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(resultSet,
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
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(resultSet,
                Scene.v().makeMethodRef(hashSet, "remove", parameters, BooleanType.v(), false), falseDisjunct)));
        Stmt labelReturnFalseC = Jimple.v().newNopStmt();
        // if(resultSet.isEmpty) goto label;
        units.addLast(Jimple.v().newAssignStmt(booleanLocal, Jimple.v().newInterfaceInvokeExpr(resultSet,
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
        StaticFieldRef falseC = Jimple.v().newStaticFieldRef(
                Scene.v().makeFieldRef(constraint, "falseC", constraint.getType(), true));
        Local falseConstraint = lgen.generateLocal(constraint.getType(), "falseConstraint");
        Jimple.v().newAssignStmt(falseConstraint, falseC);
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
        Local resultSet = lgen.generateLocal(setType, "resultSet");
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
        units.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newVirtualInvokeExpr(thisLocal, 
                    Scene.v().makeMethodRef(constraint, "copy", new LinkedList(), constraint.getType(), false))));
        units.addLast(Jimple.v().newReturnStmt(result));
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
        // Create a new Constraint object to store the result in
        units.addLast(Jimple.v().newAssignStmt(result, 
            Jimple.v().newNewExpr(constraint.getType())));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(result,
            Scene.v().makeConstructorRef(constraint, new LinkedList()))));
        // resultSet = result.disjuncts;
        units.addLast(Jimple.v().newAssignStmt(resultSet, Jimple.v().newInstanceFieldRef(result,
                Scene.v().makeFieldRef(constraint, "disjuncts", setType, false))));
        // add all disjuncts to resultSet
        parameters.clear();
        RefType collection = RefType.v("java.util.Collection");
        parameters.add(collection);
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(resultSet,
            Scene.v().makeMethodRef(setClass, "addAll", parameters, BooleanType.v(), false),
            localSet)));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(resultSet,
            Scene.v().makeMethodRef(setClass, "addAll", parameters, BooleanType.v(), false),
            remoteSet)));
        // return result;
        units.addLast(Jimple.v().newReturnStmt(result));
    }
    
    protected void addConstraintAndMethod(SootClass constraint, SootClass disjunct) {
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
        
    }
    
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
                VoidType.v(), Modifier.STATIC);
        Body b = Jimple.v().newBody(init);
        init.setActiveBody(b);
        constraint.addMethod(init);

        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        Local tempSet = lgen.generateLocal(RefType.v("java.util.Set"), "tempSet");
        Local thisLocal = lgen.generateLocal(constraint.getType(), "thisLocal");
        Chain units = b.getUnits();
        units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(constraint.getType())));
        units.addLast( Jimple.v().newAssignStmt(tempSet, 
                Jimple.v().newNewExpr(RefType.v("java.util.LinkedHashSet"))));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(tempSet, 
                Scene.v().makeConstructorRef(Scene.v().getSootClass("java.util.LinkedHashSet"), new LinkedList()))));
        units.addLast(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(thisLocal,
                Scene.v().makeFieldRef(constraint, "disjuncts", RefType.v("java.util.Set"), false)),
                tempSet));
       
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
        units.addLast(Jimple.v().newAssignStmt(trueConstraintField, tempConstraint));
        units.addLast(Jimple.v().newAssignStmt(tempConstraint, 
                Jimple.v().newNewExpr(constraint.getType())));
        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(tempConstraint, 
                Scene.v().makeConstructorRef(constraint, new LinkedList()))));
        units.addLast(Jimple.v().newAssignStmt(falseConstraintField, tempConstraint));
    }
    
    protected void addConstraintGetDisjunctIteratorMethod(SootClass constraint, SootClass disjunct) {
        RefType iteratorType = RefType.v("java.util.Iterator");
        RefType setType = RefType.v("java.util.Set");
        
        SootMethod getIterator = new SootMethod("getDisjunctIterator", new LinkedList(), 
                iteratorType, Modifier.PUBLIC);
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
        
        // return set.iterator();
        Local iteratorLocal = lgen.generateLocal(RefType.v("java.util.Iterator"), "iterator");
        units.addLast(Jimple.v().newAssignStmt(iteratorLocal, Jimple.v().newInterfaceInvokeExpr(set, 
                Scene.v().makeMethodRef(Scene.v().getSootClass("java.util.Set"), "iterator",
                        new LinkedList(), iteratorType, false))));
        units.addLast(Jimple.v().newReturnStmt(iteratorLocal));
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
        }
        addDisjunctEqualsMethod(disjunct, varNames);
        
        // now -- where the bulk of the work happens, addBindingsForSymbolX and the state-
        // specific versions, addBindingsForSymbolXInState<Number>, also negative bindings-
        // versions of these.
        addDisjunctAddBindingsForSymbolMethods(tm, disjunct, Scene.v().getSootClass("java.lang.ref.WeakReference") /* FIXME */);
        addDisjunctAddNegBindingsForSymbolMethods(tm, disjunct, Scene.v().getSootClass("java.lang.ref.WeakReference") /* FIXME */);
        
        // We need a copy() method..
        addDisjunctCopyMethod(tm, disjunct);
        
        // The variable getters
        addDisjunctGetVarMethods(tm, disjunct, Scene.v().getSootClass("java.lang.ref.WeakReference") /* FIXME */);
        
        addDisjunctInitialiser(tm, disjunct);
        
        addDisjunctStaticInitialiser(disjunct);
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
        
        // first things first -- identity statements
        Chain units = b.getUnits();
        units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(disjunct.getType())));
        units.addLast(Jimple.v().newIdentityStmt(compareTo, Jimple.v().newParameterRef(
                objectType, 0)));
        
        // label to jump to as soon as we know we can return false (or true)
        Stmt labelReturnFalse = Jimple.v().newNopStmt();
        Stmt labelReturnTrue = Jimple.v().newNopStmt();
        
        // if the type is wrong, return false
        Local booleanLocal = lgen.generateLocal(BooleanType.v(), "booleanLocal");
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
            Stmt labelNotBothBound = Jimple.v().newNopStmt();
            Local thisBound = lgen.generateLocal(BooleanType.v(), "thisBound");
            Local paramBound = lgen.generateLocal(BooleanType.v(), "paramBound");
            Local varThis = lgen.generateLocal(objectType, "varThis");
            Local varParam = lgen.generateLocal(objectType, "varParam");
            units.addLast(Jimple.v().newAssignStmt(varThis, Jimple.v().newInstanceFieldRef(thisLocal,
                    Scene.v().makeFieldRef(disjunct, "var$" + varName, objectType, false))));
            units.addLast(Jimple.v().newAssignStmt(varParam, Jimple.v().newInstanceFieldRef(compareToDisjunct,
                    Scene.v().makeFieldRef(disjunct, "var$" + varName, objectType, false))));

            units.addLast(Jimple.v().newAssignStmt(thisBound, Jimple.v().newInstanceFieldRef(thisLocal,
                    Scene.v().makeFieldRef(disjunct, varName + "$isBound", BooleanType.v(), false))));
            units.addLast(Jimple.v().newAssignStmt(paramBound, Jimple.v().newInstanceFieldRef(compareToDisjunct,
                    Scene.v().makeFieldRef(disjunct, varName + "$isBound", BooleanType.v(), false))));
            units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(thisBound, IntConstant.v(1)),
                    labelNotBothBound));
            units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(paramBound, IntConstant.v(1)),
                    labelNotBothBound));
            
            units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(varThis, varParam),
                    labelReturnFalse));
            
            // if exactly one is bound, then return false
            units.addLast(labelNotBothBound);
            units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(thisBound, paramBound),
                labelReturnFalse));
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
        }
        // We have now checked all variables -- if we haven't jumped to labelReturnFalse, return true
        units.addLast(Jimple.v().newReturnStmt(IntConstant.v(1)));
        
        // now for the unfinished business
        units.addLast(labelReturnFalse);
        units.addLast(Jimple.v().newReturnStmt(IntConstant.v(0)));  

        units.addLast(labelReturnTrue);
        units.addLast(Jimple.v().newReturnStmt(IntConstant.v(1)));  
        /////////////////// end Disjunct.equals() method ////////////////////////////

    }
    
    protected void addDisjunctAddBindingsForSymbolMethods(TraceMatch tm, SootClass disjunct, SootClass myWeakRef) {
        RefType objectType = RefType.v("java.lang.Object");
        RefType setType = RefType.v("java.util.Set");
        List singleObjectParameter = new LinkedList();
        singleObjectParameter.add(objectType);
        Iterator symbolIt = tm.getSym_to_vars().keySet().iterator();
        while(symbolIt.hasNext()) {
            String symbolName = (String)symbolIt.next();
            List parameters = new LinkedList();
            parameters.add(IntType.v());
            Set variableSet = (Set)tm.getSym_to_vars().get(symbolName);
            for(int i = 0; i < variableSet.size(); i++) parameters.add(objectType);
            SootMethod symbolMethod = new SootMethod("addBindingsForSymbol" + symbolName,
                    parameters, disjunct.getType(), Modifier.PUBLIC);
            Body b = Jimple.v().newBody(symbolMethod);
            symbolMethod.setActiveBody(b);
            disjunct.addMethod(symbolMethod);
            
            LocalGeneratorEx lgen = new LocalGeneratorEx(b);
            Local thisLocal = lgen.generateLocal(disjunct.getType(), "thisLocal");
            Local stateNumber = lgen.generateLocal(IntType.v(), "stateNumber");
            Local curVarNegBindings = lgen.generateLocal(setType, "curNegBindings");
            Local result = lgen.generateLocal(disjunct.getType(), "result");
            Local weakRef = lgen.generateLocal(myWeakRef.getType(), "weakRef");
            
            Chain units = b.getUnits();
            units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(disjunct.getType())));
            units.addLast(Jimple.v().newIdentityStmt(stateNumber,
                    Jimple.v().newParameterRef(IntType.v(), 0)));
            
            List variableLocalsList = new LinkedList();
            int parameterIndex = 1;
            Iterator varIt = variableSet.iterator();
            while(varIt.hasNext()) {
                String varName = (String)varIt.next();
                // add local for this variable and identity statement to store the parameter
                Local curVar = lgen.generateLocal(objectType, varName);
                units.addLast(Jimple.v().newIdentityStmt(curVar, 
                        Jimple.v().newParameterRef(objectType, parameterIndex)));
                variableLocalsList.add(curVar);
                parameterIndex++;
            }
            
            // label to jump to if new bindings are incompatible
            Stmt labelReturnFalse = Jimple.v().newNopStmt();
            
            // now we have all the locals. Generate the code for this method.
            varIt = variableSet.iterator();
            Iterator localIt = variableLocalsList.iterator();
            while(varIt.hasNext()) {
                String varName = (String)varIt.next();
                Local curVar = (Local)localIt.next();
                units.addLast(Jimple.v().newAssignStmt(curVarNegBindings,
                        Jimple.v().newInstanceFieldRef(thisLocal, 
                                Scene.v().makeFieldRef(disjunct, "not$" + varName, setType, false))));
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
                // if(this.not_var.contains(var1)) return false;
                // Note that this relies on MyWeakRef.equals() returning true if it has a weak
                // reference to var1.
                Local booleanLocal = lgen.generateLocal(BooleanType.v(), "booleanLocal");
                units.addLast(Jimple.v().newAssignStmt(booleanLocal, Jimple.v().newInterfaceInvokeExpr(curVarNegBindings,
                        Scene.v().makeMethodRef(Scene.v().getSootClass("java.util.Set"), "contains",
                                singleObjectParameter, BooleanType.v(), false), curVar)));
                units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(booleanLocal, IntConstant.v(1)), 
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
            units.addLast(Jimple.v().newLookupSwitchStmt(stateNumber, switchValues, labels, labelThrowException));
            
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
                // result = copy();
                units.addLast(Jimple.v().newAssignStmt(result, 
                        Jimple.v().newVirtualInvokeExpr(thisLocal, 
                                Scene.v().makeMethodRef(disjunct, "copy", new LinkedList(), 
                                        disjunct.getType(), false))));
                // for each variable..
                varIt = variableSet.iterator();
                // IMPORTANT ASSUMPTION -- variableLocalsList is iterated in the order in which
                // elements are added and variableSet is iterated in the same order as last time,
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
                                IntConstant.v(0)));
                        // result.var$var = new MyWeakRef(var);
                        units.addLast(Jimple.v().newAssignStmt(weakRef, 
                                Jimple.v().newNewExpr(myWeakRef.getType())));
                        units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(weakRef,
                                Scene.v().makeConstructorRef(myWeakRef, singleObjectParameter),
                                curVar)));
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
            // unfinished business -- still have two labels we haven't inserted:
            units.addLast(labelReturnFalse);
            
            Local falseDisjunct = lgen.generateLocal(disjunct.getType(), "falseDisjunct");
            units.addLast(Jimple.v().newAssignStmt(falseDisjunct, Jimple.v().newStaticFieldRef(
                    Scene.v().makeFieldRef(disjunct, "falseD", disjunct.getType(), true))));
            units.addLast(Jimple.v().newReturnStmt(falseDisjunct));

            // we really shouldn't need this label -- it's there for the default jump of the switch.
            // For now, it just returns false.
            units.addLast(labelThrowException);
            
            // throwable = new RuntimeException("AddDisjunctAddBindings got invalid state number N");
            parameters.clear();
            parameters.add(RefType.v("java.lang.String"));
            Local throwable = lgen.generateLocal(RefType.v("java.lang.Throwable"), "exception");
            units.addLast(Jimple.v().newAssignStmt(throwable, Jimple.v().newNewExpr(
                    RefType.v("java.lang.RuntimeException"))));
            units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(throwable, 
                    Scene.v().makeConstructorRef(Scene.v().getSootClass("java.lang.RuntimeException"),
                    parameters), StringConstant.v("AddDisjunctAddBindingsForSymbol got invalid state number " + stateNumber))));
            units.addLast(Jimple.v().newThrowStmt(throwable));
        }
    }
    
    protected void addDisjunctAddNegBindingsForSymbolMethods(TraceMatch tm, SootClass disjunct, SootClass myWeakRef) {
        RefType objectType = RefType.v("java.lang.Object");
        RefType setType = RefType.v("java.util.Set");
        List singleObjectParameter = new LinkedList();
        singleObjectParameter.add(objectType);
        Iterator symbolIt = tm.getSym_to_vars().keySet().iterator();
        while(symbolIt.hasNext()) {
            String symbolName = (String)symbolIt.next();
            List parameters = new LinkedList();
            
            // take state number for legacy reasons..
            parameters.add(IntType.v());
            
            Set variableSet = (Set)tm.getSym_to_vars().get(symbolName);
            for(int i = 0; i < variableSet.size(); i++) parameters.add(objectType);
            SootMethod symbolMethod = new SootMethod("addNegativeBindingsForSymbol" + symbolName,
                    parameters, disjunct.getType(), Modifier.PUBLIC);
            Body b = Jimple.v().newBody(symbolMethod);
            symbolMethod.setActiveBody(b);
            disjunct.addMethod(symbolMethod);
            
            LocalGeneratorEx lgen = new LocalGeneratorEx(b);
            Local thisLocal = lgen.generateLocal(disjunct.getType(), "thisLocal");
            Local curVarNegBindings = lgen.generateLocal(setType, "curNegBindings");
            Local result = lgen.generateLocal(disjunct.getType(), "result");
            Local weakRef = lgen.generateLocal(myWeakRef.getType(), "weakRef");
            
            Chain units = b.getUnits();
            units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(disjunct.getType())));
            
            List variableLocalsList = new LinkedList();
            int parameterIndex = 0;
            
            // XXX since we take the state number as a first parameter and don't need it
            parameterIndex = 1;
            ////////////////////////////////////////////////////////////////////////////
            
            Iterator varIt = variableSet.iterator();
            while(varIt.hasNext()) {
                String varName = (String)varIt.next();
                // add local for this variable and identity statement to store the parameter
                Local curVar = lgen.generateLocal(objectType, varName);
                units.addLast(Jimple.v().newIdentityStmt(curVar, 
                        Jimple.v().newParameterRef(objectType, parameterIndex)));
                variableLocalsList.add(curVar);
                parameterIndex++;
            }
            
            // label to jump to if new bindings are incompatible
            Stmt labelReturnFalse = Jimple.v().newNopStmt();
            
            // now we have all the locals. Generate the code for this method.
            // The disjunct is only incompatible if a variable is bound to a value that is
            // passed as a parameter to add as a negative binding.
            varIt = variableSet.iterator();
            Iterator localIt = variableLocalsList.iterator();
            while(varIt.hasNext()) {
                String varName = (String)varIt.next();
                Local curVar = (Local)localIt.next();
                Stmt labelCheckNextVar = Jimple.v().newNopStmt();
                Local thisBound = lgen.generateLocal(BooleanType.v(), "thisBound");
                units.addLast(Jimple.v().newAssignStmt(thisBound,
                        Jimple.v().newInstanceFieldRef(thisLocal, 
                                Scene.v().makeFieldRef(disjunct, varName + "$isBound", BooleanType.v(), false))));
                units.addLast(Jimple.v().newIfStmt(Jimple.v().newNeExpr(thisBound, IntConstant.v(1)),
                        labelCheckNextVar));
                // TODO -- implicit assumption that the name and variable lists are ordered identically.
                // make sure it's valid.
                // TODO -- all calls to get$ could be avoided if we specialised this method to know
                // whether each var is bound weakly or strongly
                Local tmpObject = lgen.generateLocal(objectType, "tmpObject");
                units.addLast(Jimple.v().newAssignStmt(tmpObject, Jimple.v().newVirtualInvokeExpr(thisLocal, 
                        Scene.v().makeMethodRef(disjunct, "get$" + varName, new LinkedList(),
                                objectType, false))));
                units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(tmpObject, curVar), 
                        labelReturnFalse));
                units.addLast(labelCheckNextVar);
            }
            
            // OK, if we fall through here then this disjunct is compatible with the new bindings,
            // otherwise we would have jumped to labelReturnFalse.
            
            // Adding negative bindings is easier than positive bindings, as it doesn't depend on
            // the state we're in. If the new negative bindings are consistent (i.e. if they aren't
            // equal to positive bindings already on the state), then adding them can be done by
            // simply adding each binding to the negative bindings set of the respective variable.
            
            // result = copy();
            units.addLast(Jimple.v().newAssignStmt(result, 
                    Jimple.v().newVirtualInvokeExpr(thisLocal, 
                            Scene.v().makeMethodRef(disjunct, "copy", new LinkedList(), 
                                    disjunct.getType(), false))));
            // for each variable..
            varIt = variableSet.iterator();
            // IMPORTANT ASSUMPTION -- variableLocalsList is iterated in the order in which
            // elements are added and variableSet is iterated in the same order as last time,
            // so that the correspondence between variable name and local variable storing 
            // the relevant parameter is preserved.
            localIt = variableLocalsList.iterator();
            while(varIt.hasNext()) {
                String varName = (String)varIt.next();
                Local curVar = (Local)localIt.next();
                // if the current variable is bound, then we do not want to accumulate
                // negative bindings
                Stmt labelHandleNextVar = Jimple.v().newNopStmt();
                Local thisBound = lgen.generateLocal(BooleanType.v(), "thisBound");
                units.addLast(Jimple.v().newAssignStmt(thisBound, Jimple.v().newInstanceFieldRef(thisLocal, 
                        Scene.v().makeFieldRef(disjunct, varName + "$isBound", BooleanType.v(), false))));
                units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(thisBound, IntConstant.v(1)),
                        labelHandleNextVar));
                // otherwise, add the new binding to the set:
                // 1. Store not_var in a local
                units.addLast(Jimple.v().newAssignStmt(curVarNegBindings, 
                        Jimple.v().newInstanceFieldRef(result,
                        Scene.v().makeFieldRef(disjunct, "not$" + varName, setType, false))));
                // 2. create a new MyWeakRef(curVar)
                units.addLast(Jimple.v().newAssignStmt(weakRef, Jimple.v().newNewExpr(myWeakRef.getType())));
                units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(weakRef, 
                        Scene.v().makeConstructorRef(myWeakRef, singleObjectParameter),
                        curVar)));
                // 3. not_var.add(weakRef).
                units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(curVarNegBindings,
                        Scene.v().makeMethodRef(Scene.v().getSootClass("java.util.Set"), 
                                "add", singleObjectParameter, BooleanType.v(), false),
                        weakRef)));
                units.addLast(labelHandleNextVar);
            }
            // now all the new bindings are recorded -- return the result
            units.addLast(Jimple.v().newReturnStmt(result));

            // unfinished business
            units.addLast(labelReturnFalse);
            
            Local falseDisjunct = lgen.generateLocal(disjunct.getType(), "falseDisjunct");
            units.addLast(Jimple.v().newAssignStmt(falseDisjunct, Jimple.v().newStaticFieldRef(
                    Scene.v().makeFieldRef(disjunct, "falseD", disjunct.getType(), true))));
            units.addLast(Jimple.v().newReturnStmt(falseDisjunct));
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
                    Scene.v().makeFieldRef(disjunct, varName + "$isBound", objectType, false))));
            units.addLast(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(result, 
                    Scene.v().makeFieldRef(disjunct, varName + "$isBound", objectType, false)),
                tmpBound));
            // resuult.X$isWeak = this.X$isWeak;
            Local tmpWeak = lgen.generateLocal(BooleanType.v(), "tmpWeak");
            units.addLast(Jimple.v().newAssignStmt(tmpWeak, Jimple.v().newInstanceFieldRef(thisLocal,
                    Scene.v().makeFieldRef(disjunct, varName + "$isWeak", objectType, false))));
            units.addLast(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(result, 
                    Scene.v().makeFieldRef(disjunct, varName + "$isWeak", objectType, false)),
                tmpWeak));
            // If variable isn't bound, we need to keep track of negative bindings
            Stmt labelNextVar = Jimple.v().newNopStmt();
            units.addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(tmpBound, IntConstant.v(1)),
                    labelNextVar));
            // not_X has been set to a new LinkedHashSet in the constructor -- potentially we
            // could consider keeping it null by only initialising it here.. TODO
            Jimple.v().newAssignStmt(curNegBindings, Jimple.v().newInstanceFieldRef(result,
                    Scene.v().makeFieldRef(disjunct, "not$" + varName, setType, false)));
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
        Jimple.v().newReturnStmt(result);
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
            Local throwable = lgen.generateLocal(RefType.v("java.lang.Throwable"), "exception");
            
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
            units.addLast(Jimple.v().newAssignStmt(weakRef, Jimple.v().newInstanceFieldRef(thisLocal,
                    Scene.v().makeFieldRef(disjunct, "var$" + varName, objectType, false))));
            units.addLast(Jimple.v().newAssignStmt(result, Jimple.v().newVirtualInvokeExpr(weakRef,
                    Scene.v().makeMethodRef(Scene.v().getSootClass("java.lang.ref.WeakReference"),
                            "get", new LinkedList(), objectType, false))));
            units.addLast(Jimple.v().newReturnStmt(result));
            
            // finally, the exception throwing code:
            units.addLast(labelThrowException);
            // throwable = new RuntimeException("Attempt to get an unbound variable");
            List parameters = new LinkedList();
            parameters.add(RefType.v("java.lang.String"));
            units.addLast(Jimple.v().newAssignStmt(throwable, Jimple.v().newNewExpr(
                    RefType.v("java.lang.RuntimeException"))));
            units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(throwable, 
                    Scene.v().makeConstructorRef(Scene.v().getSootClass("java.lang.RuntimeException"),
                    parameters), StringConstant.v("Attempt to get an unbound variable: " + varName))));
            units.addLast(Jimple.v().newThrowStmt(throwable));
        }
    }

    protected void addDisjunctInitialiser(TraceMatch tm, SootClass disjunct) {
        SootMethod init = new SootMethod(SootMethod.constructorName, new LinkedList(), 
                VoidType.v(), Modifier.STATIC);
        Body b = Jimple.v().newBody(init);
        init.setActiveBody(b);
        disjunct.addMethod(init);

        LocalGeneratorEx lgen = new LocalGeneratorEx(b);
        Local tempSet = lgen.generateLocal(RefType.v("java.util.Set"), "tempSet");
        Local thisLocal = lgen.generateLocal(disjunct.getType(), "thisLocal");
        Chain units = b.getUnits();
        units.addLast(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(disjunct.getType())));
        
        // For now -- initialise each not$X set with a new set on construction.
        List varNames = tm.getFormalNames();
        Iterator varIt = varNames.iterator();
        while(varIt.hasNext()) {
            String varName = (String)varIt.next();
            units.addLast(Jimple.v().newAssignStmt(tempSet, 
                    Jimple.v().newNewExpr(RefType.v("java.util.LinkedHashSet"))));
            units.addLast(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(tempSet,
                    Scene.v().makeConstructorRef(Scene.v().getSootClass("java.util.LinkedHashSet"),
                            new LinkedList()))));
            units.addLast(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(thisLocal,
                    Scene.v().makeFieldRef(disjunct, "not$" + varName, 
                            RefType.v("java.util.Set"), false)), tempSet));
        }
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

            while (edges.hasNext()) {
                SMEdge edge = (SMEdge) edges.next();

                if (edge.getLabel().equals(symbol)) {
                    SMNode from = (SMNode) edge.getSource();

                    helper.genLabelUpdate(from.getNumber(), to.getNumber(),
                                            edge.getLabel(), method);
                }
            }

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
    	

    	
    	Body body = sm.getActiveBody();
    	Unit u = null;
    	Chain units = body.getUnits();
    	for (Iterator unitIter = units.iterator(); unitIter.hasNext(); ) {
    		u = (Unit) unitIter.next();
			if (u instanceof InvokeStmt) {
				InvokeStmt is = (InvokeStmt) u;
				InvokeExpr ie = is.getInvokeExpr();
				String name = ie.getMethodRef().name();
				if  (name.equals("proceed"))
					break;
			} else u = null;
    	}
    
    	if (u == null) return;
		
		SootClass scIter = Scene.v().getSootClass("java.util.Iterator");
		paramTypes.add(scIter.getType());
		sm.setParameterTypes(paramTypes);
		
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
        
        // Fill in the advice bodies. The method stubs have been created by the frontend and
        // can be obtained from the TraceMatch object; code to keep track of changing 
        // constraints and to run the tracematch advice when appropriate, with the necessary
        // bindings, should be added.
        fillInAdviceBodies(tm, unused);
    }
}
