package abc.weaving.weaver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import soot.Body;
import soot.BooleanType;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
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
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.AdviceSpec;
import abc.weaving.aspectinfo.AroundAdvice;
import abc.weaving.aspectinfo.Formal;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.ExecutionAdviceApplication;
import abc.weaving.matching.StmtAdviceApplication;
import abc.weaving.residues.AdviceFormal;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.AndResidue;
import abc.weaving.residues.AspectOf;
import abc.weaving.residues.Bind;
import abc.weaving.residues.Box;
import abc.weaving.residues.CheckType;
import abc.weaving.residues.Copy;
import abc.weaving.residues.HasAspect;
import abc.weaving.residues.IfResidue;
import abc.weaving.residues.Load;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.NotResidue;
import abc.weaving.residues.OrResidue;
import abc.weaving.residues.Residue;

/** Handle around weaving.
 * @author Sascha Kuzins 
 * @date May 6, 2004
 */



public class AroundWeaver {

	private static class InternalError extends RuntimeException {
		InternalError(String message) {
			super("ARD around weaver internal error: " + message);
		}
		InternalError() {super("ARD around weaver internal error");}
	}
	/** set to false to disable debugging messages for Around Weaver */
	public static boolean debugflag = true;


	public static void reset() {
		state=new State();
	}
	private static void debug(String message)
	 { if (abc.main.Debug.v().aroundWeaver) 
		  System.err.println("ARD*** " + message);
	 }

	public static class ObjectBox {
		Object object;
	}

	public static class State {
		/*private static class ClassInterfacePair {
			String className;
			String interfaceName;		
			ClassInterfacePair(String className, String interfaceName) {
				this.className=className;
				this.interfaceName=interfaceName;
			}

			public boolean equals(Object arg0) {
				if (!(arg0 instanceof ClassInterfacePair))
					return false;
				ClassInterfacePair rhs=(ClassInterfacePair)arg0;
				return className.equals(rhs.className) && 
						interfaceName.equals(rhs.interfaceName);
			}
			public int hashCode() {
				return className.hashCode()+interfaceName.hashCode();
			}
		}*/
		public static class AccessMethodInfo {
			//HashMap /*String, Integer*/ fieldIDs=new HashMap();
			
			List targets=new LinkedList();
			List lookupValues=new LinkedList();
			NopStmt defaultTarget;
			NopStmt defaultEnd;
			Stmt lookupStmt;
			int nextID;
			Local idParamLocal;
			Local skipParamLocal;
			//Local targetLocal;
			
			List dynParamLocals=new LinkedList();
			List adviceFormalLocals=new LinkedList();
			
			SootMethod method;
			
			//boolean hasSuperCall=false;
			SootClass superCallTarget=null;
			Stmt superInvokeStmt=null;
			
			/*public boolean implementsField(String field) {
				return fieldIDs.containsKey(field);
			}*/
		}
		//private HashMap /*ClassInterfacePair, AccessMethodInfo*/ 
		//	accessInterfacesImplementations=new HashMap();
		/*public boolean hasInterface(String className, String interfaceName) {
			ClassInterfacePair key=new ClassInterfacePair(className, interfaceName);
			return accessInterfacesGet.containsKey(key);
		}*/
		public AccessMethodInfo getAccessMethodInfo(String className, String interfaceName, boolean bStatic) {
			InterfaceInfo info=getInterfaceInfo(interfaceName);
			return info.getAccessMethodInfo(className, bStatic);			
		}
		public static class InterfaceInfo {
			public Set interfaceInvokationStmts=new HashSet();
			public Set adviceMethodInvokationStmts=new HashSet();
			public Set directInvokationStmts=new HashSet();
			//public Set superInvokationStmts=new HashSet();
			//public HashMap /*InvokeExpr, ValueBox*/ invokationBoxes=new HashMap();
			List /*Type*/ dynamicArguments=new LinkedList();
			
			List[] dynamicArgsByType=new List[Restructure.JavaTypeInfo.typeCount];
			
			InterfaceInfo() {
				for (int i=0; i<dynamicArgsByType.length; i++) {
					dynamicArgsByType[i]=new LinkedList();
				}
			}
			public List getAllAccessMethodImplementations() {
				List result=new LinkedList();
				result.addAll(accessMethodImplementations.values());
				result.addAll(accessMethodImplementationsStatic.values());
				return result;
			}
			public AccessMethodInfo getAccessMethodInfo(String className, boolean bStatic) {
				if (bStatic) {
					if (!accessMethodImplementationsStatic.containsKey(className)) {
						accessMethodImplementationsStatic.put(className, new AccessMethodInfo());
					}
					return (AccessMethodInfo)accessMethodImplementationsStatic.get(className);
				} else {				
					if (!accessMethodImplementations.containsKey(className)) {
						accessMethodImplementations.put(className, new AccessMethodInfo());
					}
					return (AccessMethodInfo)accessMethodImplementations.get(className);
				}
			}
			
			private HashMap /*String, AccessMethodInfo*/ accessMethodImplementations=new HashMap();	
			private HashMap /*String, AccessMethodInfo*/ accessMethodImplementationsStatic=new HashMap();			
		}
		public HashMap /*String, InterfaceInfo*/ interfaces=new HashMap();
		public InterfaceInfo getInterfaceInfo(String interfaceName) {
			if (!interfaces.containsKey(interfaceName)) {
				interfaces.put(interfaceName, new InterfaceInfo());
			}
			return (InterfaceInfo)interfaces.get(interfaceName);
		}
		public int getUniqueID() {
			return currentUniqueID++;
		}
		int currentUniqueID;
		
		public int getUniqueShadowID() {
			return currentUniqueShadowID++;
		}
		int currentUniqueShadowID;
		
		public static class AdviceApplicationInfo {
			
		}
		private HashMap /* AdviceApplication,  */ adviceApplications=new HashMap();
		AdviceApplicationInfo getApplicationInfo(AdviceApplication app) {
			if (!adviceApplications.containsKey(app)) {
				adviceApplications.put(app, new AdviceApplicationInfo());
			}
			return (AdviceApplicationInfo)adviceApplications.get(app);
		}
		public int getStaticDispatchTypeID(Type type) {
			String name=type.toString();
			if (!staticDispatchTypeIDs.containsKey(name)) {
				staticDispatchTypeIDs.put(name, new Integer(nextStaticTypeDispatchID++));				
			}
			return ((Integer)staticDispatchTypeIDs.get(name)).intValue();
		}
		int nextStaticTypeDispatchID=1; // 0 is special value
		HashMap /*String, int*/ staticDispatchTypeIDs=new HashMap();
		
		public static class AdviceMethodInfo {
			public List originalAdviceFormals=new LinkedList();
			public List proceedParameters=new LinkedList();
			public Local interfaceLocal;
			//public Local targetLocal;
			public Local idLocal;
			public Local staticDispatchLocal;
			public HashSet /*String*/ staticProceedTypes=new HashSet();
			public boolean hasDynamicProceed=false;
			public boolean bAllwaysStaticAccessMethod=false;
			
			public static class ProceedInvokation {
				public Local lhs;
				public NopStmt begin;
				public NopStmt end;	
				
				
				//List lookupValues=new LinkedList();
				List defaultTargetStmts;
				//Stmt lookupStmt;
				List staticInvokes=new LinkedList();
				List staticLookupValues=new LinkedList();
				
				Stmt dynamicInvoke;
				
				List originalActuals=new LinkedList();
			}			  
			private List proceedInvokations=new LinkedList();
		}
		private HashMap /* String, AdviceMethodInfo */ adviceMethods=new HashMap();
		AdviceMethodInfo getAdviceMethodInfo(String aspectName, String adviceMethodName) {
			String key=aspectName + "-" + adviceMethodName;
			if (!adviceMethods.containsKey(key)) {
				adviceMethods.put(key, new AdviceMethodInfo());
			}
			return (AdviceMethodInfo)adviceMethods.get(key);
		}
	}
	public static State state=new State();


