package abc.weaving.weaver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import soot.Body;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.util.Chain;
import soot.tagkit.*;
import soot.baf.*;
import soot.jimple.*;
import soot.toolkits.graph.*;
import soot.*;
import soot.util.*;
import java.util.*;
import java.io.*;

import javax.sound.sampled.BooleanControl;

import com.sun.rsasign.i;

import soot.toolkits.scalar.*;
import soot.options.*;
import sun.security.action.GetLongAction;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.AdviceSpec;
import abc.weaving.aspectinfo.AroundAdvice;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.ExecutionAdviceApplication;
import abc.weaving.matching.StmtAdviceApplication;

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

	private static void debug(String message)
	 { if (abc.main.Debug.v().aroundWeaver) 
		  System.err.println("ARD*** " + message);
	 }

	private static class JavaTypeInfo {
		public final static int booleanType=0;
		public final static int byteType=1;
		public final static int shortType=2;
		public final static int charType=3;
		public final static int intType=4;
		public final static int longType=5;
		public final static int floatType=6;
		public final static int doubleType=7;
		public final static int refType=8;
		public final static int typeCount=9;
		
		public static int sootTypeToInt(Type type) {
			if (type.equals(IntType.v()))
				return intType;
			else if (type.equals(BooleanType.v())) 
				return booleanType;
			else if (type.equals(ByteType.v())) 
				return byteType;
			else if (type.equals(ShortType.v())) 
							return shortType;
			else if (type.equals(CharType.v())) 
							return charType;
			else if (type.equals(LongType.v())) 
							return longType;
			else if (type.equals(FloatType.v())) 
							return floatType;
			else if (type.equals(DoubleType.v())) 
							return doubleType;
			else 
				return refType;
		}
		public static Value getDefaultValue(Type type) {
			if (type.equals(IntType.v()))
				return IntConstant.v(0);
			else if (type.equals(BooleanType.v())) 
				return IntConstant.v(0); 
			else if (type.equals(ByteType.v())) 
				return IntConstant.v(0); ///
			else if (type.equals(ShortType.v())) 
				return IntConstant.v(0); ///
			else if (type.equals(CharType.v())) 
				return IntConstant.v(0); ///
			else if (type.equals(LongType.v())) 
				return LongConstant.v(0);
			else if (type.equals(FloatType.v())) 
				return FloatConstant.v(0.0f);
			else if (type.equals(DoubleType.v())) 
				return DoubleConstant.v(0.0);
			else 
				return NullConstant.v();
		}
		public static SootClass getBoxingClass(Type type) {
			if (type.equals(IntType.v()))
				return Scene.v().getSootClass("java.lang.Integer");
			else if (type.equals(BooleanType.v())) 
				return Scene.v().getSootClass("java.lang.Boolean");
			else if (type.equals(ByteType.v())) 
				return Scene.v().getSootClass("java.lang.Byte");
			else if (type.equals(ShortType.v())) 
				return Scene.v().getSootClass("java.lang.Short");
			else if (type.equals(CharType.v())) 
				return Scene.v().getSootClass("java.lang.Character");
			else if (type.equals(LongType.v())) 
				return Scene.v().getSootClass("java.lang.Long");
			else if (type.equals(FloatType.v())) 
				return Scene.v().getSootClass("java.lang.Float");
			else if (type.equals(DoubleType.v())) 
				return Scene.v().getSootClass("java.lang.Double");
			else 
				throw new RuntimeException();
		}
		public static String getBoxingClassMethodName(Type type) {	
			if (type.equals(IntType.v()))
				return "intValue";
			else if (type.equals(BooleanType.v())) 
				return "booleanValue";
			else if (type.equals(ByteType.v())) 
				return "byteValue";
			else if (type.equals(ShortType.v())) 
				return "shortValue";
			else if (type.equals(CharType.v())) 
				return "charValue";
			else if (type.equals(LongType.v())) 
				return "longValue";
			else if (type.equals(FloatType.v())) 
				return "floatValue";
			else if (type.equals(DoubleType.v())) 
				return "doubleValue";
			else 
				throw new RuntimeException();
		}
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
			Local targetLocal;
			
			List dynParamLocals=new LinkedList();
			
			SootMethod method;
			
			//boolean hasSuperCall=false;
			SootClass superCallTarget=null;
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
			//public HashMap /*InvokeExpr, ValueBox*/ invokationBoxes=new HashMap();
			List /*Type*/ dynamicArguments=new LinkedList();
			
			List[] dynamicArgsByType=new List[JavaTypeInfo.typeCount];
			
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
			public List proceedParameters=new LinkedList();
			public Local interfaceLocal;
			public Local targetLocal;
			public Local idLocal;
			public Local staticDispatchLocal;
			public HashSet /*String*/ staticProceedTypes=new HashSet();
			public boolean hasDynamicProceed=false;
			
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
			}			  
			public List proceedInvokations=new LinkedList();
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
					LocalGenerator localgen,
					AdviceApplication adviceAppl) {
		debug("Handling aound: " + adviceAppl);
		
		Body joinpointBody = joinpointMethod.getActiveBody();
		boolean bStatic=joinpointMethod.isStatic();
		Chain joinpointStatements = joinpointBody.getUnits();
		AdviceDecl adviceDecl = adviceAppl.advice;
		AdviceSpec adviceSpec = adviceDecl.getAdviceSpec();
		AroundAdvice aroundSpec = (AroundAdvice) adviceSpec;
		SootClass theAspect =
			adviceDecl.getAspect().getInstanceClass().getSootClass();
		SootMethod adviceMethod = adviceDecl.getImpl().getSootMethod();
	
		Type adviceReturnType=adviceMethod.getReturnType();
	
			
		String adviceReturnTypeName=
				adviceReturnType.toString();// .getClass().getName();
		String adviceMangledReturnTypeName= // TODO: proper mangling!
			adviceReturnTypeName.replace('.', '_');
		//String accessTypeString= bGet ? "get" : "set";
		
		
		final boolean interfacePerAdviceMethod=true;
		String adviceMethodIdentifierString="$" + 	theAspect.getName() + "$" + adviceMethod.getName();
		
		String interfaceName="abc$access$" + adviceMangledReturnTypeName + 
			(interfacePerAdviceMethod ? adviceMethodIdentifierString : "");
		
		String accessMethodName;
		String dynamicAccessMethodName="abc$proceed$" + adviceMangledReturnTypeName+ 
			(interfacePerAdviceMethod ? adviceMethodIdentifierString : "");;	
		if (bStatic) {
			accessMethodName="abc$static$proceed$" + adviceMangledReturnTypeName+ 
						(interfacePerAdviceMethod ? adviceMethodIdentifierString : "");	
		} else {
			accessMethodName=dynamicAccessMethodName;
		}
		
		SootClass accessInterface=null;
	
		List /*type*/ accessMethodParameters=new LinkedList();
		accessMethodParameters.add(Scene.v().getSootClass("java.lang.Object").getType());
		accessMethodParameters.add(IntType.v()); // the id
		
		accessInterface =
			createAccessInterface(
				adviceReturnType,
				interfaceName,
				dynamicAccessMethodName,
				accessMethodParameters);
				
		SootMethod abstractAccessMethod=accessInterface.getMethodByName(dynamicAccessMethodName);
		
		
		SootMethod accessMethod=
			generateAccessMethod(
				joinpointClass,
				bStatic,
				adviceReturnType,
				accessMethodName,
				accessInterface,
				accessMethodParameters);
		
		// Change advice method: add parameters and replace proceed with call to get-interface
		{
			boolean bFirstModification=
				adviceMethod.getParameterCount()==adviceAppl.advice.numFormals();
			if (bFirstModification) {
				modifyAdviceMethod(adviceAppl, adviceMethod, accessInterface, abstractAccessMethod, 
					bStatic, accessMethodName, joinpointClass,
					theAspect
					);
			}
			generateProceedCalls(adviceAppl, adviceMethod, accessInterface, abstractAccessMethod, 
				adviceReturnType, bStatic, accessMethodName, joinpointClass,
				theAspect, accessMethod, interfaceName);
			
		}
		implementInterface(
			theAspect,
			joinpointMethod,
			adviceAppl,
			 joinpointClass,
			 adviceReturnType,
			 accessInterface,
			 accessMethod,
			 abstractAccessMethod
			 );
	}


	private static SootMethod generateAccessMethod(
		SootClass joinpointClass,
		boolean bStatic,
		Type adviceReturnType,
		String accessMethodName,
		SootClass accessInterface,
		List accessMethodParameters) {
		
		
		//=abstractAccessMethod.getParameterTypes();
		
		String interfaceName=accessInterface.getName();
			
		State.AccessMethodInfo accessMethodInfo=state.getAccessMethodInfo(joinpointClass.getName(), interfaceName, bStatic);
		
		
		SootMethod accessMethod=null;
	
		
		boolean createdNewAccessMethod=false;		 
		 if (bStatic) {
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

			Chain accessStatements=accessBody.getUnits();

			// generate this := @this
			LocalGeneratorEx lg=new LocalGeneratorEx(accessBody);
			Local lThis=null;
			if (!bStatic) {
				lThis=lg.generateLocal(joinpointClass.getType(), "this");
				accessStatements.addFirst(
					Jimple.v().newIdentityStmt(lThis, 
						Jimple.v().newThisRef(
							RefType.v(joinpointClass))));
			}
			validateMethod(accessMethod);
			accessMethodInfo.targetLocal=addParameterToMethod(
				accessMethod, (Type)accessMethodParameters.get(0), "targetArg");
			
			accessMethodInfo.idParamLocal=addParameterToMethod(
				accessMethod, (Type)accessMethodParameters.get(1), "shadowID");
			
			if (accessMethodParameters.size()!=2)
				throw new InternalError();
			
			Stmt lastIDStmt=getParameterIdentityStatement(accessMethod, 
				accessMethodParameters.size()-1);
			
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
				Local l=addParameterToMethod(accessMethod, type, "dynArgFormal");
				accessMethodInfo.dynParamLocals.add(l);
			}
			
			if (!bStatic) {
				fixAccessMethodSuperCalls(interfaceName, joinpointClass);
			}
		}	
		validateMethod(accessMethod);
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
					String accessMethodName, 
					SootClass joinpointClass,
					SootClass theAspect,
					SootMethod accessMethod,
					String interfaceName) {
		
		State.AdviceMethodInfo adviceMethodInfo=state.getAdviceMethodInfo(theAspect.getName(), adviceMethod.getName());
		State.InterfaceInfo interfaceInfo=state.getInterfaceInfo(interfaceName);
		
		
		String newStaticInvoke=null;
		if (bStatic) {
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
			
			
			if (invokation.dynamicInvoke==null && adviceMethodInfo.hasDynamicProceed){
				InvokeExpr newInvokeExpr=
							Jimple.v().newInterfaceInvokeExpr( 
								adviceMethodInfo.interfaceLocal, abstractAccessMethod, 
									adviceMethodInfo.proceedParameters);
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
								m , adviceMethodInfo.proceedParameters);
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
	
	private static void insertBoxingCast(Body body, AssignStmt stmt) {
		ValueBox source=stmt.getRightOpBox();
		Value targetVal=stmt.getLeftOp();
		Type targetType=stmt.getLeftOp().getType();
		Chain units=body.getUnits();
		Type sourceType=source.getValue().getType();
		if (!sourceType.equals(targetType)) {
			LocalGeneratorEx localgen=new LocalGeneratorEx(body);
			Local castLocal=localgen.generateLocal(sourceType, "castTmp");
			debug("cast: source has type " + sourceType.toString());
			debug("cast: target has type " + targetType.toString());
			stmt.setLeftOp(castLocal);
			
			AssignStmt tmpStmt=Jimple.v().newAssignStmt(targetVal, targetVal /*dummy*/);
			units.insertAfter(tmpStmt, stmt);
						
			Value castedExpr;
			//debug("boxing: source " + sourceType + " target " + targetType);
			// boxing
			if (JavaTypeInfo.sootTypeToInt(sourceType)!=JavaTypeInfo.refType &&
				targetType.equals(Scene.v().getSootClass("java.lang.Object").getType())) {
				SootClass boxClass=JavaTypeInfo.getBoxingClass(sourceType);	
				 Local box=localgen.generateLocal(boxClass.getType(), "box");
				 Stmt newAssignStmt = Jimple.v().newAssignStmt( box, Jimple.v().newNewExpr( boxClass.getType() ) );
				 List initParams=new LinkedList();
				 initParams.add(sourceType);
				 Stmt initBox=Jimple.v().newInvokeStmt( 
				 	Jimple.v().newSpecialInvokeExpr( box, boxClass.getMethod( "<init>", initParams), 
				 			castLocal)) ;
				units.insertBefore(newAssignStmt, tmpStmt);
				units.insertBefore(initBox, tmpStmt);
				castedExpr=box;
			} else if /*unboxing*/
				(JavaTypeInfo.sootTypeToInt(targetType)!=JavaTypeInfo.refType &&
					sourceType.equals(Scene.v().getSootClass("java.lang.Object").getType())	){ 
				SootClass boxClass=JavaTypeInfo.getBoxingClass(targetType);	
				Local box=localgen.generateLocal(boxClass.getType(), "box");
				Stmt newAssignStmt=Jimple.v().newAssignStmt(box, 
					Jimple.v().newCastExpr(castLocal, boxClass.getType()));
				SootMethod method=boxClass.getMethodByName(
					JavaTypeInfo.getBoxingClassMethodName(targetType));
				castedExpr=Jimple.v().newVirtualInvokeExpr(box, 
						 method);		
				units.insertBefore(newAssignStmt, tmpStmt);						
			} else { // normal cast
				CastExpr castExpr=Jimple.v().newCastExpr(castLocal,targetType);
				castedExpr=castExpr;	
			}
			
			tmpStmt.setRightOp(castedExpr);
		//	Jimple.v().newCastExpr()
			/*
			if (stmt instanceof AssignStmt) {
				source.setValue(castedExpr);
			} else {
				Local tmpLocal=localgen.generateLocal(targetType, "castTarget");
				AssignStmt tmpStmt2=Jimple.v().newAssignStmt(tmpLocal, castedExpr);
				units.insertBefore(tmpStmt2, stmt);
				source.setValue(tmpLocal);
			}*/
		} 			
	}	
	private static void insertCast(Body body, Stmt stmt, ValueBox source, Type targetType) {
		Chain units=body.getUnits();
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
				if (newArgs.size()>0)
					throw new InternalError();
				return Jimple.v().newSpecialInvokeExpr(base, old.getMethod());
			} else if (old instanceof VirtualInvokeExpr)
				return Jimple.v().newVirtualInvokeExpr(base, old.getMethod(), newArgs);
			else
				throw new InternalError();
		}  else {
			return Jimple.v().newStaticInvokeExpr(old.getMethod(), newArgs);
		}
	}
	
	private static IdentityStmt getParameterIdentityStatement(SootMethod method, int arg) {
		if (arg>=method.getParameterCount())
			throw new InternalError();
		Chain units=method.getActiveBody().getUnits();
		Iterator it=units.iterator();
		while (it.hasNext()) {
			Stmt stmt=(Stmt)it.next();
			if (stmt instanceof IdentityStmt) {
				IdentityStmt ids=(IdentityStmt)stmt;
				if (ids.getRightOp() instanceof ParameterRef) {
					ParameterRef paramRef=(ParameterRef)ids.getRightOp();
					if (paramRef.getIndex()==arg)
						return ids;
					
				} else if (ids.getRightOp() instanceof ThisRef) {
					
				} else 
					throw new InternalError();
			} else
				throw new InternalError();
		}
		throw new InternalError();
	}
	private static Local addParameterToMethod(SootMethod method, Type type, String suggestedName) {
		//validateMethod(method);
		Body body=method.getActiveBody();
		Chain units=body.getUnits();
		List params=method.getParameterTypes();
		
		IdentityStmt lastIDStmt=null;
		if (params.isEmpty()) {
			if (units.isEmpty()) {
				if (!method.isStatic())
					throw new InternalError();
			} else {
				lastIDStmt=(IdentityStmt)units.getFirst();
				if (! (lastIDStmt.getRightOp() instanceof ThisRef))
					if (!method.isStatic())
						throw new InternalError();
			}
		} else {
		//	debug("param id: " + (params.size()-1));
			lastIDStmt=getParameterIdentityStatement(method, params.size()-1);
		}
		params.add(type);
		method.setParameterTypes(params);
		LocalGeneratorEx lg=new LocalGeneratorEx(body);
		Local l=lg.generateLocal(type, suggestedName);
		IdentityStmt newIDStmt=Jimple.v().newIdentityStmt(l, 
			Jimple.v().newParameterRef(type, params.size()-1));
		if (lastIDStmt==null)
			units.addFirst(newIDStmt);
		else
			units.insertAfter(newIDStmt, lastIDStmt);
		return l;		
	}
	
	private static void implementInterface(
		SootClass theAspect,
		SootMethod joinpointMethod,
		AdviceApplication adviceAppl,
		SootClass joinpointClass,
		Type accessReturnType,
		SootClass accessInterface,
		SootMethod accessMethod,
		SootMethod abstractAccessMethod) {
		
		String interfaceName=accessInterface.getName();
		String accessMethodName=accessMethod.getName();
		
		Body accessBody=accessMethod.getActiveBody();
		Chain accessStatements=accessBody.getUnits();
		
		boolean bStatic=joinpointMethod.isStatic();
	
		Chain joinpointStatements=joinpointMethod.getActiveBody().getUnits();
		
		SootMethod adviceMethod=adviceAppl.advice.getImpl().getSootMethod();
		if (adviceMethod==null)
			throw new InternalError();
		
		State.AccessMethodInfo accessMethodInfo=state.getAccessMethodInfo(joinpointClass.getName(), interfaceName, bStatic);
		State.InterfaceInfo interfaceInfo=state.getInterfaceInfo(interfaceName);
		
		Body joinpointBody=joinpointMethod.getActiveBody();
		Chain joinpointChain=joinpointBody.getUnits();		
		
		Stmt begin=adviceAppl.shadowpoints.getBegin();
		Stmt end=adviceAppl.shadowpoints.getEnd();
		


		if (!(adviceAppl instanceof StmtAdviceApplication)) {
			debug("NYI: advice application: " + adviceAppl);
			throw new CodeGenException(
					"Can only handle Statement-AdviceApplication "); 
		}
		StmtAdviceApplication stmtAppl=(StmtAdviceApplication)adviceAppl;
		Stmt applStmt=stmtAppl.stmt;
		
		if (!joinpointChain.contains(applStmt))
			throw new InternalError();
		
		List /*ValueBox*/ actuals=new LinkedList();
		List /*Type*/ actualsTypes=new LinkedList();
		
		Local returnedLocal=null;
		AssignStmt assignStmt=null;
		ValueBox invokeTarget=null;
		Stmt invokeStmt=null;
		ValueBox theTarget=null;
		if (applStmt instanceof AssignStmt) {
			assignStmt=(AssignStmt)applStmt;
			Value leftOp=assignStmt.getLeftOp();
			Value rightOp=assignStmt.getRightOp();
			if (leftOp instanceof Local) {
				invokeTarget=assignStmt.getRightOpBox();
				invokeStmt=assignStmt;
				returnedLocal=(Local) leftOp;
				 if (rightOp instanceof InvokeExpr) {
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
				 }
			} else if (leftOp instanceof FieldRef && rightOp instanceof Local) {
				// set...
				// must be replaced by call to void
				actuals.add(assignStmt.getRightOpBox());
				actualsTypes.add(((FieldRef)leftOp).getType());
				if (leftOp instanceof InstanceFieldRef) {
					InstanceFieldRef ix=(InstanceFieldRef) leftOp;
					theTarget=ix.getBaseBox();
				}
			} else if (leftOp instanceof FieldRef && rightOp instanceof Constant) {
				// set from constant.
				// Add an assignment to a local before stmt 
				LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);
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
				}
			} else {
				// unexpected statement type
				throw new InternalError();
			}
		} else if (applStmt instanceof InvokeStmt) {
			// call (void)
			invokeStmt=(InvokeStmt) applStmt;
			InvokeExpr invokeEx=invokeStmt.getInvokeExpr();
			for (int i=0; i<invokeEx.getArgCount(); i++)
				actuals.add(invokeEx.getArgBox(i));
			invokeTarget=invokeStmt.getInvokeExprBox();
			actualsTypes=invokeEx.getMethod().getParameterTypes();
			if (invokeEx instanceof InstanceInvokeExpr) {
				InstanceInvokeExpr ix=(InstanceInvokeExpr) invokeEx;
				theTarget=ix.getBaseBox();
			}
		} else {
			// unexpected statement type
			throw new InternalError();		
		}
		
		
