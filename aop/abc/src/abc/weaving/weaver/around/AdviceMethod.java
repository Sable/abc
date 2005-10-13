/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Sascha Kuzins
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

package abc.weaving.weaver.around;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.main.Debug;
import abc.main.options.OptionsParser;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.Formal;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.ExecutionAdviceApplication;
import abc.weaving.weaver.around.AroundWeaver.ObjectBox;

public class AdviceMethod {
	public final AroundWeaver aroundWeaver;

	AdviceMethod(AroundWeaver aroundWeaver, SootMethod method,
			final List originalAdviceFormalTypes, final List proceedSootMethods) {

		this.aroundWeaver = aroundWeaver;

		if (originalAdviceFormalTypes == null)
			throw new InternalAroundError();
		this.originalAdviceFormalTypes = originalAdviceFormalTypes;

		if (proceedSootMethods == null)
			throw new InternalAroundError();

		this.sootAdviceMethod = method;
		this.adviceBody = method.getActiveBody();
		this.adviceStatements = adviceBody.getUnits().getNonPatchingChain();
		//this.adviceDecl = adviceDecl;
		for (int i = 0; i < contextArgsByType.length; i++) {
			contextArgsByType[i] = new LinkedList();
		}

		String aspectName = getAspect().getName();
		String shortAspectName = getAspect().getShortName();
		String packageName = getAspect().getPackageName();
		String mangledAspectName = Util.mangleTypeName(aspectName);

		adviceMethodIdentifierString = mangledAspectName + "$"
				+ method.getName();

		interfaceName = (packageName.length() == 0 ? "" : packageName + ".")
				+ "Abc$proceed$" + adviceMethodIdentifierString;

		instanceProceedMethodName = "abc$proceed$"
				+ adviceMethodIdentifierString;
		

		proceedMethodParameterTypes.add(IntType.v()); // the shadow id
		proceedMethodParameterTypes.add(IntType.v()); // the bind mask

		{
			List allProceedMethodParameters = new LinkedList();
			allProceedMethodParameters.addAll(originalAdviceFormalTypes);
			allProceedMethodParameters.addAll(proceedMethodParameterTypes);

			interfaceInfo = new InterfaceInfo();

			interfaceInfo.closureInterface = createClosureInterface(allProceedMethodParameters);
			interfaceInfo.abstractProceedMethod = interfaceInfo.closureInterface
					.getMethodByName(instanceProceedMethodName);
		}

		if (!proceedSootMethods.contains(sootAdviceMethod))
			proceedSootMethods.add(sootAdviceMethod);

		if (Debug.v().aroundWeaver) {
			AroundWeaver.debug("QQQQQQQQQQQQQQ Methods for advice-method"
					+ sootAdviceMethod + "\n");
			for (Iterator it = proceedSootMethods.iterator(); it.hasNext();) {
				SootMethod m = (SootMethod) it.next();
				AroundWeaver.debug(" " + m);

			}
		}

		//	Add all the proceed classes				
		for (Iterator it = proceedSootMethods.iterator(); it.hasNext();) {
			SootMethod m = (SootMethod) it.next();
			if (!adviceLocalClasses.containsKey(m.getDeclaringClass())) {
				AdviceLocalClass newClass = new AdviceLocalClass(this, m
						.getDeclaringClass());
				adviceLocalClasses.put(m.getDeclaringClass(), newClass);

				while (newClass != null && !newClass.isAspect()
						&& !newClass.isFirstDegree()) {
					SootClass enclosing = newClass.getEnclosingSootClass();

					if (!adviceLocalClasses.containsKey(enclosing)) {
						newClass = new AdviceLocalClass(this, enclosing);
						adviceLocalClasses.put(enclosing, newClass);
					} else
						newClass = null;
				}
			}
		}
		// add the corresponding methods.
		// important: keep this order. all classes have to be added first.
		{
			for (Iterator it = proceedSootMethods.iterator(); it.hasNext();) {
				SootMethod m = (SootMethod) it.next();
				if (!m.equals(method)) {
					AdviceLocalClass pl = (AdviceLocalClass) adviceLocalClasses
							.get(m.getDeclaringClass());
					pl.addAdviceLocalMethod(m);
				}
			}
			// add advice method itself last.
			AdviceLocalClass pl = (AdviceLocalClass) adviceLocalClasses
					.get(method.getDeclaringClass());
			pl.addAdviceLocalMethod(method);
		}
		{
			for (Iterator it = adviceLocalClasses.values().iterator(); it
					.hasNext();) {
				AdviceLocalClass pc = (AdviceLocalClass) it.next();
				if (pc.isFirstDegree())
					pc.addDefaultParameters();
			}
			for (Iterator it = adviceLocalClasses.values().iterator(); it
					.hasNext();) {
				AdviceLocalClass pc = (AdviceLocalClass) it.next();
				if (!pc.isAspect() && !pc.isFirstDegree())
					pc.addDefaultParameters();
			}
			for (Iterator it = adviceLocalClasses.values().iterator(); it
					.hasNext();) {
				AdviceLocalClass pc = (AdviceLocalClass) it.next();
				if (pc.isAspect())
					pc.addDefaultParameters();
			}
		}
		if (Debug.v().aroundWeaver) {
			AroundWeaver.debug("QQQQQQQQQQQQQQ Classes for advice-method"
					+ sootAdviceMethod + "\n");
			for (Iterator it = adviceLocalClasses.values().iterator(); it
					.hasNext();) {
				AdviceLocalClass pc = (AdviceLocalClass) it.next();
				AroundWeaver.debug(" " + pc.sootClass);

			}
		}
	}