	public static void doWeave(
					SootClass joinpointClass,
					SootMethod joinpointMethod,
					LocalGeneratorEx localgen,
					AdviceApplication adviceAppl) {
		debug("Handling aound: " + adviceAppl);
		//if (joinpointClass!=null)
		//	return;
			
		Body joinpointBody = joinpointMethod.getActiveBody();
		joinpointBody.validate();
		
		
		
		
		Chain joinpointStatements = joinpointBody.getUnits().getNonPatchingChain();
		// Around weaver only supports "proper" advice, at least for now
		AdviceDecl adviceDecl = (AdviceDecl) adviceAppl.advice;
		AdviceSpec adviceSpec = adviceDecl.getAdviceSpec();
		AroundAdvice aroundSpec = (AroundAdvice) adviceSpec;
		SootClass theAspect =
			adviceDecl.getAspect().getInstanceClass().getSootClass();
		SootMethod adviceMethod = adviceDecl.getImpl().getSootMethod();
		adviceMethod.getActiveBody().validate();
	
		Type adviceReturnType=adviceMethod.getReturnType();
	
		State.AdviceMethodInfo adviceMethodInfo=state.getAdviceMethodInfo(theAspect.getName(), adviceMethod.getName());
		boolean bStatic=joinpointMethod.isStatic();
		boolean bAllwaysStaticAccessMethod=adviceMethodInfo.bAllwaysStaticAccessMethod;
	
			
		String adviceReturnTypeName=
				adviceReturnType.toString();// .getClass().getName();
		String adviceMangledReturnTypeName= // TODO: proper mangling!
			mangleTypeName(adviceReturnTypeName);
		//String accessTypeString= bGet ? "get" : "set";
		
		String aspectName=theAspect.getName();
		String mangledAspectName=mangleTypeName(aspectName);
		
		final boolean interfacePerAdviceMethod=true;
		String adviceMethodIdentifierString=mangledAspectName + "$" + adviceMethod.getName();
		
		String interfaceName="abc$access$" +  
			(interfacePerAdviceMethod ? adviceMethodIdentifierString : adviceMangledReturnTypeName);
		
		String accessMethodName;
		String dynamicAccessMethodName="abc$proceed$" + adviceMangledReturnTypeName+ 
			(interfacePerAdviceMethod ? adviceMethodIdentifierString : "");;	
		if (bStatic || bAllwaysStaticAccessMethod) {
			accessMethodName="abc$static$proceed$" + adviceMangledReturnTypeName+ 
						(interfacePerAdviceMethod ? adviceMethodIdentifierString : "");	
		} else {
			accessMethodName=dynamicAccessMethodName;
		}
		
		SootClass accessInterface=null;
	
		{
			boolean bFirstModification=
							adviceMethod.getParameterCount()==adviceDecl.numFormals();
			if (bFirstModification) {
				if (!adviceMethodInfo.originalAdviceFormals.isEmpty())
					throw new InternalError();
				
				Iterator it=adviceDecl.getImpl().getFormals().iterator();
				while (it.hasNext()) {
					Formal formal=(Formal)it.next();
					adviceMethodInfo.originalAdviceFormals.add(formal.getType().getSootType());
					//formal.
				}
				// TODO: clean up the following 7 lines
				int size=adviceMethodInfo.originalAdviceFormals.size();
				if (adviceDecl.hasEnclosingJoinPoint()) 
					adviceMethodInfo.originalAdviceFormals.remove(--size); 
				if (adviceDecl.hasJoinPoint()) 
					adviceMethodInfo.originalAdviceFormals.remove(--size); 
				if (adviceDecl.hasJoinPointStaticPart())
					adviceMethodInfo.originalAdviceFormals.remove(--size); 
						
				//adviceMethodInfo.originalAdviceFormals.addAll(adviceMethod.getParameterTypes());
			}
		}
	
	
		List /*type*/ accessMethodParameters=new LinkedList();
		// accessMethodParameters.add(Scene.v().getSootClass("java.lang.Object").getType()); // target
		accessMethodParameters.add(IntType.v()); // the shadow id
		accessMethodParameters.add(BooleanType.v()); // the skip flag
		
		{
			List allAccessMethodParameters=new LinkedList();
			allAccessMethodParameters.addAll(adviceMethodInfo.originalAdviceFormals);
			allAccessMethodParameters.addAll(accessMethodParameters);
			
			accessInterface =
				createAccessInterface(
					adviceReturnType,
					interfaceName,
					dynamicAccessMethodName,
					allAccessMethodParameters);
		}		
		SootMethod abstractAccessMethod=accessInterface.getMethodByName(dynamicAccessMethodName);
		
		
		SootMethod accessMethod=
			generateAccessMethod(
				joinpointClass,
				bStatic,
				bAllwaysStaticAccessMethod,
				adviceReturnType,
				accessMethodName,
				accessInterface,
				adviceMethodInfo.originalAdviceFormals,
				accessMethodParameters);
		
		// Change advice method: add parameters and replace proceed with call to get-interface
		{
			boolean bFirstModification=
				adviceMethod.getParameterCount()==adviceDecl.numFormals();
			if (bFirstModification) {
				doInitialAdviceMethodModification(adviceAppl, adviceMethod, accessInterface, abstractAccessMethod, 
					accessMethodName, joinpointClass,
					theAspect
					);
			}			
		}
		implementInterface(
			theAspect,
			joinpointMethod,
			adviceAppl,
			 joinpointClass,
			 adviceReturnType,
			 accessInterface,
			 accessMethod,
			 abstractAccessMethod,
			 bAllwaysStaticAccessMethod
			 );
			 
		joinpointBody.validate();
		accessMethod.getActiveBody().validate();
	}


	private static SootMethod generateAccessMethod(
		SootClass joinpointClass,
		boolean bStatic,
		boolean bAllwaysStaticAccessMethod,
		Type adviceReturnType,
		String accessMethodName,
		SootClass accessInterface,
		List originalAdviceFormals,
		List accessMethodParameters) {
		
		
		//=abstractAccessMethod.getParameterTypes();
		
		String interfaceName=accessInterface.getName();
			
		State.AccessMethodInfo accessMethodInfo=state.getAccessMethodInfo(
			joinpointClass.getName(), interfaceName, bStatic || bAllwaysStaticAccessMethod);
		
		
		SootMethod accessMethod=null;
	
		
		boolean createdNewAccessMethod=false;		 
		 if (bStatic || bAllwaysStaticAccessMethod) {
		 	try {
				accessMethod=joinpointClass.getMethodByName(accessMethodName);
		 	}
		 	catch (Exception ex) {
//				create new method					
				 accessMethod=new SootMethod(
					 accessMethodName, new LinkedList(), adviceReturnType ,
						 Modifier.PUBLIC | Modifier.STATIC);
						 
				createdNewAccessMethod=true;
		 	}
		 	
		 } else {
			if (joinpointClass.implementsInterface(interfaceName)){
				debug("found interface " + interfaceName + " in class: " + joinpointClass.getName());
				accessMethod=joinpointClass.getMethodByName(accessMethodName);
			} else {
				debug("adding interface " + interfaceName + " to class " + joinpointClass.getName());
				
				joinpointClass.addInterface(accessInterface);
			
				// create new method					
				accessMethod=new SootMethod(
					accessMethodName, new LinkedList(), adviceReturnType ,
						Modifier.PUBLIC);
		
				createdNewAccessMethod=true;	
						
			}
		 }
		if (createdNewAccessMethod) {
			Body accessBody=Jimple.v().newBody(accessMethod);
		
			accessMethod.setActiveBody(accessBody);
			debug("adding method " + accessMethod.getName() + " to class " + joinpointClass.getName());
			joinpointClass.addMethod(accessMethod);

			accessMethodInfo.method=accessMethod;

			Chain accessStatements=accessBody.getUnits().getNonPatchingChain();

			// generate this := @this
			LocalGeneratorEx lg=new LocalGeneratorEx(accessBody);
			Local lThis=null;
			if (!bStatic && !bAllwaysStaticAccessMethod) {
				lThis=lg.generateLocal(joinpointClass.getType(), "this");
				accessStatements.addFirst(
					Jimple.v().newIdentityStmt(lThis, 
						Jimple.v().newThisRef(
							RefType.v(joinpointClass))));
			}
			Restructure.validateMethod(accessMethod);
			//accessMethodInfo.targetLocal=Restructure.addParameterToMethod(
			//	accessMethod, (Type)accessMethodParameters.get(0), "targetArg");
			
			{
				Iterator it=originalAdviceFormals.iterator();
				while (it.hasNext()) {
					Type type=(Type)it.next();
					Local l=Restructure.addParameterToMethod(
						accessMethod, type, "orgAdviceFormal");
					accessMethodInfo.adviceFormalLocals.add(l);
				}				
			}
			Restructure.validateMethod(accessMethod);
			
			accessMethodInfo.idParamLocal=Restructure.addParameterToMethod(
				accessMethod, (Type)accessMethodParameters.get(0), "shadowID");
			accessMethodInfo.skipParamLocal=Restructure.addParameterToMethod(
							accessMethod, (Type)accessMethodParameters.get(1), "skipAdvice");
							
			if (accessMethodParameters.size()!=2)
				throw new InternalError();
			
			Stmt lastIDStmt=Restructure.getParameterIdentityStatement(accessMethod, 
				accessMethod.getParameterCount()-1);
			
			// generate exception code (default target)
			SootClass exception=Scene.v().getSootClass("java.lang.RuntimeException");	
			Local ex=lg.generateLocal(exception.getType(), "exception");
			Stmt newExceptStmt = Jimple.v().newAssignStmt( ex, Jimple.v().newNewExpr( exception.getType() ) );
			Stmt initEx=Jimple.v().newInvokeStmt( Jimple.v().newSpecialInvokeExpr( ex, exception.getMethod( "<init>", new ArrayList()))) ;
			Stmt throwStmt=Jimple.v().newThrowStmt(ex);
			
			accessMethodInfo.defaultTarget=Jimple.v().newNopStmt();
			accessStatements.add(accessMethodInfo.defaultTarget);			
			accessStatements.add(newExceptStmt);
			accessStatements.add(initEx);
			accessStatements.add(throwStmt);
			accessMethodInfo.defaultEnd=Jimple.v().newNopStmt();
			accessStatements.add(accessMethodInfo.defaultEnd);	
			

			// just generate a nop for now.
			accessMethodInfo.lookupStmt=Jimple.v().newNopStmt();

			accessStatements.insertAfter(accessMethodInfo.lookupStmt, lastIDStmt);
			
			State.InterfaceInfo interfaceInfo=state.getInterfaceInfo(interfaceName);
			Iterator it=interfaceInfo.dynamicArguments.iterator(); 
			while (it.hasNext()) {
				Type type=(Type)it.next();
				Local l=Restructure.addParameterToMethod(accessMethod, type, "dynArgFormal");
				accessMethodInfo.dynParamLocals.add(l);
			}
			
			if (!bStatic && !bAllwaysStaticAccessMethod) {
				fixAccessMethodSuperCalls(interfaceName, joinpointClass);
			}
		}	
		Restructure.validateMethod(accessMethod);
		return accessMethod;
	}


	private static SootClass createAccessInterface(
		Type adviceReturnType,
		String interfaceName,
		String dynamicAccessMethodName,
		List accessMethodParameters) {
		SootClass accessInterface;
		// create access interface if it doesn't exist
		if (Scene.v().containsClass(interfaceName)) {
			debug("found access interface in scene");
			accessInterface=Scene.v().getSootClass(interfaceName);
			//abstractAccessMethod=accessInterface.getMethodByName(dynamicAccessMethodName);
		} else {
			debug("generating access interface type");

			
			accessInterface=new SootClass(interfaceName, 
				Modifier.INTERFACE | Modifier.PUBLIC);						
			
			accessInterface.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
			
			SootMethod abstractAccessMethod=
				new SootMethod(dynamicAccessMethodName, accessMethodParameters, 
						adviceReturnType,  
						Modifier.ABSTRACT | 
						Modifier.PUBLIC);
			
			accessInterface.addMethod(abstractAccessMethod);
			//signature.setActiveBody(Jimple.v().newBody(signature));
			
			
			Scene.v().addClass(accessInterface);
			accessInterface.setApplicationClass();
			
			//GlobalAspectInfo.v().getGeneratedClasses().add(interfaceName);						 
		}
		return accessInterface;
	}
	