//		determine parameter mappings and necessary additions
		List /*Type*/ addedDynArgsTypes=new LinkedList();
		int[] argIndex=new int[actuals.size()];
		{
			int[] currentIndex=new int[JavaTypeInfo.typeCount];
			Iterator it=actualsTypes.iterator();
			int i=0;
			while (it.hasNext()) {
				 Type type=((Type)it.next());
				 // pass all reference types as java.lang.Object
				 if (JavaTypeInfo.sootTypeToInt(type)==JavaTypeInfo.refType) {
				 	type=Scene.v().getRefType("java.lang.Object");
				 	if (type==null)
				 		throw new InternalError();
				 }
				 int typeNum=JavaTypeInfo.sootTypeToInt(type);
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
		
		{ // modify the interface definition
			SootMethod m=abstractAccessMethod;
			List p=m.getParameterTypes();
			p.addAll(addedDynArgsTypes);
			m.setParameterTypes(p);
		}

		/*{
			List boxes=stmtAppl.stmt.getUseBoxes();
			Iterator
		}*/
		
//		At the advice application statement, extract any parameters into locals.
		Local[] generatedLocals=new Local[actuals.size()];
		{  
			Iterator it=actuals.iterator();
			Iterator it2=actualsTypes.iterator();
			LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);
			int i=0;
			while (it.hasNext()) {
				ValueBox box=(ValueBox)it.next();
				Type type=(Type)it2.next();
				Value val=box.getValue();
				String name="dynArg" + argIndex[i] + "act";
				Local l=lg.generateLocal(type, name);
				AssignStmt s=Jimple.v().newAssignStmt(l, val);
				joinpointStatements.insertBefore(s, stmtAppl.shadowpoints.getBegin());	
				box.setValue(l);
				generatedLocals[i]=l;
				i++;
 			} 			
 		}
		Local targetLocal;
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
 		}

		// create list of default values for the added arguments
		List addedDynArgs=new LinkedList();
		{
			Iterator it=addedDynArgsTypes.iterator();
			while (it.hasNext()) {
				Type type=(Type) it.next();
				addedDynArgs.add(JavaTypeInfo.getDefaultValue(type));	
			}
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
		Body adviceBody=adviceMethod.getActiveBody();
		Chain adviceStatements=adviceBody.getUnits();
		List addedParameterLocals=new LinkedList();
		{ // Add the new parameters to the advice method 
		  // and keep track of the newly created locals corresponding to the parameters.
		  	//validateMethod(adviceMethod);
			 List params=adviceMethod.getParameterTypes();
			 Iterator it=addedDynArgsTypes.iterator();
			 while (it.hasNext()) {
			 	Type type=(Type)it.next();
			 	Local l=addParameterToMethod(adviceMethod, type, "dynArgFormal");
			 	addedParameterLocals.add(l);
			 }
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
				Iterator it2=addedParameterLocals.iterator();
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
			adviceMethodInfo.proceedParameters.addAll(addedParameterLocals);
		}
		
		// add parameters to all access method implementations
		{
			//Set keys=interfaceInfo.accessMethodImplementations.keySet();
			List accessMethodImplementations=interfaceInfo.getAllAccessMethodImplementations();
			Iterator it=accessMethodImplementations.iterator();
			while (it.hasNext()) {
				State.AccessMethodInfo info=
					(State.AccessMethodInfo) it.next();
				
				debug("adding parameters to " + info.method);
				validateMethod(info.method);
				
				Iterator it2=addedDynArgsTypes.iterator();
				while (it2.hasNext()) {			
					Type type=(Type)it2.next();	
					Local l=addParameterToMethod(info.method, type, "dynArgFormal");
					info.dynParamLocals.add(l);
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
		{
			// find the corresponding statement in the access method
			Stmt newStmt=(Stmt)bindings.get(stmtAppl.stmt);
	
			// Assign the correct access parameters to the locals 
			Stmt insertionPoint=(Stmt)first; 	
			for (int i=0; i<generatedLocals.length; i++) {
				Local l=(Local)bindings.get(generatedLocals[i]);
				validateMethod(accessMethodInfo.method);
				Local paramLocal=(Local)accessMethodInfo.dynParamLocals.get(argIndex[i]);
				AssignStmt s=Jimple.v().newAssignStmt(l, paramLocal);
				accessStatements.insertBefore(s, insertionPoint);
				// maybe we have to cast from Object to the original type
				insertCast(accessMethod.getActiveBody(), s, s.getRightOpBox(), l.getType());
			}
			Local l=(Local)bindings.get(targetLocal);
			AssignStmt s=Jimple.v().newAssignStmt(l, accessMethodInfo.targetLocal);
			accessStatements.insertBefore(s, insertionPoint);
			insertBoxingCast(accessBody, s);
		}
		updateSavedReferencesToStatements(bindings);
		
		// modify the lookup statement in the access method
		int shadowID=state.getUniqueShadowID();//accessMethodInfo.nextID++;
		accessMethodInfo.lookupValues.add(IntConstant.v(shadowID));
		accessMethodInfo.targets.add(switchTarget);
		// generate new lookup statement and replace the old one
		Stmt lookupStmt=Jimple.v().newLookupSwitchStmt(accessMethodInfo.idParamLocal, 
			accessMethodInfo.lookupValues, accessMethodInfo.targets, accessMethodInfo.defaultTarget);
		accessStatements.insertAfter(lookupStmt, accessMethodInfo.lookupStmt);
		accessStatements.remove(accessMethodInfo.lookupStmt);
		accessMethodInfo.lookupStmt=lookupStmt;
		
		// remove any traps from the shadow before removing the shadow
		removeTraps(joinpointBody, begin, end);
		// remove statements except original assignment
		removeStatements(joinpointBody, begin, end, applStmt);
		
		cleanLocals(accessBody);
		
		Local lThis=null;
		if (!joinpointMethod.isStatic())
			lThis=joinpointBody.getThisLocal();
		//lThis.setName("this");
		
		LocalGeneratorEx localgen=new LocalGeneratorEx(joinpointBody);	
		
		// aspectOf() call		
		Local aspectref = localgen.generateLocal( theAspect.getType(), "theAspect" );
		AssignStmt stmtAspectOf =  
			Jimple.v().newAssignStmt( 
				aspectref, 
					Jimple.v().newStaticInvokeExpr(
						theAspect.getMethod("aspectOf", new ArrayList())));
		joinpointStatements.insertBefore(stmtAspectOf,applStmt);
 	
 	
 		// generate basic invoke statement (to advice method) and preparatory stmts
		// FIXME
		Chain invokeStmts =  
					PointcutCodeGen.makeAdviceInvokeStmt 
										  (aspectref,adviceAppl,joinpointStatements,localgen,null);

		// copy all the statements before the actual call into the shadow
		InvokeExpr invokeEx= ((InvokeStmt)invokeStmts.getLast()).getInvokeExpr();
		invokeStmts.removeLast();
		for (Iterator stmtlist = invokeStmts.iterator(); stmtlist.hasNext(); ){
			Stmt nextstmt = (Stmt) stmtlist.next();
			joinpointStatements.insertBefore(nextstmt,applStmt);
		}
		
		// we need to add some of our own parameters to the invokation
		List params=new LinkedList();
		if (lThis==null) {
			params.add(NullConstant.v());
		} else {
			params.add(lThis); // pass the closure
		}
		params.add(targetLocal);
		params.add(IntConstant.v(shadowID));
		if (bStatic) { // pass the static class id
			params.add(IntConstant.v(state.getStaticDispatchTypeID(joinpointClass.getType())));
		} else {
			params.add(IntConstant.v(0));
		}
		// and add the original parameters
		params.addAll(0, invokeEx.getArgs());
		{
			Value[] parameters=new Value[interfaceInfo.dynamicArguments.size()];
			
			for (int i=0; i<argIndex.length; i++) {
				parameters[argIndex[i]]=generatedLocals[i];	
			}
			for (int i=0; i<parameters.length; i++) {
				if (parameters[i]==null) {
					parameters[i]=JavaTypeInfo.getDefaultValue((Type)interfaceInfo.dynamicArguments.get(i));	
				}	
				params.add(parameters[i]);
			}			
		}
		
		
		// generate a new invoke expression to replace the old one
		InvokeExpr invokeEx2=
			Jimple.v().newVirtualInvokeExpr( aspectref, adviceMethod, params);
		
		if (invokeTarget!=null) {
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
		}
		if (invokeStmt==null)
			throw new InternalError();
			
		interfaceInfo.adviceMethodInvokationStmts.add(invokeStmt);
		
		if (assignStmt!=null) {
			// perform cast if necessary
			insertBoxingCast(joinpointMethod.getActiveBody(), assignStmt);
			//insertCast(joinpointMethod.getActiveBody(), applStmt, assignStmt.getRightOpBox(), 
			//	assignStmt.getLeftOp().getType());
		}
	} 
	
	private static void updateSavedReferencesToStatements(HashMap bindings) {
		Set keys=state.interfaces.keySet();
		Iterator it=keys.iterator();
		// all interfaces
		while (it.hasNext()) {
			String key=(String)it.next();
			State.InterfaceInfo info=(State.InterfaceInfo) state.interfaces.get(key);
			Set keys2=bindings.keySet();
			Iterator it2=keys2.iterator();
			// all bindings
			while (it2.hasNext()) {
				Object old=it2.next();
				if (!(old instanceof Value) && !(old instanceof Stmt))
					continue;
				if (info.adviceMethodInvokationStmts.contains(old)) {
					info.adviceMethodInvokationStmts.remove(old);
					info.adviceMethodInvokationStmts.add(bindings.get(old));// replace with new
				}
				// this is only necessary if proceed calls are ever part of a shadow,
				// for example if the advice body were to be matched by an execution pointcut. 
				// TODO: does this kind of thing ever happen?
				if (info.interfaceInvokationStmts.contains(old)) {
					info.interfaceInvokationStmts.remove(old);
					info.interfaceInvokationStmts.add(bindings.get(old));// replace with new
				}				
			}			
		}
	}
	
	private static void validateMethod(SootMethod method) {
		debug("validating " + method.getName());
		
		if (method.isAbstract())
			return;
		
		Body body=method.getActiveBody();
		Chain units=body.getUnits();
		List params=method.getParameterTypes();
		
		Iterator itUnits=units.iterator();
		if (!method.isStatic()) {
			Stmt first=(Stmt)itUnits.next();		
			
			IdentityStmt id=(IdentityStmt) first;
			Local local=(Local)id.getLeftOp();
			ThisRef ref=(ThisRef)id.getRightOp();
			if (!ref.getType().equals(method.getDeclaringClass().getType()))
				throw new InternalError();
			
			if (!local.getType().equals(method.getDeclaringClass().getType()))
							throw new InternalError();
			
		}	
		
		Iterator it=params.iterator();
		int i=0;
		while (it.hasNext()) {
			Type type=(Type)it.next();
			Stmt stmt=(Stmt)itUnits.next();
			IdentityStmt id=(IdentityStmt)stmt;
			Local local=(Local)id.getLeftOp();
			ParameterRef ref=(ParameterRef)id.getRightOp();
		
			debug("  parameter " + i + ": " + type.toString() + ":" + local.getName());		
			
			if (!Type.toMachineType(local.getType()).equals(Type.toMachineType(type))) {
				debug("type mismatch: local: " + local.getType() + " param: " + type);
				throw new InternalError();
			}
			if (ref.getIndex()!=i++) {
				throw new InternalError();
			}
			if (!ref.getType().equals(type)) {
				throw new InternalError();
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
		
		Chain units=body.getUnits();
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
		State.InterfaceInfo info=state.getInterfaceInfo(interfaceName);
		Set keys=info.accessMethodImplementations.keySet();
		
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
					info.accessMethodImplementations.get(className);

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
					validateMethod(accessInfo.method);
					SpecialInvokeExpr ex=Jimple.v().newSpecialInvokeExpr(
							lThis, accessInfo.superCallTarget.getMethodByName(accessMethodName) , getParameterLocals(body));
					
					if (returnType.equals(VoidType.v())) {
						Stmt s=Jimple.v().newInvokeStmt(ex);
						statements.insertBefore(s, accessInfo.defaultEnd);
						statements.insertBefore(
							Jimple.v().newReturnVoidStmt(), 
								accessInfo.defaultEnd);
					} else {
						
						Local l=lg.generateLocal(returnType, "retVal");
						AssignStmt s=Jimple.v().newAssignStmt(l, ex);
						statements.insertBefore(s, accessInfo.defaultEnd);
						statements.insertBefore(
							Jimple.v().newReturnStmt(l),					
							accessInfo.defaultEnd);						
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
		Chain units=body.getUnits();
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
		
		Local lThisSource=null;
		if (!source.getMethod().isStatic())
			lThisSource=source.getThisLocal();
		
		Local lThisDest=null;
		if (!dest.getMethod().isStatic()) {
			lThisDest=dest.getThisLocal();
		}			
		
		HashMap bindings = new HashMap();
		//HashMap boxes=new HashMap();
		
		Iterator it = source.getUnits().iterator(begin);
		if (it.hasNext())
			it.next(); // skip begin

		Chain unitChain=dest.getUnits();
		
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
			Trap copy = (Trap) original.clone();
    
			// Add cloned unit to our trap list.
			trapChain.addLast(copy);

			
			// Store old <-> new mapping.
			bindings.put(original, copy);
		}


		Chain destLocals=dest.getLocals();

		// Clone local units.
		it = source.getLocals().iterator();
		while(it.hasNext()) {
			Local original = (Local) it.next();
			Local copy = (Local) original.clone();
    
    		if (original==lThisSource) {
				bindings.put(lThisSource, lThisDest);
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
				}
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
			insertBoxingCast(dest, s);
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
	
	private static void modifyAdviceMethod(
		AdviceApplication adviceAppl,
		SootMethod adviceMethod,
		SootClass accessInterface,
		SootMethod abstractAccessMethod,
		boolean bStatic, 
		String accessMethodName, 
		SootClass joinpointClass,
		SootClass theAspect) {
		
		Type adviceReturnType=adviceMethod.getReturnType();
		String interfaceName=accessInterface.getName();
		
		State.AdviceMethodInfo adviceMethodInfo=state.getAdviceMethodInfo(theAspect.getName(), adviceMethod.getName());
							
		debug("modifying advice method: " + adviceMethod.toString());
		
		validateMethod(adviceMethod);
		
		Body aroundBody=adviceMethod.getActiveBody();
		Chain statements=aroundBody.getUnits();
		
		List adviceMethodParameters=adviceMethod.getParameterTypes();
		
		Local lInterface=addParameterToMethod(adviceMethod, accessInterface.getType(), "accessInterface");
		Local lTarget=addParameterToMethod(adviceMethod, 
				Scene.v().getSootClass("java.lang.Object").getType(), "targetArg");
		Local lShadowID=addParameterToMethod(adviceMethod, IntType.v(), "shadowID");
		Local lStaticClassID=addParameterToMethod(adviceMethod, IntType.v(), "staticClassID");
		
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

		validateMethod(adviceMethod);
		
		adviceMethodInfo.proceedParameters.add(lTarget);
		adviceMethodInfo.proceedParameters.add(lShadowID);
		adviceMethodInfo.interfaceLocal=lInterface;
		adviceMethodInfo.targetLocal=lTarget;
		adviceMethodInfo.idLocal=lShadowID;
		adviceMethodInfo.staticDispatchLocal=lStaticClassID;
		
		
		State.InterfaceInfo interfaceInfo=state.getInterfaceInfo(interfaceName);
		
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
					
					State.AdviceMethodInfo.ProceedInvokation invokation=new 
											State.AdviceMethodInfo.ProceedInvokation();
					adviceMethodInfo.proceedInvokations.add(invokation);
					
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
				"StmtAdviceApplication.stmt is expected to be instanceof AssignStmt"); // TODO: 
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
 
