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

package abc.weaving.weaver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import polyglot.util.ErrorInfo;
import polyglot.util.InternalCompilerError;
import soot.Body;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Trap;
import soot.Type;
import soot.Unit;
import soot.UnitBox;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.Constant;
import soot.jimple.FieldRef;
import soot.jimple.ArrayRef;
import soot.jimple.GotoStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.tagkit.Tag;
import soot.util.Chain;
import abc.main.Debug;
import abc.main.Main;
import abc.main.Options;
import abc.main.options.OptionsParser;
import abc.polyglot.util.ErrorInfoFactory;
import abc.soot.util.AroundShadowInfoTag;
import abc.soot.util.DisableExceptionCheckTag;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.RedirectedExceptionSpecTag;
import abc.soot.util.Restructure;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.AdviceSpec;
import abc.weaving.aspectinfo.AroundAdvice;
import abc.weaving.aspectinfo.Formal;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.ConstructorAdviceApplication;
import abc.weaving.matching.ExecutionAdviceApplication;
import abc.weaving.matching.HandlerAdviceApplication;
import abc.weaving.matching.NewStmtAdviceApplication;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.StmtAdviceApplication;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.Residue;

/** Handle around weaving.
 * @author Sascha Kuzins 
 * @date May 6, 2004
 */

public class AroundWeaver {
		
	public static void reset() {
		state = new State();
	}
	private static void debug(String message) {
		if (abc.main.Debug.v().aroundWeaver)
			System.err.println("ARD*** " + message);
	}

	public static class ObjectBox {
		Object object;
	}

	public static class Util {
		
		public static Stmt newSwitchStmt(Value key, List lookupValues, List targets, Unit defaultTarget) {
			int min=Integer.MAX_VALUE;
			int max=Integer.MIN_VALUE;
			int count=lookupValues.size();
			TreeMap map=new TreeMap();
			Iterator itTg=targets.iterator();
			for (Iterator it=lookupValues.iterator();it.hasNext();) {
				IntConstant intConst=(IntConstant)it.next();
				Unit target=(Unit)itTg.next();
				int val=intConst.value;
				max = (val>max ? val : max);
				min = (val<min ? val : min);
				map.put(new Integer(val), target);
			}
			if (max-min+1==count) {
				targets=new LinkedList(map.values()); /// is this map necessary, or are lookupValues always sorted?
				return Jimple.v().newTableSwitchStmt(key, min, max, targets, defaultTarget);
			} else {
				return Jimple.v().newLookupSwitchStmt(key, lookupValues, targets, defaultTarget);
			}
		}
		
		public static void validateMethod(SootMethod method) {
			if (abc.main.Debug.v().aroundWeaver)
				Restructure.validateMethod(method);
		}
		public static String printMethod(SootMethod m) {
			String result=m + "\n";
			for(Iterator it=m.getActiveBody().getUnits().iterator(); it.hasNext();) {
				result += it.next().toString() + "\n";
			}
			return result;
		}
		public static boolean isAroundAdviceMethod(SootMethod method) {
			return isAroundAdviceMethodName(method.getName()); // TODO: something more solid
		}
		public static boolean isAroundAdviceMethodName(String methodName) {
			return methodName.startsWith("around$"); // TODO: something more solid
		}
		public static boolean isProceedMethodName(String methodName) {
			return methodName.indexOf("$proceed$")!=-1; // TODO: something much more solid!
		}
		private static boolean chainContainsLocal(Chain locals, String name) {
			Iterator it = locals.iterator();
			while (it.hasNext()) {
				if (((soot.Local) it.next()).getName().equals(name))
					return true;
			}
			return false;
		}
		

		/**
		 * Removes all unused locals from the local chain
		 * @param body
		 */
		private static void cleanLocals(Body body) {

			Chain locals = body.getLocals();

			HashSet usedLocals = new HashSet();

			Iterator it = body.getUseAndDefBoxes().iterator();
			while (it.hasNext()) {
				ValueBox vb = (ValueBox) it.next();
				if (vb.getValue() instanceof Local) {
					usedLocals.add(vb.getValue());
				}
			}

			List removed = new LinkedList();
			it = body.getLocals().iterator();
			while (it.hasNext()) {
				Local local = (Local) it.next();
				if (!usedLocals.contains(local))
					removed.add(local);
			}
			Iterator it2 = removed.iterator();
			while (it2.hasNext())
				locals.remove(it2.next());
		}

		public static List getParameterLocals(Body body) {
			List result = new LinkedList();

			for (int i = 0; i < body.getMethod().getParameterCount(); i++) {
				result.add(body.getParameterLocal(i));
			}
			return result;
		}

		/**
		 * Checks if @param test 
		 * is in ]@param begin, @param end[
		 * 
		 * @param body 
		 * @return
		 */
		private static boolean isInSequence(Body body, Unit begin, Unit end, Unit test) {
			Chain statements = body.getUnits().getNonPatchingChain();

			Iterator it = statements.iterator(begin);
			it.next();
			while (it.hasNext()) {
				Unit ut = (Unit) it.next();
				if (ut == end)
					break;

				if (ut == test)
					return true;
			}
			return false;
		}

		private static String mangleTypeName(String name) {
			return name.replaceAll("_", "__").replaceAll("\\.", "_d_").replaceAll("/", "_s_");

		}

		/**
		 * Removes statements between begin and end, excluding these and skip.
		 */
		private static void removeStatements(Body body, Unit begin, Unit end, Unit skip) {
			Chain units = body.getUnits().getNonPatchingChain();
			List removed = new LinkedList();
			Iterator it = units.iterator(begin);
			if (it.hasNext())
				it.next(); // skip begin
			while (it.hasNext()) {
				Unit ut = (Unit) it.next();
				if (ut == end)
					break;

				if (ut != skip) {
					removed.add(ut);
				}
			}
			Iterator it2 = removed.iterator();
			while (it2.hasNext()) {
				Unit ut=(Unit)it2.next();
				debug("******* Removing unit: " + ut);
				units.remove(ut);
			}
		}

		/**
		 * Removes all traps that refer to statements between begin and end.
		 * Throws an exception if traps partially refer to that range.
		 * @param body
		 * @param begin
		 * @param end
		 */
		private static void removeTraps(Body body, Unit begin, Unit end) {
			HashSet range = new HashSet();

			Chain units = body.getUnits().getNonPatchingChain();
			Iterator it = units.iterator(begin);
			if (it.hasNext())
				it.next(); // skip begin
			while (it.hasNext()) {
				Unit ut = (Unit) it.next();
				if (ut == end)
					break;
				range.add(ut);
			}

			List removed = new LinkedList();
			Chain traps = body.getTraps();
			it = traps.iterator();
			while (it.hasNext()) {
				Trap trap = (Trap) it.next();
				if (range.contains(trap.getBeginUnit())) {
					if (!range.contains(trap.getEndUnit()))
						throw new CodeGenException("partial trap in shadow");

					if (!range.contains(trap.getHandlerUnit()))
						throw new CodeGenException("partial trap in shadow");

					removed.add(trap);
				} else {
					if (range.contains(trap.getEndUnit()))
						throw new CodeGenException("partial trap in shadow");

					if (range.contains(trap.getHandlerUnit()))
						throw new CodeGenException("partial trap in shadow");
				}
			}
			it = removed.iterator();
			while (it.hasNext()) {
				traps.remove(it.next());
			}
		}

		/**
		 *  Assigns a suggested name to a local, dealing with possible collisions
		 */
		private static void setLocalName(Chain locals, Local local, String suggestedName) {
			//if (!locals.contains(local))
			//	throw new InternalCompilerError();

			String name = suggestedName;
			int i = 0;
			while (AroundWeaver.Util.chainContainsLocal(locals, name)) {
				name = suggestedName + (++i);
			}
			local.setName(name);
		}

		/**
		 * Creates a new InvokeExpr based on an existing one but with new arguments.
		 */
		public static InvokeExpr createNewInvokeExpr(InvokeExpr old, List newArgs, List newTypes) {
			if (newArgs.size()!=newTypes.size())
				throw new InternalAroundError();
			/*{ // sanity check:
				Iterator it0=newTypes.iterator();
				for (Iterator it=newArgs.iterator(); it.hasNext();) {
					Value val=(Value)it.next();
					Type type=(Type)it0.next();
				}
			}*/
			
			soot.SootMethodRef oldRef=old.getMethodRef();
			//debug("createNewInvokeExpr: old ref: " + ref + " "  + ref.getSignature());
			soot.SootMethodRef ref=Scene.v().makeMethodRef(
					oldRef.declaringClass(),
					oldRef.name(),
					newTypes,
					oldRef.returnType(),
					oldRef.isStatic()					
					);
			//ref.parameterTypes().clear();
			//ref.parameterTypes().addAll(newTypes);
			//soot.SootMethodRef ref=newTarget.makeRef();
			debug("createNewInvokeExpr: new ref: " + ref +  " " + ref.getSignature());
			if (old instanceof InstanceInvokeExpr) {
				Local base = (Local) ((InstanceInvokeExpr) old).getBase();
				if (old instanceof InterfaceInvokeExpr)
					return Jimple.v().newInterfaceInvokeExpr(base, ref, newArgs);
				else if (old instanceof SpecialInvokeExpr) {
					return Jimple.v().newSpecialInvokeExpr(base, ref, newArgs);
				} else if (old instanceof VirtualInvokeExpr)
					return Jimple.v().newVirtualInvokeExpr(base, ref, newArgs);
				else
					throw new AroundWeaver.InternalAroundError();
			} else {
				return Jimple.v().newStaticInvokeExpr(ref, newArgs);
			}
		}
	}
	
	
	/*public static class ClosureClass {
		AdviceMethod adviceMethod;
		
		SootMethod staticProceedMethod;
	}*/