	private static void generateProceedCalls(AdviceApplication adviceAppl,
					SootMethod adviceMethod,
					SootClass accessInterface,
					SootMethod abstractAccessMethod,
					Type returnType,
					boolean bStatic, 
					boolean bAllwaysStaticAccessMethod,
					String accessMethodName, 
					SootClass joinpointClass,
					SootClass theAspect,
					SootMethod accessMethod,
					String interfaceName) {
		
		State.AdviceMethodInfo adviceMethodInfo=state.getAdviceMethodInfo(theAspect.getName(), adviceMethod.getName());
		State.InterfaceInfo interfaceInfo=state.getInterfaceInfo(interfaceName);
		
		
		String newStaticInvoke=null;
		if (bStatic || bAllwaysStaticAccessMethod) {
			if (!adviceMethodInfo.staticProceedTypes.contains(joinpointClass.getName())) {
				newStaticInvoke=joinpointClass.getName();
				adviceMethodInfo.staticProceedTypes.add(joinpointClass.getName());
			} else return;
		} else {
			if (adviceMethodInfo.hasDynamicProceed)
				return;
			else
				adviceMethodInfo.hasDynamicProceed=true;	
		}
		
		
		Body body=adviceMethod.getActiveBody();
		Chain statements=body.getUnits().getNonPatchingChain();
		
		Iterator it=adviceMethodInfo.proceedInvokations.iterator();
		while (it.hasNext()) {
			State.AdviceMethodInfo.ProceedInvokation invokation=
				(State.AdviceMethodInfo.ProceedInvokation)it.next();
			
			removeStatements(body, invokation.begin, invokation.end, null);
			
			
			List parameters=new LinkedList();
			parameters.addAll(invokation.originalActuals);
			parameters.addAll(adviceMethodInfo.proceedParameters);
			if (invokation.dynamicInvoke==null && adviceMethodInfo.hasDynamicProceed){
				InvokeExpr newInvokeExpr=
							Jimple.v().newInterfaceInvokeExpr( 
								adviceMethodInfo.interfaceLocal, abstractAccessMethod, 
									parameters);
				Stmt s;
				if (invokation.lhs==null) {
					s=Jimple.v().newInvokeStmt(newInvokeExpr);
				} else {
					s=Jimple.v().newAssignStmt(invokation.lhs, newInvokeExpr);
				}
				invokation.dynamicInvoke=s;
				interfaceInfo.interfaceInvokationStmts.add(s);		
			}

			//List staticInvokes=new LinkedList();
			//List targets=new LinkedList();
			//Iterator it2=info.staticProceedTypes.iterator();
			if (newStaticInvoke!=null) {
				SootClass cl=Scene.v().getSootClass(newStaticInvoke);
				SootMethod m=cl.getMethodByName(accessMethodName);
				
				invokation.staticLookupValues.add(IntConstant.v(state.getStaticDispatchTypeID(cl.getType())));
				
				InvokeExpr newInvokeExpr=
							Jimple.v().newStaticInvokeExpr( 
								m , parameters);
				Stmt s;
				if (invokation.lhs==null) {
					s=Jimple.v().newInvokeStmt(newInvokeExpr);
				} else {
					s=Jimple.v().newAssignStmt(invokation.lhs, newInvokeExpr);
				}
				invokation.staticInvokes.add(s);
				interfaceInfo.interfaceInvokationStmts.add(s);
			}
			if (invokation.defaultTargetStmts==null) {
//				generate exception code (default target)
				invokation.defaultTargetStmts=new LinkedList();
				LocalGeneratorEx lg=new LocalGeneratorEx(adviceMethod.getActiveBody());
				 SootClass exception=Scene.v().getSootClass("java.lang.RuntimeException");	
				 Local ex=lg.generateLocal(exception.getType(), "exception");
				 Stmt newExceptStmt = Jimple.v().newAssignStmt( ex, Jimple.v().newNewExpr( exception.getType() ) );
				 Stmt initEx=Jimple.v().newInvokeStmt( Jimple.v().newSpecialInvokeExpr( ex, exception.getMethod( "<init>", new ArrayList()))) ;
				 Stmt throwStmt=Jimple.v().newThrowStmt(ex);
				invokation.defaultTargetStmts.add(newExceptStmt);
				invokation.defaultTargetStmts.add(initEx);
				invokation.defaultTargetStmts.add(throwStmt);	
			}
			
			if (adviceMethodInfo.staticProceedTypes.isEmpty()) {
				statements.insertAfter(invokation.dynamicInvoke, invokation.begin);					
			} else if (adviceMethodInfo.hasDynamicProceed==false && adviceMethodInfo.staticProceedTypes.size()==1) {
				statements.insertAfter(invokation.staticInvokes.get(0), invokation.begin);
			} else {
				List targets=new LinkedList();
				List lookupValues=new LinkedList();
				if (invokation.dynamicInvoke!=null) {
					targets.add(invokation.dynamicInvoke);
					lookupValues.add(IntConstant.v(0));	
				}
				targets.addAll(invokation.staticInvokes);
				lookupValues.addAll(invokation.staticLookupValues);
				 
				Local key=adviceMethodInfo.staticDispatchLocal; ///
				LookupSwitchStmt lookupStmt=
					Jimple.v().newLookupSwitchStmt(key, lookupValues, targets, (Unit)invokation.defaultTargetStmts.get(0));
				
				statements.insertBefore(lookupStmt, invokation.end);
				if (invokation.dynamicInvoke!=null) {
					statements.insertBefore(invokation.dynamicInvoke, invokation.end);
					statements.insertBefore(Jimple.v().newGotoStmt(invokation.end), invokation.end);
				}
				
				Iterator it2=invokation.staticInvokes.iterator();
				while (it2.hasNext()) {
					Stmt stmt=(Stmt)it2.next();
					statements.insertBefore(stmt, invokation.end);
					statements.insertBefore(Jimple.v().newGotoStmt(invokation.end), invokation.end);
				}
				it2=invokation.defaultTargetStmts.iterator();
				while (it2.hasNext()) {
					Stmt stmt=(Stmt)it2.next();
					statements.insertBefore(stmt, invokation.end);
				}
				// just in case:
				statements.insertBefore(Jimple.v().newGotoStmt(invokation.end), invokation.end);
			}
		}
	}
	
	private static void insertCast(Body body, Stmt stmt, ValueBox source, Type targetType) {
		Chain units=body.getUnits().getNonPatchingChain();
		if (!source.getValue().getType().equals(targetType)) {
			LocalGeneratorEx localgen=new LocalGeneratorEx(body);
			Local castLocal=localgen.generateLocal(source.getValue().getType(), "castTmp");
			debug("cast: source has type " + source.getValue().getType().toString());
			debug("cast: target has type " + targetType.toString());
			AssignStmt tmpStmt=Jimple.v().newAssignStmt(castLocal, source.getValue());
			CastExpr castExpr=Jimple.v().newCastExpr(castLocal,targetType);
		//	Jimple.v().newCastExpr()
			units.insertBefore(tmpStmt, stmt);
			if (stmt instanceof AssignStmt) {
				source.setValue(castExpr);
			} else {
				Local tmpLocal=localgen.generateLocal(targetType, "castTarget");
				AssignStmt tmpStmt2=Jimple.v().newAssignStmt(tmpLocal, castExpr);
				units.insertBefore(tmpStmt2, stmt);
				source.setValue(tmpLocal);
			}
		} 			
	}
	
	/*private static ValueBox getInvokeExprBox(Stmt stmt) {
		stmt.getInvokeExprBox()
		if (stmt instanceof AssignStmt)
			return ((AssignStmt)stmt).getInvokeExprBox()
			
	}*/
	/**
	 * Creates a new InvokeExpr based on an existing one but with new arguments.
	 */
	private static InvokeExpr createNewInvokeExpr(InvokeExpr old, List newArgs) {
		if (old instanceof InstanceInvokeExpr) {
			Local base=(Local)((InstanceInvokeExpr)old).getBase();
			if (old instanceof InterfaceInvokeExpr)
				return Jimple.v().newInterfaceInvokeExpr(base, old.getMethod(), newArgs);
			else if (old instanceof SpecialInvokeExpr) {
				return Jimple.v().newSpecialInvokeExpr(base, old.getMethod(), newArgs);
			} else if (old instanceof VirtualInvokeExpr)
				return Jimple.v().newVirtualInvokeExpr(base, old.getMethod(), newArgs);
			else
				throw new InternalError();
		}  else {
			return Jimple.v().newStaticInvokeExpr(old.getMethod(), newArgs);
		}
	}
	
