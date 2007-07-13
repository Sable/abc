/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
 * Copyright (C) 2007 Patrick Lam
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
package abc.tm.weaving.weaver.tmanalysis.dynainst;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.ArrayType;
import soot.Body;
import soot.BooleanType;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.Type;
import soot.VoidType;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NewArrayExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import abc.main.Debug;
import abc.main.Main;
import abc.soot.util.Restructure;
import abc.tm.weaving.aspectinfo.PerSymbolTMAdviceDecl;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.util.Naming;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolShadow;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.residues.AndResidue;
import abc.weaving.residues.Residue;
import abc.weaving.weaver.AdviceApplicationVisitor;
import abc.weaving.weaver.Unweaver;
import abc.weaving.weaver.AdviceApplicationVisitor.AdviceApplicationHandler;

/**
 * The {@link DynamicInstrumenter} generates dynamic {@link Residue}s for each
 * shadow that is still enabled. Those residues allow to enable/disable those
 * shadows dynamically by setting a boolean flag.
 * 
 * The mapping from shadow groups to their shadows is encoded in a generated
 * class with name {@link #INITIALIZER_CLASS_NAME}. This mapping is then set
 * into fields of the class with name {@link #SHADOW_SWITCH_CLASS_NAME}. This
 * class is part of the abc runtime (and hence <i>not</i> a generated class).
 * 
 * @author Eric Bodden
 * @author Patrick Lam
 */
public class DynamicInstrumenter {

	/** maps a shadow ID to a unique number for that id */
	protected Map shadowIdToNumber = new HashMap();
	
	/** Name of the shadow switch class in the abc runtime. */
	// do not change this name, unless you change it in AbcExtension, too
	public static final String SHADOW_SWITCH_CLASS_NAME = "org.aspectbench.tm.runtime.internal.ShadowSwitch";

	/**
	 * Name of the class that initializes the data structures in the shadow
	 * switch class. This class is generated.
	 */
	public static final String INITIALIZER_CLASS_NAME = "org.aspectbench.tm.runtime.internal.ShadowSwitchInitializer";

	/** Name of the generic interface of the initializer class. */
	public static final String INITIALIZER_INTERFACE_NAME = "org.aspectbench.tm.runtime.internal.IShadowSwitchInitializer";

	/** Shortcut to the {@link Unweaver}. */
	private static final Unweaver unweaver = Main.v().getAbcExtension()
			.getWeaver().getUnweaver();

	/**
	 * TODO
	 */
	public void createClassesAndSetDynamicResidues() {
		createShadowSwitchInitializerClass();
		
        AdviceApplicationVisitor.v().traverse(
            new AdviceApplicationHandler() {
            
                public void adviceApplication(AdviceApplication aa,SootMethod context) {
                    //if we have a tracematch symbol advice
                    if(aa.advice instanceof PerSymbolTMAdviceDecl) {
                        PerSymbolTMAdviceDecl decl = (PerSymbolTMAdviceDecl) aa.advice;
                        String traceMatchID = decl.getTraceMatchID();
                        String uniqueShadowId = Naming.uniqueShadowID(traceMatchID, decl.getSymbolId(),aa.shadowmatch.shadowId).intern();
                        Residue originalResidue = aa.getResidue();
                        //conjoin residue
                        aa.setResidue(
                                AndResidue.construct(                   
                                        new DynamicInstrumentationResidue(numberOf(uniqueShadowId)),
                                        originalResidue
                                )
                        );          
                    }
                }
            }
        );
	}

    public void insertDumpCall() {
        SootMethod mainMethod = Scene.v().getMainMethod();
        Stmt nopAtReturn = Restructure.restructureReturn(mainMethod);
        Body b = mainMethod.getActiveBody();
        StaticInvokeExpr invokeExpr =
            Jimple.v().newStaticInvokeExpr(
                    Scene.v().makeMethodRef(Scene.v().getSootClass(SHADOW_SWITCH_CLASS_NAME), "dumpShadowCounts", Collections.<Type>emptyList(), VoidType.v(), true)
            );
        InvokeStmt dumpStmt = Jimple.v().newInvokeStmt(invokeExpr);
        b.getUnits().insertAfter(dumpStmt, nopAtReturn);
    }