	public void doWeave(AdviceApplication adviceAppl, SootMethod shadowMethod) {
		final boolean bExecutionAdvice = adviceAppl instanceof ExecutionAdviceApplication;

		final boolean bExecutionWeavingIntoSelf = bExecutionAdvice
				&& (sootAdviceMethod.equals(shadowMethod) || (AroundWeaver.v()
						.getEnclosingAroundAdviceMethod(shadowMethod) != null && AroundWeaver
						.v().getEnclosingAroundAdviceMethod(shadowMethod)
						.equals(sootAdviceMethod)));

		boolean bStaticShadowMethod = shadowMethod.isStatic();
		boolean bUseClosureObject;

		final boolean bAlwaysUseClosures;

		/*if (Debug.v().aroundWeaver)	{
		 bAlwaysUseClosures=false;//false; // change this to suit your debugging needs...
		 } else {
		 bAlwaysUseClosures=false; // don't change this!
		 }*/
		bAlwaysUseClosures = OptionsParser.v().around_force_closures();

		if (bHasBeenWovenInto || bExecutionWeavingIntoSelf)
			bUseClosureObject = true;
		else
			bUseClosureObject = bAlwaysUseClosures;

		final boolean bUseStaticProceedMethod = bStaticShadowMethod
				|| bAlwaysStaticProceedMethod;

		String proceedMethodName;
		if (bUseClosureObject) {
			proceedMethodName = "abc$closure$proceed$"
					+ adviceMethodIdentifierString + "$"
					+ AroundWeaver.v().getUniqueID();
		} else {
			if (bUseStaticProceedMethod) {
				proceedMethodName = "abc$static$proceed$"
						+ adviceMethodIdentifierString;
			} else {
				proceedMethodName = instanceProceedMethodName;
			}
		}

		ProceedMethod proceedMethod = null;
		if (!bUseClosureObject) {
			proceedMethod = getProceedMethod(shadowMethod.getDeclaringClass()
					.getName(), bUseStaticProceedMethod);
		}
		if (proceedMethod == null) {
			proceedMethod = new ProceedMethod(this, shadowMethod
					.getDeclaringClass(), bUseStaticProceedMethod,
					proceedMethodName, bUseClosureObject);

			if (bUseClosureObject)
				setClosureProceedMethod(proceedMethod);
			else
				setProceedMethod(shadowMethod.getDeclaringClass().getName(),
						bUseStaticProceedMethod, proceedMethod);
		}

		proceedMethod.doWeave(adviceAppl, shadowMethod);
	}