	private static void implementInterface(
		SootClass theAspect,
		SootMethod joinpointMethod,
		AdviceApplication adviceAppl,
		SootClass joinpointClass,
		Type accessReturnType,
		SootClass accessInterface,
		SootMethod accessMethod,
		SootMethod abstractAccessMethod,
		boolean bAllwaysStaticAccessMethod) {

	        AdviceDecl adviceDecl=(AdviceDecl) adviceAppl.advice;
		
		String interfaceName=accessInterface.getName();
		String accessMethodName=accessMethod.getName();
		
		Body accessBody=accessMethod.getActiveBody();
		Chain accessStatements=accessBody.getUnits().getNonPatchingChain();
		
		boolean bStatic=joinpointMethod.isStatic();
	
		Chain joinpointStatements=joinpointMethod.getActiveBody().getUnits().getNonPatchingChain();
		
		SootMethod adviceMethod=adviceDecl.getImpl().getSootMethod();
		if (adviceMethod==null)
			throw new InternalError();
		
		State.AccessMethodInfo accessMethodInfo=
			state.getAccessMethodInfo(joinpointClass.getName(), interfaceName, bStatic || bAllwaysStaticAccessMethod);
		State.InterfaceInfo interfaceInfo=state.getInterfaceInfo(interfaceName);
		
		Body joinpointBody=joinpointMethod.getActiveBody();
		Chain joinpointChain=joinpointBody.getUnits().getNonPatchingChain();		
		
		Stmt begin=adviceAppl.shadowpoints.getBegin();
		Stmt end=adviceAppl.shadowpoints.getEnd();
		
		
		StmtAdviceApplication stmtAppl=null;
		if (adviceAppl instanceof StmtAdviceApplication) {
			stmtAppl=(StmtAdviceApplication)adviceAppl;
			//if (!joinpointChain.contains(stmtAppl.stmt))
			//	throw new InternalError(); /// This will actually happen	
		}
		// find returned local
		Local returnedLocal=null;
		if (adviceAppl instanceof ExecutionAdviceApplication) {
			ExecutionAdviceApplication ea=(ExecutionAdviceApplication)adviceAppl;
			if (joinpointMethod.getName().startsWith("around$"))
				throw new CodeGenException("Execution pointcut matching advice method.");
				
			if (!bStatic) {
				Local lThisCopy=Restructure.getThisCopy(joinpointMethod);
				Stmt succ=(Stmt)joinpointStatements.getSuccOf(begin);
				if (succ instanceof AssignStmt) {
					AssignStmt s=(AssignStmt)succ;
					if (s.getLeftOp()==lThisCopy) {
						debug("moving 'thisCopy=this' out of execution shadow."); // TODO: fix thisCopy strategy.
						joinpointStatements.remove(s);
						joinpointStatements.insertBefore(s, begin);				
					}
				}
			}
			if (joinpointMethod.getReturnType().equals(VoidType.v())) {
				
			} else {
				ReturnStmt returnStmt;
				try {
					returnStmt=(ReturnStmt)joinpointStatements.getSuccOf(end);
				} catch (Exception ex) {
					throw new CodeGenException(
							"Expecting return statement after shadow " +								"for execution advice in non-void method");
				}
				returnedLocal=(Local)returnStmt.getOp(); // TODO: could return constant?
			}
		} else if (stmtAppl.stmt instanceof AssignStmt) {
			AssignStmt assignStmt=(AssignStmt)stmtAppl.stmt;
			Value leftOp=assignStmt.getLeftOp();
			Value rightOp=assignStmt.getRightOp();
			if (leftOp instanceof Local) {
				returnedLocal=(Local) leftOp;				
			} else if (leftOp instanceof FieldRef && rightOp instanceof Local) {
			} else if (leftOp instanceof FieldRef && rightOp instanceof Constant) {
			
			} else {
				// unexpected statement type
				throw new InternalError();
			}
		} else if (stmtAppl.stmt instanceof InvokeStmt) {
			
		} else {
			// unexpected statement type
			throw new InternalError();		
		}
		
		List /*ValueBox*/ actuals=new LinkedList();
		actuals.addAll(findLocalsGoingIn(joinpointBody, begin, end));
		{ // print debug information
			debug("Locals going in: ");
			debug(" Method + " + joinpointMethod.toString());
			debug(" Application: " + adviceAppl.toString());
			//debug("Method + " + joinpointMethod.toString());
			Iterator it=actuals.iterator();
			while (it.hasNext()) {
				Local l=(Local) it.next();
				debug(" " + l.toString());
			}
		}
		
		NopStmt beforeEndShadow=Jimple.v().newNopStmt();
		{
			joinpointStatements.insertBefore(beforeEndShadow, end);
			end.redirectJumpsToThisTo(beforeEndShadow);
		}
		
		validateShadow(joinpointBody, begin, end);
		
//		determine parameter mappings and necessary additions
		List /*Type*/ addedDynArgsTypes=new LinkedList();
		int[] argIndex=new int[actuals.size()];
		{
			int[] currentIndex=new int[Restructure.JavaTypeInfo.typeCount];
			Iterator it=actuals.iterator();
			int i=0;
			while (it.hasNext()) {
				Local local=(Local)it.next();
				 Type type=local.getType();
				 // pass all reference types as java.lang.Object
				 if (Restructure.JavaTypeInfo.sootTypeToInt(type)==Restructure.JavaTypeInfo.refType) {
				 	type=Scene.v().getRefType("java.lang.Object");
				 	if (type==null)
				 		throw new InternalError();
				 }
				 int typeNum=Restructure.JavaTypeInfo.sootTypeToInt(type);
				 if (currentIndex[typeNum]<interfaceInfo.dynamicArgsByType[typeNum].size()) {
				 	Integer dynArgID=(Integer)interfaceInfo.dynamicArgsByType[typeNum].get(currentIndex[typeNum]);
				 	++currentIndex[typeNum];
				 	argIndex[i]=dynArgID.intValue();
				 } else {
				 	addedDynArgsTypes.add(type);
				 	interfaceInfo.dynamicArguments.add(type);
				 	int newIndex=interfaceInfo.dynamicArguments.size()-1;
				 	interfaceInfo.dynamicArgsByType[typeNum].add(new Integer(newIndex));
				 	argIndex[i]=newIndex;
					++currentIndex[typeNum];
				 }
				 i++;
			}
		}
		
 		
 		List dynamicActuals=new LinkedList();
		{ // create list of dynamic actuals to add (including default values)
			Value[] parameters=new Value[interfaceInfo.dynamicArguments.size()];
	
			for (int i=0; i<argIndex.length; i++) {
				parameters[argIndex[i]]=(Local)actuals.get(i);	
			}
			for (int i=0; i<parameters.length; i++) {
				if (parameters[i]==null) {
					parameters[i]=Restructure.JavaTypeInfo.getDefaultValue((Type)interfaceInfo.dynamicArguments.get(i));	
				}	
				dynamicActuals.add(parameters[i]);
			}			
		}
		
		// create list of default values for the added arguments
		// (for invokations at other locations)
		List addedDynArgs=getDefaultValues(addedDynArgsTypes);
		

		{ // modify the interface definition
			SootMethod m=abstractAccessMethod;
			List p=m.getParameterTypes();
			p.addAll(addedDynArgsTypes);
			m.setParameterTypes(p);
		}
		{ // modify all existing advice method invokations by adding the default parameters
			Iterator it=interfaceInfo.adviceMethodInvokationStmts.iterator();
			while (it.hasNext()) {
				Stmt stmt=(Stmt)it.next();
				//addEmptyDynamicParameters(method, addedDynArgs, accessMethodName);
				InvokeExpr invoke=(InvokeExpr)stmt.getInvokeExprBox().getValue();
				List newParams=invoke.getArgs();
				newParams.addAll(addedDynArgs); /// should we do deep copy?	
				InvokeExpr newInvoke=createNewInvokeExpr(invoke, newParams);
				stmt.getInvokeExprBox().setValue(newInvoke);						
			}
		}
		{ // modify all existing direct interface invokations by adding the default parameters
			Iterator it=interfaceInfo.directInvokationStmts.iterator();
			while (it.hasNext()) {
				Stmt stmt=(Stmt)it.next();
				//addEmptyDynamicParameters(method, addedDynArgs, accessMethodName);
				InvokeExpr invoke=(InvokeExpr)stmt.getInvokeExprBox().getValue();
				List newParams=invoke.getArgs();
				newParams.addAll(addedDynArgs); /// should we do deep copy?	
				InvokeExpr newInvoke=createNewInvokeExpr(invoke, newParams);
				stmt.getInvokeExprBox().setValue(newInvoke);						
			}
		}
		
		Body adviceBody=adviceMethod.getActiveBody();
		Chain adviceStatements=adviceBody.getUnits().getNonPatchingChain();
		List addedAdviceParameterLocals=new LinkedList();
		{ // Add the new parameters to the advice method 
		  // and keep track of the newly created locals corresponding to the parameters.
		  	//validateMethod(adviceMethod);
			 List params=adviceMethod.getParameterTypes();
			 Iterator it=addedDynArgsTypes.iterator();
			 while (it.hasNext()) {
			 	Type type=(Type)it.next();
			 	Local l=Restructure.addParameterToMethod(adviceMethod, type, "dynArgFormal");
			 	addedAdviceParameterLocals.add(l);
			 }
		}
		{
			generateProceedCalls(adviceAppl, adviceMethod, accessInterface, abstractAccessMethod, 
									adviceMethod.getReturnType(), bStatic, bAllwaysStaticAccessMethod, accessMethodName, joinpointClass,
									theAspect, accessMethod, interfaceName);	
		}

		{ // Modify the interface invokations. These must all be in the advice method.
			// This constraint is violated by adviceexecution() pointcuts.
			Iterator it=interfaceInfo.interfaceInvokationStmts.iterator();
			while (it.hasNext()) {
				Stmt stmt=(Stmt)it.next();
				if (!adviceStatements.contains(stmt))
					throw new InternalError();
				//addEmptyDynamicParameters(method, addedDynArgsTypes, accessMethodName);
				//stmt.
				InvokeExpr intfInvoke=stmt.getInvokeExpr();
				List params=intfInvoke.getArgs();
				Iterator it2=addedAdviceParameterLocals.iterator();
				while (it2.hasNext()) {
					Local l=(Local)it2.next();
					params.add(l);
				}
				InvokeExpr newInvoke=createNewInvokeExpr(intfInvoke, params);
				debug("newInvoke: " + newInvoke);
				stmt.getInvokeExprBox().setValue(newInvoke);
				debug("newInvoke2" + stmt.getInvokeExpr());
			}
			
			State.AdviceMethodInfo adviceMethodInfo=
					state.getAdviceMethodInfo(theAspect.getName(), adviceMethod.getName());
			
			adviceMethodInfo.proceedParameters.addAll(addedAdviceParameterLocals);
		}
		
		adviceBody.validate();
		
		// add parameters to all access method implementations
		{
			//Set keys=interfaceInfo.accessMethodImplementations.keySet();
			List accessMethodImplementations=interfaceInfo.getAllAccessMethodImplementations();
			Iterator it=accessMethodImplementations.iterator();
			while (it.hasNext()) {
				State.AccessMethodInfo info=
					(State.AccessMethodInfo) it.next();
				
				debug("adding parameters to " + info.method);
				Restructure.validateMethod(info.method);
				
				Iterator it2=addedDynArgsTypes.iterator();
				while (it2.hasNext()) {			
					Type type=(Type)it2.next();	
					Local l=Restructure.addParameterToMethod(info.method, type, "dynArgFormal");
					info.dynParamLocals.add(l);
				}	
		
//				modify existing super call in the access method		
				Stmt stmt=info.superInvokeStmt;
				if (stmt!=null) {
					//addEmptyDynamicParameters(method, addedDynArgs, accessMethodName);
					InvokeExpr invoke=(InvokeExpr)stmt.getInvokeExprBox().getValue();
					List newParams=new LinkedList();
					newParams.addAll(getParameterLocals(info.method.getActiveBody())); /// should we do deep copy?	
					InvokeExpr newInvoke=createNewInvokeExpr(invoke, newParams);
					stmt.getInvokeExprBox().setValue(newInvoke);		
				}		
			}
		}
		
		// copy shadow into access method with a return returning the relevant local.
		Stmt first;
		HashMap bindings;
		Stmt switchTarget;
		{
			ObjectBox result=new ObjectBox();
			bindings=copyStmtSequence(joinpointBody, begin, end, accessBody, 
				accessMethodInfo.lookupStmt, returnedLocal, result);
			first=(Stmt)result.object;
			switchTarget=Jimple.v().newNopStmt();
			accessStatements.insertBefore(switchTarget, first);
		}
		updateSavedReferencesToStatements(bindings);
		
		{
			// remove any traps from the shadow before removing the shadow
			removeTraps(joinpointBody, begin, end);
			// remove statements except original assignment
			removeStatements(joinpointBody, begin, end, null);
			if (stmtAppl!=null) {
				stmtAppl.stmt=null; /// just for sanity, because we deleted that stmt
			}
		}
		
		Local lThis=null;
		if (!bStatic)
			lThis=Restructure.getThisCopy(joinpointMethod); //joinpointBody.getThisLocal();
		//lThis.setName("this");
		
		
		int shadowID;		
		{ // determine shadow ID
			if (bStatic || bAllwaysStaticAccessMethod) {
				shadowID=accessMethodInfo.nextID++;
			} else {
				shadowID=state.getUniqueShadowID();	
			}
		}
		
		WeavingContext wc=PointcutCodeGen.makeWeavingContext(adviceAppl);
		LocalGeneratorEx localgen=new LocalGeneratorEx(joinpointBody);
		Stmt beforeFailPoint=Jimple.v().newNopStmt();
		Stmt failPoint = Jimple.v().newNopStmt();
		Stmt endResidue;
		Vector staticBindings=getStaticBinding(adviceAppl, wc);
		verifyBindings(staticBindings);
		
		{
			// weave in dynamic residue
			
//			find location to weave in statements, 
			Stmt beginshadow = begin;
			Stmt endshadow=end;			
			
			joinpointStatements.insertBefore(failPoint,endshadow);
			joinpointStatements.insertBefore(beforeFailPoint, failPoint);
			
		
			// weave in residue
			endResidue=adviceAppl.residue.codeGen
				(joinpointMethod,localgen,joinpointStatements,beginshadow,failPoint,wc);
			
			AdviceWeavingContext awc=(AdviceWeavingContext)wc;
			{
				Iterator it=awc.arglist.iterator();
				while (it.hasNext()) {
					Local l=(Local)it.next();
					//actuals.contain)
				}
			}
			
			if (!(adviceAppl.residue instanceof AlwaysMatch)) {				
				InvokeExpr directInvoke;
				List directParams=new LinkedList();
				//directParams.add(targetLocal);
				State.AdviceMethodInfo adviceInfo=state.getAdviceMethodInfo(theAspect.getName(), adviceMethod.getName());
				List defaultValues=getDefaultValues(adviceInfo.originalAdviceFormals);
				directParams.addAll(defaultValues);
				directParams.add(IntConstant.v(shadowID));
				directParams.add(IntConstant.v(1)); // skipAdvice parameter
				directParams.addAll(dynamicActuals);
				if (bStatic || bAllwaysStaticAccessMethod) {
					directInvoke=Jimple.v().newStaticInvokeExpr(accessMethod, directParams);
				} else {
					// TODO: can this call be replaced with an InvokeSpecial?
					directInvoke=Jimple.v().newInterfaceInvokeExpr(
						Restructure.getThisCopy(joinpointMethod), abstractAccessMethod, directParams);
				}
				{				
					Stmt skipAdvice;
					if (returnedLocal!=null) {				
						AssignStmt assign=Jimple.v().newAssignStmt(returnedLocal, directInvoke);
						joinpointStatements.insertAfter(assign, failPoint);
						Restructure.insertBoxingCast(joinpointBody, assign, true);
						skipAdvice=assign;
					} else {						
						skipAdvice=Jimple.v().newInvokeStmt(directInvoke);
						joinpointStatements.insertAfter(skipAdvice, failPoint);
					}
					interfaceInfo.directInvokationStmts.add(skipAdvice);
				}
			}			
		}
		
		{	
			// Assign the correct access parameters to the locals 
			Stmt insertionPoint=(Stmt)first; 
			Stmt skippedCase=Jimple.v().newNopStmt();
			Stmt nonSkippedCase=Jimple.v().newNopStmt();
			Stmt neverBoundCase=Jimple.v().newNopStmt();
			Stmt gotoStmt=Jimple.v().newGotoStmt(neverBoundCase);
			Stmt ifStmt=Jimple.v().newIfStmt(
			 	Jimple.v().newEqExpr(accessMethodInfo.skipParamLocal, IntConstant.v(1)) , skippedCase);
			accessStatements.insertBefore(ifStmt, insertionPoint);
			accessStatements.insertBefore(nonSkippedCase, insertionPoint);
			accessStatements.insertBefore(gotoStmt, insertionPoint);
			accessStatements.insertBefore(skippedCase, insertionPoint);
			accessStatements.insertBefore(neverBoundCase, insertionPoint);
			for (int i=0; i<actuals.size(); i++) {
				Local actual=(Local)actuals.get(i);
				Local actual2=(Local)bindings.get(actual);
				if (!accessBody.getLocals().contains(actual2))
					throw new InternalError();
				if (actual2==null) 
					throw new InternalError();
			
				Restructure.validateMethod(accessMethodInfo.method);
				
				Local paramLocal;
				if (staticBindings.contains(actual)) {
					// We use lastIndexOf here to mimic ajc's behavior:
					// When binding the same value multiple times, ajc's
					// proceed only regards the last one passed to it.
					// Can be changed to indexOf to pick the first one 
					// (which would seem more reasonable). 
					int index=staticBindings.lastIndexOf(actual);
					
					{ // non-skipped case: assign advice formal
						paramLocal=(Local)accessMethodInfo.adviceFormalLocals.get(index);
						AssignStmt s=Jimple.v().newAssignStmt(actual2, paramLocal);
						accessStatements.insertAfter(s, nonSkippedCase);
						Restructure.insertBoxingCast(accessMethod.getActiveBody(), s, true);/// allow boxing?
					}
					{ // skipped case: assign dynamic argument
						paramLocal=(Local)accessMethodInfo.dynParamLocals.get(argIndex[i]);
						AssignStmt s=Jimple.v().newAssignStmt(actual2, paramLocal);
						accessStatements.insertAfter(s, skippedCase);
						Restructure.insertBoxingCast(accessMethod.getActiveBody(), s, true);/// allow boxing?
					}
				} else {
					// no binding
					paramLocal=(Local)accessMethodInfo.dynParamLocals.get(argIndex[i]);
					AssignStmt s=Jimple.v().newAssignStmt(actual2, paramLocal);
					accessStatements.insertAfter(s, neverBoundCase);
					insertCast(accessMethod.getActiveBody(), s, s.getRightOpBox(), actual2.getType());
				}
			}
		}

		// modify the lookup statement in the access method
		{
			accessMethodInfo.lookupValues.add(IntConstant.v(shadowID));
			accessMethodInfo.targets.add(switchTarget);
			// generate new lookup statement and replace the old one
			Stmt lookupStmt=Jimple.v().newLookupSwitchStmt(accessMethodInfo.idParamLocal, 
				accessMethodInfo.lookupValues, accessMethodInfo.targets, accessMethodInfo.defaultTarget);
			accessStatements.insertAfter(lookupStmt, accessMethodInfo.lookupStmt);
			accessStatements.remove(accessMethodInfo.lookupStmt);
			accessMethodInfo.lookupStmt=lookupStmt;
			
			cleanLocals(accessBody);
		}
		

			
 		// generate basic invoke statement (to advice method) and preparatory stmts
		Chain invokeStmts = adviceDecl.makeAdviceExecutionStmts(adviceAppl,localgen,wc);

		// copy all the statements before the actual call into the shadow
		VirtualInvokeExpr invokeEx
		    = (VirtualInvokeExpr) ((InvokeStmt)invokeStmts.getLast()).getInvokeExpr();
		Local aspectRef=(Local) invokeEx.getBase();
		invokeStmts.removeLast();
		for (Iterator stmtlist = invokeStmts.iterator(); stmtlist.hasNext(); ){
			Stmt nextstmt = (Stmt) stmtlist.next();
			if (nextstmt==null)
				throw new RuntimeException();
			if (beforeFailPoint==null)
				throw new RuntimeException();
			if (joinpointStatements==null)
				throw new RuntimeException();
			if (!joinpointStatements.contains(beforeFailPoint))
				throw new RuntimeException();
			joinpointStatements.insertBefore(nextstmt,beforeFailPoint);
		}
		
		// we need to add some of our own parameters to the invokation
		List params=new LinkedList();
		if (lThis==null) {
			params.add(NullConstant.v());
		} else {
			params.add(lThis); // pass the closure
		}
		//params.add(targetLocal);
		params.add(IntConstant.v(shadowID));
		if (bStatic || bAllwaysStaticAccessMethod) { // pass the static class id
			params.add(IntConstant.v(state.getStaticDispatchTypeID(joinpointClass.getType())));
		} else {
			params.add(IntConstant.v(0));
		}
		// and add the original parameters 
		params.addAll(0, invokeEx.getArgs());
		
		
		params.addAll(dynamicActuals);
		
		
		// generate a new invoke expression to replace the old one
		VirtualInvokeExpr invokeEx2=
			Jimple.v().newVirtualInvokeExpr( aspectRef, adviceMethod, params);
		
		Stmt invokeStmt;
		if (returnedLocal==null) {
			invokeStmt=Jimple.v().newInvokeStmt(invokeEx2);
			joinpointStatements.insertBefore(invokeStmt, beforeFailPoint);
		} else {
			AssignStmt assign=Jimple.v().newAssignStmt(returnedLocal, invokeEx2);
			joinpointStatements.insertBefore(assign, beforeFailPoint);
			Restructure.insertBoxingCast(joinpointMethod.getActiveBody(), assign, true);
			invokeStmt=assign;
		}
		joinpointStatements.insertBefore(Jimple.v().newGotoStmt(end), beforeFailPoint);
		
		
		if (invokeStmt==null)
			throw new InternalError();
			
		interfaceInfo.adviceMethodInvokationStmts.add(invokeStmt);
	} 
	
