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
import soot.jimple.GotoStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.tagkit.Tag;
import soot.util.Chain;
import abc.main.Debug;
import abc.main.Main;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import abc.soot.util.RedirectedExceptionSpecTag;
import abc.soot.util.DisableExceptionCheckTag;
import abc.weaving.aspectinfo.AbcClass;
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
import abc.weaving.matching.StmtAdviceApplication;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.Residue;

/** Handle around weaving.
 * @author Sascha Kuzins 
 * @date May 6, 2004
 */

public class AroundWeaver {

	
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
			return method.getName().startsWith("around$"); // TODO: something more solid
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

		private static List getParameterLocals(Body body) {
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
			while (it2.hasNext())
				units.remove(it2.next());
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
		private static InvokeExpr createNewInvokeExpr(InvokeExpr old, List newArgs, List newTypes) {
			if (newArgs.size()!=newTypes.size())
				throw new InternalAroundError();
			{ // sanity check:
				Iterator it0=newTypes.iterator();
				for (Iterator it=newArgs.iterator(); it.hasNext();) {
					Value val=(Value)it.next();
					Type type=(Type)it0.next();
				}
			}
			
			soot.SootMethodRef ref=old.getMethodRef();
			debug("createNewInvokeExpr: old ref: " + ref + " "  + ref.getSignature());
			//soot.SootMethodRef ref2=new soot.SootMethodRef();
			ref.parameterTypes().clear();
			ref.parameterTypes().addAll(newTypes);
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

	public static void doWeave(SootClass joinpointClass, SootMethod joinpointMethod, LocalGeneratorEx localgen, AdviceApplication adviceAppl) {
		debug("Weaving advice application: " + adviceAppl);
		if (abc.main.Debug.v().aroundWeaver) {
			// uncomment to skip around weaving (for debugging)
		//	if (joinpointClass!=null)	return;
			//throw new RuntimeException();
		}
		
		
		try {

			AdviceDecl adviceDecl = (AdviceDecl) adviceAppl.advice;
			
			AdviceSpec adviceSpec = adviceDecl.getAdviceSpec();
			AroundAdvice aroundSpec = (AroundAdvice) adviceSpec;
			SootClass theAspect = adviceDecl.getAspect().getInstanceClass().getSootClass();
			SootMethod method = adviceDecl.getImpl().getSootMethod();
			
			AdviceMethod adviceMethodInfo = state.getAdviceMethod(method);
			List sootProceeds=new LinkedList();
			sootProceeds.addAll(adviceDecl.getLocalSootMethods());
			if (!sootProceeds.contains(method))
				sootProceeds.add(method);
			
			if (adviceMethodInfo == null) {
				adviceMethodInfo = new AdviceMethod(method, 
						AdviceMethod.getOriginalAdviceFormals(adviceDecl),
							sootProceeds);
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

			adviceMethodInfo.doWeave(adviceAppl, joinpointMethod);
			
		} catch (InternalAroundError e) {
			throw e;
		} catch (Throwable e) {
			System.err.println(" " + e.getClass().getName() + " " + e.getCause());
			
			StackTraceElement[] els=e.getStackTrace();
			for (int i=0; i<els.length; i++) {
				System.err.println(e.getStackTrace()[i].toString());
			}			
			throw new InternalAroundError("", e);
		}
		
		if (abc.main.Debug.v().aroundWeaver) {
				//state.validate(); 
				//validate();
				//abc.soot.util.Validate.validate(Scene.v().getSootClass("org.aspectj.runtime.reflect.Factory"));
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
			for (int i = 0; i < dynamicArgsByType.length; i++) {
				dynamicArgsByType[i] = new LinkedList();
			}

			
			String aspectName = getAspect().getName();
			String mangledAspectName = Util.mangleTypeName(aspectName);

			adviceMethodIdentifierString = mangledAspectName + "$" + method.getName();

			interfaceName = "Abc$access$" + adviceMethodIdentifierString;

			dynamicAccessMethodName = "abc$proceed$" + adviceMethodIdentifierString;
			;

			accessMethodParameterTypes.add(IntType.v()); // the shadow id
			accessMethodParameterTypes.add(IntType.v()); // the skip mask

			{
				List allAccessMethodParameters = new LinkedList();
				allAccessMethodParameters.addAll(originalAdviceFormalTypes);
				allAccessMethodParameters.addAll(accessMethodParameterTypes);

				interfaceInfo = new InterfaceInfo();

				interfaceInfo.accessInterface = createAccessInterface(allAccessMethodParameters);
				interfaceInfo.abstractAccessMethod = interfaceInfo.accessInterface.getMethodByName(dynamicAccessMethodName);
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
				if (!proceedClasses.containsKey(m.getDeclaringClass())) {
					ProceedLocalClass newClass=
						new ProceedLocalClass(m.getDeclaringClass());
					proceedClasses.put(
						m.getDeclaringClass(), 
							newClass);
					
					while (newClass!=null && !newClass.isAspect() && !newClass.isFirstDegree()) {
						SootClass enclosing=newClass.getEnclosingSootClass();
						
						if (!proceedClasses.containsKey(enclosing)) {
							newClass=
								new ProceedLocalClass(enclosing);
							proceedClasses.put(
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
						ProceedLocalClass pl=(ProceedLocalClass)proceedClasses.get(m.getDeclaringClass());
						pl.addProceedMethod(m);
					}
				}
				// add advice method itself last.
				ProceedLocalClass pl=(ProceedLocalClass)proceedClasses.get(method.getDeclaringClass());
				pl.addProceedMethod(method);
			}
			{
				for (Iterator it=proceedClasses.values().iterator();it.hasNext();) {
					ProceedLocalClass pc=(ProceedLocalClass)it.next();
					if (pc.isFirstDegree())
						pc.addDefaultParameters();
				}
				for (Iterator it=proceedClasses.values().iterator();it.hasNext();) {
					ProceedLocalClass pc=(ProceedLocalClass)it.next();
					if (!pc.isAspect() && !pc.isFirstDegree())
						pc.addDefaultParameters();
				}
				for (Iterator it=proceedClasses.values().iterator();it.hasNext();) {
					ProceedLocalClass pc=(ProceedLocalClass)it.next();
					if (pc.isAspect())
						pc.addDefaultParameters();
				}
			}
			if (Debug.v().aroundWeaver){
				debug("QQQQQQQQQQQQQQ Classes for advice-method" + sootAdviceMethod + "\n");
				for (Iterator it=proceedClasses.values().iterator();it.hasNext();) {
					ProceedLocalClass pc=(ProceedLocalClass)it.next();
					debug(" " + pc.sootClass);
					
				}
			}
		}

		public void doWeave(AdviceApplication adviceAppl, SootMethod joinpointMethod) {
			final boolean bExecutionAdvice =	
				adviceAppl instanceof ExecutionAdviceApplication;
				
			
			final boolean bExecutionWeavingIntoSelf=
					bExecutionAdvice &&
						 sootAdviceMethod.equals(joinpointMethod);
			
			boolean bStaticJoinPoint = joinpointMethod.isStatic();
			boolean bUseClosureObject;
			
			final boolean bAlwaysUseClosures;
			if (Debug.v().aroundWeaver)	{
				bAlwaysUseClosures=false;//true;//false; // change this to suit your debugging needs...
			} else {
				bAlwaysUseClosures=false; // don't change this!
			}
			
			if (bHasBeenWovenInto || 
					 bExecutionWeavingIntoSelf)
				bUseClosureObject=true;
			else
				bUseClosureObject=bAlwaysUseClosures;// true;//true;//false;

			final boolean bUseStaticAccessMethod= 
				bStaticJoinPoint || bAlwaysStaticAccessMethod;
			
			String accessMethodName;
			if (bUseClosureObject) {
				accessMethodName = "abc$closure$proceed$" + adviceMethodIdentifierString + "$" + state.getUniqueID();
			} else {
				if ( bUseStaticAccessMethod) {
					accessMethodName = "abc$static$proceed$" + adviceMethodIdentifierString;
				} else {
					accessMethodName = dynamicAccessMethodName;
				}
			}
	
			
			AccessMethod accessMethod=null;
			if (!bUseClosureObject) {
				accessMethod = getAccessMethod(joinpointMethod.getDeclaringClass().getName(), bUseStaticAccessMethod);
			}
			if (accessMethod == null) {
				accessMethod = new AdviceMethod.AccessMethod(joinpointMethod.getDeclaringClass(), bUseStaticAccessMethod, accessMethodName, bUseClosureObject);
					
				if (bUseClosureObject)
					setClosureAccessMethod(accessMethod);
				else
					setAccessMethod(joinpointMethod.getDeclaringClass().getName(), bUseStaticAccessMethod, accessMethod);
			}
			
			accessMethod.doWeave(adviceAppl, joinpointMethod);			
		}
		public class AccessMethod {
			AccessMethod(SootClass joinpointClass, boolean bStaticAccessMethod, String accessMethodName, boolean bClosureMethod) {
				this.bStaticAccessMethod=bStaticAccessMethod;
//				this.adviceMethod = parent;
				this.joinpointClass = joinpointClass;
				this.bUseClosureObject=bClosureMethod;
				
				String interfaceName = interfaceInfo.accessInterface.getName();

				if (bStaticAccessMethod || bClosureMethod) {
						sootAccessMethod = new SootMethod(accessMethodName, new LinkedList(), getAdviceReturnType(), Modifier.PUBLIC | Modifier.STATIC);
				} else {
					debug("adding interface " + interfaceName + " to class " + joinpointClass.getName());
		
					joinpointClass.addInterface(interfaceInfo.accessInterface);

					// create new method					
					sootAccessMethod = new SootMethod(accessMethodName, new LinkedList(), getAdviceReturnType(), Modifier.PUBLIC);
				}
				sootAccessMethod.addTag(new DisableExceptionCheckTag());
				Body accessBody = Jimple.v().newBody(sootAccessMethod);

				sootAccessMethod.setActiveBody(accessBody);
				debug("adding method " + sootAccessMethod.getName() + " to class " + joinpointClass.getName());
				joinpointClass.addMethod(sootAccessMethod);

				Chain accessStatements = accessBody.getUnits().getNonPatchingChain();

				// generate this := @this
				LocalGeneratorEx lg = new LocalGeneratorEx(accessBody);
				Local lThis = null;
				if (!bStaticAccessMethod && !bClosureMethod) {
					lThis = lg.generateLocal(joinpointClass.getType(), "this");
					accessStatements.addFirst(Jimple.v().newIdentityStmt(lThis, Jimple.v().newThisRef(RefType.v(joinpointClass))));
				}
				Util.validateMethod(sootAccessMethod);
				//accessMethodInfo.targetLocal=Restructure.addParameterToMethod(
				//	accessMethod, (Type)accessMethodParameters.get(0), "targetArg");

				{
					Iterator it = originalAdviceFormalTypes.iterator();
					while (it.hasNext()) {
						Type type = (Type) it.next();
						//System.out.println(" " +method.getActiveBody().getUnits());
						Local l = Restructure.addParameterToMethod(sootAccessMethod, type, "orgAdviceFormal");
						//System.out.println(" " +method.getActiveBody().getUnits());
						Util.validateMethod(sootAccessMethod);
						adviceFormalLocals.add(l);
					}
				}
				Util.validateMethod(sootAccessMethod);

				shadowIdParamLocal = Restructure.addParameterToMethod(sootAccessMethod, (Type) accessMethodParameterTypes.get(0), "shadowID");
				bindMaskParamLocal = Restructure.addParameterToMethod(sootAccessMethod, (Type) accessMethodParameterTypes.get(1), "skipAdvice");

				if (accessMethodParameterTypes.size() != 2)
					throw new InternalAroundError();

				Stmt lastIDStmt = Restructure.getParameterIdentityStatement(sootAccessMethod, sootAccessMethod.getParameterCount() - 1);

				if (!bClosureMethod) {
					// generate exception code (default target)
					SootClass exception = Scene.v().getSootClass("java.lang.RuntimeException");
					Local ex = lg.generateLocal(exception.getType(), "exception");
					Stmt newExceptStmt = Jimple.v().newAssignStmt(ex, Jimple.v().newNewExpr(exception.getType()));
					Stmt initEx = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(ex, exception.getMethod("<init>", new ArrayList()).makeRef()));
					Stmt throwStmt = Jimple.v().newThrowStmt(ex);
		
					defaultTarget = Jimple.v().newNopStmt();
					accessStatements.add(defaultTarget);
					accessStatements.add(newExceptStmt);
					accessStatements.add(initEx);
					accessStatements.add(throwStmt);
					defaultEnd = Jimple.v().newNopStmt();
					accessStatements.add(defaultEnd);
		
					// just generate a nop for now.
					lookupStmt = Jimple.v().newNopStmt();
		
					accessStatements.insertAfter(lookupStmt, lastIDStmt);
					
					//AdviceMethod adviceMethodInfo = state.getInterfaceInfo(interfaceName);
					Iterator it = dynamicArguments.iterator();
					while (it.hasNext()) {
						Type type = (Type) it.next();
						Local l = Restructure.addParameterToMethod(sootAccessMethod, type, "dynArgFormal");
						dynParamLocals.add(l);
					}
				} else {
					defaultTarget = Jimple.v().newNopStmt();
					accessStatements.add(defaultTarget);
					defaultEnd = Jimple.v().newNopStmt();
					accessStatements.add(defaultEnd);
					// just generate a nop for now.
					lookupStmt = Jimple.v().newNopStmt();
					accessStatements.insertAfter(lookupStmt, lastIDStmt);
				}
				
				// fixSuperCalls used to be here...

				Util.validateMethod(sootAccessMethod);

				accessMethodBody=sootAccessMethod.getActiveBody();
				accessMethodStatements=accessMethodBody.getUnits().getNonPatchingChain();
			}

			public void doWeave(AdviceApplication adviceAppl, SootMethod joinpointMethod) {
				AdviceApplicationInfo adviceApplication=new AdviceApplicationInfo(adviceAppl, joinpointMethod);
				adviceApplication.doWeave();
			}
			public class AdviceApplicationInfo {
//				final boolean bHasProceed;
				AdviceApplicationInfo(AdviceApplication adviceAppl, SootMethod joinpointMethod) {
					//this.accessMethodName=AccessMethod.this.accessMethodSoot.getName();
					//this.bUseStaticAccessMethod=bS
					this.adviceAppl=adviceAppl;
					
					final boolean bExecutionAdvice =	
						adviceAppl instanceof ExecutionAdviceApplication;
					
					
					AdviceDecl adviceDecl = (AdviceDecl) adviceAppl.advice;
					
					AdviceSpec adviceSpec = adviceDecl.getAdviceSpec();
					AroundAdvice aroundSpec = (AroundAdvice) adviceSpec;
					SootClass theAspect = adviceDecl.getAspect().getInstanceClass().getSootClass();
					SootMethod method = adviceDecl.getImpl().getSootMethod();
					
					//this.bHasProceed=adviceDecl.getSootProceeds().size()>0;
					
					

					this.joinpointMethod=joinpointMethod;
					this.joinpointClass=joinpointMethod.getDeclaringClass();
					this.joinpointBody=joinpointMethod.getActiveBody();
					this.joinpointStatements=joinpointBody.getUnits().getNonPatchingChain();
					
					this.bStaticJoinPoint = joinpointMethod.isStatic();
					this.begin = adviceAppl.shadowmatch.sp.getBegin();
					this.end = adviceAppl.shadowmatch.sp.getEnd();


					
					
					
					

					
					
					debug("CLOSURE: " + (bUseClosureObject ? "Using closure" : "Not using closure"));

					if (bUseClosureObject) {
						Main.v().error_queue.enqueue(
							ErrorInfo.WARNING, "Using closure object. This may impact performance.");
					}
					
					// if the target is an around-advice method, 
					// make sure proceed has been generated for that method.
					if (bExecutionAdvice && Util.isAroundAdviceMethod(joinpointMethod)) {
						AdviceMethod adviceMethodWovenInto = state.getAdviceMethod(joinpointMethod);
						if (adviceMethodWovenInto == null) {
							AdviceDecl advdecl;
							advdecl=getAdviceDecl(joinpointMethod);
							List sootProceeds2=new LinkedList();
							sootProceeds2.addAll(advdecl.getLocalSootMethods());
							if (!sootProceeds2.contains(joinpointMethod))
								sootProceeds2.add(joinpointMethod);
							
							adviceMethodWovenInto = new AdviceMethod(joinpointMethod, 
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
							//debug(Util.printMethod(joinpointMethod));
						}

						context=findLocalsGoingIn(joinpointBody, begin, end, returnedLocal);
						{ // print debug information
							
							debug(" Method: " + joinpointMethod.toString());
							debug(" Application: " + adviceAppl.toString());
							//debug("Method + " + joinpointMethod.toString());
							Iterator it = context.iterator();
							while (it.hasNext()) {
								Local l = (Local) it.next();
								debug("  " + l.toString());
							}
						}
			
						 
						validateShadow(joinpointBody, begin, end);
									 
						
						if (bUseClosureObject) {
							closureClass=generateClosure( 
									interfaceInfo.abstractAccessMethod.getName(), 
									sootAccessMethod, context);
						}
						
						List dynamicActuals;
						
						
						if (bUseClosureObject) {
							if (!hasDynamicProceed) {
								AdviceMethod.this.generateProceedCalls(false, true, null);
							}
							//	throw new InternalAroundError();
							argIndex=new int[context.size()];
							List types=new LinkedList();
							int i=0;
							for (Iterator it=context.iterator(); it.hasNext();i++) {
								Local l=(Local)it.next();
								types.add(l.getType());
								argIndex[i]=i;
								Local argLocal=Restructure.addParameterToMethod(sootAccessMethod, l.getType(), 
										"dynArg");
								dynParamLocals.add(argLocal);
							}
							dynamicActuals=getDefaultValues(dynamicArguments);
							skipDynamicActuals=context;
						} else {
							ObjectBox dynamicActualsBox=new ObjectBox();
							argIndex=AdviceMethod.this.modifyAdviceMethod(context,AccessMethod.this, dynamicActualsBox, bStaticAccessMethod, bUseClosureObject);
							dynamicActuals=(List)dynamicActualsBox.object;
							skipDynamicActuals=dynamicActuals;
						}
						// copy shadow into access method with a return returning the relevant local.
						Stmt first;
						HashMap localMap;
						Stmt switchTarget;
						{ // copy shadow into access method
							ObjectBox result = new ObjectBox();
							if (lookupStmt==null)	
								throw new InternalAroundError();
							localMap = copyStmtSequence(joinpointBody, begin, end, accessMethodBody, lookupStmt, returnedLocal, result);
							first = (Stmt) result.object;
							if (first==null)
								throw new InternalAroundError();
							switchTarget = Jimple.v().newNopStmt();
						
							if (first==lookupStmt)
								throw new InternalAroundError();
							
							accessMethodStatements.insertBefore(switchTarget, first);
						}

						updateSavedReferencesToStatements(localMap);

						// Construct a tag to place on the invokes that are put in place of the removed
						// statements

						List newstmts=new LinkedList();
						Chain units=joinpointBody.getUnits();
						Stmt s=(Stmt) units.getSuccOf(begin);
						while(s!=end) {
						    newstmts.add(localMap.get(s));
						    s=(Stmt) units.getSuccOf(s);
						}
						Tag redirectExceptions=new RedirectedExceptionSpecTag(accessMethodBody,newstmts);
						
						
					//}
					{ // remove old shadow
						// remove any traps from the shadow before removing the shadow
						Util.removeTraps(joinpointBody, begin, end);
						// remove statements except original assignment
						Util.removeStatements(joinpointBody, begin, end, null);
						StmtAdviceApplication stmtAppl = null;
						if (adviceAppl instanceof StmtAdviceApplication) {
							stmtAppl = (StmtAdviceApplication) adviceAppl;
							stmtAppl.stmt = null;
							/// just for sanity, because we deleted that stmt
						}
					}

					//if (bHasProceed) {
						
						
						{ // determine shadow ID
							if (bUseClosureObject) {
								shadowID = -1; // bogus value, since not used in this case.
							} else if (bStaticAccessMethod) {
								shadowID = nextShadowID++;
							} else {
								shadowID = getUniqueShadowID();
							}
						}
			
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
						
						adviceAppl.getResidue().getAdviceFormalBindings(bindings);
						bindings.calculateBitMaskLayout();
						
						debug(" " + bindings);
						
						{
							LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);
							bindMaskLocal=lg.generateLocal(IntType.v(), "bindMask");			
						}
						adviceAppl.setResidue(
								adviceAppl.getResidue().restructureToCreateBindingsMask(bindMaskLocal, bindings));
					//}
					
					Stmt endResidue=weaveDynamicResidue(
						returnedLocal,
						skipDynamicActuals,
						shadowID,
						wc,
						failPoint,
						redirectExceptions);
					
					joinpointStatements.insertAfter(
						Jimple.v().newAssignStmt(bindMaskLocal, IntConstant.v(0))
						, begin);
					
					//List assignments=getAssignmentsToAdviceFormals(begin, endResidue, staticBindings);
					//createBindingMask(assignments, staticBindings, wc, begin, endResidue);
					
					AccessMethod.this.assignCorrectParametersToLocals(
						context,
						argIndex,
						first,
						localMap,
						bindings);
			
					
					if (!bUseClosureObject) 
						AccessMethod.this.modifyLookupStatement(switchTarget, shadowID);
					
					Local lThis = null;
					if (!bStaticJoinPoint)
						lThis = joinpointBody.getThisLocal();
		
					makeAdviceInvokation(bindMaskLocal,returnedLocal, dynamicActuals, 
							     (bUseClosureObject ? lClosure : lThis), shadowID, failPoint, wc,
							     new DisableExceptionCheckTag());
						
					if (abc.main.Debug.v().aroundWeaver)
						AccessMethod.this.sootAccessMethod.getActiveBody().validate();
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
						if (newLocal == null)
							throw new InternalAroundError();
			
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
				private List findLocalsGoingIn(Body body, Stmt begin, Stmt end, Local additionallyUsed) {
					Chain statements = body.getUnits().getNonPatchingChain();
			
					if (!statements.contains(begin))
						throw new InternalAroundError();
			
					if (!statements.contains(end))
						throw new InternalAroundError();
			
					Set usedInside = new HashSet();
					if (additionallyUsed!=null)
						usedInside.add(additionallyUsed);
						
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
					usedInside.retainAll(definedOutside);
					return new LinkedList(usedInside);
				}

				private Stmt weaveDynamicResidue(
					Local returnedLocal,
					List dynamicActuals,
					int shadowID,
					WeavingContext wc,
					Stmt failPoint,
                                        Tag attachToInvoke) {
					LocalGeneratorEx localgen = new LocalGeneratorEx(joinpointBody);	
					
					joinpointStatements.insertBefore(failPoint, end);
					
					// weave in residue
					Stmt endResidue = adviceAppl.getResidue().codeGen
					    (joinpointMethod, localgen, joinpointStatements, begin, failPoint, true, wc);
					
					// debug("weaving residue: " + adviceAppl.residue);
					if (!(adviceAppl.getResidue() instanceof AlwaysMatch)) {
						InvokeExpr directInvoke;
						List directParams = new LinkedList();
						
						List defaultValues = getDefaultValues(originalAdviceFormalTypes);
						directParams.addAll(defaultValues);
						directParams.add(IntConstant.v(shadowID));
						directParams.add(IntConstant.v(1)); //  bindMask parameter (1 => skip)
						directParams.addAll(dynamicActuals);
						if (bUseClosureObject) {
							directInvoke = Jimple.v().newStaticInvokeExpr(sootAccessMethod.makeRef(), directParams);
						} else if (bStaticAccessMethod) {
							directInvoke = Jimple.v().newStaticInvokeExpr(sootAccessMethod.makeRef(), directParams);
						} else {
							// TODO: can this call be replaced with an InvokeSpecial?
							directInvoke = Jimple.v().newInterfaceInvokeExpr(joinpointBody.getThisLocal() , interfaceInfo.abstractAccessMethod.makeRef(), directParams);
						}
						{
							Stmt skipAdvice;
							if (returnedLocal != null) {
								AssignStmt assign = Jimple.v().newAssignStmt(returnedLocal, directInvoke);
								joinpointStatements.insertAfter(assign, failPoint);
								Restructure.insertBoxingCast(joinpointBody, assign, true);
								skipAdvice = assign;
							} else {
								skipAdvice = Jimple.v().newInvokeStmt(directInvoke);
								joinpointStatements.insertAfter(skipAdvice, failPoint);
							}
							skipAdvice.addTag(attachToInvoke);
							directInvokationStmts.add(skipAdvice);
						}
					}
					return endResidue;
				}
								
				public SootClass generateClosure(
						String closureRunMethodName, SootMethod targetAccessMethod,
						List /*Local*/ context) {
					
					SootClass closureClass = 
						new SootClass("Abc$closure$" + state.getUniqueID(), 
							Modifier.PUBLIC);

					closureClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
					//closureClass.implementsInterface(interfaceName);
					closureClass.addInterface(interfaceInfo.accessInterface);

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
								interfaceInfo.abstractAccessMethod.getParameterTypes().iterator(); 
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
					
					
					InvokeExpr invEx=Jimple.v().newStaticInvokeExpr(targetAccessMethod.makeRef(), invokeLocals);
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
					
					LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);
					Local l=lg.generateLocal(closureClass.getType(), "closure");
					Stmt newStmt = Jimple.v().newAssignStmt(l, Jimple.v().newNewExpr(closureClass.getType()));
//					Stmt init = Jimple.v().newInvokeStmt(
//						Jimple.v().newSpecialInvokeExpr(l, 
//							Scene.v().getSootClass("java.lang.Object").getMethod("<init>", new ArrayList())));
					Stmt init = Jimple.v().newInvokeStmt(
						Jimple.v().newSpecialInvokeExpr(l, 
							closureClass.getMethodByName("<init>").makeRef()));//, new ArrayList())));

					joinpointStatements.insertAfter(init, begin);
					joinpointStatements.insertAfter(newStmt, begin);
					int i=0;
					for (Iterator it=context.iterator(); it.hasNext();i++) {
						Local lContext=(Local)it.next();
						SootField f=closureClass.getFieldByName("context" + i);
						debug("2" + f.getType()+ " : " + lContext.getType());
						AssignStmt as=Jimple.v().newAssignStmt(
							Jimple.v().newInstanceFieldRef(l, f.makeRef()), lContext);
						if (!f.getType().equals(lContext.getType()))
							throw new InternalAroundError("" + f.getType()+ " : " + lContext.getType());
						joinpointStatements.insertAfter(as, init);
					}
					return l;
				}
								
			        private void makeAdviceInvokation(Local bindMaskLocal, Local returnedLocal, List dynamicActuals, Local lThis, int shadowID, Stmt insertionPoint, WeavingContext wc,Tag attachToInvoke) {
					LocalGeneratorEx lg = new LocalGeneratorEx(joinpointBody);
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
						if (joinpointStatements == null)
							throw new InternalAroundError();
						if (!joinpointStatements.contains(insertionPoint))
							throw new InternalAroundError();
						joinpointStatements.insertBefore(nextstmt, insertionPoint);
					}
					
					// we need to add some of our own parameters to the invokation
					List params = new LinkedList();
					if (bUseClosureObject)  {
						if (lThis==null)
							throw new InternalAroundError();
							
						params.add(lThis); // pass the closure
					} else if (bStaticAccessMethod) { 
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
					} else if (bStaticAccessMethod) { // pass the static class id
						params.add(IntConstant.v(state.getStaticDispatchTypeID(joinpointClass.getType())));
					} else {
						params.add(IntConstant.v(0));
					}
					//params.add(targetLocal);
					params.add(bindMaskLocal);
					
					// and add the original parameters 
					params.addAll(0, invokeEx.getArgs());
					
					params.addAll(dynamicActuals);
					
					// generate a new invoke expression to replace the old one
					VirtualInvokeExpr invokeEx2 = Jimple.v().newVirtualInvokeExpr(aspectRef, sootAdviceMethod.makeRef(), params);
					
					Stmt invokeStmt;
					if (returnedLocal == null) {
						invokeStmt = Jimple.v().newInvokeStmt(invokeEx2);
						joinpointStatements.insertBefore(invokeStmt, insertionPoint);
					} else {
						AssignStmt assign = Jimple.v().newAssignStmt(returnedLocal, invokeEx2);
						joinpointStatements.insertBefore(assign, insertionPoint);
						Restructure.insertBoxingCast(joinpointMethod.getActiveBody(), assign, true);
						invokeStmt = assign;
					}
					invokeStmt.addTag(attachToInvoke);
					Stmt beforeEnd=Jimple.v().newNopStmt();
					joinpointStatements.insertBefore(beforeEnd, end);
					joinpointStatements.insertBefore(Jimple.v().newGotoStmt(beforeEnd), insertionPoint);
					
					if (invokeStmt == null)
						throw new InternalAroundError();
					
					adviceMethodInvokationStmts.add(invokeStmt);
					
					if (abc.main.Debug.v().aroundWeaver)
						joinpointBody.validate();
				}

				
				private Local findReturnedLocal() {
					Body joinpointBody = joinpointMethod.getActiveBody();
					if (abc.main.Debug.v().aroundWeaver)
						joinpointBody.validate();
					Chain joinpointStatements = joinpointBody.getUnits().getNonPatchingChain();
					boolean bStatic = joinpointMethod.isStatic();
			
					Stmt begin = adviceAppl.shadowmatch.sp.getBegin();
					Stmt end = adviceAppl.shadowmatch.sp.getEnd();
			
					Local returnedLocal = null;
					
					Type objectType=Scene.v().getRefType("java.lang.Object");
			
					if (adviceAppl instanceof ExecutionAdviceApplication || 
						adviceAppl instanceof ConstructorAdviceApplication) {
						//ExecutionAdviceApplication ea = (ExecutionAdviceApplication) adviceAppl;
						
						if (adviceAppl instanceof ConstructorAdviceApplication) {
							if (!joinpointMethod.getReturnType().equals(VoidType.v()))
								throw new InternalAroundError("Constructor must have void return type: " + 
									joinpointMethod);
						}
							
						if (joinpointMethod.getReturnType().equals(VoidType.v())) {
							if (
								! getAdviceReturnType().equals(VoidType.v())					 
								) { 
								// make dummy local to be returned. assign default value.
								LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);
								Local l=lg.generateLocal(getAdviceReturnType(), "returnedLocal");
								Stmt s=Jimple.v().newAssignStmt(l, 
									Restructure.JavaTypeInfo.getDefaultValue(getAdviceReturnType()));
								joinpointStatements.insertAfter(s, begin);
								returnedLocal=l;			
							}
						} else {
							ReturnStmt returnStmt;
							try {
								returnStmt = (ReturnStmt) joinpointStatements.getSuccOf(end);
							} catch (Exception ex) {
								throw new InternalAroundError("Expecting return statement after shadow " + "for execution advice in non-void method");
							}
							if (returnStmt.getOp() instanceof Local) {
								returnedLocal = (Local) returnStmt.getOp();
							} else { 
								// Some other value. This may never occur...
								// it seems some earlier stage ensures it's always a local.
								// anyways. make local to be returned.
								LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);
								Local l=lg.generateLocal(getAdviceReturnType(), "returnedLocal");
								Stmt s=Jimple.v().newAssignStmt(l, 
									returnStmt.getOp());
								joinpointStatements.insertBefore(s, end);
								returnedLocal=l;	
							}
							
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
							
						if (applStmt instanceof AssignStmt) {					   
							AssignStmt assignStmt = (AssignStmt) applStmt;
							Value leftOp = assignStmt.getLeftOp();
							Value rightOp = assignStmt.getRightOp();
							if (leftOp instanceof Local) {
								// get
								returnedLocal = (Local) leftOp;
								
							} else if ((leftOp instanceof FieldRef && rightOp instanceof Local) ||	
									   (leftOp instanceof FieldRef && rightOp instanceof Constant)) {
								// set
								
								// special case: with return type object, set() returns null.
								if (getAdviceReturnType().equals(objectType)) {
									LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);
									Local l=lg.generateLocal(objectType, "nullValue");
									Stmt s=Jimple.v().newAssignStmt(l, NullConstant.v());
									joinpointStatements.insertAfter(s, begin);
									returnedLocal=l;
								}			
							} else {
								// unexpected statement type
								throw new InternalAroundError();
							}
						} else if (applStmt instanceof InvokeStmt) {
							InvokeStmt invStmt=(InvokeStmt)applStmt;
							
							// if advice method is non-void, we have to return something
							// TODO: type checking to throw out invalid cases?
							if (
								! getAdviceReturnType().equals(VoidType.v())					 
								) { 
								// make dummy local to be returned. assign default value.
								Type returnType=getAdviceReturnType(); 
								
								LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);
								Local l=lg.generateLocal(returnType, "returnedLocal"); 
								Stmt s=Jimple.v().newAssignStmt(l, 
									Restructure.JavaTypeInfo.getDefaultValue(returnType));
								joinpointStatements.insertAfter(s, begin);
								returnedLocal=l;			
							}
						} else {
							// unexpected statement type
							throw new InternalAroundError();
						}
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
				public final SootClass joinpointClass;
				public final SootMethod joinpointMethod;
				public final Chain joinpointStatements;
				public final Body joinpointBody;
				public final boolean bStaticJoinPoint;
				//public final boolean bUseStaticAccessMethod;
				//public final AdviceMethod.AccessMethod accessMethod;	
			}
			public void modifyLookupStatement(Stmt switchTarget, int shadowID) {
				// modify the lookup statement in the access method
				lookupValues.add(IntConstant.v(shadowID));
				accessMethodTargets.add(switchTarget);
				// generate new lookup statement and replace the old one
				Stmt newLookupStmt =
					Jimple.v().newLookupSwitchStmt(
						shadowIdParamLocal,
						lookupValues,
						accessMethodTargets,
						defaultTarget);
				accessMethodStatements.insertAfter(newLookupStmt, lookupStmt);
				accessMethodStatements.remove(lookupStmt);
				lookupStmt = newLookupStmt;

				if (!bStaticAccessMethod) {
					 AdviceMethod.this.fixAccessMethodSuperCalls(joinpointClass);
				}

				Util.cleanLocals(accessMethodBody);			
			}
			//HashMap /*String, Integer*/ fieldIDs=new HashMap();
			private void addParameters(List addedDynArgsTypes)  {
				debug("adding parameters to access method " + sootAccessMethod);
				Util.validateMethod(sootAccessMethod);

				Iterator it2 = addedDynArgsTypes.iterator();
				while (it2.hasNext()) {
					Type type = (Type) it2.next();
					debug(" " + type);
					Local l = Restructure.addParameterToMethod(sootAccessMethod, type, "dynArgFormal");
					dynParamLocals.add(l);
				}

				//				modify existing super call in the access method		
				Stmt stmt = superInvokeStmt;
				if (stmt != null) {
					//addEmptyDynamicParameters(method, addedDynArgs, accessMethodName);
					InvokeExpr invoke = (InvokeExpr) stmt.getInvokeExprBox().getValue();
					List newParams = new LinkedList();
					newParams.addAll(Util.getParameterLocals(sootAccessMethod.getActiveBody()));
					List types=new LinkedList(sootAccessMethod.getParameterTypes());
					/// should we do deep copy?	
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
			
				LocalGeneratorEx lg=new LocalGeneratorEx(accessMethodBody);
				
				// Assign the correct access parameters to the locals 
				Stmt insertionPoint = first;
				Stmt skippedCase = Jimple.v().newNopStmt();
				Stmt nonSkippedCase = Jimple.v().newNopStmt();
				Stmt neverBoundCase = Jimple.v().newNopStmt();
				Stmt gotoStmt = Jimple.v().newGotoStmt(neverBoundCase);
				Stmt ifStmt = Jimple.v().newIfStmt(Jimple.v().newEqExpr(bindMaskParamLocal, IntConstant.v(1)), skippedCase);
				accessMethodStatements.insertBefore(ifStmt, insertionPoint);
				accessMethodStatements.insertBefore(nonSkippedCase, insertionPoint);
				accessMethodStatements.insertBefore(gotoStmt, insertionPoint);
				accessMethodStatements.insertBefore(skippedCase, insertionPoint);
				accessMethodStatements.insertBefore(neverBoundCase, insertionPoint);
				NopStmt afterDefault=Jimple.v().newNopStmt();
				accessMethodStatements.insertAfter(afterDefault, nonSkippedCase);

				Local maskLocal=lg.generateLocal(IntType.v(), "maskLocal");
				
				Set defaultLocals=new HashSet();

				// Process the bindings.
				// The order is important.
				for (int index=bindings.numOfFormals()-1; index>=0; index--){
					List localsFromIndex=bindings.localsFromIndex(index);
					if (localsFromIndex==null) { 
					} else {
						
						if (localsFromIndex.size()==1) 	{ // non-skipped case: assign advice formal
							Local paramLocal = (Local) adviceFormalLocals.get(index);
							Local actual=(Local)localsFromIndex.get(0);
							Local actual2=(Local)localMap.get(actual);
							AssignStmt s = Jimple.v().newAssignStmt(actual2, paramLocal);
							accessMethodStatements.insertAfter(s, nonSkippedCase);
							Restructure.insertBoxingCast(sootAccessMethod.getActiveBody(), s, true);
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
										Local paramLocal = (Local) dynParamLocals.get(argIndex[id]);
										Local actual3=(Local)localMap.get(l);
										AssignStmt s = Jimple.v().newAssignStmt(actual3, paramLocal);
										accessMethodStatements.insertBefore(s, afterDefault);
										Restructure.insertBoxingCast(sootAccessMethod.getActiveBody(), s, true);
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
							
							accessMethodStatements.insertAfter(as, afterDefault);
							accessMethodStatements.insertAfter(as2, as);
							NopStmt endStmt=Jimple.v().newNopStmt();
							accessMethodStatements.insertAfter(endStmt, as2);
							
							int localIndex=0;
							List lookupValues=new LinkedList();
							List targets=new LinkedList();
							for (Iterator itl=localsFromIndex.iterator(); itl.hasNext();localIndex++) {
								Local l=(Local)itl.next();
								lookupValues.add(IntConstant.v(localIndex));
								
								Local actual3=(Local)localMap.get(l);
								
								NopStmt targetNop=Jimple.v().newNopStmt();
								
								accessMethodStatements.insertAfter(targetNop, as2);
								targets.add(targetNop);
								
								
								Local paramLocal = (Local) adviceFormalLocals.get(index);
								AssignStmt s = Jimple.v().newAssignStmt(actual3, paramLocal);
								accessMethodStatements.insertAfter(s, targetNop);
								GotoStmt g=Jimple.v().newGotoStmt(endStmt);
								accessMethodStatements.insertAfter(g, s);
								Restructure.insertBoxingCast(sootAccessMethod.getActiveBody(), s, true);							
							}
							
			
							// default case (exception)								
							SootClass exception = Scene.v().getSootClass("java.lang.RuntimeException");
							Local ex = lg.generateLocal(exception.getType(), "exception");
							Stmt newExceptStmt = Jimple.v().newAssignStmt(ex, Jimple.v().newNewExpr(exception.getType()));
							Stmt initEx = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(ex, exception.getMethod("<init>", new ArrayList()).makeRef()));
							Stmt throwStmt = Jimple.v().newThrowStmt(ex);
							accessMethodStatements.insertAfter(newExceptStmt, as2);
							accessMethodStatements.insertAfter(initEx, newExceptStmt);
							accessMethodStatements.insertAfter(throwStmt, initEx);
						
							
							LookupSwitchStmt lp=Jimple.v().newLookupSwitchStmt(
								maskLocal, lookupValues, targets, newExceptStmt
								);
							accessMethodStatements.insertAfter(lp, as2);
						}
					}
				}
				
				int i=0;
				// process the context
				for (Iterator it=context.iterator(); it.hasNext(); i++) {
					Local actual = (Local) it.next(); // context.get(i);
					Local actual2 = (Local) localMap.get(actual);
					if (!accessMethodBody.getLocals().contains(actual2))
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
							Local paramLocal = (Local) dynParamLocals.get(argIndex[i]);
							AssignStmt s = Jimple.v().newAssignStmt(actual2, paramLocal);
							accessMethodStatements.insertAfter(s, skippedCase);
							Restructure.insertBoxingCast(sootAccessMethod.getActiveBody(), s, true);
							/// allow boxing?
						}
					} else {
						debug(" no binding: " + actual.getName());
						// no binding
						Local paramLocal = (Local) dynParamLocals.get(argIndex[i]);
						AssignStmt s = Jimple.v().newAssignStmt(actual2, paramLocal);
						accessMethodStatements.insertAfter(s, neverBoundCase);
						insertCast(sootAccessMethod.getActiveBody(), s, s.getRightOpBox(), actual2.getType());
					}
				}
				debug("done: Access method: assigning correct parameters to locals*********************");
			}
		

			//public final AdviceMethod adviceMethod;
			public final SootClass joinpointClass;
			public final boolean bStaticAccessMethod;
			public final Body accessMethodBody;
			public final Chain accessMethodStatements;
			
			public final boolean bUseClosureObject;
			
			
			final List accessMethodTargets = new LinkedList();
			final List lookupValues = new LinkedList();
			final NopStmt defaultTarget;
			final NopStmt defaultEnd;
			Stmt lookupStmt;
			int nextShadowID;
			public final Local shadowIdParamLocal;
			public final Local bindMaskParamLocal;
			
			final List dynParamLocals = new LinkedList();
			final List adviceFormalLocals = new LinkedList();

			public final SootMethod sootAccessMethod;

			SootClass superCallTarget = null;
			Stmt superInvokeStmt = null;

			
		}
		private void validate() {
			{
				Iterator it=adviceMethodInvokationStmts.iterator();
				
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
			for (Iterator it0=proceedClasses.values().iterator(); it0.hasNext();){
				ProceedLocalClass c=(ProceedLocalClass)it0.next();
				
				for (Iterator it1=c.proceedMethods.iterator(); it1.hasNext();) {
					ProceedLocalClass.ProceedCallMethod m=
						(ProceedLocalClass.ProceedCallMethod)it1.next();
					
					Iterator it=m.interfaceInvokationStmts.iterator();
					
					while (it.hasNext()) {
						Stmt stmt=(Stmt)it.next();
						if (stmt.getInvokeExpr().getArgCount()!=interfaceInfo.abstractAccessMethod.getParameterCount() ) {
							throw new InternalAroundError(
								"Call to interface method in advice method " + sootAdviceMethod + 
								" has wrong number of arguments: " + stmt
								);
						}
					}
				}
			}
			{
				List accessMethodImplementations = getAllAccessMethods();
				Iterator it = accessMethodImplementations.iterator();
				while (it.hasNext()) {
					AccessMethod info = (AccessMethod) it.next();
					if (!info.bUseClosureObject) {
						if (info.sootAccessMethod.getParameterCount()!=interfaceInfo.abstractAccessMethod.getParameterCount()) {
							throw new InternalAroundError(
								"Access method " + info.sootAccessMethod + 
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
					if (method.getParameterCount()!=interfaceInfo.abstractAccessMethod.getParameterCount()) {
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
		private void fixAccessMethodSuperCalls(SootClass newAccessClass) {
			
			if (!accessMethodImplementations.containsKey(newAccessClass.getName()))
				throw new InternalAroundError();
			
			Set keys = accessMethodImplementations.keySet();
	
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
				AccessMethod accessInfo = (AccessMethod) accessMethodImplementations.get(className);
	
				SootClass cl = Scene.v().getSootClass(className);
				// if the class is a sub-class of the new class or 
				// if this is the new class and we need to add a super to the new class
				if (Restructure.isBaseClass(newAccessClass, cl) || 
					(className.equals(newAccessClass.getName()) && bAddSuperToNewMethod)) {
					if (accessInfo.superCallTarget == null
						|| // if the class has no super() call 
						Restructure.isBaseClass(accessInfo.superCallTarget, newAccessClass)) { // or if it's invalid
	
						// generate new super() call
						Body body = accessInfo.sootAccessMethod.getActiveBody();
						Chain statements = body.getUnits().getNonPatchingChain();
						Type returnType = accessInfo.sootAccessMethod.getReturnType();
	
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
	
						String accessMethodName = accessInfo.sootAccessMethod.getName();
						Util.validateMethod(accessInfo.sootAccessMethod);
						SpecialInvokeExpr ex =
							Jimple.v().newSpecialInvokeExpr(lThis, accessInfo.superCallTarget.getMethodByName(accessMethodName).makeRef(), Util.getParameterLocals(body));
	
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

		private void addParametersToAccessMethodImplementations(List addedDynArgsTypes) 
		{
			//Set keys=adviceMethodInfo.accessMethodImplementations.keySet();
			List accessMethodImplementations = getAllAccessMethods();
			Iterator it = accessMethodImplementations.iterator();
			while (it.hasNext()) {
				AccessMethod info = (AccessMethod) it.next();
	
				info.addParameters(addedDynArgsTypes);
			}
		}

		public int[] modifyAdviceMethod(List contextParameters, AccessMethod accessMethod, ObjectBox dynamicActualsResult, 
				boolean bStaticAccessMethod, boolean bUseClosureObject) {
			//		determine parameter mappings and necessary additions
			
			List /*Type*/ addedDynArgsTypes = new LinkedList();
			
			int[] argIndex;
			if (bUseClosureObject) {
				argIndex = new int[0]; 
			} else {
				argIndex = determineContextParameterMappings(contextParameters, addedDynArgsTypes);
			}

			List dynamicActuals;
			if (bUseClosureObject) {
				dynamicActuals = new LinkedList();
			} else {
			 	dynamicActuals = getContextActualsList(contextParameters, argIndex);
			}

			// create list of default values for the added arguments
			// (for invokations at other locations)
			if (!bUseClosureObject){			
				List addedDynArgs = getDefaultValues(addedDynArgsTypes);
	
				addContextParamsToInterfaceDefinition(addedDynArgsTypes);
				modifyAdviceMethodInvokations(addedDynArgs, addedDynArgsTypes);
				modifyDirectInterfaceInvokations(addedDynArgs, addedDynArgsTypes);
			}
			
			if (abc.main.Debug.v().aroundWeaver)
				adviceBody.validate();

			generateProceedCalls(bStaticAccessMethod, bUseClosureObject, accessMethod);
			
			if (abc.main.Debug.v().aroundWeaver)
					adviceBody.validate();
	

			// add parameters to all access method implementations
			addParametersToAccessMethodImplementations(addedDynArgsTypes);
	
			{ // process all classes. the aspect class is processed last.
				for (Iterator it=proceedClasses.values().iterator(); it.hasNext();) {
					ProceedLocalClass pc=(ProceedLocalClass)it.next();			
					if (pc.isFirstDegree()) 
						pc.addParameters(addedDynArgsTypes, false);
				}
				for (Iterator it=proceedClasses.values().iterator(); it.hasNext();) {
					ProceedLocalClass pc=(ProceedLocalClass)it.next();			
					if (!pc.isFirstDegree() && !pc.isAspect()) 
						pc.addParameters(addedDynArgsTypes, false);
				}
				for (Iterator it=proceedClasses.values().iterator(); it.hasNext();) {
					ProceedLocalClass pc=(ProceedLocalClass)it.next();			
					if (pc.isAspect()) 
						pc.addParameters(addedDynArgsTypes, false);
				}
			}
			
			dynamicActualsResult.object=dynamicActuals;
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
					if (currentIndex[typeNum] < dynamicArgsByType[typeNum].size()) {
						Integer dynArgID = (Integer) dynamicArgsByType[typeNum].get(currentIndex[typeNum]);
						++currentIndex[typeNum];
						argIndex[i] = dynArgID.intValue();
					} else {
						addedDynArgsTypes.add(type);
						dynamicArguments.add(type);
						int newIndex = dynamicArguments.size() - 1;
						dynamicArgsByType[typeNum].add(new Integer(newIndex));
						argIndex[i] = newIndex;
						++currentIndex[typeNum];
					}
					i++;
				}
			}
			return argIndex;
		}

		private List getContextActualsList(List context, int[] argIndex) {
			List dynamicActuals = new LinkedList();
			{ // create list of dynamic actuals to add (including default values)
				Value[] parameters = new Value[dynamicArguments.size()];

				for (int i = 0; i < argIndex.length; i++) {
					parameters[argIndex[i]] = (Local) context.get(i);
				}
				for (int i = 0; i < parameters.length; i++) {
					if (parameters[i] == null) {
						parameters[i] = Restructure.JavaTypeInfo.getDefaultValue((Type) dynamicArguments.get(i));
					}
					dynamicActuals.add(parameters[i]);
				}
			}
			return dynamicActuals;
		}
		private SootClass createAccessInterface(List accessMethodParameters) {
			SootClass accessInterface;
			// create access interface if it doesn't exist
			if (Scene.v().containsClass(interfaceName)) {
				debug("found access interface in scene");
				accessInterface = Scene.v().getSootClass(interfaceName);
				//abstractAccessMethod=accessInterface.getMethodByName(dynamicAccessMethodName);
			} else {
				debug("generating access interface type " + interfaceName);

				accessInterface = new SootClass(interfaceName, Modifier.INTERFACE | Modifier.PUBLIC);

				accessInterface.setSuperclass(Scene.v().getSootClass("java.lang.Object"));

				SootMethod abstractAccessMethod =
					new SootMethod(dynamicAccessMethodName, accessMethodParameters, getAdviceReturnType(), Modifier.ABSTRACT | Modifier.PUBLIC);

				accessInterface.addMethod(abstractAccessMethod);
				//signature.setActiveBody(Jimple.v().newBody(signature));

				Scene.v().addClass(accessInterface);
				accessInterface.setApplicationClass();

				//GlobalAspectInfo.v().getGeneratedClasses().add(interfaceName);						 
			}
			return accessInterface;
		}

		private void generateProceedCalls(boolean bStaticAccessMethod,boolean bClosure, AccessMethod accessMethod) {

			//AdviceMethod adviceMethodInfo = state.getInterfaceInfo(interfaceName);

			String newStaticInvoke = null;
			boolean bContinue=true;
			if (!bClosure && bStaticAccessMethod) {
				if (!staticProceedTypes.contains(accessMethod.joinpointClass.getName())) {
					newStaticInvoke = accessMethod.joinpointClass.getName();
					staticProceedTypes.add(accessMethod.joinpointClass.getName());
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
			
			Iterator it = proceedClasses.values().iterator();
			while (it.hasNext()) {
				AdviceMethod.ProceedLocalClass pm = (AdviceMethod.ProceedLocalClass) it.next();
				pm.generateProceeds(accessMethod, newStaticInvoke, this);
			}
		}

		

		

		private void modifyDirectInterfaceInvokations(List addedDynArgs, List addedDynArgTypes) {
			if (addedDynArgs.size()!=addedDynArgTypes.size())
				throw new InternalAroundError();
			{ // modify all existing direct interface invokations by adding the default parameters
				Iterator it = directInvokationStmts.iterator();
				while (it.hasNext()) {
					Stmt stmt = (Stmt) it.next();
					//addEmptyDynamicParameters(method, addedDynArgs, accessMethodName);
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
		private void modifyAdviceMethodInvokations(List addedDynArgs, List addedDynArgTypes) {
			if (addedDynArgs.size()!=addedDynArgTypes.size())
				throw new InternalAroundError();
			{ // modify all existing advice method invokations by adding the default parameters
				Iterator it = adviceMethodInvokationStmts.iterator();
				while (it.hasNext()) {
					Stmt stmt = (Stmt) it.next();
					//addEmptyDynamicParameters(method, addedDynArgs, accessMethodName);
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
				SootMethod m = interfaceInfo.abstractAccessMethod;
				List p = new LinkedList(m.getParameterTypes());
				p.addAll(addedDynArgsTypes);
				m.setParameterTypes(p);
			}
		}
		public Type getAdviceReturnType() {
			return sootAdviceMethod.getReturnType();
		}
		
		public static class InterfaceInfo {
			SootClass accessInterface;
			SootMethod abstractAccessMethod;
		}
		InterfaceInfo interfaceInfo = null;

		final String dynamicAccessMethodName;
		final String interfaceName;
		final String adviceMethodIdentifierString;
		final List /*type*/ accessMethodParameterTypes=new LinkedList();
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
		public final boolean bAlwaysStaticAccessMethod = true;//true;//false;//true; //false;

		public boolean bHasBeenWovenInto=false;
		
		public class ProceedLocalClass {
			
			public ProceedLocalClass getEnclosingFirstDegreeClass() {
				if (isAspect() || isFirstDegree())
					throw new InternalAroundError();
				
				ProceedLocalClass enclosing=getEnclosingClass();
				if (enclosing.isFirstDegree())
					return enclosing;
				else
					return enclosing.getEnclosingFirstDegreeClass();
			}
			public ProceedLocalClass getEnclosingClass() {
				if (isAspect())
					throw new InternalAroundError();
				
				return (ProceedLocalClass)proceedClasses.get(enclosingSootClass);
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
			public void generateProceeds(AccessMethod accessMethod, String newStaticInvoke, AdviceMethod adviceMethod) {
				for (Iterator it=this.proceedMethods.iterator(); it.hasNext();) {
					ProceedCallMethod pm=(ProceedCallMethod)it.next();
					pm.generateProceeds(accessMethod, newStaticInvoke, adviceMethod);
				}
			}
			
			private void modifyInterfaceInvokations(List addedAdviceParameterLocals,
					List addedTypes) {
				for (Iterator it=this.proceedMethods.iterator(); it.hasNext();) {
					ProceedCallMethod pm=(ProceedCallMethod)it.next();
					pm.modifyInterfaceInvokations(addedAdviceParameterLocals, addedTypes);
				}
			}
			List addedFields=new LinkedList();
			
			private void addParameters(List addedDynArgsTypes, boolean bDefault) {
				
				if (bDefault && addedDynArgsTypes!=null)
					throw new InternalAroundError();
				
				if (bDefault) {
					addedDynArgsTypes=new LinkedList();
					addedDynArgsTypes.add(interfaceInfo.accessInterface.getType());
					//, "accessInterface"
					addedDynArgsTypes.add(IntType.v());
					addedDynArgsTypes.add(IntType.v());
					addedDynArgsTypes.add(IntType.v());
				}
				if (isAspect()){ 
					// Add the new parameters to the advice method 
					// and keep track of the newly created locals corresponding to the parameters.
					//validateMethod(adviceMethod);
					if (proceedMethods.size()!=1)
						throw new InternalAroundError();
					
					ProceedCallMethod pm=(ProceedCallMethod)proceedMethods.get(0);
					
					List addedAdviceParameterLocals = new LinkedList();
					debug("adding parameters to advice method " + pm.sootProceedCallMethod);
					for (Iterator it = addedDynArgsTypes.iterator();
						it.hasNext();) {
						Type type = (Type) it.next();
						debug(" " + type);
						Local l;
						
						 l = Restructure.addParameterToMethod(pm.sootProceedCallMethod, type, "dynArgFormal");
						
						addedAdviceParameterLocals.add(l);						
					}
					
					pm.modifyNestedInits(addedAdviceParameterLocals);
					
					if (bDefault) {
						pm.setDefaultParameters(addedAdviceParameterLocals);
					}
					if (!bDefault)
						pm.modifyInterfaceInvokations(addedAdviceParameterLocals, addedDynArgsTypes);
					
					for (Iterator it=proceedClasses.values().iterator(); it.hasNext();) {
						ProceedLocalClass pl=(ProceedLocalClass)it.next();
						pl.addedFields.clear();
					}
					
				} else  {
					addedFields=new LinkedList();
					if (isFirstDegree()) {
						for (Iterator it = addedDynArgsTypes.iterator();
							it.hasNext();) {
							Type type = (Type) it.next();
						
							SootField f=new SootField("dynArgField" + state.getUniqueID(), 
										type, Modifier.PUBLIC);
							sootClass.addField(f);
							addedFields.add(f);
						}
					} else {
						addedFields=getEnclosingFirstDegreeClass().addedFields;
					}
					// add locals referencing the fields
					for (Iterator it=proceedMethods.iterator(); it.hasNext();) {
						ProceedCallMethod pm=(ProceedCallMethod)it.next();
						
						List addedAdviceParameterLocals = new LinkedList();
						
						Chain statements=pm.proceedCallBody.getUnits().getNonPatchingChain();
						Stmt insertion=pm.nopAfterEnclosingLocal;
						LocalGeneratorEx lg=new LocalGeneratorEx(pm.proceedCallBody);
						
						for (Iterator it0=addedFields.iterator(); it0.hasNext();) {
							SootField f=(SootField)it0.next();
							Local l=lg.generateLocal(f.getType(), "dynFieldLocal");
							Stmt sf=
								Jimple.v().newAssignStmt(l, 
										Jimple.v().newInstanceFieldRef(
												pm.dynArgfieldBaseLocal,
												f.makeRef()));
							statements.insertBefore(sf, insertion);
							addedAdviceParameterLocals.add(l);
						}
						pm.modifyNestedInits(addedAdviceParameterLocals);
						if (bDefault) {
							pm.setDefaultParameters(addedAdviceParameterLocals);
						}
						if (!bDefault)
							pm.modifyInterfaceInvokations(addedAdviceParameterLocals, addedDynArgsTypes);	
					}
				}
				//return addedAdviceParameterLocals;
			}
			
			/**
			 * @param pm
			 */
			
			public final SootClass sootClass;
			//public final SootClass aspectClass;
			public ProceedLocalClass(SootClass sootClass) {
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
			
			public void addProceedMethod(SootMethod m) {
				this.proceedMethods.add(
						new ProceedCallMethod(AdviceMethod.this, m));
			}
			private final List proceedMethods=new LinkedList();
			

		
			public class ProceedCallMethod {
				private void modifyNestedInits(List addedAdviceParameterLocals) {
					ProceedCallMethod pm=this;
					for (Iterator it0=pm.nestedInitCalls.iterator(); it0.hasNext();) {
						ProceedCallMethod.NestedInitCall nc=
							(ProceedCallMethod.NestedInitCall)it0.next();
						
						if (addedAdviceParameterLocals.size()!=nc.proceedClass.addedFields.size())
							throw new InternalAroundError();
						
						Iterator it=addedAdviceParameterLocals.iterator();
						Iterator it1=nc.proceedClass.addedFields.iterator();
						while (it.hasNext()) {
							Local l=(Local)it.next();
							SootField f=(SootField)it1.next();
							Stmt ns=
								Jimple.v().newAssignStmt(
										Jimple.v().newInstanceFieldRef(
												nc.baseLocal,
												f.makeRef()), l);
							pm.proceedCallBody.getUnits().getNonPatchingChain().insertAfter(ns, nc.statement);
						}
					}
				}
				private void setDefaultParameters(List addedAdviceParameterLocals) {
					ProceedCallMethod pm=this;
					int i=0;
					for (Iterator it=addedAdviceParameterLocals.iterator();it.hasNext();i++) {
						Local l=(Local)it.next();
					switch(i) {
						case 0: pm.interfaceLocal=l;
							 	l.setName("accessInterface" + state.getUniqueID()); break;
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
							final ProceedLocalClass proceedClass,
							final Local baseLocal) {
						super();
						this.statement = statement;
						this.proceedClass = proceedClass;
						this.baseLocal=baseLocal;
					}
					public final Stmt statement;
					public final ProceedLocalClass proceedClass;
					public final Local baseLocal;
				}
				private Local interfaceLocal;
				private Local staticDispatchLocal;
				private Local idLocal;
				private Local bindMaskLocal;
				
				private Local dynArgfieldBaseLocal;
				
				private final List implicitProceedParameters=new LinkedList();
				
				private void modifyInterfaceInvokations(List addedAdviceParameterLocals,
						List addedAdviceParameterTypes) {
					if (addedAdviceParameterLocals.size()!=addedAdviceParameterTypes.size())
						throw new InternalAroundError();
					// Modify the interface invokations. These must all be in the advice method.
					// This constraint is violated by adviceexecution() pointcuts.
					Iterator it = interfaceInvokationStmts.iterator();
					Chain statements=proceedCallBody.getUnits().getNonPatchingChain();
					while (it.hasNext()) {
						Stmt stmt = (Stmt) it.next();
						if (!statements.contains(stmt))
							throw new InternalAroundError();
					
						InvokeExpr intfInvoke = stmt.getInvokeExpr();
						
						if (intfInvoke.getArgCount()!=intfInvoke.getMethodRef().parameterTypes().size())
							throw new InternalAroundError(
									"Signature: " + intfInvoke.getMethodRef().getSignature() + 
									" Args" + intfInvoke.getArgs().size());
						
						List params = new LinkedList(intfInvoke.getArgs());
						params.addAll(addedAdviceParameterLocals);
						List types=new LinkedList(intfInvoke.getMethodRef().parameterTypes()); 
						types.addAll(addedAdviceParameterTypes);
						if (params.size()!=types.size())
							throw new InternalAroundError();
						InvokeExpr newInvoke = Util.createNewInvokeExpr(intfInvoke, params, types);
						//debug("newInvoke: " + newInvoke);
						stmt.getInvokeExprBox().setValue(newInvoke);
						//debug("newInvoke2" + stmt.getInvokeExpr());
					}
					implicitProceedParameters.addAll(addedAdviceParameterLocals);				
				}
				boolean isAdviceMethod() {
					return sootProceedCallMethod.equals(sootAdviceMethod);
				}
				
				private final Set nestedInitCalls=new HashSet();
				private final NopStmt nopAfterEnclosingLocal;
				public ProceedCallMethod(AdviceMethod adviceMethod, SootMethod method) {
					//this.adviceMethod=adviceMethod;
					this.sootProceedCallMethod=method;
					this.proceedCallBody=method.getActiveBody();			
					
					debug("YYYYYYYYYYYYYYYYYYY creating ProceedCallMethod " + method);
					
					this.nopAfterEnclosingLocal=Jimple.v().newNopStmt();
					
					Chain proceedStatements=proceedCallBody.getUnits().getNonPatchingChain();			
					
					for (Iterator it = proceedStatements.snapshotIterator();
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
								ProceedInvokation invokation = 
									new ProceedInvokation(
											invokeEx.getArgs(), s);
								proceedInvokations.add(invokation);							
							}
							// find <init> calls to local/anonymous classes
							if (invokeEx instanceof SpecialInvokeExpr) {
								 
								SpecialInvokeExpr si=(SpecialInvokeExpr) invokeEx;
								Local baseLocal=(Local)si.getBase();
								SootClass baseClass=((RefType)baseLocal.getType()).getSootClass();
								if (!baseClass.equals(getAspect())) {									
									if (si.getMethodRef().name().equals("<init>") && 
										!this.proceedCallBody.getThisLocal().equals(baseLocal) ) {
										if (adviceMethod.proceedClasses.containsKey(baseClass)) {
											debug("WWWWWWWWWWWW base class: " + baseClass);
											AroundWeaver.AdviceMethod.ProceedLocalClass pl=
												(AroundWeaver.AdviceMethod.ProceedLocalClass)
													adviceMethod.proceedClasses.get(baseClass);
											
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
					Stmt insert=Restructure.findFirstRealStmtOrNop(method, proceedStatements);
					
					proceedStatements.insertBefore(nopAfterEnclosingLocal, insert);
					insert=nopAfterEnclosingLocal;
					if (isAdviceMethod()) {
						dynArgfieldBaseLocal=null;						
					} else {
						if (isFirstDegree()) {
							dynArgfieldBaseLocal=proceedCallBody.getThisLocal();
						} else {
							LocalGeneratorEx lg=new LocalGeneratorEx(proceedCallBody);
							SootClass cl=sootClass;
							
							Local lBase=proceedCallBody.getThisLocal();
							
							
							while (true)  {
								debug(" Class: " + cl);
								SootField f=cl.getFieldByName("this$0");
								
								if (!proceedClasses.containsKey(((RefType)f.getType()).getSootClass()))
									throw new InternalAroundError(" " + ((RefType)f.getType()).getSootClass());		
								
								ProceedLocalClass pl=(ProceedLocalClass)proceedClasses.get(((RefType)f.getType()).getSootClass());
								
								Local l=lg.generateLocal(f.getType(), "enclosingLocal");
								AssignStmt as=Jimple.v().newAssignStmt(
										l, Jimple.v().newInstanceFieldRef(
												lBase, f.makeRef()));
								proceedStatements.insertBefore(as , insert);
								if (pl.isFirstDegree()) {
									dynArgfieldBaseLocal=l;
									break;
								} else {
									lBase=l;
									cl=pl.sootClass;
								}
							}							
							if (dynArgfieldBaseLocal==null)
								throw new InternalAroundError();
						}
					} 
				}
				public void generateProceeds(AccessMethod accessMethod, String newStaticInvoke, AdviceMethod adviceMethod) {
					for (Iterator it=proceedInvokations.iterator(); it.hasNext();) {
						ProceedInvokation inv=(ProceedInvokation)it.next();
						inv.generateProceed(accessMethod, newStaticInvoke, adviceMethod);
					}				
				}
				public final List proceedInvokations=new LinkedList();
				
				//private final AdviceMethod adviceMethod;
				private final SootMethod sootProceedCallMethod;
				private final Body proceedCallBody;
				//private final Chain statements;
				private final Set interfaceInvokationStmts = new HashSet();
		
				
			
				public class ProceedInvokation {
					public ProceedInvokation(
								List originalActuals, Stmt originalStmt) {					
						
						this.originalActuals.addAll(originalActuals);
		
						
						
						this.begin = Jimple.v().newNopStmt();
						this.end = Jimple.v().newNopStmt();
						if (originalStmt instanceof AssignStmt) {
							lhs = (Local) (((AssignStmt) originalStmt).getLeftOp());
						}
						Chain statements=sootProceedCallMethod.getActiveBody().getUnits().getNonPatchingChain();
						statements.insertBefore(begin, originalStmt);
						statements.insertAfter(end, originalStmt);
						originalStmt.redirectJumpsToThisTo(begin);
						statements.remove(originalStmt);
					}
								
					private Local lhs;
					final public NopStmt begin;
					final public NopStmt end;
		
					//List lookupValues=new LinkedList();
					List defaultTargetStmts;
					//Stmt lookupStmt;
					List staticInvokes = new LinkedList();
					List staticLookupValues = new LinkedList();
		
					Stmt dynamicInvoke;
		
					final List originalActuals = new LinkedList();
				
					
					
					public void generateProceed(AccessMethod accessMethod, String newStaticInvoke, AdviceMethod adviceMethod) {
						Util.removeStatements(proceedCallBody, begin, end, null);
						debug("YYYYYYYYYYYYYY generateProceed()" + adviceMethod.sootAdviceMethod);
						List parameters = new LinkedList();
						parameters.addAll(this.originalActuals);
						debug(" param count: " + parameters.size());
						parameters.addAll(implicitProceedParameters);
						debug(" param count: " + parameters.size());
						if (this.dynamicInvoke == null && adviceMethod.hasDynamicProceed) {
							InvokeExpr newInvokeExpr = Jimple.v().newInterfaceInvokeExpr(
										interfaceLocal, 
										adviceMethod.interfaceInfo.abstractAccessMethod.makeRef(), parameters);
							Stmt s;
							if (this.lhs == null) {
								s = Jimple.v().newInvokeStmt(newInvokeExpr);
							} else {
								s = Jimple.v().newAssignStmt(this.lhs, newInvokeExpr);
							}
							this.dynamicInvoke = s;
							interfaceInvokationStmts.add(s);
						}
						
						if (newStaticInvoke != null) {
							SootClass cl = Scene.v().getSootClass(newStaticInvoke);

							this.staticLookupValues.add(IntConstant.v(state.getStaticDispatchTypeID(cl.getType())));
						
							InvokeExpr newInvokeExpr = Jimple.v().newStaticInvokeExpr(
									accessMethod.sootAccessMethod.makeRef(), parameters);
							
							
							Stmt s;
							if (this.lhs == null) {
								s = Jimple.v().newInvokeStmt(newInvokeExpr);
							} else {
								s = Jimple.v().newAssignStmt(this.lhs, newInvokeExpr);
							}
							this.staticInvokes.add(s);
							interfaceInvokationStmts.add(s);
						}
						if (this.defaultTargetStmts == null) {
							//				generate exception code (default target)
							this.defaultTargetStmts = new LinkedList();
							LocalGeneratorEx lg = new LocalGeneratorEx(proceedCallBody);
							SootClass exception = Scene.v().getSootClass("java.lang.RuntimeException");
							Local ex = lg.generateLocal(exception.getType(), "exception");
							Stmt newExceptStmt = Jimple.v().newAssignStmt(ex, Jimple.v().newNewExpr(exception.getType()));
							Stmt initEx = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(ex, exception.getMethod("<init>", new ArrayList()).makeRef()));
							Stmt throwStmt = Jimple.v().newThrowStmt(ex);
							this.defaultTargetStmts.add(newExceptStmt);
							this.defaultTargetStmts.add(initEx);
							this.defaultTargetStmts.add(throwStmt);
						}
						
						Chain statements=proceedCallBody.getUnits().getNonPatchingChain();
						if (adviceMethod.staticProceedTypes.isEmpty()) {
							statements.insertAfter(this.dynamicInvoke, this.begin);
						} else if (adviceMethod.hasDynamicProceed == false && adviceMethod.staticProceedTypes.size() == 1) {
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
							LookupSwitchStmt lookupStmt = Jimple.v().newLookupSwitchStmt(key, lookupValues, targets, (Unit) this.defaultTargetStmts.get(0));
						
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
							// just in case:
							statements.insertBefore(Jimple.v().newGotoStmt(this.end), this.end);
						}
					}
				
				}
			}
		}
		
		final public Set adviceMethodInvokationStmts = new HashSet();
		final public Set directInvokationStmts = new HashSet();
		final List /*Type*/ dynamicArguments = new LinkedList();

		final List[] dynamicArgsByType = new List[Restructure.JavaTypeInfo.typeCount];
		
		public final Map /*SootClass, ProceedLocalClass */ proceedClasses=new HashMap();
				
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
		
		public List getAllAccessMethods() {
			List result = new LinkedList();
			result.addAll(accessMethodImplementations.values());
			result.addAll(accessMethodImplementationsStatic.values());
			result.addAll(accessMethodImplementationsClosure);
			return result;
		}
		public AccessMethod getAccessMethod(String className, boolean bStatic) {
			if (bStatic) {
				return (AccessMethod) accessMethodImplementationsStatic.get(className);
			} else {
				return (AccessMethod) accessMethodImplementations.get(className);
			}
		}
		public void setClosureAccessMethod(AccessMethod m) {
			accessMethodImplementationsClosure.add(m);
		}
		public void setAccessMethod(String className, boolean bStatic, AccessMethod accessMethod) {
			if (bStatic) {
				accessMethodImplementationsStatic.put(className, accessMethod);
			} else {
				accessMethodImplementations.put(className, accessMethod);
			}
		}
		final private HashMap /*String, AccessMethod*/ accessMethodImplementations = new HashMap();
		final private HashMap /*String, AccessMethod*/ accessMethodImplementationsStatic = new HashMap();
		final private Set /* AccessMethod */ accessMethodImplementationsClosure=new HashSet();
		public int getUniqueShadowID() {
			return currentUniqueShadowID++;
		}
		int currentUniqueShadowID;
	}

	public static class State {
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

		final private HashMap /* AdviceApplication,  */ adviceApplications = new HashMap();

		public int getStaticDispatchTypeID(Type type) {
			String name = type.toString();
			if (!staticDispatchTypeIDs.containsKey(name)) {
				staticDispatchTypeIDs.put(name, new Integer(nextStaticTypeDispatchID++));
			}
			return ((Integer) staticDispatchTypeIDs.get(name)).intValue();
		}
		int nextStaticTypeDispatchID = 1; // 0 is a special value
		final HashMap /*String, int*/ staticDispatchTypeIDs = new HashMap();

		final private HashMap /* String, AdviceMethod */ adviceMethods = new HashMap();
		void setAdviceMethod(SootMethod adviceMethod, AdviceMethod m) {
			adviceMethods.put(adviceMethod, m);
		}
		AdviceMethod getAdviceMethod(SootMethod adviceMethod) {
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
				if (adviceMethodInfo.adviceMethodInvokationStmts.contains(old)) {
					adviceMethodInfo.adviceMethodInvokationStmts.remove(old);
					adviceMethodInfo.adviceMethodInvokationStmts.add(bindings.get(old));
					// replace with new
				}
				// this is only necessary if proceed calls are ever part of a shadow,
				// for example if the advice body were to be matched by an adviceexecution pointcut. 
				// TODO: does this kind of thing ever happen?
				// Doesn't matter. Once an advice method has been woven into,
				// the proceeds aren't changed anymore anyways.
				// So we might as well keep all these references updated.
				// (or delete them otherwise).
				for (Iterator it3=adviceMethodInfo.proceedClasses.values().iterator(); it3.hasNext();) {
					AdviceMethod.ProceedLocalClass pl=
						(AdviceMethod.ProceedLocalClass)it3.next();
					
					for (Iterator it0=pl.proceedMethods.iterator();it0.hasNext();) {
						AdviceMethod.ProceedLocalClass.ProceedCallMethod pm=
							(AdviceMethod.ProceedLocalClass.ProceedCallMethod)it0.next();
						
						if (pm.interfaceInvokationStmts.contains(old)) {
							pm.interfaceInvokationStmts.remove(old);
							pm.interfaceInvokationStmts.add(bindings.get(old));
							// replace with new
						}
					}
				}
				if (adviceMethodInfo.directInvokationStmts.contains(old)) {
					adviceMethodInfo.directInvokationStmts.remove(old);
					adviceMethodInfo.directInvokationStmts.add(bindings.get(old));
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