    /**
	 * Creates a class with a single method that initializes the data structures
	 * in the shadow switch class, i.e. mapping of shadow groups to their
	 * shadows.
	 */
	protected void createShadowSwitchInitializerClass() {
		// public class <INITIALIZER_CLASS_NAME> extends Object implements
		// IShadowSwitchInitializer { ... }
		SootClass switchClass = new SootClass(INITIALIZER_CLASS_NAME,
				Modifier.PUBLIC);
		SootClass superClass = Scene.v().getSootClass("java.lang.Object");
		switchClass.setSuperclass(superClass);
		SootClass interf = Scene.v().getSootClass(INITIALIZER_INTERFACE_NAME);
		switchClass.addInterface(interf);

		generateDefaultConstructor(switchClass, superClass);

		generateInitMethod(switchClass);
	}

	/**
	 * Generates the initialization method. 
	 * @param switchClass the class to attach the method to
	 */
	private void generateInitMethod(SootClass switchClass) {
		// public void initialize() { ... }
		SootMethod initMethod = new SootMethod("initialize",
				Collections.EMPTY_LIST, VoidType.v(), Modifier.PUBLIC);
		Body body = Jimple.v().newBody(initMethod);
		initMethod.setActiveBody(body);
		switchClass.addMethod(initMethod);
		PatchingChain units = body.getUnits();
		// do not weave into this method
		MethodCategory.register(initMethod, MethodCategory.IF_EXPR);

		// thisLocal = @this;
		Local thisLocal = Jimple.v().newLocal("thisLocal",
				switchClass.getType());
		body.getLocals().add(thisLocal);
		units.addLast(Jimple.v().newIdentityStmt(thisLocal,
				Jimple.v().newThisRef(switchClass.getType())));

		// retrieve the set of enabled shadows and the set of all (sound) probes
		Set enabledShadows = ShadowRegistry.v().enabledShadows();
		Set probes = Probe.generateAllSoundProbes();

		// create a new array "grouptable". This is an array of arrays of
		// boolean, i.e. a 2D array.
		// groupTable = newarray (boolean[])[|shadowGroups|];
		Local array = Jimple.v().newLocal("groupTable",
				ArrayType.v(BooleanType.v(), 2));
		body.getLocals().add(array);
		NewArrayExpr newArrayExpr = Jimple.v().newNewArrayExpr(
				ArrayType.v(BooleanType.v(), 1),
				IntConstant.v(probes.size()));
		SootFieldRef fieldRef = Scene.v().makeFieldRef(
				Scene.v().getSootClass(
						DynamicInstrumenter.SHADOW_SWITCH_CLASS_NAME),
				"groupTable", ArrayType.v(BooleanType.v(), 2), true);
		AssignStmt assignStmt = Jimple.v().newAssignStmt(array, newArrayExpr);
		units.add(assignStmt);

		// assign this array to the field in <SHADOW_SWITCH_CLASS_NAME>
		// field "groupTable" = groupTable
		StaticFieldRef staticFieldRef = Jimple.v().newStaticFieldRef(fieldRef);
		AssignStmt fieldAssignStmt = Jimple.v().newAssignStmt(staticFieldRef,
				array);
		units.add(fieldAssignStmt);

		// create an array of boolean "enabledShadows"
		// enabled = newarray (boolean)[|enabledShadows|];
		Local enabledArray = soot.jimple.Jimple.v().newLocal("enabled",
				ArrayType.v(BooleanType.v(), 1));
		body.getLocals().add(enabledArray);
		NewArrayExpr newEnabledArrayExpr = Jimple.v().newNewArrayExpr(
				BooleanType.v(), IntConstant.v(enabledShadows.size()));
		assignStmt = Jimple.v()
				.newAssignStmt(enabledArray, newEnabledArrayExpr);
		units.add(assignStmt);

		// assign this array to the field in <SHADOW_SWITCH_CLASS_NAME>
		// field "enabled" = enabled
		SootFieldRef enabledFieldRef = Scene.v().makeFieldRef(
				Scene.v().getSootClass(
						DynamicInstrumenter.SHADOW_SWITCH_CLASS_NAME),
				"enabled", ArrayType.v(BooleanType.v(), 1), true);
		staticFieldRef = Jimple.v().newStaticFieldRef(enabledFieldRef);
		fieldAssignStmt = Jimple.v()
				.newAssignStmt(staticFieldRef, enabledArray);
		units.add(fieldAssignStmt);
		
		// create an array of int "counts"
		// counts = newarray (int)[|enabledShadows|];
		Local countsArray = soot.jimple.Jimple.v().newLocal("counts",
		        ArrayType.v(IntType.v(), 1));
		body.getLocals().add(countsArray);
		NewArrayExpr newCountsArrayExpr = Jimple.v().newNewArrayExpr(
		        IntType.v(), IntConstant.v(enabledShadows.size()));
		assignStmt = Jimple.v()
		        .newAssignStmt(countsArray, newCountsArrayExpr);
		units.add(assignStmt);
	
		// assign this array to the field in <SHADOW_SWITCH_CLASS_NAME>
		// field "counts" = counts
		SootFieldRef countsFieldRef = Scene.v().makeFieldRef(
		        Scene.v().getSootClass(
		                DynamicInstrumenter.SHADOW_SWITCH_CLASS_NAME),
		        "counts", ArrayType.v(IntType.v(), 1), true);
		staticFieldRef = Jimple.v().newStaticFieldRef(countsFieldRef);
		fieldAssignStmt = Jimple.v()
		        .newAssignStmt(staticFieldRef, countsArray);
		units.add(fieldAssignStmt);			

		// initialize the array "groupTable"
		// for all shadow groups
		for (Iterator groupIter = probes.iterator(); groupIter.hasNext();) {
			Probe probe = (Probe) groupIter.next();

			// create a method that initializes the array for this single group,
			// i.e. one dimension of the array;
			// this is done, because otherwise this method could easily grow too
			// large (>65535 bytes)
			SootMethod methodForGroup = createMethodForGroup(probe,
					switchClass, enabledShadows);

			// create a call to this method
			VirtualInvokeExpr invokeExpr = Jimple.v().newVirtualInvokeExpr(thisLocal, methodForGroup.makeRef());
			units.add(Jimple.v().newInvokeStmt(invokeExpr));
		}

		// have to add a return statement to make the method complete
		units.add(Jimple.v().newReturnVoidStmt());

		Scene.v().addClass(switchClass);
		switchClass.setApplicationClass();

		// make sure that the unweaver does not remove the newly added class
		// again when restoring the state
		unweaver.retainAddedClass(switchClass);

		if (Debug.v().doValidate) {
			body.validate();
		}
	}