	private static void updateSavedReferencesToStatements(HashMap bindings) {
		Set keys=state.interfaces.keySet();
		Iterator it=keys.iterator();
		// all interfaces
		while (it.hasNext()) {
			String key=(String)it.next();
			State.InterfaceInfo interfaceInfo=(State.InterfaceInfo) state.interfaces.get(key);
			Set keys2=bindings.keySet();
			Iterator it2=keys2.iterator();
			// all bindings
			while (it2.hasNext()) {
				Object old=it2.next();
				if (!(old instanceof Value) && !(old instanceof Stmt))
					continue;
				if (interfaceInfo.adviceMethodInvokationStmts.contains(old)) {
					interfaceInfo.adviceMethodInvokationStmts.remove(old);
					interfaceInfo.adviceMethodInvokationStmts.add(bindings.get(old));// replace with new
				}
				// this is only necessary if proceed calls are ever part of a shadow,
				// for example if the advice body were to be matched by an adviceexecution pointcut. 
				// TODO: does this kind of thing ever happen?
				if (interfaceInfo.interfaceInvokationStmts.contains(old)) {
					interfaceInfo.interfaceInvokationStmts.remove(old);
					interfaceInfo.interfaceInvokationStmts.add(bindings.get(old));// replace with new
				}
				if (interfaceInfo.directInvokationStmts.contains(old)) {
					interfaceInfo.directInvokationStmts.remove(old);
					interfaceInfo.directInvokationStmts.add(bindings.get(old));
				}
			}			
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
		HashSet range=new HashSet();
		
		Chain units=body.getUnits().getNonPatchingChain();
		Iterator it=units.iterator(begin);
		if (it.hasNext())
			it.next(); // skip begin
		while (it.hasNext()) {
			Unit ut=(Unit)it.next();
			if (ut==end)
				break;
			range.add(ut);
		}
		
		List removed=new LinkedList();
		Chain traps=body.getTraps();
		it=traps.iterator();
		while (it.hasNext()){
			Trap trap=(Trap)it.next();
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
		it=removed.iterator();
		while (it.hasNext()) {
			traps.remove(it.next());
		}
	}
	
	private static List getParameterLocals(Body body) {
		List result=new LinkedList();
		
		for (int i=0; i<body.getMethod().getParameterCount();i++) {
			result.add(body.getParameterLocal(i));
		}
		return result;
	}
	/**
	 * Called when a new access method has been added to a class.
	 * Looks at all other access methods of that interface type and 
	 * adds/changes super() calls where necessary
	 * @param interfaceName
	 * @param newAccessClass
	 */
	private static void fixAccessMethodSuperCalls(String interfaceName, SootClass newAccessClass) {
		State.InterfaceInfo interfaceInfo=state.getInterfaceInfo(interfaceName);
		Set keys=interfaceInfo.accessMethodImplementations.keySet();
		
		boolean bAddSuperToNewMethod=false;
		{ // determine if the class that houses the new access method has any base classes 
		  // that implement the method 
			Iterator it=keys.iterator();
			while (!bAddSuperToNewMethod && it.hasNext()) {
				String className=(String)it.next();
				SootClass cl=Scene.v().getSootClass(className);
				if (isBaseClass(cl, newAccessClass)) {
					bAddSuperToNewMethod=true;
				}			
			}
		}
		
		// Iterate over all classes that implement the interface
		Iterator it=keys.iterator();
		while (it.hasNext()) {
			String className=(String)it.next();
			State.AccessMethodInfo accessInfo=(State.AccessMethodInfo)
					interfaceInfo.accessMethodImplementations.get(className);

			SootClass cl=Scene.v().getSootClass(className);
			// if the class is a sub-class of the new class or 
			// if this is the new class and we need to add a super to the new class
			if (isBaseClass(newAccessClass, cl) ||
			  (className.equals(newAccessClass.getName()) && bAddSuperToNewMethod	)) {
				if (accessInfo.superCallTarget==null || // if the class has no super() call 
					isBaseClass(accessInfo.superCallTarget, newAccessClass) ) { // or if it's invalid
					
					// generate new super() call
					
					Body body=accessInfo.method.getActiveBody();
					Chain statements=body.getUnits().getNonPatchingChain();
					Type returnType=accessInfo.method.getReturnType();
					
					// find super class that implements the interface.
					// This is the target class of the super call.
					accessInfo.superCallTarget=cl.getSuperclass();
					while ( !keys.contains(accessInfo.superCallTarget.getName()) ) {
						accessInfo.superCallTarget=accessInfo.superCallTarget.getSuperclass();
					}
										
					removeStatements(body, accessInfo.defaultTarget, accessInfo.defaultEnd, null);
					LocalGeneratorEx lg=new LocalGeneratorEx(body);
					Local lThis=body.getThisLocal();
					
					String accessMethodName=accessInfo.method.getName();
					Restructure.validateMethod(accessInfo.method);
					SpecialInvokeExpr ex=Jimple.v().newSpecialInvokeExpr(
							lThis, accessInfo.superCallTarget.getMethodByName(accessMethodName) , getParameterLocals(body));
					
					if (returnType.equals(VoidType.v())) {
						Stmt s=Jimple.v().newInvokeStmt(ex);
						statements.insertBefore(s, accessInfo.defaultEnd);
						statements.insertBefore(
							Jimple.v().newReturnVoidStmt(), 
								accessInfo.defaultEnd);
						
						accessInfo.superInvokeStmt=s;
					} else {						
						Local l=lg.generateLocal(returnType, "retVal");
						AssignStmt s=Jimple.v().newAssignStmt(l, ex);
						statements.insertBefore(s, accessInfo.defaultEnd);
						statements.insertBefore(
							Jimple.v().newReturnStmt(l),					
							accessInfo.defaultEnd);
							
						accessInfo.superInvokeStmt=s;					
					}
					//accessInfo.hasSuperCall=true;	
				}
			}
									
		}
	}
	
	private static boolean isBaseClass(SootClass baseClass, SootClass subClass) {
		SootClass sub=subClass;

		while (sub.hasSuperclass()) {
			SootClass superClass=sub.getSuperclass();
			if (superClass.equals(baseClass))
				return true;
			
			sub=superClass;			
		}
		return false;
	}
	
	/**
	 * Removes all unused locals from the local chain
	 * @param body
	 */
	private static void cleanLocals(Body body) {

		Chain locals=body.getLocals();

		HashSet usedLocals=new HashSet();
		
		 Iterator it = body.getUseAndDefBoxes().iterator();
		 while(it.hasNext()) {
			 ValueBox vb = (ValueBox) it.next();
			 if(vb.getValue() instanceof Local) {
				 usedLocals.add(vb.getValue());
			 }			
		 }
	
		List removed=new LinkedList();
		 it = body.getLocals().iterator();
		 while(it.hasNext()) {
			 Local local = (Local) it.next();
			 if (!usedLocals.contains(local))
			 	removed.add(local);
		 }
		Iterator it2=removed.iterator();
		while (it2.hasNext())
			locals.remove(it2.next());
	}
	/**
	 * Removes statements between begin and end, excluding these and skip.
	 */
	private static void removeStatements(Body body, Unit begin, Unit end, Unit skip) {
		Chain units=body.getUnits().getNonPatchingChain();
		List removed=new LinkedList();
		Iterator it=units.iterator(begin);
		if (it.hasNext())
			it.next(); // skip begin
		while (it.hasNext()) {
			Unit ut=(Unit)it.next();
			if (ut==end)
				break;
			
			if (ut!=skip) {
				removed.add(ut);
			}
		}
		Iterator it2=removed.iterator();
		while (it2.hasNext())
			units.remove(it2.next());
	}
	
	private static boolean chainContainsLocal(Chain locals, String name) {
		Iterator it = locals.iterator();
		while (it.hasNext()){
			if (((soot.Local)it.next()).getName().equals(name)) return true;
		}
		return false;
	}
	/**
	 *  Assigns a suggested name to a local, dealing with possible collisions
	 */
	private static void setLocalName(Chain locals, Local local, String suggestedName) {
		//if (!locals.contains(local))
		//	throw new RuntimeException();
		
		String name=suggestedName;
		int i=0;
		while (chainContainsLocal(locals, name)) {
			name=suggestedName + (++i);
		}
		local.setName(name);
	}
	
	/**
	 * Checks if @param test 
	 * is in ]@param begin, @param end[
	 * 
	 * @param body 
	 * @return
	 */
	private static boolean isInSequence(Body body, Unit begin, Unit end, Unit test) {
		Chain statements=body.getUnits().getNonPatchingChain();
		
		Iterator it=statements.iterator(begin);
		it.next();
		while (it.hasNext()) {
			Unit ut=(Unit)it.next();
			if (ut==end)
				break;
			
			if (ut==test)
				return true;
		}
		return false;
	}
	

	/**
	 * Checks that:
	 * 	No units outside the shadow point to units inside the shadow
	 *  No units inside the shadow point to units outside the shadow, including end and start
	 * @param body
	 * @param begin
	 * @param end
	 */
	private static void validateShadow(Body body, Stmt begin, Stmt end) {
		Chain statements=body.getUnits().getNonPatchingChain();
		
		if (!statements.contains(begin))
			throw new RuntimeException();
			
		if (!statements.contains(end))
					throw new RuntimeException();
			
			
		boolean insideRange=false;
		
		Iterator it=statements.iterator();
		while (it.hasNext()) {
			Stmt s=(Stmt)it.next();
			if (s==begin) {
				if (insideRange)
					throw new RuntimeException();
						
				insideRange=true;
			}
					
			if (s==end) {
				if (!insideRange)
					throw new RuntimeException();
					
				insideRange=false;
			}
					
			
			List unitBoxes=s.getUnitBoxes();
			Iterator it2=unitBoxes.iterator();
			while (it2.hasNext()) {
				UnitBox box=(UnitBox)it2.next();
				if (insideRange) {
					if (!isInSequence(body,begin, end, box.getUnit())) {
						if (box.getUnit()==end) {
							throw new InternalError("Unit in shadow points to endshadow");	
						} else if (box.getUnit()==begin) {
							throw new InternalError("Unit in shadow points to beginshadow");	
						} else 
							throw new InternalError("Unit in shadow points outside of the shadow" +	
								body.toString());					
					}
				} else {
					if (isInSequence(body,begin, end, box.getUnit())) {
						throw new InternalError("Unit outside of shadow points inside the shadow");					
					}
				}
			}
		}
	}
	private static String mangleTypeName(String name) {
		return name.replaceAll("_","__").replaceAll("\\.","_d_").replaceAll("/","_s_");

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
	private static Set findLocalsGoingIn(Body body, Stmt begin, Stmt end) {
		Chain statements=body.getUnits().getNonPatchingChain();
		
		if (!statements.contains(begin))
			throw new RuntimeException();
		
		if (!statements.contains(end))
					throw new RuntimeException();
		
		Set usedInside=new HashSet();
		Set definedOutside=new HashSet();
		
		boolean insideRange=false;
		{
			Iterator it=statements.iterator();
			while (it.hasNext()) {
				Stmt s=(Stmt)it.next();
				if (s==begin) {
					if (insideRange)
						throw new RuntimeException();
						
					insideRange=true;
				}
					
				if (s==end) {
					if (!insideRange)
						throw new RuntimeException();
					
					insideRange=false;
				}
					
				if (insideRange) {
					List useBoxes=s.getUseBoxes();
					Iterator it2=useBoxes.iterator();
					while (it2.hasNext()) {
						ValueBox box=(ValueBox)it2.next();
						if (box.getValue() instanceof Local) {
							Local l=(Local)box.getValue();							
							usedInside.add(l);							
						}
					}
				} else {
					List defBoxes=s.getDefBoxes();
					Iterator it2=defBoxes.iterator();
					while (it2.hasNext()) {
						ValueBox box=(ValueBox)it2.next();
						if (box.getValue() instanceof Local) {
							Local l=(Local)box.getValue();							
							definedOutside.add(l);											
						}
					}
				}
			}
		}
		usedInside.retainAll(definedOutside);
		return usedInside;
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
	private static HashMap copyStmtSequence(Body source, Unit begin, Unit end, 
			Body dest, Unit insertAfter,
				Local returnedLocal, ObjectBox resultingFirstCopy) {
		
		boolean bInstance=!source.getMethod().isStatic() && !dest.getMethod().isStatic();
		Local lThisSource=null;
		Local lThisCopySource=null;
		if (bInstance) {
			lThisSource=source.getThisLocal();
			lThisCopySource=Restructure.getThisCopy(source.getMethod());
		}
		
		Local lThisDest=null;
		Local lThisCopyDest=null;
		if (bInstance) {
			lThisDest=dest.getThisLocal();
			lThisCopyDest=Restructure.getThisCopy(dest.getMethod());
		}			
		
		HashMap bindings = new HashMap();
		//HashMap boxes=new HashMap();
		
		Iterator it = source.getUnits().getNonPatchingChain().iterator(begin);
		if (it.hasNext())
			it.next(); // skip begin

		Chain unitChain=dest.getUnits().getNonPatchingChain();
		
		Unit firstCopy=null;
		// Clone units in body's statement list 
		while(it.hasNext()) {
			Unit original = (Unit) it.next();
			if (original==end)
				break;
				
			Unit copy = (Unit) original.clone();
     
			// Add cloned unit to our unitChain.
			unitChain.insertAfter(copy, insertAfter);
			insertAfter=copy;
			if (firstCopy==null)
				firstCopy=copy;
			// Build old <-> new map to be able to patch up references to other units 
			// within the cloned units. (these are still refering to the original
			// unit objects).
			bindings.put(original, copy);
		}

		Chain trapChain=dest.getTraps();
		
		// Clone trap units.
		it = source.getTraps().iterator();
		while(it.hasNext()) {
			Trap original = (Trap) it.next();
			if (isInSequence(source, begin, end, original.getBeginUnit()) &&
			    isInSequence(source, begin, end, original.getEndUnit()) &&
				isInSequence(source, begin, end, original.getHandlerUnit())) {
				
			
				Trap copy = (Trap) original.clone();
    
				// Add cloned unit to our trap list.
				trapChain.addLast(copy);

			
				// Store old <-> new mapping.
				bindings.put(original, copy);
			}
		}


		Chain destLocals=dest.getLocals();

		// Clone local units.
		it = source.getLocals().iterator();
		while(it.hasNext()) {
			Local original = (Local) it.next();
			Local copy = (Local) original.clone();
    
    		if (original==lThisSource) {
				bindings.put(lThisSource, lThisDest);
    		} else if (original==lThisCopySource) {
				bindings.put(lThisCopySource, lThisCopyDest);
    		} else {
				//copy.setName(copy.getName() + "$abc$" + state.getUniqueID());
				setLocalName(destLocals, copy, original.getName()); // TODO: can comment this line out in release build
				
				// Add cloned unit to our local list.
				destLocals.addLast(copy);

				// Build old <-> new mapping.
				bindings.put(original, copy);	
    		}
		}



		// Patch up references within units using our (old <-> new) map.
		it = dest.getAllUnitBoxes().iterator();
		while(it.hasNext()) {
			UnitBox box = (UnitBox) it.next();
			Unit newObject, oldObject = box.getUnit();
    	
			// if we have a reference to an old object, replace it 
			// it's clone.
			if( (newObject = (Unit)  bindings.get(oldObject)) != null )
				box.setUnit(newObject);
        
		}        

		// backpatching all local variables.
		it = dest.getUseAndDefBoxes().iterator();
		while(it.hasNext()) {
			ValueBox vb = (ValueBox) it.next();
			if(vb.getValue() instanceof Local) {
				Local oldLocal=(Local)vb.getValue();
				Local newLocal=(Local) bindings.get(oldLocal);
				
				if (newLocal!=null)
					vb.setValue(newLocal);
			}
				
		}

		// fix the trap destinations
		it = dest.getTraps().iterator();
		while (it.hasNext()) {
			Trap trap=(Trap) it.next();
			List boxes=trap.getUnitBoxes();
			Iterator it2=boxes.iterator();

			while (it2.hasNext()) {
				UnitBox box=(UnitBox)it2.next();
				Unit ut=box.getUnit();
				Unit newUnit=(Unit)bindings.get(ut);
				if (newUnit!=null) {
					box.setUnit(newUnit);
				} /*else {
					
				}*/
			}
		}

		if (returnedLocal!=null) {
			Local newLocal=(Local)bindings.get(returnedLocal);
			if (newLocal==null)
				throw new InternalError();
			
			LocalGeneratorEx lg=new LocalGeneratorEx(dest);
			Local castLocal=lg.generateLocal(dest.getMethod().getReturnType());
			AssignStmt s=Jimple.v().newAssignStmt(castLocal, newLocal);
			unitChain.insertAfter(s, insertAfter);	
			insertAfter=s;
			ReturnStmt returnStmt=Jimple.v().newReturnStmt(castLocal);	
			unitChain.insertAfter(returnStmt, insertAfter);			
			Restructure.insertBoxingCast(dest, s, true);
			//insertBoxingCast(dest, returnStmt, returnStmt.getOpBox(), dest.getMethod().getReturnType());
			//JasminClass
			insertAfter=returnStmt;
		} else {
			if (!dest.getMethod().getReturnType().equals(VoidType.v()))
				throw new InternalError();
			
			ReturnVoidStmt returnStmt=Jimple.v().newReturnVoidStmt();
			unitChain.insertAfter(returnStmt, insertAfter);
			insertAfter=returnStmt;
		}
		resultingFirstCopy.object=firstCopy;
		return bindings;
	}
	
	private static List getDefaultValues(List types) {
		List result=new LinkedList();
		{
			Iterator it=types.iterator();
			while (it.hasNext()) {
				Type type=(Type) it.next();
				result.add(Restructure.JavaTypeInfo.getDefaultValue(type));	
			}
		}
		return result;
	}
	
	private static List bindList(Residue r) {
	    // explicitly go through all the options to force early
	    // errors when a new one gets added, to make sure we have
	    // thought about it properly. This should all be delegated
	    // in the future.
		if (r instanceof AlwaysMatch) {
			
		} else if (r instanceof AndResidue) {
			List l= bindList( ((AndResidue)r).getLeftOp());
			l.addAll(bindList(((AndResidue)r).getRightOp()));
			return l;
		} else if (r instanceof AspectOf) {
			
		} else if (r instanceof Bind) {
			Bind bind=(Bind)r;
			List l=new LinkedList();
			l.add(r);
			return l;
		} else if (r instanceof Box) {

		} else if (r instanceof Load) {

		} else if (r instanceof CheckType) {
			
		} else if (r instanceof Copy) {
			
		} else if (r instanceof HasAspect) {
			
		} else if (r instanceof IfResidue) {
			
		} else if (r instanceof NeverMatch) {
			
		} else if (r instanceof NotResidue) {
			return bindList( ((NotResidue)r).getOp());
		} else if (r instanceof OrResidue) {
			List l= bindList( ((OrResidue)r).getLeftOp());
			l.addAll(bindList(((OrResidue)r).getRightOp()));
			return l;
		} else {
			throw new InternalError();
		}
		return new LinkedList();
	}
	private static Vector getStaticBinding(AdviceApplication appl, WeavingContext wc) {
		List bindings=bindList(appl.residue);
		debug("getStaticBinding: Binds found:" + bindings.size());
		Vector result=new Vector();
		result.setSize(bindings.size());
		
		
		Iterator it=bindings.iterator();
		while (it.hasNext()) {
			Bind bind=(Bind)it.next();
			if (bind.variable instanceof AdviceFormal) {
				AdviceFormal formal=(AdviceFormal)bind.variable;
				Value value=bind.value.getSootValue();
				if (value instanceof Local) {
					Local local=(Local) value;
					debug(" Binding: " + local.getName() + " => " + formal.pos );
					if (result.get(formal.pos)!=null)
						throw new RuntimeException("Ambiguous variable binding"); // TODO: 
					
					result.set(formal.pos, local);
				} else {
				    throw new InternalError("Expecting bound values to be of type Local: "+value +" (came from: "+bind+")"); 
				}
			} else {
				
			}
		}
		return result;
	}
	private static void verifyBindings(Vector bindings) {
		for (int i=0; i<bindings.size();i++) {
			if (bindings.get(i)==null)
				throw new InternalError("Not all arguments are bound"); // TODO:
		}
		
	}
	private static void doInitialAdviceMethodModification(
		AdviceApplication adviceAppl,
		SootMethod adviceMethod,
		SootClass accessInterface,
		SootMethod abstractAccessMethod,
		String accessMethodName, 
		SootClass joinpointClass,
		SootClass theAspect) {
		
		Type adviceReturnType=adviceMethod.getReturnType();
		String interfaceName=accessInterface.getName();
		
		State.AdviceMethodInfo adviceMethodInfo=state.getAdviceMethodInfo(theAspect.getName(), adviceMethod.getName());
							
		debug("modifying advice method: " + adviceMethod.toString());
		
		Restructure.validateMethod(adviceMethod);
		
		Body adviceBody=adviceMethod.getActiveBody();
		Chain statements=adviceBody.getUnits().getNonPatchingChain();
		
		List adviceMethodParameters=adviceMethod.getParameterTypes();
		
		
		
		Local lInterface=Restructure.addParameterToMethod(adviceMethod, accessInterface.getType(), "accessInterface");
		//Local lTarget=Restructure.addParameterToMethod(adviceMethod, 
		//		Scene.v().getSootClass("java.lang.Object").getType(), "targetArg");
		Local lShadowID=Restructure.addParameterToMethod(adviceMethod, IntType.v(), "shadowID");
		Local lStaticClassID=Restructure.addParameterToMethod(adviceMethod, IntType.v(), "staticClassID");
		
		Restructure.validateMethod(adviceMethod);
		
		//adviceMethodInfo.proceedParameters.add(lTarget);
		adviceMethodInfo.proceedParameters.add(lShadowID);
		adviceMethodInfo.proceedParameters.add(IntConstant.v(0)); // skipAdvice parameter
		
		adviceMethodInfo.interfaceLocal=lInterface;
		//adviceMethodInfo.targetLocal=lTarget;
		adviceMethodInfo.idLocal=lShadowID;
		adviceMethodInfo.staticDispatchLocal=lStaticClassID;
		
		
		State.InterfaceInfo interfaceInfo=state.getInterfaceInfo(interfaceName);
		
		Set proceedActuals=new HashSet();
		
		Iterator it=statements.snapshotIterator();
		while (it.hasNext()) { 
			Stmt s=(Stmt)it.next();
			InvokeExpr invokeEx;
			try {
				invokeEx=s.getInvokeExpr();
			} catch(Exception ex) {
				invokeEx=null;
			}
			
			if (invokeEx!=null) {
				if (invokeEx.getMethod().getName().startsWith("proceed$")) {
					
					// check if changed values are passed to proceed.
					for (int i=0; i<invokeEx.getArgCount(); i++) {
						Value v=invokeEx.getArg(i);
						debug("proceed$ arg " + i + ": " + v);
						proceedActuals.add(v);
						/*if (!v.equals(adviceBody.getParameterLocal(i))) {
							throw new CodeGenException(
								"Passing modified values to proceed is not yet implemented. \n" +
								" Aspect: " + adviceMethod.getDeclaringClass().getName() + "\n" +
								" Advice method: " + adviceMethod.getName() + "\n" +  
								" Argument " + i + "\n" + 
								" Statement: " + s.toString() 
								 );
						}*/
					}
					State.AdviceMethodInfo.ProceedInvokation invokation=new 
											State.AdviceMethodInfo.ProceedInvokation();
					adviceMethodInfo.proceedInvokations.add(invokation);
					
					invokation.originalActuals.addAll(invokeEx.getArgs());
					
					invokation.begin=Jimple.v().newNopStmt();
					invokation.end=Jimple.v().newNopStmt();
					if (s instanceof AssignStmt) {
						invokation.lhs=(Local)(((AssignStmt)s).getLeftOp());
					}
					statements.insertBefore(invokation.begin, s);
					statements.insertAfter(invokation.end, s);
					statements.remove(s);
				}
			}
		}
		it=statements.iterator();
		while (it.hasNext()) {
			Stmt s=(Stmt)it.next();
			
			if (s instanceof AssignStmt) {
				AssignStmt assign=(AssignStmt)s;
				/*if (proceedActuals.contains(assign.getLeftOp())) {
					throw new CodeGenException(
						"Passing modified values to proceed is not yet implemented. \n" +
						"Found assignment to local passed to proceed. \n" +
						" Aspect: " + adviceMethod.getDeclaringClass().getName() + "\n" +
						" Advice method: " + adviceMethod.getName() + "\n" +  
						" Statement: " + s.toString() 
						 );		
				}*/
			}
		}
	}
	
}


// find out what kind of pointcut 
/*if (adviceAppl instanceof StmtAdviceApplication) {
	debug("found statement advice application");
	StmtAdviceApplication stmtAdv=(StmtAdviceApplication) adviceAppl;

	if (adviceAppl.sjpInfo.kind.equals("field-get") ||
		adviceAppl.sjpInfo.kind.equals("method-call")  ) {
		debug("found " + adviceAppl.sjpInfo.kind);
		if (!(stmtAdv.stmt instanceof AssignStmt)) {
			throw new CodeGenException(
				"StmtAdviceApplication.stmt is expected to be instanceof AssignStmt");  
		}
		debug("found assignment statement");
		AssignStmt assignStmt=(AssignStmt)stmtAdv.stmt;				
								
	} else {
		debug("NYI: type of stmt advice application " + adviceAppl);
	}			
} else if (adviceAppl instanceof ExecutionAdviceApplication) {
	debug("NYI: execution advice application: " + adviceAppl);
} else {
	debug("NYI: advice application: " + adviceAppl);
}*/
 

/*
adviceMethodParameters.add();
int interfaceParam=adviceMethodParameters.size()-1;
adviceMethodParameters.add(IntType.v());			
int idParam=adviceMethodParameters.size()-1;	
adviceMethodParameters.add(IntType.v());
int staticDispatchParam=adviceMethodParameters.size()-1;
		
adviceMethod.setParameterTypes(adviceMethodParameters);
		
		
debug("id1: " + interfaceParam);
debug("id2: " + idParam);
debug("count:" + adviceMethod.getParameterCount());
	
Stmt lastExistingIDStmt=(Stmt)statements.getFirst();
	
if (adviceAppl.advice.numFormals()>0){		
	Iterator it=statements.iterator();
	for (int i=0; i<adviceAppl.advice.numFormals()+1; i++) {
		lastExistingIDStmt=(Stmt)it.next();
	}
	if (lastExistingIDStmt==null)
		throw new InternalError();

	IdentityStmt id=(IdentityStmt)lastExistingIDStmt;
	
	Local local=(Local)id.getLeftOp();

	// local.

	if ( local!=aroundBody.getParameterLocal(0))
		throw new InternalError();

	debug("param:" + aroundBody.getParameterLocal(0).getType());
	//if (((IdentityStmt)lastExistingIDStmt).getRightOp().)
}
		
LocalGeneratorEx localgen2 = new LocalGeneratorEx(aroundBody);
Local lInterface=localgen2.generateLocal(accessInterface.getType());//, "accessIntf");
// insert id for first param (interface reference)
Stmt intRefIDstmt=Jimple.v().newIdentityStmt(lInterface, 
		Jimple.v().newParameterRef(	
				accessInterface.getType(),interfaceParam));
statements.insertAfter(intRefIDstmt, lastExistingIDStmt);
// id for second param (id of field accessed)
Local l2=localgen2.generateLocal(IntType.v());//, "id");
Stmt fieldIDStmt=Jimple.v().newIdentityStmt(l2, 
		Jimple.v().newParameterRef(IntType.v(),idParam));
statements.insertAfter(fieldIDStmt, intRefIDstmt);
// id for third param (value for set operation)
Local l3=localgen2.generateLocal(IntType.v());//, "id");
Stmt staticDispatchStmt=Jimple.v().newIdentityStmt(l3, 
		Jimple.v().newParameterRef(IntType.v(),staticDispatchParam));
statements.insertAfter(staticDispatchStmt, fieldIDStmt);
*/
// set from constant.
			// Add an assignment to a local before stmt 
			/*LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);
			Local l=lg.generateLocal(((Constant)rightOp).getType(), "setTmp");
			AssignStmt s=Jimple.v().newAssignStmt(l, rightOp);
			joinpointChain.insertBefore(s, adviceAppl.shadowpoints.getBegin());
			assignStmt.setRightOp(l);
			actuals.add(assignStmt.getRightOpBox());
			actualsTypes.add(((FieldRef)leftOp).getType());
			//throw new CodeGenException("Can't handle assignment from constant yet");
			if (leftOp instanceof InstanceFieldRef) {
				InstanceFieldRef ix=(InstanceFieldRef) leftOp;
				theTarget=ix.getBaseBox();
			}*/
			
			/*if (invokeTarget!=null) {
						invokeTarget.setValue(invokeEx2);
					} else {
						if (assignStmt==null)
							throw new InternalError(); // must/should never be reached
						// set()
						// replace old "fieldref=local" with invokation 
						invokeStmt=Jimple.v().newInvokeStmt(invokeEx2);
						joinpointStatements.insertAfter(invokeStmt, assignStmt);
						joinpointStatements.remove(assignStmt);			
						stmtAppl.stmt=invokeStmt;
						invokeTarget=invokeStmt.getInvokeExprBox();
						//Jimple.v().newEqExpr()
					}*/
//				call (void)
						 /*invokeStmt=(InvokeStmt) applStmt;
						 InvokeExpr invokeEx=invokeStmt.getInvokeExpr();
						 for (int i=0; i<invokeEx.getArgCount(); i++)
							 actuals.add(invokeEx.getArgBox(i));
						 invokeTarget=invokeStmt.getInvokeExprBox();
						 actualsTypes=invokeEx.getMethod().getParameterTypes();
						 if (invokeEx instanceof InstanceInvokeExpr) {
							 InstanceInvokeExpr ix=(InstanceInvokeExpr) invokeEx;
							 theTarget=ix.getBaseBox();
						 }*/
						 /*Local targetLocal;
								 { // create a local for the target
									 LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);
									 AssignStmt s;
									 if (theTarget!=null) {
										 targetLocal=lg.generateLocal(theTarget.getValue().getType(), "targetArg");
										 s=Jimple.v().newAssignStmt(targetLocal, theTarget.getValue());
										 theTarget.setValue(targetLocal);
									 } else {
										 targetLocal=lg.generateLocal(
											 Scene.v().getSootClass("java.lang.Object").getType(),
												  "targetArg");
										 s=Jimple.v().newAssignStmt(targetLocal, NullConstant.v());
									 }
									 joinpointStatements.insertBefore(s, stmtAppl.shadowpoints.getBegin());
								 }*/
 		
								 /*Local interfaceLocal=null;
								 if (!bStatic) {
									 LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);
			
									 interfaceLocal=lg.generateLocal(accessInterface.getType(), "closureType");
									 AssignStmt s=Jimple.v().newAssignStmt(interfaceLocal, 
										 Jimple.v().newCastExpr(Restructure.getThisCopy(joinpointMethod),
													 accessInterface.getType()));
									 joinpointStatements.insertBefore(s, begin);
								 }*/
								 /* if (rightOp instanceof InvokeExpr) {
													 // call
													 InvokeExpr invokeEx=(InvokeExpr)rightOp;
													 for (int i=0; i<invokeEx.getArgCount(); i++)
														 actuals.add(invokeEx.getArgBox(i));
				 	
													 actualsTypes=invokeEx.getMethod().getParameterTypes();
					
													 if (rightOp instanceof InstanceInvokeExpr) {
														 InstanceInvokeExpr ix=(InstanceInvokeExpr) rightOp;
														 theTarget=ix.getBaseBox();
													 }
												  } else if (rightOp instanceof FieldRef) {
													 // get
													 // no actuals
													 if (rightOp instanceof InstanceFieldRef) {
														 InstanceFieldRef ix=(InstanceFieldRef) rightOp;
														 theTarget=ix.getBaseBox();
													 }
												  }*/
//									At the advice application statement, extract any parameters into locals.
									/*Local[] generatedLocals=new Local[actuals.size()];
									{  
										Iterator it=actuals.iterator();
										//Iterator it2=actualsTypes.iterator();
										LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);
										int i=0;
										while (it.hasNext()) {
											//ValueBox box=(ValueBox)it.next();
											Local local=(Local)it.next();
											Type type=local.getType();//(Type)it2.next();
											//Value val=box.getValue();
											String name="dynArg" + argIndex[i] + "act";
											Local l=lg.generateLocal(type, name);
											AssignStmt s=Jimple.v().newAssignStmt(l, local);
											joinpointStatements.insertBefore(s, stmtAppl.shadowpoints.getBegin());	
											box.setValue(l);
											generatedLocals[i]=l;
											i++;
										} 			
									}*/