	public void validate() {
		{
			Iterator it = adviceMethodInvocationStmts.iterator();

			while (it.hasNext()) {
				Stmt stmt = (Stmt) it.next();
				if (stmt.getInvokeExpr().getArgCount() != sootAdviceMethod
						.getParameterCount()) {
					throw new InternalAroundError("Call to advice method "
							+ sootAdviceMethod
							+ " has wrong number of arguments: " + stmt);
				}
			}
		}
		for (Iterator it0 = adviceLocalClasses.values().iterator(); it0
				.hasNext();) {
			AdviceLocalClass c = (AdviceLocalClass) it0.next();

			for (Iterator it1 = c.adviceLocalMethods.iterator(); it1.hasNext();) {
				AdviceLocalMethod m = (AdviceLocalMethod) it1
						.next();

				Iterator it = m.interfaceInvocationStmts.iterator();

				while (it.hasNext()) {
					Stmt stmt = (Stmt) it.next();
					if (stmt.getInvokeExpr().getArgCount() != interfaceInfo.abstractProceedMethod
							.getParameterCount()) {
						throw new InternalAroundError(
								"Call to interface method in advice method "
										+ sootAdviceMethod
										+ " has wrong number of arguments: "
										+ stmt);
					}
				}
			}
		}
		{
			List proceedMethodImplementations = getAllProceedMethods();
			Iterator it = proceedMethodImplementations.iterator();
			while (it.hasNext()) {
				ProceedMethod info = (ProceedMethod) it.next();
				if (!info.bUseClosureObject) {
					if (info.sootProceedMethod.getParameterCount() != interfaceInfo.abstractProceedMethod
							.getParameterCount()) {
						throw new InternalAroundError("Access method "
								+ info.sootProceedMethod
								+ " has wrong number of arguments.");
					}
				}
			}
		}
		{
			Iterator it = closureProceedMethods.iterator();
			while (it.hasNext()) {
				SootMethod method = (SootMethod) it.next();
				if (method.getParameterCount() != interfaceInfo.abstractProceedMethod
						.getParameterCount()) {
					throw new InternalAroundError("Closure method " + method
							+ " has wrong number of arguments.");
				}
			}
		}
	}