	/**
	 * Generates a default constructor.
	 * @param switchClass the class to attach the constructor to
	 * @param superClass the super class whose constructor is called
	 */
	private void generateDefaultConstructor(SootClass switchClass,
			SootClass superClass) {
		// default constructor
		SootMethod constructor = new SootMethod(SootMethod.constructorName,
				Collections.EMPTY_LIST, VoidType.v(), Modifier.PUBLIC);
		Body body = Jimple.v().newBody(constructor);
		constructor.setActiveBody(body);
		switchClass.addMethod(constructor);
		// thisLocal = @this;
		Local thisLocal = Jimple.v().newLocal("thisLocal",
				switchClass.getType());
		body.getLocals().add(thisLocal);
		body.getUnits().addLast(
				Jimple.v().newIdentityStmt(thisLocal,
						Jimple.v().newThisRef(switchClass.getType())));
		// Object.<init>(); //=super();
		body.getUnits().addLast(
				Jimple.v().newInvokeStmt(
						Jimple.v().newSpecialInvokeExpr(
								thisLocal,
								Scene.v().makeConstructorRef(superClass,
										Collections.EMPTY_LIST))));
		// return;
		body.getUnits().add(Jimple.v().newReturnVoidStmt());
		// do not weave into this constructor
		MethodCategory.register(constructor, MethodCategory.IF_EXPR);

		if (Debug.v().doValidate) {
			body.validate();
		}
	}