	public static void doWeave(SootClass shadowClass, SootMethod shadowMethod, LocalGeneratorEx localgen, AdviceApplication adviceAppl) {
		
		state.shadowMethods.add(shadowMethod);
		
		debug("Weaving advice application: " + adviceAppl);
		if (abc.main.Debug.v().aroundWeaver) {
			// uncomment to skip around weaving (for debugging)
			// if (shadowClass!=null)	return;
			//throw new RuntimeException();
		}
		
		if (abc.main.Debug.v().aroundWeaver) {
			try {
//				UnreachableCodeEliminator.v().transform(shadowMethod.getActiveBody());
				shadowMethod.getActiveBody().validate();
			} catch (RuntimeException e ) {
				debug("shadow method: " + Util.printMethod(shadowMethod));
				throw e;
			}
		}
		
		SootMethod adviceMethod=null;
		//try {

			AdviceDecl adviceDecl = (AdviceDecl) adviceAppl.advice;
			
			AdviceSpec adviceSpec = adviceDecl.getAdviceSpec();
			AroundAdvice aroundSpec = (AroundAdvice) adviceSpec;
			SootClass theAspect = adviceDecl.getAspect().getInstanceClass().getSootClass();
			SootMethod method = adviceDecl.getImpl().getSootMethod();
			adviceMethod=method;
			if (abc.main.Debug.v().aroundWeaver) {
				try {
	//				UnreachableCodeEliminator.v().transform(method.getActiveBody());
					method.getActiveBody().validate();
				} catch (RuntimeException e ) {
					debug("advice method: " + Util.printMethod(method));
					throw e;
				}
			}
			AdviceMethod adviceMethodInfo = state.getAdviceMethod(method);
			List sootLocalAdviceMethods=new LinkedList();
			sootLocalAdviceMethods.addAll(adviceDecl.getLocalSootMethods());
			if (!sootLocalAdviceMethods.contains(method))
				sootLocalAdviceMethods.add(method);
			
			if (adviceMethodInfo == null) {
				adviceMethodInfo = new AdviceMethod(method, 
						AdviceMethod.getOriginalAdviceFormals(adviceDecl),
							sootLocalAdviceMethods);
				state.setAdviceMethod(method, adviceMethodInfo);
			} else {
				if (AdviceMethod.getOriginalAdviceFormals(adviceDecl).size()
						!= adviceMethodInfo.originalAdviceFormalTypes.size())
					throw new InternalAroundError("Expecting consistent adviceDecl each time for same advice method");
							// if this occurs, fix getOriginalAdviceFormals()
				
				//if (adviceMethodInfo.proceedMethods.size()!=adviceDecl.getSootProceeds().size())
				//	throw new InternalAroundError("" + adviceMethodInfo.proceedMethods.size() +
				//				" : " + adviceDecl.getSootProceeds().size());
			}

			adviceMethodInfo.doWeave(adviceAppl, shadowMethod);
			
		/*} catch (InternalAroundError e) {
			throw e;
		} catch (Throwable e) {
			System.err.println(" " + e.getClass().getName() + " " + e.getCause());
			
			StackTraceElement[] els=e.getStackTrace();
			for (int i=0; i<els.length; i++) {
				System.err.println(e.getStackTrace()[i].toString());
			}			
			throw new InternalAroundError("", e);
		}*/
		
		if (abc.main.Debug.v().aroundWeaver) {
				state.validate(); 
				//validate();
				//abc.soot.util.Validate.validate(Scene.v().getSootClass("org.aspectj.runtime.reflect.Factory"));
		}
		if (abc.main.Debug.v().aroundWeaver) {
			try {
	//			UnreachableCodeEliminator.v().transform(shadowMethod.getActiveBody());
				shadowMethod.getActiveBody().validate();
			} catch (RuntimeException e ) {
				debug("shadow method: " + Util.printMethod(shadowMethod));
				throw e;
			}
		}
		if (abc.main.Debug.v().aroundWeaver) {
			try {
		//		UnreachableCodeEliminator.v().transform(adviceMethod.getActiveBody());
				adviceMethod.getActiveBody().validate();
			} catch (RuntimeException e ) {
				debug("advice method: " + Util.printMethod(adviceMethod));
				throw e;
			}
		}
	}
	
    
	public static void validate() {
	    for(Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {
	        final AbcClass cl = (AbcClass) clIt.next();
	        abc.soot.util.Validate.validate(cl.getSootClass());
	    }
    }
	
	public static class AdviceMethod {
		AdviceMethod(SootMethod method, final List originalAdviceFormalTypes, final List proceedSootMethods) {
			
			if (originalAdviceFormalTypes==null)
				throw new InternalAroundError();
			this.originalAdviceFormalTypes=originalAdviceFormalTypes;
			
			if (proceedSootMethods==null)
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
			String packageName=getAspect().getPackageName();
			String mangledAspectName = Util.mangleTypeName(aspectName);

			adviceMethodIdentifierString = mangledAspectName + "$" + method.getName();

			
			interfaceName =
				(packageName.length()==0 ? "" : packageName + ".") +
				"Abc$proceed$" + adviceMethodIdentifierString;
			
			instanceProceedMethodName = "abc$proceed$" + adviceMethodIdentifierString;
			;

			proceedMethodParameterTypes.add(IntType.v()); // the shadow id
			proceedMethodParameterTypes.add(IntType.v()); // the bind mask

			{
				List allProceedMethodParameters = new LinkedList();
				allProceedMethodParameters.addAll(originalAdviceFormalTypes);
				allProceedMethodParameters.addAll(proceedMethodParameterTypes);

				interfaceInfo = new InterfaceInfo();

				interfaceInfo.closureInterface = createClosureInterface(allProceedMethodParameters);
				interfaceInfo.abstractProceedMethod = interfaceInfo.closureInterface.getMethodByName(instanceProceedMethodName);
			}
			
			if (!proceedSootMethods.contains(sootAdviceMethod)) 
				proceedSootMethods.add(sootAdviceMethod);
			
			if (Debug.v().aroundWeaver){
				debug("QQQQQQQQQQQQQQ Methods for advice-method" + sootAdviceMethod + "\n");
				for (Iterator it=proceedSootMethods.iterator();it.hasNext();) {
					SootMethod m=(SootMethod)it.next();
					debug(" " + m);
					
				}
			}
			
			//	Add all the proceed classes				
			for (Iterator it=proceedSootMethods.iterator();it.hasNext();) {
				SootMethod m=(SootMethod)it.next();
				if (!adviceLocalClasses.containsKey(m.getDeclaringClass())) {
					AdviceLocalClass newClass=
						new AdviceLocalClass(m.getDeclaringClass());
					adviceLocalClasses.put(
						m.getDeclaringClass(), 
							newClass);
					
					while (newClass!=null && !newClass.isAspect() && !newClass.isFirstDegree()) {
						SootClass enclosing=newClass.getEnclosingSootClass();
						
						if (!adviceLocalClasses.containsKey(enclosing)) {
							newClass=
								new AdviceLocalClass(enclosing);
							adviceLocalClasses.put(
								enclosing, 
									newClass);
						} else
							newClass=null;
					}
				}
			}
			// add the corresponding methods.
			// important: keep this order. all classes have to be added first.
			{
				for (Iterator it=proceedSootMethods.iterator();it.hasNext();) {
					SootMethod m=(SootMethod)it.next();
					if (!m.equals(method)) {
						AdviceLocalClass pl=(AdviceLocalClass)adviceLocalClasses.get(m.getDeclaringClass());
						pl.addAdviceLocalMethod(m);
					}
				}
				// add advice method itself last.
				AdviceLocalClass pl=(AdviceLocalClass)adviceLocalClasses.get(method.getDeclaringClass());
				pl.addAdviceLocalMethod(method);
			}
			{
				for (Iterator it=adviceLocalClasses.values().iterator();it.hasNext();) {
					AdviceLocalClass pc=(AdviceLocalClass)it.next();
					if (pc.isFirstDegree())
						pc.addDefaultParameters();
				}
				for (Iterator it=adviceLocalClasses.values().iterator();it.hasNext();) {
					AdviceLocalClass pc=(AdviceLocalClass)it.next();
					if (!pc.isAspect() && !pc.isFirstDegree())
						pc.addDefaultParameters();
				}
				for (Iterator it=adviceLocalClasses.values().iterator();it.hasNext();) {
					AdviceLocalClass pc=(AdviceLocalClass)it.next();
					if (pc.isAspect())
						pc.addDefaultParameters();
				}
			}
			if (Debug.v().aroundWeaver){
				debug("QQQQQQQQQQQQQQ Classes for advice-method" + sootAdviceMethod + "\n");
				for (Iterator it=adviceLocalClasses.values().iterator();it.hasNext();) {
					AdviceLocalClass pc=(AdviceLocalClass)it.next();
					debug(" " + pc.sootClass);
					
				}
			}
		}

		public void doWeave(AdviceApplication adviceAppl, SootMethod shadowMethod) {
			final boolean bExecutionAdvice =	
				adviceAppl instanceof ExecutionAdviceApplication;
				
			
			final boolean bExecutionWeavingIntoSelf=
					bExecutionAdvice && 
					 (sootAdviceMethod.equals(shadowMethod) || 
					 		(state.getEnclosingAroundAdviceMethod(shadowMethod)!=null &&
					 		state.getEnclosingAroundAdviceMethod(shadowMethod).equals(sootAdviceMethod)));
			
			boolean bStaticShadowMethod = shadowMethod.isStatic();
			boolean bUseClosureObject;
			
			final boolean bAlwaysUseClosures;
			
			/*if (Debug.v().aroundWeaver)	{
				bAlwaysUseClosures=false;//false; // change this to suit your debugging needs...
			} else {
				bAlwaysUseClosures=false; // don't change this!
			}*/
			bAlwaysUseClosures=OptionsParser.v().around_force_closures();
			
			
			if (bHasBeenWovenInto || 
					 bExecutionWeavingIntoSelf)
				bUseClosureObject=true;
			else
				bUseClosureObject=bAlwaysUseClosures;

			final boolean bUseStaticProceedMethod= 
				bStaticShadowMethod || bAlwaysStaticProceedMethod;
			
			String proceedMethodName;
			if (bUseClosureObject) {
				proceedMethodName = "abc$closure$proceed$" + adviceMethodIdentifierString + "$" + state.getUniqueID();
			} else {
				if ( bUseStaticProceedMethod) {
					proceedMethodName = "abc$static$proceed$" + adviceMethodIdentifierString;
				} else {
					proceedMethodName = instanceProceedMethodName;
				}
			}
	
			
			ProceedMethod proceedMethod=null;
			if (!bUseClosureObject) {
				proceedMethod = getProceedMethod(shadowMethod.getDeclaringClass().getName(), bUseStaticProceedMethod);
			}
			if (proceedMethod == null) {
				proceedMethod = new AdviceMethod.ProceedMethod(shadowMethod.getDeclaringClass(), bUseStaticProceedMethod, proceedMethodName, bUseClosureObject);
					
				if (bUseClosureObject)
					setClosureProceedMethod(proceedMethod);
				else
					setProceedMethod(shadowMethod.getDeclaringClass().getName(), bUseStaticProceedMethod, proceedMethod);
			}
			
			proceedMethod.doWeave(adviceAppl, shadowMethod);			
		}
		public class ProceedMethod {
			private Set adviceApplications=new HashSet();
			
			private HashMap shadowInformation=new HashMap();
			
			
			ProceedMethod(SootClass shadowClass, boolean bStaticProceedMethod, String proceedMethodName, boolean bClosureMethod) {
				this.bStaticProceedMethod=bStaticProceedMethod;
//				this.adviceMethod = parent;
				this.shadowClass = shadowClass;
				this.bUseClosureObject=bClosureMethod;
				
				
				
				String interfaceName = interfaceInfo.closureInterface.getName();

				if (bStaticProceedMethod || bClosureMethod) {
						sootProceedMethod = new SootMethod(proceedMethodName, new LinkedList(), getAdviceReturnType(), Modifier.PUBLIC | Modifier.STATIC);
				} else {
					debug("adding interface " + interfaceName + " to class " + shadowClass.getName());
		
					shadowClass.addInterface(interfaceInfo.closureInterface);

					// create new method					
					sootProceedMethod = new SootMethod(proceedMethodName, new LinkedList(), getAdviceReturnType(), Modifier.PUBLIC);
				}
				sootProceedMethod.addTag(new DisableExceptionCheckTag());
				Body proceedBody = Jimple.v().newBody(sootProceedMethod);

				state.proceedMethods.put(sootProceedMethod, this);
				
				sootProceedMethod.setActiveBody(proceedBody);
				debug("adding method " + sootProceedMethod.getName() + " to class " + shadowClass.getName());
				shadowClass.addMethod(sootProceedMethod);

				Chain proceedStatements = proceedBody.getUnits().getNonPatchingChain();

				// generate this := @this
				LocalGeneratorEx lg = new LocalGeneratorEx(proceedBody);
				Local lThis = null;
				if (!bStaticProceedMethod && !bClosureMethod) {
					lThis = lg.generateLocal(shadowClass.getType(), "this");
					proceedStatements.addFirst(Jimple.v().newIdentityStmt(lThis, Jimple.v().newThisRef(RefType.v(shadowClass))));
				}
				Util.validateMethod(sootProceedMethod);
				//proceedMethodInfo.targetLocal=Restructure.addParameterToMethod(
				//	proceedMethod, (Type)proceedMethodParameters.get(0), "targetArg");

				{
					Iterator it = originalAdviceFormalTypes.iterator();
					while (it.hasNext()) {
						Type type = (Type) it.next();
						//System.out.println(" " +method.getActiveBody().getUnits());
						Local l = Restructure.addParameterToMethod(sootProceedMethod, type, "orgAdviceFormal");
						//System.out.println(" " +method.getActiveBody().getUnits());
						Util.validateMethod(sootProceedMethod);
						adviceFormalLocals.add(l);
					}
				}
				Util.validateMethod(sootProceedMethod);

				shadowIdParamLocal = Restructure.addParameterToMethod(sootProceedMethod, (Type) proceedMethodParameterTypes.get(0), "shadowID");
				shadowIDParamIndex=sootProceedMethod.getParameterCount()-1;
				bindMaskParamLocal = Restructure.addParameterToMethod(sootProceedMethod, (Type) proceedMethodParameterTypes.get(1), "bindMask");

				if (proceedMethodParameterTypes.size() != 2)
					throw new InternalAroundError();

				Stmt lastIDStmt = Restructure.getParameterIdentityStatement(sootProceedMethod, sootProceedMethod.getParameterCount() - 1);

				if (!bClosureMethod) {
					// generate exception code (default target)
					SootClass exception = Scene.v().getSootClass("java.lang.RuntimeException");
					Local ex = lg.generateLocal(exception.getType(), "exception");
					Stmt newExceptStmt = Jimple.v().newAssignStmt(ex, Jimple.v().newNewExpr(exception.getType()));
					Stmt initEx = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(ex, exception.getMethod("<init>", new ArrayList()).makeRef()));
					Stmt throwStmt = Jimple.v().newThrowStmt(ex);
		
					defaultTarget = Jimple.v().newNopStmt();
					proceedStatements.add(defaultTarget);
					proceedStatements.add(newExceptStmt);
					proceedStatements.add(initEx);
					proceedStatements.add(throwStmt);
					defaultEnd = Jimple.v().newNopStmt();
					proceedStatements.add(defaultEnd);
		
					// just generate a nop for now.
					lookupStmt = Jimple.v().newNopStmt();
		
					proceedStatements.insertAfter(lookupStmt, lastIDStmt);
					
					//AdviceMethod adviceMethodInfo = state.getInterfaceInfo(interfaceName);
					Iterator it = contextArguments.iterator();
					while (it.hasNext()) {
						Type type = (Type) it.next();
						Local l = Restructure.addParameterToMethod(sootProceedMethod, type, "contextArg");
						contextParamLocals.add(l);
					}
				} else {
					defaultTarget = Jimple.v().newNopStmt();
					proceedStatements.add(defaultTarget);
					defaultEnd = Jimple.v().newNopStmt();
					proceedStatements.add(defaultEnd);
					// just generate a nop for now.
					lookupStmt = Jimple.v().newNopStmt();
					proceedStatements.insertAfter(lookupStmt, lastIDStmt);
				}
				
				Util.validateMethod(sootProceedMethod);

				proceedMethodBody=sootProceedMethod.getActiveBody();
				proceedMethodStatements=proceedMethodBody.getUnits().getNonPatchingChain();
			}
			public int numOfShadows=0;
			
			public void doWeave(AdviceApplication adviceAppl, SootMethod shadowMethod) {

				AdviceApplicationInfo adviceApplication=new AdviceApplicationInfo(adviceAppl, shadowMethod);
				this.adviceApplications.add(adviceApplication);
				adviceApplication.doWeave();
				numOfShadows++;
			}
			public class AdviceApplicationInfo {
				public final int shadowSize;
				public int shadowInternalLocalCount;
				public int getShadowSize() {

					Chain statements = shadowMethodStatements;
			
					if (!statements.contains(begin))
						throw new InternalAroundError();
			
					if (!statements.contains(end))
						throw new InternalAroundError();
			
					boolean insideRange = false;
					int size=0;
					Iterator it = statements.iterator();
					while (it.hasNext()) {
						Stmt s = (Stmt) it.next();
						if (s == end) {
							if (!insideRange)
								throw new InternalAroundError();
			
							insideRange = false;
						}			
						
						if (s == begin) {
							if (insideRange)
								throw new InternalAroundError();
			
							insideRange = true;
						} else if (insideRange) {
							size++;
						}							
					}
					return size;			
				}
//				final boolean bHasProceed;
				AdviceApplicationInfo(AdviceApplication adviceAppl, SootMethod shadowMethod) {
					//this.proceedMethodName=ProceedMethod.this.proceedMethodSoot.getName();
					//this.bUseStaticProceedMethod=bS
					this.adviceAppl=adviceAppl;
					
					final boolean bExecutionAdvice =	
						adviceAppl instanceof ExecutionAdviceApplication;
					
					
					AdviceDecl adviceDecl = (AdviceDecl) adviceAppl.advice;
					
					AdviceSpec adviceSpec = adviceDecl.getAdviceSpec();
					AroundAdvice aroundSpec = (AroundAdvice) adviceSpec;
					SootClass theAspect = adviceDecl.getAspect().getInstanceClass().getSootClass();
					SootMethod method = adviceDecl.getImpl().getSootMethod();
					
					//this.bHasProceed=adviceDecl.getSootProceeds().size()>0;
					
					

					this.shadowMethod=shadowMethod;
					this.shadowClass=shadowMethod.getDeclaringClass();
					this.shadowMethodBody=shadowMethod.getActiveBody();
					this.shadowMethodStatements=shadowMethodBody.getUnits().getNonPatchingChain();
					
					this.bStaticShadowMethod = shadowMethod.isStatic();
					this.begin = adviceAppl.shadowmatch.sp.getBegin();
					this.end = adviceAppl.shadowmatch.sp.getEnd();

					this.shadowSize=getShadowSize();
					
					debug("CLOSURE: " + (bUseClosureObject ? "Using closure" : "Not using closure"));

					if (bUseClosureObject) {
						ShadowMatch sm=adviceAppl.shadowmatch;
						abc.main.Main.v().error_queue.enqueue
	                    (ErrorInfoFactory.newErrorInfo
	                     (ErrorInfo.WARNING,
	                     		"Using closure object. This may impact performance.",
	                      sm.getContainer(),
	                      sm.getHost()));
					}
					
					// if the target is an around-advice method, 
					// make sure proceed has been generated for that method.
					if (bExecutionAdvice && 
							(Util.isAroundAdviceMethod(shadowMethod) ||
							state.getEnclosingAroundAdviceMethod(shadowMethod)!=null) ) {
						
						SootMethod relevantAdviceMethod;
						if (Util.isAroundAdviceMethod(shadowMethod))
							relevantAdviceMethod=shadowMethod;
						else
							relevantAdviceMethod=state.getEnclosingAroundAdviceMethod(shadowMethod);
							
						AdviceMethod adviceMethodWovenInto = state.getAdviceMethod(relevantAdviceMethod);
						if (adviceMethodWovenInto == null) {
							AdviceDecl advdecl;
							advdecl=getAdviceDecl(relevantAdviceMethod);
							List sootProceeds2=new LinkedList();
							sootProceeds2.addAll(advdecl.getLocalSootMethods());
							if (!sootProceeds2.contains(relevantAdviceMethod))
								sootProceeds2.add(relevantAdviceMethod);
							
							adviceMethodWovenInto = new AdviceMethod(relevantAdviceMethod, 
									AdviceMethod.getOriginalAdviceFormals(advdecl),
									sootProceeds2);
						}			
						adviceMethodWovenInto.generateProceedCalls(false, true, null);
						adviceMethodWovenInto.bHasBeenWovenInto=true;
					}
					
				}
				public void doWeave() {						
					Local lClosure=null;
					SootClass closureClass=null;
					List /*Local*/context=null;
					Local returnedLocal = null;
					List skipDynamicActuals;
					int shadowID;
					int [] argIndex;
					//if (bHasProceed) {
						returnedLocal = findReturnedLocal();			
						{
							debug("Locals going in: ");
							//debug(Util.printMethod(shadowMethod));
						}

						context=findLocalsGoingIn(shadowMethodBody, begin, end); 
								
						
						
						{ // print debug information
							
							debug(" Method: " + shadowMethod.toString());
							debug(" Application: " + adviceAppl.toString());
							//debug("Method + " + shadowMethod.toString());
							Iterator it = context.iterator();
							while (it.hasNext()) {
								Local l = (Local) it.next();
								debug("  " + l.toString());
							}
						}
			
						 
						validateShadow(shadowMethodBody, begin, end);
									 
						
						
						
						List contextActuals;
						
						
						if (bUseClosureObject) {
							if (!hasDynamicProceed) {
								AdviceMethod.this.generateProceedCalls(
											false, //bStaticProceedMethod 
											true, // bClosure
											null); // proceedMethod
							}
							//	throw new InternalAroundError();
							argIndex=new int[context.size()];
							List types=new LinkedList();
							int i=0;
							for (Iterator it=context.iterator(); it.hasNext();i++) {
								Local l=(Local)it.next();
								types.add(l.getType());
								argIndex[i]=i;
								Local argLocal=Restructure.addParameterToMethod(sootProceedMethod, l.getType(), 
										"contextArg");
								contextParamLocals.add(argLocal);
							}
							contextActuals=getDefaultValues(contextArguments);
							skipDynamicActuals=context;
						} else {
							ObjectBox contextActualsBox=new ObjectBox();
							argIndex=AdviceMethod.this.modifyAdviceMethod(context,ProceedMethod.this, contextActualsBox, bStaticProceedMethod, bUseClosureObject);
							contextActuals=(List)contextActualsBox.object;
							skipDynamicActuals=contextActuals;
						}
						if (bUseClosureObject) {
							closureClass=generateClosure( 
									interfaceInfo.abstractProceedMethod.getName(), 
									sootProceedMethod, context);
						}
						// copy shadow into proceed method with a return returning the relevant local.
						Stmt first;
						HashMap localMap;
						Stmt switchTarget;
						{ // copy shadow into proceed method
							ObjectBox result = new ObjectBox();
							if (lookupStmt==null)	
								throw new InternalAroundError();
							localMap = copyStmtSequence(shadowMethodBody, begin, end, proceedMethodBody, lookupStmt, returnedLocal, result);
							first = (Stmt) result.object;
							if (first==null)
								throw new InternalAroundError();
							switchTarget = Jimple.v().newNopStmt();
						
							if (first==lookupStmt)
								throw new InternalAroundError();
							
							proceedMethodStatements.insertBefore(switchTarget, first);
						}

						updateSavedReferencesToStatements(localMap);

						// Construct a tag to place on the invokes that are put in place of the removed
						// statements

						List newstmts=new LinkedList();
						Chain units=shadowMethodBody.getUnits();
						Stmt s=(Stmt) units.getSuccOf(begin);
						while(s!=end) {
						    newstmts.add(localMap.get(s));
						    s=(Stmt) units.getSuccOf(s);
						}
						Tag redirectExceptions=new RedirectedExceptionSpecTag(proceedMethodBody,newstmts);
						
						
					//}
					{ // remove old shadow
						// remove any traps from the shadow before removing the shadow
						Util.removeTraps(shadowMethodBody, begin, end);
						// remove statements except original assignment
						Util.removeStatements(shadowMethodBody, begin, end, null);
						//StmtAdviceApplication stmtAppl = null;
						//if (adviceAppl instanceof StmtAdviceApplication) {
						//	stmtAppl = (StmtAdviceApplication) adviceAppl;
						//	stmtAppl.stmt = null;
							/// just for sanity, because we deleted that stmt
						//}
					}

					//if (bHasProceed) {
						
						
						{ // determine shadow ID
							if (bUseClosureObject) {
								shadowID = -1; // bogus value, since not used in this case.
							} else if (bStaticProceedMethod) {
								shadowID = nextShadowID++;
							} else {
								shadowID = getUniqueShadowID();
							}
						}
						
						ProceedMethod.this.shadowInformation.put(
								new Integer(shadowID),
								new ShadowInlineInfo(shadowSize, shadowInternalLocalCount));
						
			
						if (bUseClosureObject) {
							lClosure=generateClosureCreation(closureClass, context);
						}
						//verifyBindings(staticBindings);
					//}
					
					Stmt failPoint = Jimple.v().newNopStmt();
					WeavingContext wc = PointcutCodeGen.makeWeavingContext(adviceAppl);
					
					Local bindMaskLocal=null;
					//if (bHasProceed) {
						Residue.Bindings bindings = new Residue.Bindings();
						
						adviceAppl.getResidue().getAdviceFormalBindings(bindings, null);
						bindings.calculateBitMaskLayout();
						
						debug(" " + bindings);
						
						{
							LocalGeneratorEx lg=new LocalGeneratorEx(shadowMethodBody);
							bindMaskLocal=lg.generateLocal(IntType.v(), "bindMask");			
						}
						
						debug("Residue before modification: " + adviceAppl.getResidue());
						
						adviceAppl.setResidue(
								adviceAppl.getResidue().restructureToCreateBindingsMask(bindMaskLocal, bindings));
						
						debug("Residue after modification: " + adviceAppl.getResidue());
						
					//}
					
					Stmt endResidue=weaveDynamicResidue(
						returnedLocal,
						skipDynamicActuals,
						shadowID,
						wc,
						failPoint,
						redirectExceptions);
					
					shadowMethodStatements.insertAfter(
						Jimple.v().newAssignStmt(bindMaskLocal, IntConstant.v(0))
						, begin);
					
					//List assignments=getAssignmentsToAdviceFormals(begin, endResidue, staticBindings);
					//createBindingMask(assignments, staticBindings, wc, begin, endResidue);
					
					ProceedMethod.this.assignCorrectParametersToLocals(
						context,
						argIndex,
						first,
						localMap,
						bindings);
			
					
					if (!bUseClosureObject) 
						ProceedMethod.this.modifyLookupStatement(switchTarget, shadowID);
					
					Local lThis = null;
					if (!bStaticShadowMethod)
						lThis = shadowMethodBody.getThisLocal();
		
					makeAdviceInvocation(bindMaskLocal,returnedLocal, contextActuals, 
							     (bUseClosureObject ? lClosure : lThis), shadowID, failPoint, wc,
							     new DisableExceptionCheckTag());
						
					if (abc.main.Debug.v().aroundWeaver)
						ProceedMethod.this.sootProceedMethod.getActiveBody().validate();
				}
				/**Copies a sequence of statements from one method to another.
				 * Copied units exclude begin and end.
				 * Returns bindings (old-unit<->new-unit).
				 * Returns first inserted unit in the destination method in the UnitBox.
				 * 
				 * If returnedLocal is not null, the corresponding new local is returned after the 
				 * copy of the block.
				 * 
				 * The former local "this" is mapped to the new "this". 
				 * 
				 * This is a modified version of Body.importBodyContentsFrom()
				 * */
				private HashMap copyStmtSequence(
					Body source,
					Unit begin,
					Unit end,
					Body dest,
					Unit insertAfter,
					Local returnedLocal,
					ObjectBox resultingFirstCopy) {
					
					if (returnedLocal!=null && !source.getLocals().contains(returnedLocal))
						throw new InternalAroundError("returnedLocal " + returnedLocal + 
								" not in source method " + source.getMethod());
			
					boolean bInstance = !source.getMethod().isStatic() && !dest.getMethod().isStatic();
					Local lThisSource = null;
					//Local lThisCopySource = null;
					if (bInstance) {
						lThisSource = source.getThisLocal();
					//	lThisCopySource = Restructure.getThisCopy(source.getMethod());
					}
			
					Local lThisDest = null;
					//Local lThisCopyDest = null;
					if (bInstance) {
						lThisDest = dest.getThisLocal();
					//	lThisCopyDest = Restructure.getThisCopy(dest.getMethod());
					}
			
					HashMap bindings = new HashMap();
					//HashMap boxes=new HashMap();
			
					Iterator it = source.getUnits().getNonPatchingChain().iterator(begin);
					if (it.hasNext())
						it.next(); // skip begin
			
					Chain unitChain = dest.getUnits().getNonPatchingChain();
			
					Unit firstCopy = null;
					// Clone units in body's statement list 
					while (it.hasNext()) {
						Unit original = (Unit) it.next();
						if (original == end)
							break;
			
						Unit copy = (Unit) original.clone();
						copy.addAllTagsOf(original);
			
						// Add cloned unit to our unitChain.
						unitChain.insertAfter(copy, insertAfter);
						insertAfter = copy;
						if (firstCopy == null)
							firstCopy = insertAfter;
						// Build old <-> new map to be able to patch up references to other units 
						// within the cloned units. (these are still refering to the original
						// unit objects).
						bindings.put(original, copy);
					}
			
					Chain trapChain = dest.getTraps();
			
					// Clone trap units.
					it = source.getTraps().iterator();
					while (it.hasNext()) {
						Trap original = (Trap) it.next();
						if (Util.isInSequence(source, begin, end, original.getBeginUnit())
							&& Util.isInSequence(source, begin, end, original.getEndUnit())
							&& Util.isInSequence(source, begin, end, original.getHandlerUnit())) {
			
							Trap copy = (Trap) original.clone();
			
							// Add cloned unit to our trap list.
							trapChain.addLast(copy);
			
							// Store old <-> new mapping.
							bindings.put(original, copy);
						}
					}
			
					Chain destLocals = dest.getLocals();
			
					// Clone local units.
					it = source.getLocals().iterator();
					while (it.hasNext()) {
						Local original = (Local) it.next();
						Local copy = (Local) original.clone();
			
						if (original == lThisSource) {
							bindings.put(lThisSource, lThisDest);						
						} else {
							//copy.setName(copy.getName() + "$abc$" + state.getUniqueID());
							Util.setLocalName(destLocals, copy, original.getName());
							
			
							// Add cloned unit to our local list.
							destLocals.addLast(copy);
			
							// Build old <-> new mapping.
							bindings.put(original, copy);
						}
					}
			
					// Patch up references within units using our (old <-> new) map.
					it = dest.getAllUnitBoxes().iterator();
					while (it.hasNext()) {
						UnitBox box = (UnitBox) it.next();
						Unit newObject, oldObject = box.getUnit();
			
						// if we have a reference to an old object, replace it 
						// it's clone.
						if ((newObject = (Unit) bindings.get(oldObject)) != null)
							box.setUnit(newObject);
			
					}
			
					// backpatching all local variables.
					it = dest.getUseAndDefBoxes().iterator();
					while (it.hasNext()) {
						ValueBox vb = (ValueBox) it.next();
						if (vb.getValue() instanceof Local) {
							Local oldLocal = (Local) vb.getValue();
							Local newLocal = (Local) bindings.get(oldLocal);
			
							if (newLocal != null)
								vb.setValue(newLocal);
						}
			
					}
			
					// fix the trap destinations
					it = dest.getTraps().iterator();
					while (it.hasNext()) {
						Trap trap = (Trap) it.next();
						List boxes = trap.getUnitBoxes();
						Iterator it2 = boxes.iterator();
			
						while (it2.hasNext()) {
							UnitBox box = (UnitBox) it2.next();
							Unit ut = box.getUnit();
							Unit newUnit = (Unit) bindings.get(ut);
							if (newUnit != null) {
								box.setUnit(newUnit);
							} /*else {
																												
							}*/
						}
					}
			
					if (returnedLocal != null) {
						Local newLocal = (Local) bindings.get(returnedLocal);
						if (newLocal == null) {
							if (!source.getLocals().contains(returnedLocal)) {
								debug("returnedLocal " + returnedLocal +
								" is not in local chain of source method.");
							}
							debug("Source: " + Util.printMethod(source.getMethod()));
							debug("Dest : " + Util.printMethod(dest.getMethod()));
							throw new InternalAroundError("Could not find " + returnedLocal + 
									" in the bindings map. " + 
									"Source: " + source.getMethod() +
									" Dest: " + dest.getMethod());
						}
						LocalGeneratorEx lg = new LocalGeneratorEx(dest);
						Local castLocal = lg.generateLocal(dest.getMethod().getReturnType());
						AssignStmt s = Jimple.v().newAssignStmt(castLocal, newLocal);
						unitChain.insertAfter(s, insertAfter);
						insertAfter = s;
						ReturnStmt returnStmt = Jimple.v().newReturnStmt(castLocal);
						unitChain.insertAfter(returnStmt, insertAfter);
						Restructure.insertBoxingCast(dest, s, true);
						//insertBoxingCast(dest, returnStmt, returnStmt.getOpBox(), dest.getMethod().getReturnType());
						//JasminClass
						insertAfter = returnStmt;						
					} else {
						if (!dest.getMethod().getReturnType().equals(VoidType.v())) {					
							throw new InternalAroundError(
								"destination method: " + dest.getMethod() + 
								"\nsource method: " + source.getMethod());
						}
			
						ReturnVoidStmt returnStmt = Jimple.v().newReturnVoidStmt();
						unitChain.insertAfter(returnStmt, insertAfter);
						insertAfter = returnStmt;
						
					}
					if (firstCopy == null)
						firstCopy = insertAfter;
					
					resultingFirstCopy.object = firstCopy;
					return bindings;
				}

				/**
				 * Checks that:
				 * 	No units outside the shadow point to units inside the shadow
				 *  No units inside the shadow point to units outside the shadow, including end and start
				 * @param body
				 * @param begin
				 * @param end
				 */
				private void validateShadow(Body body, Stmt begin, Stmt end) {
					
					if (!abc.main.Debug.v().aroundWeaver)
						return;
					
					Chain statements = body.getUnits().getNonPatchingChain();
			
					if (!statements.contains(begin))
						throw new InternalAroundError();
			
					if (!statements.contains(end))
						throw new InternalAroundError();
			
					boolean insideRange = false;
			
					Iterator it = statements.iterator();
					while (it.hasNext()) {
						Stmt s = (Stmt) it.next();
						if (s == begin) {
							if (insideRange)
								throw new InternalAroundError();
			
							insideRange = true;
						}
			
						if (s == end) {
							if (!insideRange)
								throw new InternalAroundError();
			
							insideRange = false;
						}
			
						List unitBoxes = s.getUnitBoxes();
						Iterator it2 = unitBoxes.iterator();
						while (it2.hasNext()) {
							UnitBox box = (UnitBox) it2.next();
							if (insideRange) {
								if (!Util.isInSequence(body, begin, end, box.getUnit())) {
									if (box.getUnit() == end) {
										throw new InternalAroundError("Unit in shadow points to endshadow");
									} else if (box.getUnit() == begin) {
										throw new InternalAroundError("Unit in shadow points to beginshadow");
									} else
										throw new InternalAroundError("Unit in shadow points outside of the shadow" + body.toString());
								}
							} else {
								if (Util.isInSequence(body, begin, end, box.getUnit())) {
									throw new InternalAroundError("Unit outside of shadow points inside the shadow");
								}
							}
						}
					}
				}
				
				
				
				/**
				 * 
				 * Algorithm:
				 * 	Find all locals which are used in the range.
				 *  Intersect with all locals written to outside the range.
				 * 
				 * @param body
				 * @param begin
				 * @param end
				 */
				private List findLocalsGoingIn(Body body, Stmt begin, Stmt end) {
					Chain statements = body.getUnits().getNonPatchingChain();
			
					if (!statements.contains(begin))
						throw new InternalAroundError();
			
					if (!statements.contains(end))
						throw new InternalAroundError();
			
					Set usedInside = new HashSet();
					
					Set definedOutside = new HashSet();
			
					boolean insideRange = false;
					{
						Iterator it = statements.iterator();
						while (it.hasNext()) {
							Stmt s = (Stmt) it.next();
							if (s == begin) {
								if (insideRange)
									throw new InternalAroundError();
			
								insideRange = true;
							}
			
							if (s == end) {
								if (!insideRange)
									throw new InternalAroundError();
			
								insideRange = false;
							}
			
							if (insideRange) {
								List useBoxes = s.getUseBoxes();
								Iterator it2 = useBoxes.iterator();
								while (it2.hasNext()) {
									ValueBox box = (ValueBox) it2.next();
									if (box.getValue() instanceof Local) {
										Local l = (Local) box.getValue();
										usedInside.add(l);
									}
								}
							} else {
								List defBoxes = s.getDefBoxes();
								Iterator it2 = defBoxes.iterator();
								while (it2.hasNext()) {
									ValueBox box = (ValueBox) it2.next();
									if (box.getValue() instanceof Local) {
										Local l = (Local) box.getValue();
										definedOutside.add(l);
									}
								}
							}
						}
					}
					
					int usedInsideCount=usedInside.size();
					
					List result=new LinkedList(usedInside);
					result.retainAll(definedOutside);					
					
					shadowInternalLocalCount=usedInsideCount-result.size();
					return result; 
				}

				private Stmt weaveDynamicResidue(
					Local returnedLocal,
					List contextActuals,
					int shadowID,
					WeavingContext wc,
					Stmt failPoint,
                                        Tag attachToInvoke) {
					LocalGeneratorEx localgen = new LocalGeneratorEx(shadowMethodBody);	
					
					shadowMethodStatements.insertBefore(failPoint, end);
					
					// weave in residue
					Stmt endResidue = adviceAppl.getResidue().codeGen
					    (shadowMethod, localgen, shadowMethodStatements, begin, failPoint, true, wc);
					
					// debug("weaving residue: " + adviceAppl.residue);
					if (adviceAppl.getResidue() instanceof AlwaysMatch //||
						//adviceAppl.getResidue() instanceof AspectOf
							)  { ///TODO: work out proper solution with ganesh!!!!!
						// can't fail
					} else {
						InvokeExpr directInvoke;
						List directParams = new LinkedList();
						
						List defaultValues = getDefaultValues(originalAdviceFormalTypes);
						directParams.addAll(defaultValues);
						directParams.add(IntConstant.v(shadowID));
						directParams.add(IntConstant.v(1)); //  bindMask parameter (1 => skip)
						directParams.addAll(contextActuals);
						if (bUseClosureObject) {
							directInvoke = Jimple.v().newStaticInvokeExpr(sootProceedMethod.makeRef(), directParams);
						} else if (bStaticProceedMethod) {
							directInvoke = Jimple.v().newStaticInvokeExpr(sootProceedMethod.makeRef(), directParams);
						} else {
							// TODO: can this call be replaced with an InvokeSpecial?
							directInvoke = Jimple.v().newInterfaceInvokeExpr(shadowMethodBody.getThisLocal() , interfaceInfo.abstractProceedMethod.makeRef(), directParams);
						}
						{
							Stmt skipAdvice;
							if (returnedLocal != null) {
								AssignStmt assign = Jimple.v().newAssignStmt(returnedLocal, directInvoke);
								shadowMethodStatements.insertAfter(assign, failPoint);
								Restructure.insertBoxingCast(shadowMethodBody, assign, true);
								skipAdvice = assign;
							} else {
								skipAdvice = Jimple.v().newInvokeStmt(directInvoke);
								shadowMethodStatements.insertAfter(skipAdvice, failPoint);
							}
							skipAdvice.addTag(attachToInvoke);
							skipAdvice.addTag(new AroundShadowInfoTag(new ShadowInlineInfo(shadowSize, shadowInternalLocalCount)));
							directInvocationStmts.add(skipAdvice);
						}
					}
					return endResidue;
				}
								
				public SootClass generateClosure(
						String closureRunMethodName, SootMethod targetProceedMethod,
						List /*Local*/ context) {
					
					final String packageName=targetProceedMethod.getDeclaringClass().getPackageName();
					final String className=
						(packageName.length()==0 ? "" : packageName + "." ) + 
						"Abc$closure$" + state.getUniqueID();
					SootClass closureClass = 
						new SootClass(className, 
							Modifier.PUBLIC);

					closureClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
					//closureClass.implementsInterface(interfaceName);
					closureClass.addInterface(interfaceInfo.closureInterface);

					//SootMethod cons=new SootMethod()
					debug(" " + Scene.v().getSootClass("java.lang.RuntimeException").getMethod("<init>", new LinkedList()));
					//if (closureClass!=null)
					//	throw new RuntimeException();
					SootMethod cons=new SootMethod("<init>", new LinkedList(), VoidType.v(), Modifier.PUBLIC );			
					closureClass.addMethod(cons);
					cons.setActiveBody(Jimple.v().newBody(cons));
					{
						Body b=cons.getActiveBody();
						Chain statements=b.getUnits().getNonPatchingChain();
						LocalGeneratorEx lg=new LocalGeneratorEx(b);
						Local lThis = lg.generateLocal(closureClass.getType(), "this");
						statements.addFirst(Jimple.v().newIdentityStmt(lThis, Jimple.v().newThisRef(closureClass.getType())));
						
						statements.addLast(
							Jimple.v().newInvokeStmt(
								Jimple.v().newSpecialInvokeExpr(lThis, 
									Scene.v().getSootClass("java.lang.Object").getMethod("<init>", new LinkedList()).makeRef())));
						statements.addLast(Jimple.v().newReturnVoidStmt());
					}
					
					SootMethod runMethod =
						new SootMethod(closureRunMethodName, new LinkedList(),						 
								getAdviceReturnType(), Modifier.PUBLIC);

					closureClass.addMethod(runMethod);
					//signature.setActiveBody(Jimple.v().newBody(signature));
					closureProceedMethods.add(runMethod);

					Scene.v().addClass(closureClass);
					closureClass.setApplicationClass();

					Body body = Jimple.v().newBody(runMethod);
					runMethod.setActiveBody(body);
					
					LocalGeneratorEx lg=new LocalGeneratorEx(body);
				
					Chain statements=body.getUnits().getNonPatchingChain();
			
					Local lThis = lg.generateLocal(closureClass.getType(), "this");
					statements.addFirst(Jimple.v().newIdentityStmt(lThis, Jimple.v().newThisRef(closureClass.getType())));
					
					List invokeLocals=new LinkedList();
					{
						int i=0;
						for (Iterator it=
								interfaceInfo.abstractProceedMethod.getParameterTypes().iterator(); 
								it.hasNext();i++) {
							Type t=(Type)it.next();
							Local l=Restructure.addParameterToMethod(runMethod, t, "arg");
							
							// shadowID, bindMask, advice-formals
							if (i<1+1+originalAdviceFormalTypes.size())
								invokeLocals.add(l);			
						}
					}
						
					Util.validateMethod(runMethod);			
					
					{
						int i=0;
						for (Iterator it=context.iterator(); it.hasNext();i++) {
							Local l=(Local)it.next();
							SootField f=new SootField("context" + i, l.getType(), Modifier.PUBLIC);
							closureClass.addField(f);
							debug("1" + f.getType()+ " : " + l.getType());
							Local lTmp=lg.generateLocal(l.getType());
							AssignStmt as=Jimple.v().newAssignStmt(lTmp, 
								Jimple.v().newInstanceFieldRef(lThis, f.makeRef()));
							statements.add(as);
							invokeLocals.add(lTmp);
						}
					}
					
					if (targetProceedMethod.getParameterCount()!=invokeLocals.size()) {						
						throw new InternalAroundError(
								"proceed method: " + targetProceedMethod.getSignature() +
								" invoke locals: " + invokeLocals);
					}
					
					InvokeExpr invEx=Jimple.v().newStaticInvokeExpr(targetProceedMethod.makeRef(), invokeLocals);
					if (getAdviceReturnType().equals(VoidType.v())) {
						statements.add(Jimple.v().newInvokeStmt(invEx));
						statements.add(Jimple.v().newReturnVoidStmt());	
					} else {			
						Local returnedLocal=lg.generateLocal(getAdviceReturnType());
						AssignStmt as=Jimple.v().newAssignStmt(returnedLocal, invEx);
						statements.add(as);
						statements.add(Jimple.v().newReturnStmt(returnedLocal));
					}
					
					Util.validateMethod(runMethod);
							
					return closureClass;
				}
				public Local generateClosureCreation(SootClass closureClass, List /*Local*/ context) {
					
					LocalGeneratorEx lg=new LocalGeneratorEx(shadowMethodBody);
					Local l=lg.generateLocal(closureClass.getType(), "closure");
					Stmt newStmt = Jimple.v().newAssignStmt(l, Jimple.v().newNewExpr(closureClass.getType()));
//					Stmt init = Jimple.v().newInvokeStmt(
//						Jimple.v().newSpecialInvokeExpr(l, 
//							Scene.v().getSootClass("java.lang.Object").getMethod("<init>", new ArrayList())));
					Stmt init = Jimple.v().newInvokeStmt(
						Jimple.v().newSpecialInvokeExpr(l, 
							closureClass.getMethodByName("<init>").makeRef()));//, new ArrayList())));

					shadowMethodStatements.insertAfter(init, begin);
					shadowMethodStatements.insertAfter(newStmt, begin);
					int i=0;
					for (Iterator it=context.iterator(); it.hasNext();i++) {
						Local lContext=(Local)it.next();
						SootField f=closureClass.getFieldByName("context" + i);
						debug("2" + f.getType()+ " : " + lContext.getType());
						AssignStmt as=Jimple.v().newAssignStmt(
							Jimple.v().newInstanceFieldRef(l, f.makeRef()), lContext);
						if (!f.getType().equals(lContext.getType()))
							throw new InternalAroundError("" + f.getType()+ " : " + lContext.getType());
						shadowMethodStatements.insertAfter(as, init);
					}
					return l;
				}
								
			        private void makeAdviceInvocation(Local bindMaskLocal, Local returnedLocal, List contextActuals, Local lThis, int shadowID, Stmt insertionPoint, WeavingContext wc,Tag attachToInvoke) {
					LocalGeneratorEx lg = new LocalGeneratorEx(shadowMethodBody);
					Chain invokeStmts = adviceAppl.advice.makeAdviceExecutionStmts(adviceAppl, lg, wc);
					
					VirtualInvokeExpr invokeEx = (VirtualInvokeExpr) ((InvokeStmt) invokeStmts.getLast()).getInvokeExpr();
					Local aspectRef = (Local) invokeEx.getBase();
					
					{
						Stmt last = (Stmt) invokeStmts.getLast();
						NopStmt nop = Jimple.v().newNopStmt();
						invokeStmts.insertBefore(nop, last);
						last.redirectJumpsToThisTo(nop);
						invokeStmts.removeLast();
					}
					for (Iterator stmtlist = invokeStmts.iterator(); stmtlist.hasNext();) {
						Stmt nextstmt = (Stmt) stmtlist.next();
						if (nextstmt == null)
							throw new InternalAroundError();
						if (insertionPoint == null)
							throw new InternalAroundError();
						if (shadowMethodStatements == null)
							throw new InternalAroundError();
						if (!shadowMethodStatements.contains(insertionPoint))
							throw new InternalAroundError();
						shadowMethodStatements.insertBefore(nextstmt, insertionPoint);
					}
					
					// we need to add some of our own parameters to the invocation
					List params = new LinkedList();
					if (bUseClosureObject)  {
						if (lThis==null)
							throw new InternalAroundError();
							
						params.add(lThis); // pass the closure
					} else if (bStaticProceedMethod) { 
						params.add(NullConstant.v());
					} else {
						if (lThis==null)
							throw new InternalAroundError();
							
						params.add(lThis); // pass the closure
					}
					//params.add(targetLocal);
					params.add(IntConstant.v(shadowID));
					if (bUseClosureObject) {
						params.add(IntConstant.v(0));
					} else if (bStaticProceedMethod) { // pass the static class id
						params.add(IntConstant.v(getStaticDispatchTypeID(shadowClass.getType())));
					} else {
						params.add(IntConstant.v(0));
					}
					//params.add(targetLocal);
					params.add(bindMaskLocal);
					
					// and add the original parameters 
					params.addAll(0, invokeEx.getArgs());
					
					params.addAll(contextActuals);
					
					// generate a new invoke expression to replace the old one
					VirtualInvokeExpr invokeEx2 = Jimple.v().newVirtualInvokeExpr(aspectRef, sootAdviceMethod.makeRef(), params);
					
					Stmt invokeStmt;
					if (returnedLocal == null) {
						invokeStmt = Jimple.v().newInvokeStmt(invokeEx2);
						shadowMethodStatements.insertBefore(invokeStmt, insertionPoint);
					} else {
						AssignStmt assign = Jimple.v().newAssignStmt(returnedLocal, invokeEx2);
						shadowMethodStatements.insertBefore(assign, insertionPoint);
						Restructure.insertBoxingCast(shadowMethod.getActiveBody(), assign, true);
						invokeStmt = assign;
					}
					invokeStmt.addTag(attachToInvoke);
					invokeStmt.addTag(new AroundShadowInfoTag(
							new ShadowInlineInfo(shadowSize, shadowInternalLocalCount)));
					Stmt beforeEnd=Jimple.v().newNopStmt();
					shadowMethodStatements.insertBefore(beforeEnd, end);
					shadowMethodStatements.insertBefore(Jimple.v().newGotoStmt(beforeEnd), insertionPoint);
					
					if (invokeStmt == null)
						throw new InternalAroundError();
					
					adviceMethodInvocationStmts.add(invokeStmt);
					
					//if (abc.main.Debug.v().aroundWeaver)
					//	shadowMethodBody.validate();
				}

				
				private Local findReturnedLocal() {
					
					Value v=adviceAppl.shadowmatch.getReturningContextValue().getSootValue();
					
					if (v instanceof Local) {
						return (Local)v;
					} else if (true){
						LocalGeneratorEx lg=new LocalGeneratorEx(shadowMethodBody);
						Type type=getAdviceReturnType();
						if (type.equals(VoidType.v())) {
							return null;
						} else {
							Local l=lg.generateLocal(type , "returnedLocal");
							Stmt s=Jimple.v().newAssignStmt(l, v);
							shadowMethodStatements.insertAfter(s, begin);
							return l;
						}
					}
					
					// ***** the code below is unreachable *****
					
					Body shadowMethodBody = shadowMethod.getActiveBody();
					if (abc.main.Debug.v().aroundWeaver) {
						try {
							shadowMethodBody.validate();
						} catch (RuntimeException e ) {
							debug("shadow method: " + Util.printMethod(shadowMethod));
							throw e;
						}
					}
					Chain shadowMethodStatements = shadowMethodBody.getUnits().getNonPatchingChain();
					boolean bStatic = shadowMethod.isStatic();
			
					Stmt begin = adviceAppl.shadowmatch.sp.getBegin();
					Stmt end = adviceAppl.shadowmatch.sp.getEnd();
			
					Local returnedLocal;// = null;
					
					Type objectType=Scene.v().getRefType("java.lang.Object");
			
					if (adviceAppl instanceof ExecutionAdviceApplication || 
						adviceAppl instanceof ConstructorAdviceApplication) {
						//ExecutionAdviceApplication ea = (ExecutionAdviceApplication) adviceAppl;
						
						if (adviceAppl instanceof ConstructorAdviceApplication) {
							if (!shadowMethod.getReturnType().equals(VoidType.v()))
								throw new InternalAroundError("Constructor must have void return type: " + 
									shadowMethod);
						}
							
						if (shadowMethod.getReturnType().equals(VoidType.v())) {
							if (
								! getAdviceReturnType().equals(VoidType.v())					 
								) { 
								// make dummy local to be returned. assign default value.
								LocalGeneratorEx lg=new LocalGeneratorEx(shadowMethodBody);
								Local l=lg.generateLocal(getAdviceReturnType(), "returnedLocal");
								Stmt s=Jimple.v().newAssignStmt(l, 
									Restructure.JavaTypeInfo.getDefaultValue(getAdviceReturnType()));
								shadowMethodStatements.insertAfter(s, begin);
								returnedLocal=l;			
							} else {
								returnedLocal=null;
							}
						} else {
							ReturnStmt returnStmt;
							try {
								returnStmt = (ReturnStmt) shadowMethodStatements.getSuccOf(end);
							} catch (Exception ex) {
								debug(" " + Util.printMethod(shadowMethod));
								throw new InternalAroundError("Expecting return statement after shadow " + 
										"for execution advice in non-void method");
							}
			
						
							// Create a new local inside the shadow.
							// Assign the return value to that local, 
  						// 	and then return the local.
							LocalGeneratorEx lg=new LocalGeneratorEx(shadowMethodBody);
							
							if (returnStmt.getOp() instanceof Local) {	
								Local l=lg.generateLocal(
										shadowMethod.getReturnType(), "tmp");
								Stmt s=Jimple.v().newAssignStmt(l, 
										returnStmt.getOp());
								shadowMethodStatements.insertBefore(s, end);
								s=Jimple.v().newAssignStmt( 
										returnStmt.getOp(), l);
								shadowMethodStatements.insertBefore(s, end);
							} else {
								Local l=lg.generateLocal(
										shadowMethod.getReturnType(), "returnedLocal");
								Stmt s=Jimple.v().newAssignStmt(l, 
										returnStmt.getOp());
								returnStmt.setOp(l);
								
								shadowMethodStatements.insertBefore(s, end);
								//returnedLocal=l;	
							}
							//returnStmt.setOp(l);
							
							returnedLocal=(Local)returnStmt.getOp();	

							
						}
					} else if (adviceAppl instanceof HandlerAdviceApplication) {
						throw new InternalAroundError(
								"Front-end issue: " +
								"Cannot apply around advice to exception handler");
					} else if (adviceAppl instanceof StmtAdviceApplication ||
							   adviceAppl instanceof NewStmtAdviceApplication) { 
					
						Stmt applStmt=null;
						{				
							if (adviceAppl instanceof StmtAdviceApplication) {
								StmtAdviceApplication stmtAppl = (StmtAdviceApplication) adviceAppl;
								applStmt=stmtAppl.stmt;	
							} else if (adviceAppl instanceof NewStmtAdviceApplication) {
								NewStmtAdviceApplication stmtAppl = (NewStmtAdviceApplication) adviceAppl;
								applStmt=stmtAppl.stmt;
							} else {
								throw new InternalAroundError(); 
							}
						}
						if (Weaver.getUnitBindings().containsKey(applStmt))
							applStmt=(Stmt)Weaver.getUnitBindings().get(applStmt);
							
						if (applStmt instanceof AssignStmt) {					   
							AssignStmt assignStmt = (AssignStmt) applStmt;
							Value leftOp = assignStmt.getLeftOp();
							Value rightOp = assignStmt.getRightOp();
							if (leftOp instanceof Local) {
								// get
								returnedLocal = (Local) leftOp;
								
							} else if (leftOp instanceof FieldRef || leftOp instanceof ArrayRef) {
							    // This is some kind of set. AspectJ pointcuts only allow for setfield,
							    // but extenders might want things like ArrayRefs here too. Should check
							    // the Jimple grammar to see if anything else would make sense here.

						        if(!(rightOp instanceof Local || rightOp instanceof Constant)) {
							        // violates the Jimple grammar
							        throw new InternalAroundError();
   	                            }

								
								// special case: with return type object, set() returns null.
								if (getAdviceReturnType().equals(objectType)) {
									LocalGeneratorEx lg=new LocalGeneratorEx(shadowMethodBody);
									Local l=lg.generateLocal(objectType, "nullValue");
									Stmt s=Jimple.v().newAssignStmt(l, NullConstant.v());
									shadowMethodStatements.insertAfter(s, begin);
									returnedLocal=l;
								} else {
								    // This should be unreachable, because set advice only matches
								    // if the return type of the advice is void or if the return type
								    // is Object. In the former case the weaver should not be looking
								    // for a return value.
								    throw new InternalAroundError();
								}
							} else {
								// unexpected statement type
								throw new InternalAroundError();
							}
						} else if (applStmt instanceof InvokeStmt) {
							InvokeStmt invStmt=(InvokeStmt)applStmt;
							
							//if (true) throw new RuntimeException();
							
							// if advice method is non-void, we have to return something
							// TODO: type checking to throw out invalid cases?
							if (
								! getAdviceReturnType().equals(VoidType.v())					 
								) { 
								// make dummy local to be returned. assign default value.
								Type returnType=getAdviceReturnType(); 
								
								LocalGeneratorEx lg=new LocalGeneratorEx(shadowMethodBody);
								Local l=lg.generateLocal(returnType, "returnedLocal"); 
								Stmt s=Jimple.v().newAssignStmt(l, 
									Restructure.JavaTypeInfo.getDefaultValue(returnType));
								shadowMethodStatements.insertAfter(s, begin);
								returnedLocal=l;			
							} else {
								returnedLocal=null;															
							}
						} else {
							// unexpected statement type
							throw new InternalAroundError();
						}
					} else {
						throw new InternalAroundError("Unkown type of advice application: " + adviceAppl.getClass());
					}
					return returnedLocal;
				}
				

				private AdviceDecl getAdviceDecl(SootMethod method) {
					List l=GlobalAspectInfo.v().getAdviceDecls();
					for (Iterator it=l.iterator(); it.hasNext(); ) {
						AdviceDecl decl=(AdviceDecl)it.next();
						if (decl.getImpl().getSootMethod().equals(method)) {
							return decl;
						}					
					}
					throw new InternalAroundError();
				}
				public final Stmt begin;
				public final Stmt end;
				public final AdviceApplication adviceAppl;
				//public final AdviceMethod adviceMethod;
				public final SootClass shadowClass;
				public final SootMethod shadowMethod;
				public final Chain shadowMethodStatements;
				public final Body shadowMethodBody;
				public final boolean bStaticShadowMethod;
				//public final boolean bUseStaticProceedMethod;
				//public final AdviceMethod.ProceedMethod proceedMethod;	
			}
			public void modifyLookupStatement(Stmt switchTarget, int shadowID) {
				// modify the lookup statement in the proceed method
				lookupValues.add(IntConstant.v(shadowID));
				proceedMethodTargets.add(switchTarget);
				// generate new lookup statement and replace the old one
				Stmt newLookupStmt;
				if (bStaticProceedMethod && numOfShadows==0)
					newLookupStmt=Jimple.v().newNopStmt();
				else
					newLookupStmt=
					Util.newSwitchStmt(// Jimple.v().newLookupSwitchStmt(
						shadowIdParamLocal,
						lookupValues,
						proceedMethodTargets,
						defaultTarget);
				proceedMethodStatements.insertAfter(newLookupStmt, lookupStmt);
				proceedMethodStatements.remove(lookupStmt);
				lookupStmt = newLookupStmt;

				if (!bStaticProceedMethod) {
					 AdviceMethod.this.fixProceedMethodSuperCalls(shadowClass);
				}

				Util.cleanLocals(proceedMethodBody);			
			}
			//HashMap /*String, Integer*/ fieldIDs=new HashMap();
			private void addParameters(List addedDynArgsTypes)  {
				debug("adding parameters to access method " + sootProceedMethod);
				Util.validateMethod(sootProceedMethod);

				Iterator it2 = addedDynArgsTypes.iterator();
				while (it2.hasNext()) {
					Type type = (Type) it2.next();
					debug(" " + type);
					Local l = Restructure.addParameterToMethod(sootProceedMethod, type, "contextArgFormal");
					contextParamLocals.add(l);
				}

				//				modify existing super call in the access method		
				Stmt stmt = superInvokeStmt;
				if (stmt != null) {
					//throw new InternalCompilerError("This does not work until soot allows changing method refs properly");
					
					//addEmptyDynamicParameters(method, addedDynArgs, proceedMethodName);
					InvokeExpr invoke = (InvokeExpr) stmt.getInvokeExprBox().getValue();
					List newParams = new LinkedList();
					newParams.addAll(Util.getParameterLocals(sootProceedMethod.getActiveBody()));
					List types=new LinkedList(sootProceedMethod.getParameterTypes());					
					
					InvokeExpr newInvoke = Util.createNewInvokeExpr(invoke, newParams, types);
					stmt.getInvokeExprBox().setValue(newInvoke);					
				}
			}
			private void assignCorrectParametersToLocals(
				List context,
				int[] argIndex,
				Stmt first,
				HashMap localMap,
				Residue.Bindings bindings) {
			
				debug("Access method: assigning correct parameters to locals*********************");
			
				LocalGeneratorEx lg=new LocalGeneratorEx(proceedMethodBody);
				
				// Assign the correct access parameters to the locals 
				Stmt insertionPoint = first;
				Stmt skippedCase = Jimple.v().newNopStmt();
				Stmt nonSkippedCase = Jimple.v().newNopStmt();
				Stmt neverBoundCase = Jimple.v().newNopStmt();
				Stmt gotoStmt = Jimple.v().newGotoStmt(neverBoundCase);
				Stmt ifStmt = Jimple.v().newIfStmt(Jimple.v().newEqExpr(bindMaskParamLocal, IntConstant.v(1)), skippedCase);
				proceedMethodStatements.insertBefore(ifStmt, insertionPoint);
				proceedMethodStatements.insertBefore(nonSkippedCase, insertionPoint);
				proceedMethodStatements.insertBefore(gotoStmt, insertionPoint);
				proceedMethodStatements.insertBefore(skippedCase, insertionPoint);
				proceedMethodStatements.insertBefore(neverBoundCase, insertionPoint);
				NopStmt afterDefault=Jimple.v().newNopStmt();
				proceedMethodStatements.insertAfter(afterDefault, nonSkippedCase);

				Local maskLocal=lg.generateLocal(IntType.v(), "maskLocal");
				
				Set defaultLocals=new HashSet();
				
				boolean emptyIf=true;

				// Process the bindings.
				// The order is important.
				for (int index=bindings.numOfFormals()-1; index>=0; index--){
					List localsFromIndex=bindings.localsFromIndex(index);
					if (localsFromIndex==null) { 
					} else {
						emptyIf=false;
						
						if (localsFromIndex.size()==1) 	{ // non-skipped case: assign advice formal
							Local paramLocal = (Local) adviceFormalLocals.get(index);
							Local actual=(Local)localsFromIndex.get(0);
							Local actual2=(Local)localMap.get(actual);
							AssignStmt s = Jimple.v().newAssignStmt(actual2, paramLocal);
							proceedMethodStatements.insertAfter(s, nonSkippedCase);
							Restructure.insertBoxingCast(sootProceedMethod.getActiveBody(), s, true);
							
							/// allow boxing?
						} else {	
							// Before all the switch statements, the default values are
							// assigned.
							// The switch statements then (possibly) overwrite the values with 
							// the actual bindings.
							{
								//NopStmt nop=Jimple.v().newNopStmt();
								//statements.insertAfter(nop, nonSkippedCase);
								for (Iterator itl=localsFromIndex.iterator(); itl.hasNext();) {
									Local l=(Local)itl.next();
									int id=context.indexOf(l);
									if (id==-1) {
										debug(" skipped local: " + l);
									} else if (!defaultLocals.contains(l)){
										defaultLocals.add(l);
										Local paramLocal = (Local) contextParamLocals.get(argIndex[id]);
										Local actual3=(Local)localMap.get(l);
										AssignStmt s = Jimple.v().newAssignStmt(actual3, paramLocal);
										proceedMethodStatements.insertBefore(s, afterDefault);
										Restructure.insertBoxingCast(sootProceedMethod.getActiveBody(), s, true);
										emptyIf=false;
									}
								}
							}
							
							//generatedSwitchStmtsIDs.add(new Integer(index));
							int mask=bindings.getMaskBits(index);
							AssignStmt as=Jimple.v().newAssignStmt(
								maskLocal, Jimple.v().newAndExpr( bindMaskParamLocal, IntConstant.v(mask)  ));
							AssignStmt as2=Jimple.v().newAssignStmt(
								maskLocal, Jimple.v().newShrExpr(
									maskLocal, IntConstant.v(bindings.getMaskPos(index))));
							
							proceedMethodStatements.insertAfter(as, afterDefault);
							proceedMethodStatements.insertAfter(as2, as);
							NopStmt endStmt=Jimple.v().newNopStmt();
							proceedMethodStatements.insertAfter(endStmt, as2);
							
							int localIndex=0;
							List lookupValues=new LinkedList();
							List targets=new LinkedList();
							for (Iterator itl=localsFromIndex.iterator(); itl.hasNext();localIndex++) {
								Local l=(Local)itl.next();
								lookupValues.add(IntConstant.v(localIndex));
								
								Local actual3=(Local)localMap.get(l);
								
								NopStmt targetNop=Jimple.v().newNopStmt();
								
								proceedMethodStatements.insertAfter(targetNop, as2);
								targets.add(targetNop);
								
								
								Local paramLocal = (Local) adviceFormalLocals.get(index);
								AssignStmt s = Jimple.v().newAssignStmt(actual3, paramLocal);
								proceedMethodStatements.insertAfter(s, targetNop);
								GotoStmt g=Jimple.v().newGotoStmt(endStmt);
								proceedMethodStatements.insertAfter(g, s);
								Restructure.insertBoxingCast(sootProceedMethod.getActiveBody(), s, true);
						
							}
							
			
							// default case (exception)								
							SootClass exception = Scene.v().getSootClass("java.lang.RuntimeException");
							Local ex = lg.generateLocal(exception.getType(), "exception");
							Stmt newExceptStmt = Jimple.v().newAssignStmt(ex, Jimple.v().newNewExpr(exception.getType()));
							Stmt initEx = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(ex, exception.getMethod("<init>", new ArrayList()).makeRef()));
							Stmt throwStmt = Jimple.v().newThrowStmt(ex);
							proceedMethodStatements.insertAfter(newExceptStmt, as2);
							proceedMethodStatements.insertAfter(initEx, newExceptStmt);
							proceedMethodStatements.insertAfter(throwStmt, initEx);
						
							
							Stmt lp=Util.newSwitchStmt(// Jimple.v().newLookupSwitchStmt(
								maskLocal, lookupValues, targets, newExceptStmt
								);
							proceedMethodStatements.insertAfter(lp, as2);
						}
					}
				}
				
				int i=0;
				// process the context
				for (Iterator it=context.iterator(); it.hasNext(); i++) {
					
					Local actual = (Local) it.next(); // context.get(i);
					Local actual2 = (Local) localMap.get(actual);
					if (!proceedMethodBody.getLocals().contains(actual2))
						throw new InternalAroundError();
					if (actual2 == null)
						throw new InternalAroundError();

					if (bindings.contains(actual)) {
						debug(" static binding: " + actual.getName());
						
						/*
						// We use lastIndexOf here to mimic ajc's behavior:
						// When binding the same value multiple times, ajc's
						// proceed only regards the last one passed to it.
						// Can be changed to indexOf to pick the first one 
						// (which would also seem reasonable). 
						int index = bindings.lastIndexOf(actual);
						*/
						
						{ // skipped case: assign dynamic argument
							Local paramLocal = (Local) contextParamLocals.get(argIndex[i]);
							AssignStmt s = Jimple.v().newAssignStmt(actual2, paramLocal);
							proceedMethodStatements.insertAfter(s, skippedCase);
							Restructure.insertBoxingCast(sootProceedMethod.getActiveBody(), s, true);
							/// allow boxing?
						}
						emptyIf=false;
					} else {
						debug(" no binding: " + actual.getName());
						// no binding
						Local paramLocal = (Local) contextParamLocals.get(argIndex[i]);
						AssignStmt s = Jimple.v().newAssignStmt(actual2, paramLocal);
						proceedMethodStatements.insertAfter(s, neverBoundCase);
						insertCast(sootProceedMethod.getActiveBody(), s, s.getRightOpBox(), actual2.getType());
					}
				}
				
				if (emptyIf)
					proceedMethodStatements.remove(ifStmt);
				
				debug("done: Access method: assigning correct parameters to locals*********************");
			}
		

			//public final AdviceMethod adviceMethod;
			public final SootClass shadowClass;
			public final boolean bStaticProceedMethod;
			public final Body proceedMethodBody;
			public final Chain proceedMethodStatements;
			
			public final boolean bUseClosureObject;
			
			
			final List proceedMethodTargets = new LinkedList();
			final List lookupValues = new LinkedList();
			final NopStmt defaultTarget;
			final NopStmt defaultEnd;
			Stmt lookupStmt;
			int nextShadowID;
			public final int shadowIDParamIndex;
			public final Local shadowIdParamLocal;
			public final Local bindMaskParamLocal;
			
			final List contextParamLocals = new LinkedList();
			final List adviceFormalLocals = new LinkedList();

			public final SootMethod sootProceedMethod;

			SootClass superCallTarget = null;
			Stmt superInvokeStmt = null;

			
		}
		private void validate() {
			{
				Iterator it=adviceMethodInvocationStmts.iterator();
				
				while (it.hasNext()) {
					Stmt stmt=(Stmt)it.next();
					if (stmt.getInvokeExpr().getArgCount()!=sootAdviceMethod.getParameterCount()) {
						throw new InternalAroundError(
							"Call to advice method " + sootAdviceMethod +
							" has wrong number of arguments: " + stmt						
							);
					}
				}
			}
			for (Iterator it0=adviceLocalClasses.values().iterator(); it0.hasNext();){
				AdviceLocalClass c=(AdviceLocalClass)it0.next();
				
				for (Iterator it1=c.adviceLocalMethods.iterator(); it1.hasNext();) {
					AdviceLocalClass.AdviceLocalMethod m=
						(AdviceLocalClass.AdviceLocalMethod)it1.next();
					
					Iterator it=m.interfaceInvocationStmts.iterator();
					
					while (it.hasNext()) {
						Stmt stmt=(Stmt)it.next();
						if (stmt.getInvokeExpr().getArgCount()!=interfaceInfo.abstractProceedMethod.getParameterCount() ) {
							throw new InternalAroundError(
								"Call to interface method in advice method " + sootAdviceMethod + 
								" has wrong number of arguments: " + stmt
								);
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
						if (info.sootProceedMethod.getParameterCount()!=interfaceInfo.abstractProceedMethod.getParameterCount()) {
							throw new InternalAroundError(
								"Access method " + info.sootProceedMethod + 
								" has wrong number of arguments."
								);
						}
					}
				}	
			}
			{
				Iterator it = closureProceedMethods.iterator();
				while (it.hasNext()) {
					SootMethod method = (SootMethod) it.next();					
					if (method.getParameterCount()!=interfaceInfo.abstractProceedMethod.getParameterCount()) {
						throw new InternalAroundError(
							"Closure method " + method + 
							" has wrong number of arguments."
							);
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
		private void fixProceedMethodSuperCalls(SootClass newAccessClass) {
			
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
				ProceedMethod accessInfo = (ProceedMethod) proceedMethodImplementations.get(className);
	
				SootClass cl = Scene.v().getSootClass(className);
				// if the class is a sub-class of the new class or 
				// if this is the new class and we need to add a super to the new class
				if (Restructure.isBaseClass(newAccessClass, cl) || 
					(className.equals(newAccessClass.getName()) && bAddSuperToNewMethod)) {
					if (accessInfo.superCallTarget == null
						|| // if the class has no super() call 
						Restructure.isBaseClass(accessInfo.superCallTarget, newAccessClass)) { // or if it's invalid
	
						// generate new super() call
						Body body = accessInfo.sootProceedMethod.getActiveBody();
						Chain statements = body.getUnits().getNonPatchingChain();
						Type returnType = accessInfo.sootProceedMethod.getReturnType();
	
						// find super class that implements the interface.
						// This is the target class of the super call.
						accessInfo.superCallTarget = cl.getSuperclass();
						while (!keys.contains(accessInfo.superCallTarget.getName())) {
							try {
								accessInfo.superCallTarget = accessInfo.superCallTarget.getSuperclass();
							} catch (InternalAroundError e) {
								System.err.println("Class: " + accessInfo.superCallTarget);
								throw e;
							}
						}
	
						Util.removeStatements(body, accessInfo.defaultTarget, accessInfo.defaultEnd, null);
						LocalGeneratorEx lg = new LocalGeneratorEx(body);
						Local lThis = body.getThisLocal();
	
						String proceedMethodName = accessInfo.sootProceedMethod.getName();
						Util.validateMethod(accessInfo.sootProceedMethod);
						SpecialInvokeExpr ex =
							Jimple.v().newSpecialInvokeExpr(lThis, accessInfo.superCallTarget.getMethodByName(proceedMethodName).makeRef(), Util.getParameterLocals(body));
	
						if (returnType.equals(VoidType.v())) {
							Stmt s = Jimple.v().newInvokeStmt(ex);
							statements.insertBefore(s, accessInfo.defaultEnd);
							statements.insertBefore(Jimple.v().newReturnVoidStmt(), accessInfo.defaultEnd);
	
							accessInfo.superInvokeStmt = s;
						} else {
							Local l = lg.generateLocal(returnType, "retVal");
							AssignStmt s = Jimple.v().newAssignStmt(l, ex);
							statements.insertBefore(s, accessInfo.defaultEnd);
							statements.insertBefore(Jimple.v().newReturnStmt(l), accessInfo.defaultEnd);
	
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
				staticDispatchTypeIDs.put(name, new Integer(nextStaticTypeDispatchID++));
			}
			return ((Integer) staticDispatchTypeIDs.get(name)).intValue();
		}
		int nextStaticTypeDispatchID = 1; // 0 is a special value
		final HashMap /*String, int*/ staticDispatchTypeIDs = new HashMap();

		private void addParametersToProceedMethodImplementations(List addedDynArgsTypes) 
		{
			//Set keys=adviceMethodInfo.proceedMethodImplementations.keySet();
			List proceedMethodImplementations = getAllProceedMethods();
			Iterator it = proceedMethodImplementations.iterator();
			while (it.hasNext()) {
				ProceedMethod info = (ProceedMethod) it.next();
	
				info.addParameters(addedDynArgsTypes);
			}
		}

		public int[] modifyAdviceMethod(List contextParameters, ProceedMethod proceedMethod, ObjectBox contextActualsResult, 
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

			List /*Type*/ addedContextArgsTypes = new LinkedList();
			
			int[] argIndex;
			if (bUseClosureObject) {
				argIndex = new int[0]; 
			} else {
				argIndex = determineContextParameterMappings(contextParameters, addedContextArgsTypes);
			}

			List contextActuals;
			if (bUseClosureObject) {
				contextActuals = new LinkedList();
			} else {
			 	contextActuals = getContextActualsList(contextParameters, argIndex);
			}

			// create list of default values for the added arguments
			// (for invocations at other locations)
			if (!bUseClosureObject){			
				List addedDynArgs = getDefaultValues(addedContextArgsTypes);
	
				addContextParamsToInterfaceDefinition(addedContextArgsTypes);
				modifyAdviceMethodInvocations(addedDynArgs, addedContextArgsTypes);
				modifyDirectInterfaceInvocations(addedDynArgs, addedContextArgsTypes);
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

			generateProceedCalls(bStaticProceedMethod, bUseClosureObject, proceedMethod);
			
			if (abc.main.Debug.v().aroundWeaver)
					adviceBody.validate();
	

			// add parameters to all proceed method implementations
			addParametersToProceedMethodImplementations(addedContextArgsTypes);
	
			{ // process all classes. the aspect class is processed last.
				for (Iterator it=adviceLocalClasses.values().iterator(); it.hasNext();) {
					AdviceLocalClass pc=(AdviceLocalClass)it.next();			
					if (pc.isFirstDegree()) 
						pc.addParameters(addedContextArgsTypes, false);
				}
				for (Iterator it=adviceLocalClasses.values().iterator(); it.hasNext();) {
					AdviceLocalClass pc=(AdviceLocalClass)it.next();			
					if (!pc.isFirstDegree() && !pc.isAspect()) 
						pc.addParameters(addedContextArgsTypes, false);
				}
				for (Iterator it=adviceLocalClasses.values().iterator(); it.hasNext();) {
					AdviceLocalClass pc=(AdviceLocalClass)it.next();			
					if (pc.isAspect()) 
						pc.addParameters(addedContextArgsTypes, false);
				}
			}
			
			contextActualsResult.object=contextActuals;
			return argIndex;
		}
		
		private int[] determineContextParameterMappings(List context, List addedDynArgsTypes) {
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
						Integer contextArgID = (Integer) contextArgsByType[typeNum].get(currentIndex[typeNum]);
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
						parameters[i] = Restructure.JavaTypeInfo.getDefaultValue((Type) contextArguments.get(i));
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
				debug("found access interface in scene");
				closureInterface = Scene.v().getSootClass(interfaceName);
				//abstractProceedMethod=closureInterface.getMethodByName(dynamicProceedMethodName);
			} else {
				debug("generating access interface type " + interfaceName);

				closureInterface = new SootClass(interfaceName, Modifier.INTERFACE | Modifier.PUBLIC);

				closureInterface.setSuperclass(Scene.v().getSootClass("java.lang.Object"));

				SootMethod abstractProceedMethod =
					new SootMethod(instanceProceedMethodName, proceedMethodParameters, getAdviceReturnType(), Modifier.ABSTRACT | Modifier.PUBLIC);

				closureInterface.addMethod(abstractProceedMethod);
				//signature.setActiveBody(Jimple.v().newBody(signature));

				Scene.v().addClass(closureInterface);
				closureInterface.setApplicationClass();

				//GlobalAspectInfo.v().getGeneratedClasses().add(interfaceName);						 
			}
			return closureInterface;
		}

		private void generateProceedCalls(boolean bStaticProceedMethod,boolean bClosure, ProceedMethod proceedMethod) {

			//AdviceMethod adviceMethodInfo = state.getInterfaceInfo(interfaceName);

			String newStaticInvoke = null;
			boolean bContinue=true;
			if (!bClosure && bStaticProceedMethod) {
				if (!staticProceedTypes.contains(proceedMethod.shadowClass.getName())) {
					newStaticInvoke = proceedMethod.shadowClass.getName();
					staticProceedTypes.add(proceedMethod.shadowClass.getName());
				} else
					bContinue=false;
			} else {
				if (hasDynamicProceed)
					bContinue=false;
				else
					hasDynamicProceed = true;
			}

			if (!bContinue)
				return;
			
			Iterator it = adviceLocalClasses.values().iterator();
			while (it.hasNext()) {
				AdviceMethod.AdviceLocalClass pm = (AdviceMethod.AdviceLocalClass) it.next();
				pm.generateProceeds(proceedMethod, newStaticInvoke);
			}
		}

		

		

		private void modifyDirectInterfaceInvocations(List addedDynArgs, List addedDynArgTypes) {
			if (addedDynArgs.size()!=addedDynArgTypes.size())
				throw new InternalAroundError();
			{ // modify all existing direct interface invocations by adding the default parameters
				Iterator it = directInvocationStmts.iterator();
				while (it.hasNext()) {
					Stmt stmt = (Stmt) it.next();
					//addEmptyDynamicParameters(method, addedDynArgs, proceedMethodName);
					InvokeExpr invoke = (InvokeExpr) stmt.getInvokeExprBox().getValue();
					List newParams = new LinkedList(invoke.getArgs());
					List newTypes=new LinkedList(invoke.getMethodRef().parameterTypes());
					newTypes.addAll(addedDynArgTypes);
					newParams.addAll(addedDynArgs); /// should we do deep copy?	
					InvokeExpr newInvoke = Util.createNewInvokeExpr(invoke, newParams, newTypes);
					stmt.getInvokeExprBox().setValue(newInvoke);
				}
			}
		}
		private void modifyAdviceMethodInvocations(List addedDynArgs, List addedDynArgTypes) {
			if (addedDynArgs.size()!=addedDynArgTypes.size())
				throw new InternalAroundError();
			{ // modify all existing advice method invocations by adding the default parameters
				Iterator it = adviceMethodInvocationStmts.iterator();
				while (it.hasNext()) {
					Stmt stmt = (Stmt) it.next();
					//addEmptyDynamicParameters(method, addedDynArgs, proceedMethodName);
					InvokeExpr invoke = (InvokeExpr) stmt.getInvokeExprBox().getValue();
					List newParams = new LinkedList(invoke.getArgs());
					List newTypes=new LinkedList(invoke.getMethodRef().parameterTypes());
					newTypes.addAll(addedDynArgTypes);
					newParams.addAll(addedDynArgs); /// should we do deep copy?	
					InvokeExpr newInvoke = Util.createNewInvokeExpr(invoke, newParams, newTypes);
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
		final List /*type*/ proceedMethodParameterTypes=new LinkedList();
		final List /*SootMethod*/ closureProceedMethods=new LinkedList();
		SootClass getAspect() {
			return sootAdviceMethod.getDeclaringClass();
		}

		public final SootMethod sootAdviceMethod;
		//public final AdviceDecl adviceDecl;
		public final Body adviceBody;
		public final Chain adviceStatements;

		final public List originalAdviceFormalTypes;
		
		final public HashSet /*String*/ staticProceedTypes = new HashSet();
		public boolean hasDynamicProceed = false;
		public final boolean bAlwaysStaticProceedMethod = true; //false;//true;//false;//true; //false;

		public boolean bHasBeenWovenInto=false;
		
		public class AdviceLocalClass {
			
			public AdviceLocalClass getEnclosingFirstDegreeClass() {
				if (isAspect() || isFirstDegree())
					throw new InternalAroundError();
				
				AdviceLocalClass enclosing=getEnclosingClass();
				if (enclosing.isFirstDegree())
					return enclosing;
				else
					return enclosing.getEnclosingFirstDegreeClass();
			}
			public AdviceLocalClass getEnclosingClass() {
				if (isAspect())
					throw new InternalAroundError();
				
				return (AdviceLocalClass)adviceLocalClasses.get(enclosingSootClass);
			}
			private final SootClass enclosingSootClass;
			public SootClass getEnclosingSootClass() {
				if (isAspect())
					throw new InternalAroundError();
				return enclosingSootClass;
			}
			
			public void addDefaultParameters() {
				addParameters(null, true);
			}

			boolean firstDegree=false;
			public void generateProceeds(ProceedMethod proceedMethod, String newStaticInvoke) {
				for (Iterator it=this.adviceLocalMethods.iterator(); it.hasNext();) {
					AdviceLocalMethod pm=(AdviceLocalMethod)it.next();
					pm.generateProceeds(proceedMethod, newStaticInvoke);
				}
			}
			
			/*private void modifyInterfaceInvocations(List addedAdviceParameterLocals,
					List addedTypes) {
				for (Iterator it=this.adviceLocalMethods.iterator(); it.hasNext();) {
					AdviceLocalMethod pm=(AdviceLocalMethod)it.next();
					pm.modifyInterfaceInvocations(addedAdviceParameterLocals, addedTypes);
				}
			}*/
			List addedFields=new LinkedList();
			
			private void addParameters(List addedDynArgsTypes, boolean bDefault) {
				
				if (bDefault && addedDynArgsTypes!=null)
					throw new InternalAroundError();
				
				if (bDefault) {
					addedDynArgsTypes=new LinkedList();
					addedDynArgsTypes.add(interfaceInfo.closureInterface.getType());
					//, "closureInterface"
					addedDynArgsTypes.add(IntType.v());
					addedDynArgsTypes.add(IntType.v());
					addedDynArgsTypes.add(IntType.v());
				}
				if (isAspect()){ 
					// Add the new parameters to the advice method 
					// and keep track of the newly created locals corresponding to the parameters.
					//validateMethod(adviceMethod);
					if (adviceLocalMethods.size()!=1)
						throw new InternalAroundError();
					
					AdviceLocalMethod am=(AdviceLocalMethod)adviceLocalMethods.get(0);
					
					List addedAdviceParameterLocals = new LinkedList();
					debug("adding parameters to advice method " + am.sootProceedCallMethod);
					for (Iterator it = addedDynArgsTypes.iterator();
						it.hasNext();) {
						Type type = (Type) it.next();
						debug(" " + type);
						Local l;
						
						 l = Restructure.addParameterToMethod(am.sootProceedCallMethod, type, "contextArgFormal");
						
						addedAdviceParameterLocals.add(l);						
					}
					
					am.modifyNestedInits(addedAdviceParameterLocals);
					
					if (bDefault) {
						am.setDefaultParameters(addedAdviceParameterLocals);
					}
					if (!bDefault)
						am.modifyInterfaceInvocations(addedAdviceParameterLocals, addedDynArgsTypes);
					
					for (Iterator it=adviceLocalClasses.values().iterator(); it.hasNext();) {
						AdviceLocalClass pl=(AdviceLocalClass)it.next();
						pl.addedFields.clear();
					}
					
				} else  {
					addedFields=new LinkedList();
					if (isFirstDegree()) {
						for (Iterator it = addedDynArgsTypes.iterator();
							it.hasNext();) {
							Type type = (Type) it.next();
						
							SootField f=new SootField("contextField" + state.getUniqueID(), 
										type, Modifier.PUBLIC);
							sootClass.addField(f);
							addedFields.add(f);
						}
					} else {
						addedFields=getEnclosingFirstDegreeClass().addedFields;
					}
					// add locals referencing the fields
					for (Iterator it=adviceLocalMethods.iterator(); it.hasNext();) {
						AdviceLocalMethod pm=(AdviceLocalMethod)it.next();
						
						List addedAdviceParameterLocals = new LinkedList();
						
						Chain statements=pm.methodBody.getUnits().getNonPatchingChain();
						Stmt insertion=pm.nopAfterEnclosingLocal;
						LocalGeneratorEx lg=new LocalGeneratorEx(pm.methodBody);
						
						for (Iterator it0=addedFields.iterator(); it0.hasNext();) {
							SootField f=(SootField)it0.next();
							Local l=lg.generateLocal(f.getType(), "contextFieldLocal");
							Stmt sf=
								Jimple.v().newAssignStmt(l, 
										Jimple.v().newInstanceFieldRef(
												pm.contextArgfieldBaseLocal,
												f.makeRef()));
							statements.insertBefore(sf, insertion);
							addedAdviceParameterLocals.add(l);
						}
						pm.modifyNestedInits(addedAdviceParameterLocals);
						if (bDefault) {
							pm.setDefaultParameters(addedAdviceParameterLocals);
						}
						if (!bDefault)
							pm.modifyInterfaceInvocations(addedAdviceParameterLocals, addedDynArgsTypes);	
					}
				}
				//return addedAdviceParameterLocals;
			}
			
			/**
			 * @param pm
			 */
			
			public final SootClass sootClass;
			//public final SootClass aspectClass;
			public AdviceLocalClass(SootClass sootClass) {
				this.sootClass=sootClass;
				//this.aspectClass=aspectClass;

				if (isAspect())
					enclosingSootClass=null;
				else	
					enclosingSootClass=((RefType)sootClass.getFieldByName("this$0").getType()).getSootClass();
				
				this.firstDegree=
					!isAspect() && getEnclosingSootClass().equals(getAspect());
				
				debug("XXXXXXXXXXXXXXXX" + sootClass + " isAspect: " + isAspect() + " isFirst: " + isFirstDegree());
			}
			public boolean isAspect() {
				return sootClass.equals(getAspect());
			}
			boolean isFirstDegree() {
				return firstDegree;
			}
			
			public void addAdviceLocalMethod(SootMethod m) {
				this.adviceLocalMethods.add(
						new AdviceLocalMethod(AdviceMethod.this, m));
			}
			private final List adviceLocalMethods=new LinkedList();
			

		
			public class AdviceLocalMethod {
				private void modifyNestedInits(List addedAdviceParameterLocals) {
					AdviceLocalMethod pm=this;
					for (Iterator it0=pm.nestedInitCalls.iterator(); it0.hasNext();) {
						AdviceLocalMethod.NestedInitCall nc=
							(AdviceLocalMethod.NestedInitCall)it0.next();
						
						if (addedAdviceParameterLocals.size()!=nc.adviceLocalClass.addedFields.size())
							throw new InternalAroundError();
						
						Iterator it=addedAdviceParameterLocals.iterator();
						Iterator it1=nc.adviceLocalClass.addedFields.iterator();
						while (it.hasNext()) {
							Local l=(Local)it.next();
							SootField f=(SootField)it1.next();
							Stmt ns=
								Jimple.v().newAssignStmt(
										Jimple.v().newInstanceFieldRef(
												nc.baseLocal,
												f.makeRef()), l);
							pm.methodBody.getUnits().getNonPatchingChain().insertAfter(ns, nc.statement);
						}
					}
				}
				private void setDefaultParameters(List addedAdviceParameterLocals) {
					AdviceLocalMethod pm=this;
					int i=0;
					for (Iterator it=addedAdviceParameterLocals.iterator();it.hasNext();i++) {
						Local l=(Local)it.next();
					switch(i) {
						case 0: pm.interfaceLocal=l;
							 	l.setName("closureInterface" + state.getUniqueID()); break;
						case 1: pm.idLocal=l; 
								l.setName("shadowID" + state.getUniqueID());break;
						case 2: pm.staticDispatchLocal=l; 
								l.setName("staticClassID" + state.getUniqueID()); break;
						case 3: pm.bindMaskLocal=l;
								l.setName("bindMask" + state.getUniqueID());break;
						default: throw new InternalAroundError();
						}
					}
					pm.implicitProceedParameters.add(pm.idLocal);
					pm.implicitProceedParameters.add(pm.bindMaskLocal);
				}
				public class NestedInitCall {
					public NestedInitCall(final Stmt statement, 
							final AdviceLocalClass proceedClass,
							final Local baseLocal) {
						super();
						this.statement = statement;
						this.adviceLocalClass = proceedClass;
						this.baseLocal=baseLocal;
					}
					public final Stmt statement;
					public final AdviceLocalClass adviceLocalClass;
					public final Local baseLocal;
				}
				private Local interfaceLocal;
				private Local staticDispatchLocal;
				private Local idLocal;
				private Local bindMaskLocal;
				
				private Local contextArgfieldBaseLocal;
				
				private final List implicitProceedParameters=new LinkedList();
				
				private void modifyInterfaceInvocations(List addedAdviceParameterLocals,
						List addedAdviceParameterTypes) {
					if (addedAdviceParameterLocals.size()!=addedAdviceParameterTypes.size())
						throw new InternalAroundError();
					// Modify the interface invocations. These must all be in the advice method.
					// This constraint is violated by adviceexecution() pointcuts.
					Iterator it = interfaceInvocationStmts.iterator();
					Chain statements=methodBody.getUnits().getNonPatchingChain();
					while (it.hasNext()) {
						Stmt stmt = (Stmt) it.next();
						if (!statements.contains(stmt))
							throw new InternalAroundError();
					
						InvokeExpr intfInvoke = stmt.getInvokeExpr();
						
						/*if (intfInvoke.getArgCount()!=
							intfInvoke.getMethodRef().parameterTypes().size())
							throw new InternalAroundError(
									" "  + intfInvoke);*/
						
						List params = new LinkedList(intfInvoke.getArgs());
						params.addAll(addedAdviceParameterLocals);
						List types=interfaceInfo.abstractProceedMethod.getParameterTypes();// new LinkedList(intfInvoke.getMethodRef().parameterTypes()); 
						//types.addAll(addedAdviceParameterTypes);
						if (params.size()!=types.size())
							throw new InternalAroundError();
						
						InvokeExpr newInvoke = Util.createNewInvokeExpr(intfInvoke, params, types);
						//debug("newInvoke: " + newInvoke);
						stmt.getInvokeExprBox().setValue(newInvoke);
						//debug("newInvoke2" + stmt.getInvokeExpr());
						if (newInvoke.getArgCount()!=newInvoke.getMethodRef().parameterTypes().size())
							throw new InternalAroundError(
									"Signature: " + newInvoke.getMethodRef().getSignature() + 
									" Args: " + newInvoke.toString());
						
						/*try {
							newInvoke.getMethod();
						} catch (Exception e) {
							throw new InternalAroundError(
								"Signature: " + newInvoke.getMethodRef().getSignature() + 
								" Interface: " + interfaceInfo.abstractProceedMethod.getSignature()	+
								" message: " + e.getMessage()
							);
						}*/
					}
					implicitProceedParameters.addAll(addedAdviceParameterLocals);				
				}
				boolean isAdviceMethod() {
					return sootProceedCallMethod.equals(sootAdviceMethod);
				}
				
				private final Set nestedInitCalls=new HashSet();
				private final NopStmt nopAfterEnclosingLocal;
				public final int originalSize;
				public final int internalLocalCount;
				
				public AdviceLocalMethod(AdviceMethod adviceMethod, SootMethod method) {
					//this.adviceMethod=adviceMethod;
					this.sootProceedCallMethod=method;
					this.methodBody=method.getActiveBody();			
				
					this.originalSize=methodBody.getUnits().size();
					this.internalLocalCount =
						methodBody.getLocalCount() - 
						method.getParameterCount();
					if (internalLocalCount<0)
						throw new InternalAroundError();
						
					debug("YYYYYYYYYYYYYYYYYYY creating ProceedCallMethod " + method);
					
					this.nopAfterEnclosingLocal=Jimple.v().newNopStmt();
					
					Chain methodStatements=methodBody.getUnits().getNonPatchingChain();			
					
					for (Iterator it = methodStatements.snapshotIterator();
						it.hasNext();) {
						Stmt s = (Stmt) it.next();
						InvokeExpr invokeEx;
						try {
							invokeEx = s.getInvokeExpr();
						} catch (Exception ex) {
							invokeEx = null;
						}
		
						if (invokeEx != null) {
							if (invokeEx.getMethodRef().name().startsWith("proceed$")) { 							
								ProceedInvocation invocation = 
									new ProceedInvocation(
											invokeEx.getArgs(), s);
								proceedInvocations.add(invocation);							
							}
							// find <init> calls to local/anonymous classes
							if (invokeEx instanceof SpecialInvokeExpr) {
								 
								SpecialInvokeExpr si=(SpecialInvokeExpr) invokeEx;
								Local baseLocal=(Local)si.getBase();
								SootClass baseClass=((RefType)baseLocal.getType()).getSootClass();
								if (!baseClass.equals(getAspect())) {									
									if (si.getMethodRef().name().equals("<init>") && 
										!this.methodBody.getThisLocal().equals(baseLocal) ) {
										if (adviceMethod.adviceLocalClasses.containsKey(baseClass)) {
											debug("WWWWWWWWWWWW base class: " + baseClass);
											AroundWeaver.AdviceMethod.AdviceLocalClass pl=
												(AroundWeaver.AdviceMethod.AdviceLocalClass)
													adviceMethod.adviceLocalClasses.get(baseClass);
											
											if (pl.isFirstDegree()) {											
												nestedInitCalls.add(
														new NestedInitCall(s, pl, 
																(Local)si.getBase()));
											}
										//	nestedFirstDegreeClasses.add(baseClass);
										}
									}
								}
							}
						}
					}
					Stmt insert=Restructure.findFirstRealStmtOrNop(method, methodStatements);
					
					methodStatements.insertBefore(nopAfterEnclosingLocal, insert);
					insert=nopAfterEnclosingLocal;
					if (isAdviceMethod()) {
						contextArgfieldBaseLocal=null;						
					} else {
						if (isFirstDegree()) {
							contextArgfieldBaseLocal=methodBody.getThisLocal();
						} else {
							LocalGeneratorEx lg=new LocalGeneratorEx(methodBody);
							SootClass cl=sootClass;
							
							Local lBase=methodBody.getThisLocal();
							
							
							while (true)  {
								debug(" Class: " + cl);
								SootField f=cl.getFieldByName("this$0");
								
								if (!adviceLocalClasses.containsKey(((RefType)f.getType()).getSootClass()))
									throw new InternalAroundError(" " + ((RefType)f.getType()).getSootClass());		
								
								AdviceLocalClass pl=(AdviceLocalClass)adviceLocalClasses.get(((RefType)f.getType()).getSootClass());
								
								Local l=lg.generateLocal(f.getType(), "enclosingLocal");
								AssignStmt as=Jimple.v().newAssignStmt(
										l, Jimple.v().newInstanceFieldRef(
												lBase, f.makeRef()));
								methodStatements.insertBefore(as , insert);
								if (pl.isFirstDegree()) {
									contextArgfieldBaseLocal=l;
									break;
								} else {
									lBase=l;
									cl=pl.sootClass;
								}
							}							
							if (contextArgfieldBaseLocal==null)
								throw new InternalAroundError();
						}
					} 
				}
				public void generateProceeds(ProceedMethod proceedMethod, String newStaticInvoke) {
					for (Iterator it=proceedInvocations.iterator(); it.hasNext();) {
						ProceedInvocation inv=(ProceedInvocation)it.next();
						inv.generateProceed(proceedMethod, newStaticInvoke);
					}				
				}
				public final List proceedInvocations=new LinkedList();
				
				//private final AdviceMethod adviceMethod;
				private final SootMethod sootProceedCallMethod;
				private final Body methodBody;
				//private final Chain statements;
				private final Set interfaceInvocationStmts = new HashSet();
		
				
			
				public class ProceedInvocation {
					public ProceedInvocation(
								List originalActuals, Stmt originalStmt) {					
						
						this.originalActuals.addAll(originalActuals);
		
						
						
						this.begin = Jimple.v().newNopStmt();
						this.end = Jimple.v().newNopStmt();
						if (originalStmt instanceof AssignStmt) {
							lhs = (Local) (((AssignStmt) originalStmt).getLeftOp());
						}
						Chain statements=methodBody.getUnits().getNonPatchingChain();
						statements.insertBefore(begin, originalStmt);
						statements.insertAfter(end, originalStmt);
						originalStmt.redirectJumpsToThisTo(begin);
						debug("Removing original statement: " + originalStmt);
						statements.remove(originalStmt);
					}
								
					private Local lhs;
					final public NopStmt begin;
					final public NopStmt end;
		
					//List lookupValues=new LinkedList();
					List defaultTargetStmts;
					//Stmt lookupStmt;
					final List staticInvokes = new LinkedList();
					final List staticLookupValues = new LinkedList();
		
					Stmt dynamicInvoke;
		
					final List originalActuals = new LinkedList();
				
					
					
					public void generateProceed(ProceedMethod proceedMethod, String newStaticInvokeClassName) {
						debug("1YYYYYYYYYYYYYY generateProceed()");
						//debug(Util.printMethod(sootProceedCallMethod));
						Util.removeStatements(methodBody, begin, end, null);
						debug("YYYYYYYYYYYYYY generateProceed()" + AdviceMethod.this.sootAdviceMethod);
						List parameters = new LinkedList();
						parameters.addAll(this.originalActuals);
						debug(" param count: " + parameters.size());
						parameters.addAll(implicitProceedParameters);
						debug(" param count: " + parameters.size());
						if (this.dynamicInvoke == null && AdviceMethod.this.hasDynamicProceed) {
							/*if (parameters.size()!=
								interfaceInfo.abstractProceedMethod.getParameterCount())
								throw new InternalAroundError(
										"Signature " + interfaceInfo.abstractProceedMethod.getSignature() +
										" Parameters " + parameters.toString());
								*/		
							InvokeExpr newInvokeExpr = Jimple.v().newInterfaceInvokeExpr(
										interfaceLocal, 
										AdviceMethod.this.interfaceInfo.abstractProceedMethod.makeRef(), parameters);
							
							/*if (newInvokeExpr.getArgCount()!=
								interfaceInfo.abstractProceedMethod.getParameterCount())
								throw new InternalAroundError(
										"Signature " + interfaceInfo.abstractProceedMethod.getSignature() +
										" Parameters " + parameters.toString());
							
							while (newInvokeExpr.getMethodRef().parameterTypes().size()>parameters.size())
								newInvokeExpr.getMethodRef().parameterTypes().remove(
										newInvokeExpr.getMethodRef().parameterTypes().get(
												newInvokeExpr.getMethodRef().parameterTypes().size()-1
										)
								);
							*/
							
							Stmt s;
							if (this.lhs == null) {
								s = Jimple.v().newInvokeStmt(newInvokeExpr);
							} else {
								s = Jimple.v().newAssignStmt(this.lhs, newInvokeExpr);
							}
							this.dynamicInvoke = s;
							interfaceInvocationStmts.add(s);
						}
						
						if (newStaticInvokeClassName != null) {
							SootClass cl = Scene.v().getSootClass(newStaticInvokeClassName);

							this.staticLookupValues.add(IntConstant.v(getStaticDispatchTypeID(cl.getType())));
						
							InvokeExpr newInvokeExpr = Jimple.v().newStaticInvokeExpr(
									proceedMethod.sootProceedMethod.makeRef(), parameters);
							
							
							Stmt s;
							if (this.lhs == null) {
								s = Jimple.v().newInvokeStmt(newInvokeExpr);
							} else {
								s = Jimple.v().newAssignStmt(this.lhs, newInvokeExpr);
							}
							this.staticInvokes.add(s);
							interfaceInvocationStmts.add(s);
						}
						if (this.defaultTargetStmts == null) {
							//				generate exception code (default target)
							this.defaultTargetStmts = new LinkedList();
							LocalGeneratorEx lg = new LocalGeneratorEx(methodBody);
							SootClass exception = Scene.v().getSootClass("java.lang.RuntimeException");
							Local ex = lg.generateLocal(exception.getType(), "exception");
							Stmt newExceptStmt = Jimple.v().newAssignStmt(ex, Jimple.v().newNewExpr(exception.getType()));
							Stmt initEx = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(ex, exception.getMethod("<init>", new ArrayList()).makeRef()));
							Stmt throwStmt = Jimple.v().newThrowStmt(ex);
							this.defaultTargetStmts.add(newExceptStmt);
							this.defaultTargetStmts.add(initEx);
							this.defaultTargetStmts.add(throwStmt);
						}
						
						Chain statements=methodBody.getUnits().getNonPatchingChain();
						if (AdviceMethod.this.staticProceedTypes.isEmpty()) {
							statements.insertAfter(this.dynamicInvoke, this.begin);
						} else if (AdviceMethod.this.hasDynamicProceed == false && AdviceMethod.this.staticProceedTypes.size() == 1) {
							statements.insertAfter(this.staticInvokes.get(0), this.begin);
						} else {
							List targets = new LinkedList();
							List lookupValues = new LinkedList();
							if (this.dynamicInvoke != null) {
								targets.add(this.dynamicInvoke);
								lookupValues.add(IntConstant.v(0));
							}
							targets.addAll(this.staticInvokes);
							lookupValues.addAll(this.staticLookupValues);
						
							Local key = staticDispatchLocal; ///
							Stmt lookupStmt = Util.newSwitchStmt(//Jimple.v().newLookupSwitchStmt(									
									key, lookupValues, targets, (Unit) this.defaultTargetStmts.get(0));
						
							statements.insertBefore(lookupStmt, this.end);
							if (this.dynamicInvoke != null) {
								statements.insertBefore(this.dynamicInvoke, this.end);
								statements.insertBefore(Jimple.v().newGotoStmt(this.end), this.end);
							}
						
							Iterator it2 = this.staticInvokes.iterator();
							while (it2.hasNext()) {
								Stmt stmt = (Stmt) it2.next();
								statements.insertBefore(stmt, this.end);
								statements.insertBefore(Jimple.v().newGotoStmt(this.end), this.end);
							}
							it2 = this.defaultTargetStmts.iterator();
							while (it2.hasNext()) {
								Stmt stmt = (Stmt) it2.next();
								statements.insertBefore(stmt, this.end);
							}
							// just in case: // TODO: what for?
							//statements.insertBefore(Jimple.v().newGotoStmt(this.end), this.end);
						}
					}
				
				}
			}
		}
		
		final public Set adviceMethodInvocationStmts = new HashSet();
		final public Set directInvocationStmts = new HashSet();
		final List /*Type*/ contextArguments = new LinkedList();

		final List[] contextArgsByType = new List[Restructure.JavaTypeInfo.typeCount];
		
		public final Map /*SootClass, AdviceLocalClass */ adviceLocalClasses=new HashMap();
				
		public static List getOriginalAdviceFormals(AdviceDecl adviceDecl) {
			
			List result=new LinkedList();
			
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
				return (ProceedMethod) proceedMethodImplementationsStatic.get(className);
			} else {
				return (ProceedMethod) proceedMethodImplementations.get(className);
			}
		}
		public void setClosureProceedMethod(ProceedMethod m) {
			proceedMethodImplementationsClosure.add(m);
		}
		public void setProceedMethod(String className, boolean bStatic, ProceedMethod proceedMethod) {
			if (bStatic) {
				proceedMethodImplementationsStatic.put(className, proceedMethod);
			} else {
				proceedMethodImplementations.put(className, proceedMethod);
			}
		}
		final private HashMap /*String, ProceedMethod*/ proceedMethodImplementations = new HashMap();
		final private HashMap /*String, ProceedMethod*/ proceedMethodImplementationsStatic = new HashMap();
		final private Set /* ProceedMethod */ proceedMethodImplementationsClosure=new HashSet();
		public int getUniqueShadowID() {
			return currentUniqueShadowID++;
		}
		int currentUniqueShadowID;
	}
	private static class InternalAroundError extends InternalCompilerError {
		InternalAroundError(String message) {
			super("ARD around weaver internal error: " + message);
		}
		InternalAroundError(String message, Throwable cause) {
			super("ARD around weaver internal error: " + message, cause);
		}
		InternalAroundError() {
			super("ARD around weaver internal error");
		}	
	}
	
	public static class AdviceMethodInlineInfo {
		
		public int proceedInvocations=0;
		public boolean nestedClasses=false;
		public int originalSize=0;
		public int internalLocalCount=0;
		public int applications=0;
	}
	public static class ShadowInlineInfo {
		public ShadowInlineInfo(int size, int internalLocals) {
			this.size=size;
			this.internalLocals=internalLocals;
		}
		public final int size;
		public final int internalLocals;
	}
	public static class ProceedMethodInlineInfo {
		public int shadowIDParamIndex=-1;
		public Map shadowInformation;
	}
	public static class State {
	   Map buildAroundAdviceLocalMethodMap() {
		   	Map result=new HashMap();
		   	 List adviceDecls=GlobalAspectInfo.v().getAdviceDecls();
		   	 for (Iterator it=adviceDecls.iterator(); it.hasNext();) {
		   	 	AbstractAdviceDecl absdecl=(AbstractAdviceDecl)it.next();
		   	 	if (!(absdecl instanceof AdviceDecl))
		   	 		continue;
		   	 	
		   	 	AdviceDecl decl=(AdviceDecl)absdecl;
		   	    SootMethod adviceMethod=decl.getImpl().getSootMethod();
		   	    if (AroundWeaver.Util.isAroundAdviceMethod(adviceMethod)) {
		   	    	List localMethods=decl.getLocalSootMethods();
		   	    	for (Iterator itM=localMethods.iterator(); itM.hasNext();) {
		   	    		SootMethod localMethod=(SootMethod)itM.next();
		   	    		result.put(localMethod, adviceMethod);
		   	    	}
		   	    }   	 	
		   	 }
		   	 return result;
		   }
	   private Map aroundAdviceLocalMethods=null;
		public SootMethod getEnclosingAroundAdviceMethod(SootMethod m) {
			if (aroundAdviceLocalMethods==null) {
				aroundAdviceLocalMethods=buildAroundAdviceLocalMethodMap();
			}
			return (SootMethod)aroundAdviceLocalMethods.get(m);
		}
		
		private Map proceedMethods=new HashMap();
		
		private Map proceedMethodInlineInfos=new HashMap();
		public ProceedMethodInlineInfo getProceedMethodInlineInfo(SootMethod method) {
			ProceedMethodInlineInfo result=
				(ProceedMethodInlineInfo)
					proceedMethodInlineInfos.get(method);
			if (result!=null)
				return result;
			
			proceedMethodInlineInfos.put(method, result);
			
			
			result=new 
				ProceedMethodInlineInfo();
			
			AdviceMethod.ProceedMethod pm=
				(AdviceMethod.ProceedMethod)proceedMethods.get(method);
			
			
			result.shadowIDParamIndex=pm.shadowIDParamIndex;
			result.shadowInformation=pm.shadowInformation;
			
			return result;
		}
		
		private Map adviceMethodInlineInfos=new HashMap();
		public AdviceMethodInlineInfo getAdviceMethodInlineInfo(SootMethod method) {
			
			AdviceMethodInlineInfo result=
				(AdviceMethodInlineInfo)
					adviceMethodInlineInfos.get(method);
			if (result!=null)
				return result;
			
			adviceMethodInlineInfos.put(method, result);
			
			
			result=new 
				AdviceMethodInlineInfo();
			

			AdviceMethod adviceMethod=
				getAdviceMethod(method);
			
			if (adviceMethod==null) {
				throw new RuntimeException("Could not find information on " + method.getName());
			}
			
			if (adviceMethod.adviceLocalClasses.size()>1) {
				result.nestedClasses=true;				
			} else {
				AdviceMethod.AdviceLocalClass cl=
					(AdviceMethod.AdviceLocalClass)adviceMethod.adviceLocalClasses.values().iterator().next();
				AdviceMethod.AdviceLocalClass.AdviceLocalMethod m=
					(AdviceMethod.AdviceLocalClass.AdviceLocalMethod)cl.adviceLocalMethods.get(0);
				result.proceedInvocations=m.proceedInvocations.size();
				
				result.originalSize=m.originalSize;
				result.internalLocalCount=m.internalLocalCount;
				
				for (Iterator it=adviceMethod.getAllProceedMethods().iterator(); it.hasNext();) {
					AdviceMethod.ProceedMethod pm=
						(AdviceMethod.ProceedMethod)it.next();
					result.applications+=pm.adviceApplications.size();
				}
			}
			return result;
		}
		
		
		public Set shadowMethods=new HashSet();
		
		private void validate() {
			Iterator it=adviceMethods.values().iterator();
			while (it.hasNext()) {
				AdviceMethod method=(AdviceMethod) it.next();
				method.validate();
			}
		}
		public int getUniqueID() {
			return currentUniqueID++;
		}
		int currentUniqueID;

		//final private HashMap /* AdviceApplication,  */ adviceApplications = new HashMap();

		
		final private HashMap /* SootMethod, AdviceMethod */ adviceMethods = new HashMap();
		void setAdviceMethod(SootMethod adviceMethod, AdviceMethod m) {
			adviceMethods.put(adviceMethod, m);
		}
		public AdviceMethod getAdviceMethod(SootMethod adviceMethod) {
			if (!adviceMethods.containsKey(adviceMethod)) {
				return null;
			}
			return (AdviceMethod) adviceMethods.get(adviceMethod);
		}
	}
	public static State state = new State();


	
	
	


	private static void insertCast(Body body, Stmt stmt, ValueBox source, Type targetType) {
		Chain units = body.getUnits().getNonPatchingChain();
		if (!source.getValue().getType().equals(targetType)) {
			LocalGeneratorEx localgen = new LocalGeneratorEx(body);
			Local castLocal = localgen.generateLocal(source.getValue().getType(), "castTmp");
			//debug("cast: source has type " + source.getValue().getType().toString());
			//debug("cast: target has type " + targetType.toString());
			AssignStmt tmpStmt = Jimple.v().newAssignStmt(castLocal, source.getValue());
			CastExpr castExpr = Jimple.v().newCastExpr(castLocal, targetType);
			//	Jimple.v().newCastExpr()
			units.insertBefore(tmpStmt, stmt);
			if (stmt instanceof AssignStmt) {
				source.setValue(castExpr);
			} else {
				Local tmpLocal = localgen.generateLocal(targetType, "castTarget");
				AssignStmt tmpStmt2 = Jimple.v().newAssignStmt(tmpLocal, castExpr);
				units.insertBefore(tmpStmt2, stmt);
				source.setValue(tmpLocal);
			}
		}
	}

	private static void updateSavedReferencesToStatements(HashMap bindings) {
		Collection values = state.adviceMethods.values();
		Iterator it = values.iterator();
		// all advice methods
		while (it.hasNext()) {
			AdviceMethod adviceMethodInfo = (AdviceMethod) it.next();
			Set keys2 = bindings.keySet();
			Iterator it2 = keys2.iterator();
			// all bindings
			while (it2.hasNext()) {
				Object old = it2.next();
				if (!(old instanceof Value) && !(old instanceof Stmt))
					continue;
				if (adviceMethodInfo.adviceMethodInvocationStmts.contains(old)) {
					adviceMethodInfo.adviceMethodInvocationStmts.remove(old);
					adviceMethodInfo.adviceMethodInvocationStmts.add(bindings.get(old));
					// replace with new
				}
				// this is only necessary if proceed calls are ever part of a shadow,
				// for example if the advice body were to be matched by an adviceexecution pointcut. 
				// TODO: does this kind of thing ever happen?
				// Doesn't matter. Once an advice method has been woven into,
				// the proceeds aren't changed anymore anyways.
				// So we might as well keep all these references updated.
				// (or delete them otherwise).
				for (Iterator it3=adviceMethodInfo.adviceLocalClasses.values().iterator(); it3.hasNext();) {
					AdviceMethod.AdviceLocalClass pl=
						(AdviceMethod.AdviceLocalClass)it3.next();
					
					for (Iterator it0=pl.adviceLocalMethods.iterator();it0.hasNext();) {
						AdviceMethod.AdviceLocalClass.AdviceLocalMethod pm=
							(AdviceMethod.AdviceLocalClass.AdviceLocalMethod)it0.next();
						
						if (pm.interfaceInvocationStmts.contains(old)) {
							pm.interfaceInvocationStmts.remove(old);
							pm.interfaceInvocationStmts.add(bindings.get(old));
							// replace with new
						}
					}
				}
				if (adviceMethodInfo.directInvocationStmts.contains(old)) {
					adviceMethodInfo.directInvocationStmts.remove(old);
					adviceMethodInfo.directInvocationStmts.add(bindings.get(old));
				}
			}
		}
	}

	private static List getDefaultValues(List types) {
		List result = new LinkedList();
		{
			Iterator it = types.iterator();
			while (it.hasNext()) {
				Type type = (Type) it.next();
				result.add(Restructure.JavaTypeInfo.getDefaultValue(type));
			}
		}
		return result;
	}
	
}