	/**
	 * Called when a new access method has been added to a class.
	 * Looks at all other access methods of the advice method and 
	 * adds/changes super() calls where necessary
	 * @param interfaceName
	 * @param newAccessClass
	 */
	public void fixProceedMethodSuperCalls(SootClass newAccessClass) {

		if (!proceedMethodImplementations.containsKey(newAccessClass.getName()))
			throw new InternalAroundError();

		Set keys = proceedMethodImplementations.keySet();

		boolean bAddSuperToNewMethod = false;
		{ // determine if the class that houses the new access method has any base classes 
			// that implement the method 
			Iterator it = keys.iterator();
			while (!bAddSuperToNewMethod && it.hasNext()) {
				String className = (String) it.next();
				SootClass cl = Scene.v().getSootClass(className);
				if (Restructure.isBaseClass(cl, newAccessClass)) {
					bAddSuperToNewMethod = true;
				}
			}
		}

		// Iterate over all classes that implement the interface
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			String className = (String) it.next();
			ProceedMethod accessInfo = (ProceedMethod) proceedMethodImplementations
					.get(className);

			SootClass cl = Scene.v().getSootClass(className);
			// if the class is a sub-class of the new class or 
			// if this is the new class and we need to add a super to the new class
			if (Restructure.isBaseClass(newAccessClass, cl)
					|| (className.equals(newAccessClass.getName()) && bAddSuperToNewMethod)) {
				if (accessInfo.superCallTarget == null
						|| // if the class has no super() call 
						Restructure.isBaseClass(accessInfo.superCallTarget,
								newAccessClass)) { // or if it's invalid

					// generate new super() call
					Body body = accessInfo.sootProceedMethod.getActiveBody();
					Chain statements = body.getUnits().getNonPatchingChain();
					Type returnType = accessInfo.sootProceedMethod
							.getReturnType();

					// find super class that implements the interface.
					// This is the target class of the super call.
					accessInfo.superCallTarget = cl.getSuperclass();
					while (!keys.contains(accessInfo.superCallTarget.getName())) {
						try {
							accessInfo.superCallTarget = accessInfo.superCallTarget
									.getSuperclass();
						} catch (InternalAroundError e) {
							System.err.println("Class: "
									+ accessInfo.superCallTarget);
							throw e;
						}
					}

					Util.removeStatements(body, accessInfo.defaultTarget,
							accessInfo.defaultEnd, null);
					LocalGeneratorEx lg = new LocalGeneratorEx(body);
					Local lThis = body.getThisLocal();

					String proceedMethodName = accessInfo.sootProceedMethod
							.getName();
					Util.validateMethod(accessInfo.sootProceedMethod);
					SpecialInvokeExpr ex = Jimple.v().newSpecialInvokeExpr(
							lThis,
							accessInfo.superCallTarget.getMethodByName(
									proceedMethodName).makeRef(),
							Util.getParameterLocals(body));

					if (returnType.equals(VoidType.v())) {
						Stmt s = Jimple.v().newInvokeStmt(ex);
						statements.insertBefore(s, accessInfo.defaultEnd);
						statements.insertBefore(Jimple.v().newReturnVoidStmt(),
								accessInfo.defaultEnd);

						accessInfo.superInvokeStmt = s;
					} else {
						Local l = lg.generateLocal(returnType, "retVal");
						AssignStmt s = Jimple.v().newAssignStmt(l, ex);
						statements.insertBefore(s, accessInfo.defaultEnd);
						statements.insertBefore(Jimple.v().newReturnStmt(l),
								accessInfo.defaultEnd);

						accessInfo.superInvokeStmt = s;
					}
					//accessInfo.hasSuperCall=true;	
				}
			}

		}
	}

	public int getStaticDispatchTypeID(Type type) {
		String name = type.toString();
		if (!staticDispatchTypeIDs.containsKey(name)) {
			staticDispatchTypeIDs.put(name, new Integer(
					nextStaticTypeDispatchID++));
		}
		return ((Integer) staticDispatchTypeIDs.get(name)).intValue();
	}

	int nextStaticTypeDispatchID = 1; // 0 is a special value

	final HashMap /*String, int*/staticDispatchTypeIDs = new HashMap();

	private void addParametersToProceedMethodImplementations(
			List addedDynArgsTypes) {
		//Set keys=adviceMethodInfo.proceedMethodImplementations.keySet();
		List proceedMethodImplementations = getAllProceedMethods();
		Iterator it = proceedMethodImplementations.iterator();
		while (it.hasNext()) {
			ProceedMethod info = (ProceedMethod) it.next();

			info.addParameters(addedDynArgsTypes);
		}
	}

	public int[] modifyAdviceMethod(List contextParameters,
			ProceedMethod proceedMethod, ObjectBox contextActualsResult,
			boolean bStaticProceedMethod, boolean bUseClosureObject) {
		//		determine parameter mappings and necessary additions

		/*if (abc.main.Debug.v().aroundWeaver) {
		 try {
		 UnreachableCodeEliminator.v().transform(adviceBody);
		 adviceBody.validate();
		 } catch (RuntimeException e ) {
		 debug("shadow method: " + Util.printMethod(adviceBody.getMethod()));
		 throw e;
		 }
		 }*/

		List /*Type*/addedContextArgsTypes = new LinkedList();

		int[] argIndex;
		if (bUseClosureObject) {
			argIndex = new int[0];
		} else {
			argIndex = determineContextParameterMappings(contextParameters,
					addedContextArgsTypes);
		}

		List contextActuals;
		if (bUseClosureObject) {
			contextActuals = new LinkedList();
		} else {
			contextActuals = getContextActualsList(contextParameters, argIndex);
		}

		// create list of default values for the added arguments
		// (for invocations at other locations)
		if (!bUseClosureObject) {
			List addedDynArgs = Util.getDefaultValues(addedContextArgsTypes);

			addContextParamsToInterfaceDefinition(addedContextArgsTypes);
			modifyAdviceMethodInvocations(addedDynArgs, addedContextArgsTypes);
			modifyDirectInterfaceInvocations(addedDynArgs,
					addedContextArgsTypes);
		}

		/*if (abc.main.Debug.v().aroundWeaver) {
		 try {
		 UnreachableCodeEliminator.v().transform(adviceBody);
		 adviceBody.validate();
		 } catch (RuntimeException e ) {
		 debug("shadow method: " + Util.printMethod(adviceBody.getMethod()));
		 throw e;
		 }
		 }*/

		generateProceedCalls(bStaticProceedMethod, bUseClosureObject,
				proceedMethod);

		if (abc.main.Debug.v().aroundWeaver)
			adviceBody.validate();

		// add parameters to all proceed method implementations
		addParametersToProceedMethodImplementations(addedContextArgsTypes);

		{ // process all classes. the aspect class is processed last.
			for (Iterator it = adviceLocalClasses.values().iterator(); it
					.hasNext();) {
				AdviceLocalClass pc = (AdviceLocalClass) it.next();
				if (pc.isFirstDegree())
					pc.addParameters(addedContextArgsTypes, false);
			}
			for (Iterator it = adviceLocalClasses.values().iterator(); it
					.hasNext();) {
				AdviceLocalClass pc = (AdviceLocalClass) it.next();
				if (!pc.isFirstDegree() && !pc.isAspect())
					pc.addParameters(addedContextArgsTypes, false);
			}
			for (Iterator it = adviceLocalClasses.values().iterator(); it
					.hasNext();) {
				AdviceLocalClass pc = (AdviceLocalClass) it.next();
				if (pc.isAspect())
					pc.addParameters(addedContextArgsTypes, false);
			}
		}

		contextActualsResult.object = contextActuals;
		return argIndex;
	}

	private int[] determineContextParameterMappings(List context,
			List addedDynArgsTypes) {
		int[] argIndex = new int[context.size()];
		{
			int[] currentIndex = new int[Restructure.JavaTypeInfo.typeCount];
			Iterator it = context.iterator();
			int i = 0;
			while (it.hasNext()) {
				Local local = (Local) it.next();
				Type type = local.getType();
				// pass all reference types as java.lang.Object
				if (Restructure.JavaTypeInfo.sootTypeToInt(type) == Restructure.JavaTypeInfo.refType) {
					type = Scene.v().getRefType("java.lang.Object");
					if (type == null)
						throw new InternalAroundError();
				}
				int typeNum = Restructure.JavaTypeInfo.sootTypeToInt(type);
				if (currentIndex[typeNum] < contextArgsByType[typeNum].size()) {
					Integer contextArgID = (Integer) contextArgsByType[typeNum]
							.get(currentIndex[typeNum]);
					++currentIndex[typeNum];
					argIndex[i] = contextArgID.intValue();
				} else {
					addedDynArgsTypes.add(type);
					contextArguments.add(type);
					int newIndex = contextArguments.size() - 1;
					contextArgsByType[typeNum].add(new Integer(newIndex));
					argIndex[i] = newIndex;
					++currentIndex[typeNum];
				}
				i++;
			}
		}
		return argIndex;
	}

	private List getContextActualsList(List context, int[] argIndex) {
		List contextActuals = new LinkedList();
		{ // create list of dynamic actuals to add (including default values)
			Value[] parameters = new Value[contextArguments.size()];

			for (int i = 0; i < argIndex.length; i++) {
				parameters[argIndex[i]] = (Local) context.get(i);
			}
			for (int i = 0; i < parameters.length; i++) {
				if (parameters[i] == null) {
					parameters[i] = Restructure.JavaTypeInfo
							.getDefaultValue((Type) contextArguments.get(i));
				}
				contextActuals.add(parameters[i]);
			}
		}
		return contextActuals;
	}

	private SootClass createClosureInterface(List proceedMethodParameters) {
		SootClass closureInterface;
		// create access interface if it doesn't exist
		if (Scene.v().containsClass(interfaceName)) {
			AroundWeaver.debug("found access interface in scene");
			closureInterface = Scene.v().getSootClass(interfaceName);
			//abstractProceedMethod=closureInterface.getMethodByName(dynamicProceedMethodName);
		} else {
			AroundWeaver.debug("generating access interface type "
					+ interfaceName);

			closureInterface = new SootClass(interfaceName, Modifier.INTERFACE
					| Modifier.PUBLIC);

			closureInterface.setSuperclass(Scene.v().getSootClass(
					"java.lang.Object"));

			SootMethod abstractProceedMethod = new SootMethod(
					instanceProceedMethodName, proceedMethodParameters,
					getAdviceReturnType(), Modifier.ABSTRACT | Modifier.PUBLIC);

			closureInterface.addMethod(abstractProceedMethod);
			//signature.setActiveBody(Jimple.v().newBody(signature));

			Scene.v().addClass(closureInterface);
			closureInterface.setApplicationClass();

			//abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getGeneratedClasses().add(interfaceName);						 
		}
		return closureInterface;
	}

	public void generateProceedCalls(boolean bStaticProceedMethod,
			boolean bClosure, ProceedMethod proceedMethod) {

		//AdviceMethod adviceMethodInfo = state.getInterfaceInfo(interfaceName);

		String newStaticInvoke = null;
		boolean bContinue = true;
		if (!bClosure && bStaticProceedMethod) {
			if (!staticProceedTypes.contains(proceedMethod.shadowClass
					.getName())) {
				newStaticInvoke = proceedMethod.shadowClass.getName();
				staticProceedTypes.add(proceedMethod.shadowClass.getName());
			} else
				bContinue = false;
		} else {
			if (hasDynamicProceed)
				bContinue = false;
			else
				hasDynamicProceed = true;
		}

		if (!bContinue)
			return;

		Iterator it = adviceLocalClasses.values().iterator();
		while (it.hasNext()) {
			AdviceLocalClass pm = (AdviceLocalClass) it.next();
			pm.generateProceeds(proceedMethod, newStaticInvoke);
		}
	}

	private void modifyDirectInterfaceInvocations(List addedDynArgs,
			List addedDynArgTypes) {
		if (addedDynArgs.size() != addedDynArgTypes.size())
			throw new InternalAroundError();
		{ // modify all existing direct interface invocations by adding the default parameters
			Iterator it = directInvocationStmts.iterator();
			while (it.hasNext()) {
				Stmt stmt = (Stmt) it.next();
				//addEmptyDynamicParameters(method, addedDynArgs, proceedMethodName);
				InvokeExpr invoke = (InvokeExpr) stmt.getInvokeExprBox()
						.getValue();
				//List newParams = new LinkedList(invoke.getArgs());
				//List newTypes = new LinkedList(invoke.getMethodRef()
				//		.parameterTypes());
				//newTypes.addAll(addedDynArgTypes);
				//newParams.addAll(addedDynArgs); /// should we do deep copy?	
				InvokeExpr newInvoke = Util.createModifiedInvokeExpr(invoke, addedDynArgs, addedDynArgTypes);
				//Util.createNewInvokeExpr(invoke,
				//		newParams, newTypes);
				stmt.getInvokeExprBox().setValue(newInvoke);
			}
		}
	}

	private void modifyAdviceMethodInvocations(List addedDynArgs,
			List addedDynArgTypes) {
		if (addedDynArgs.size() != addedDynArgTypes.size())
			throw new InternalAroundError();
		{ // modify all existing advice method invocations by adding the default parameters
			Iterator it = adviceMethodInvocationStmts.iterator();
			while (it.hasNext()) {
				Stmt stmt = (Stmt) it.next();
				//addEmptyDynamicParameters(method, addedDynArgs, proceedMethodName);
				InvokeExpr invoke = (InvokeExpr) stmt.getInvokeExprBox()
						.getValue();
				//List newParams = new LinkedList(invoke.getArgs());
				//List newTypes = new LinkedList(invoke.getMethodRef()
				//		.parameterTypes());
				//newTypes.addAll(addedDynArgTypes);
				//newParams.addAll(addedDynArgs); /// should we do deep copy?	
				InvokeExpr newInvoke = Util.createModifiedInvokeExpr(invoke, addedDynArgs, addedDynArgTypes);
				//createNewInvokeExpr(invoke,
					//	newParams, newTypes);
				//invoke.getMethodRef().parameterTypes().add()
				stmt.getInvokeExprBox().setValue(newInvoke);
			}
		}
	}

	private void addContextParamsToInterfaceDefinition(List addedDynArgsTypes) {
		{ // modify the interface definition
			SootMethod m = interfaceInfo.abstractProceedMethod;
			List p = new LinkedList(m.getParameterTypes());
			p.addAll(addedDynArgsTypes);
			m.setParameterTypes(p);
		}
	}

	public Type getAdviceReturnType() {
		return sootAdviceMethod.getReturnType();
	}

	public static class InterfaceInfo {
		SootClass closureInterface;

		SootMethod abstractProceedMethod;
	}

	InterfaceInfo interfaceInfo = null;

	final String instanceProceedMethodName;

	final String interfaceName;

	final String adviceMethodIdentifierString;

	final List /*type*/proceedMethodParameterTypes = new LinkedList();

	final List /*SootMethod*/closureProceedMethods = new LinkedList();

	SootClass getAspect() {
		return sootAdviceMethod.getDeclaringClass();
	}

	public final SootMethod sootAdviceMethod;

	//public final AdviceDecl adviceDecl;
	public final Body adviceBody;

	public final Chain adviceStatements;

	final public List originalAdviceFormalTypes;

	final public HashSet /*String*/staticProceedTypes = new HashSet();

	public boolean hasDynamicProceed = false;

	public final boolean bAlwaysStaticProceedMethod = true; //false;//true;//false;//true; //false;

	public boolean bHasBeenWovenInto = false;

	final public Set adviceMethodInvocationStmts = new HashSet();

	final public Set directInvocationStmts = new HashSet();

	final List /*Type*/contextArguments = new LinkedList();

	final List[] contextArgsByType = new List[Restructure.JavaTypeInfo.typeCount];

	public final Map /*SootClass, AdviceLocalClass */adviceLocalClasses = new HashMap();

	public static List getOriginalAdviceFormals(AdviceDecl adviceDecl) {

		List result = new LinkedList();

		Iterator it = adviceDecl.getImpl().getFormals().iterator();
		while (it.hasNext()) {
			Formal formal = (Formal) it.next();
			result.add(formal.getType().getSootType());
			//formal.
		}

		// TODO: clean up the following 7 lines
		int size = result.size();
		if (adviceDecl.hasEnclosingJoinPoint())
			result.remove(--size);
		if (adviceDecl.hasJoinPoint())
			result.remove(--size);
		if (adviceDecl.hasJoinPointStaticPart())
			result.remove(--size);

		return result;
	}

	public List getAllProceedMethods() {
		List result = new LinkedList();
		result.addAll(proceedMethodImplementations.values());
		result.addAll(proceedMethodImplementationsStatic.values());
		result.addAll(proceedMethodImplementationsClosure);
		return result;
	}

	public ProceedMethod getProceedMethod(String className, boolean bStatic) {
		if (bStatic) {
			return (ProceedMethod) proceedMethodImplementationsStatic
					.get(className);
		} else {
			return (ProceedMethod) proceedMethodImplementations.get(className);
		}
	}

	public void setClosureProceedMethod(ProceedMethod m) {
		proceedMethodImplementationsClosure.add(m);
	}

	public void setProceedMethod(String className, boolean bStatic,
			ProceedMethod proceedMethod) {
		if (bStatic) {
			proceedMethodImplementationsStatic.put(className, proceedMethod);
		} else {
			proceedMethodImplementations.put(className, proceedMethod);
		}
	}

	final private HashMap /*String, ProceedMethod*/proceedMethodImplementations = new HashMap();

	final private HashMap /*String, ProceedMethod*/proceedMethodImplementationsStatic = new HashMap();

	final private Set /* ProceedMethod */proceedMethodImplementationsClosure = new HashSet();

	public int getUniqueShadowID() {
		return currentUniqueShadowID++;
	}

	int currentUniqueShadowID;
}