	/**
	 * Generates a method which initializes a one dimension of the group array.
	 * @param probe the probe to initialize
	 * @param switchClass the class to which the method is attached
	 * @param enabledShadows the set of all enabled shadows
	 */
	private SootMethod createMethodForGroup(Probe probe,
			SootClass switchClass, Set enabledShadows) {
		SootMethod method = new SootMethod("initGroup$" + probe.getNumber(),
				Collections.EMPTY_LIST, VoidType.v(), Modifier.PRIVATE);
		switchClass.addMethod(method);
		Body body = Jimple.v().newBody(method);
		method.setActiveBody(body);
		PatchingChain units = body.getUnits();

		// get the IDs of all shadows in the group and the unique group number
		Set shadowsOfGroup = SymbolShadow.uniqueShadowIDsOf(probe.getShadows());
		int groupNumber = probe.getNumber();

		//boolean[][] groupTable = ShadowSwitch.groupTable 
		Local array = Jimple.v().newLocal("groupTable",
				ArrayType.v(BooleanType.v(), 2));
		body.getLocals().add(array);
		SootFieldRef fieldRef = Scene.v().makeFieldRef(
				Scene.v().getSootClass(
						DynamicInstrumenter.SHADOW_SWITCH_CLASS_NAME),
				"groupTable", ArrayType.v(BooleanType.v(), 2), true);
		StaticFieldRef staticFieldRef = Jimple.v().newStaticFieldRef(fieldRef);
		AssignStmt fieldAssignStmt = Jimple.v().newAssignStmt(array,
				staticFieldRef);
		units.add(fieldAssignStmt);

		// create a new local "boolean[] innerArray$groupNumber" which is
		// assigned an array of boolean;
		// this array represents the current shadow group
		// boolean[] innerArray = newarray (boolean)[|shadowsOfGroup|]
		Local innerArray = soot.jimple.Jimple.v().newLocal("innerArray",
				ArrayType.v(BooleanType.v(), 1));
		body.getLocals().add(innerArray);
		NewArrayExpr newInnerArrayExpr = Jimple.v().newNewArrayExpr(
				BooleanType.v(), IntConstant.v(enabledShadows.size()));
		AssignStmt innerAssignStmt = Jimple.v().newAssignStmt(innerArray,
				newInnerArrayExpr);
		units.add(innerAssignStmt);

		// initialize this onedimensional array
		// for all (enabled) shadows...
		for (Iterator shadowIter = enabledShadows.iterator(); shadowIter
				.hasNext();) {
			String shadowId = (String) shadowIter.next();

			// get the unique shadow number (starts at 0, so it can be used to
			// index the array)
			int shadowNumber = numberOf(shadowId);
			// if the shadow in the current group?
			boolean isInGroup = shadowsOfGroup.contains(shadowId);

			// innerArray[shadowNumber] = isInGroup
			ArrayRef innerArrayRef = Jimple.v().newArrayRef(innerArray,
					IntConstant.v(shadowNumber));
			AssignStmt valAssignStmt = Jimple.v().newAssignStmt(innerArrayRef,
					IntConstant.v(isInGroup ? 1 : 0));
			units.add(valAssignStmt);
		}

		// assign the array into the twodimensional array
		// groupTable[groupNumber] = innerArray
		ArrayRef arrayRef = Jimple.v().newArrayRef(array,
				IntConstant.v(groupNumber));
		AssignStmt arrayAssignStmt = Jimple.v().newAssignStmt(arrayRef,
				innerArray);
		units.add(arrayAssignStmt);

		// have to add a return statement to make the method complete
		units.add(Jimple.v().newReturnVoidStmt());

		if (Debug.v().doValidate) {
			body.validate();
		}
		return method;
	}

	
	/**
	 * Returns a unique int number for the given shadow id.
	 * The numbers start with 0 and then increase by 1.
	 * @param uniqueShadowId
	 * @return
	 */
	protected int numberOf(String uniqueShadowId) {
		Integer number = (Integer) shadowIdToNumber.get(uniqueShadowId);
		if(number==null) {
			number = new Integer(shadowIdToNumber.size());
			shadowIdToNumber.put(uniqueShadowId, number);
		}
		if(Debug.v().debugTmAnalysis) {
		    System.err.println("number of dynamic residue for "+uniqueShadowId+": "+number);
		}
		return number;
	}
	
	//singleton pattern
	
	protected static DynamicInstrumenter instance;

	private DynamicInstrumenter() {
	}

	public static DynamicInstrumenter v() {
		if (instance == null) {
			instance = new DynamicInstrumenter();
		}
		return instance;
	}
}
