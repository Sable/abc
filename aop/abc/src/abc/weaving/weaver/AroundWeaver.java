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
			Unit defaultTarget;
			Stmt lookupStmt;
			int nextID;
			Local idParamLocal;
			
			List dynParamLocals=new LinkedList();
			
			SootMethod method;
			/*public boolean implementsField(String field) {
				return fieldIDs.containsKey(field);
			}*/
		}
		private HashMap /*ClassInterfacePair, AccessMethodInfo*/ 
			accessInterfacesImplementations=new HashMap();
		/*public boolean hasInterface(String className, String interfaceName) {
			ClassInterfacePair key=new ClassInterfacePair(className, interfaceName);
			return accessInterfacesGet.containsKey(key);
		}*/
		public AccessMethodInfo getMethodInfo(String className, String interfaceName, boolean bStatic) {
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
					AdviceApplication adviceappl) {
		debug("Handling aound: " + adviceappl);
		//if (cl!=null) return;
		//SootClass cl2=cl;
		Body b = joinpointMethod.getActiveBody();
		boolean bStatic=joinpointMethod.isStatic();
		Chain units = b.getUnits();
		AdviceDecl advicedecl = adviceappl.advice;
		AdviceSpec advicespec = advicedecl.getAdviceSpec();
		AroundAdvice aroundspec = (AroundAdvice) advicespec;
		SootClass theAspect =
			advicedecl.getAspect().getInstanceClass().getSootClass();
		SootMethod adviceMethod = advicedecl.getImpl().getSootMethod();
	
		Type adviceReturnType=adviceMethod.getReturnType();
	
		/*debug("Advice application - kind:" + adviceappl.sjpInfo.kind + 
				" signatureType: " + adviceappl.sjpInfo.signatureType +
				" signature: " + adviceappl.sjpInfo.signature);*/
				
		AdviceApplication adviceAppl = adviceappl;
	
		Chain units1=joinpointMethod.getActiveBody().getUnits();
		
		//InstanceFieldRef fieldRef=(InstanceFieldRef) accessFieldRef;
		//arddebug("found field access: " + fieldRef.getField().getName());
		
		
		//adviceReturnType;
		
		String typeName=
				adviceReturnType.toString();// .getClass().getName();
		
		//String accessTypeString= bGet ? "get" : "set";
		
		
		final boolean interfacePerAdviceMethod=true;
		String adviceMethodIdentifierString="$" + 	theAspect.getName() + "$" + adviceMethod.getName();
		
		String interfaceName="abc$access$" + typeName + 
			(interfacePerAdviceMethod ? adviceMethodIdentifierString : "");
		
		String accessMethodName;
		String dynamicAccessMethodName="abc$proceed$" + typeName+ 
			(interfacePerAdviceMethod ? adviceMethodIdentifierString : "");;	
		if (bStatic) {
			accessMethodName="abc$static$proceed$" + typeName+ 
						(interfacePerAdviceMethod ? adviceMethodIdentifierString : "");	
		} else {
			accessMethodName=dynamicAccessMethodName;
		}
		
		List /*type*/ accessMethodParameters=new LinkedList();
		accessMethodParameters.add(IntType.v()); // the id
		
		SootClass accessInterface=null;
		SootMethod abstractAccessMethod=null;
	
		// create "get" interface if it doesn't exist
		if (Scene.v().containsClass(interfaceName)) {
			debug("found access interface in scene");
			accessInterface=Scene.v().getSootClass(interfaceName);
			abstractAccessMethod=accessInterface.getMethodByName(dynamicAccessMethodName);
		} else {
			debug("generating access interface type");
			accessInterface=new SootClass(interfaceName, 
				Modifier.INTERFACE | Modifier.PUBLIC);						
			
			accessInterface.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
			
			abstractAccessMethod=
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
		
		
		SootMethod localAccessMethod=null;
		 Body accessBody=null;
		 Chain accessStatements=null;
		 State.AccessMethodInfo accessMethodInfo=state.getMethodInfo(joinpointClass.getName(), interfaceName, bStatic);

		 Type returnType=adviceMethod.getReturnType() ;
		boolean createdNewAccessMethod=false;		 
		 if (bStatic) {
		 	try {
				localAccessMethod=joinpointClass.getMethodByName(accessMethodName);
		 	}
		 	catch (Exception ex) {
//				create new method					
				 localAccessMethod=new SootMethod(
					 accessMethodName, accessMethodParameters, returnType ,
						 Modifier.PUBLIC | Modifier.STATIC);
						 
				createdNewAccessMethod=true;
		 	}
		 	
		 } else {
			if (joinpointClass.implementsInterface(interfaceName)){
				debug("found interface " + interfaceName + " in class: " + joinpointClass.getName());
				SootClass cl2=joinpointClass;
				localAccessMethod=cl2.getMethodByName(accessMethodName);
				accessBody=localAccessMethod.getActiveBody();
				accessStatements=accessBody.getUnits();
			} else {
				debug("adding interface " + interfaceName + " to class " + joinpointClass.getName());
				
				joinpointClass.addInterface(accessInterface);
		
				
			
				// create new method					
				localAccessMethod=new SootMethod(
					accessMethodName, accessMethodParameters, returnType ,
						Modifier.PUBLIC);
		
				createdNewAccessMethod=true;	
						
			}
		 }
		if (createdNewAccessMethod) {
			accessBody=Jimple.v().newBody(localAccessMethod);
		
			localAccessMethod.setActiveBody(accessBody);
			debug("adding method " + localAccessMethod.getName() + " to class " + joinpointClass.getName());
			joinpointClass.addMethod(localAccessMethod);

			accessMethodInfo.method=localAccessMethod;

			accessStatements=accessBody.getUnits();

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
			//lThis.setName("this");
			//getBody.getThisLocal();
			// $i0 := @parameter0: int;
			accessMethodInfo.idParamLocal=lg.generateLocal(IntType.v(), "id");
			Stmt paramIdStmt=Jimple.v().newIdentityStmt(accessMethodInfo.idParamLocal, 
			Jimple.v().newParameterRef(IntType.v(),0));
			if (lThis==null) {
				accessStatements.addFirst(paramIdStmt);
			} else {
				accessStatements.insertAfter(paramIdStmt,
				 accessStatements.getFirst());
			}
			Local p3=null;
			Stmt lastIDStmt=paramIdStmt;
			
			// generate exception code (default target)
			SootClass exception=Scene.v().getSootClass("java.lang.RuntimeException");	
			Local ex=lg.generateLocal(exception.getType(), "exception");
			Stmt newExceptStmt = Jimple.v().newAssignStmt( ex, Jimple.v().newNewExpr( exception.getType() ) );
			Stmt initEx=Jimple.v().newInvokeStmt( Jimple.v().newSpecialInvokeExpr( ex, exception.getMethod( "<init>", new ArrayList()))) ;
			Stmt throwStmt=Jimple.v().newThrowStmt(ex);
			accessStatements.add(newExceptStmt);
			accessStatements.add(initEx);
			accessStatements.add(throwStmt);
			accessMethodInfo.defaultTarget=newExceptStmt;

			// just generate a nop for now.
			accessMethodInfo.lookupStmt=Jimple.v().newNopStmt();

			accessStatements.insertAfter(accessMethodInfo.lookupStmt, lastIDStmt);
		}
		
		// Change advice method: add parameters and replace proceed with call to get-interface
		{
			boolean bFirstModification=
				adviceMethod.getParameterCount()==adviceAppl.advice.numFormals();
			if (bFirstModification) {
				modifyAdviceMethod(adviceAppl, adviceMethod, accessInterface, abstractAccessMethod, 
					returnType, bStatic, accessMethodName, joinpointClass,
					theAspect, interfaceName
					);
			}
			generateProceedCalls(adviceAppl, adviceMethod, accessInterface, abstractAccessMethod, 
				adviceReturnType, bStatic, accessMethodName, joinpointClass,
				theAspect, localAccessMethod, interfaceName);
			
		}
		implementInterface(
			theAspect,
			joinpointMethod,
			adviceAppl,
			 joinpointClass,
			 accessMethodName,
			 interfaceName,
			 accessMethodParameters,
			 adviceReturnType,
			 accessInterface,
			 localAccessMethod
			 );
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
		
		State.AdviceMethodInfo info=state.getAdviceMethodInfo(theAspect.getName(), adviceMethod.getName());
		State.InterfaceInfo interfaceInfo=state.getInterfaceInfo(interfaceName);
		
		Iterator it=info.proceedInvokations.iterator();
		String newStaticInvoke=null;
		if (bStatic) {
			if (!info.staticProceedTypes.contains(joinpointClass.getName())) {
				newStaticInvoke=joinpointClass.getName();
				info.staticProceedTypes.add(joinpointClass.getName());
			}
		} else {
			info.hasDynamicProceed=true;	
		}
		
		
		Body body=adviceMethod.getActiveBody();
		Chain statements=body.getUnits().getNonPatchingChain();
		
		while (it.hasNext()) {
			State.AdviceMethodInfo.ProceedInvokation invokation=
				(State.AdviceMethodInfo.ProceedInvokation)it.next();
			
			// TODO: fix interfaceInfo.interfaceInvokationStmts
			removeStatements(body, invokation.begin, invokation.end, null);
			
			
			if (invokation.dynamicInvoke==null && info.hasDynamicProceed){
				InvokeExpr newInvokeExpr=
							Jimple.v().newInterfaceInvokeExpr( 
								info.interfaceLocal, abstractAccessMethod, info.proceedParameters);
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
								m , info.proceedParameters);
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
			
			if (info.staticProceedTypes.isEmpty()) {
				statements.insertAfter(invokation.dynamicInvoke, invokation.begin);					
			} else if (info.hasDynamicProceed==false && info.staticProceedTypes.size()==1) {
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
				 
				Local key=info.staticDispatchLocal; ///
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
		Chain units=body.getUnits();
		if (!source.getValue().getType().equals(targetType)) {
			LocalGenerator localgen=new LocalGenerator(body);
			Local castLocal=localgen.generateLocal(source.getValue().getType());
			debug("cast: source has type " + source.getValue().getType().toString());
			debug("cast: target has type " + targetType.toString());
			AssignStmt tmpStmt=Jimple.v().newAssignStmt(castLocal, source.getValue());
			CastExpr castExpr=Jimple.v().newCastExpr(castLocal,targetType);
		//	Jimple.v().newCastExpr()
			units.insertBefore(tmpStmt, stmt);
			if (stmt instanceof AssignStmt) {
				source.setValue(castExpr);
			} else {
				Local tmpLocal=localgen.generateLocal(targetType);
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
	private static InvokeExpr createNewInvokeExpr(InvokeExpr old, List newArgs) {
		if (old instanceof InstanceInvokeExpr) {
			Local base=(Local)((InstanceInvokeExpr)old).getBase();
			if (old instanceof InterfaceInvokeExpr)
				return Jimple.v().newInterfaceInvokeExpr(base, old.getMethod(), newArgs);
			else if (old instanceof SpecialInvokeExpr) {
				if (newArgs.size()>0)
					throw new RuntimeException();
				return Jimple.v().newSpecialInvokeExpr(base, old.getMethod());
			} else if (old instanceof VirtualInvokeExpr)
				return Jimple.v().newVirtualInvokeExpr(base, old.getMethod(), newArgs);
			else
				throw new RuntimeException();
		}  else {
			return Jimple.v().newStaticInvokeExpr(old.getMethod(), newArgs);
		}
	}
	
	private static IdentityStmt getParameterIdentityStatement(SootMethod method, int arg) {
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
					throw new RuntimeException();
			} else
				throw new RuntimeException();
		}
		throw new RuntimeException();
	}
	private static Local addParameterToMethod(SootMethod method, Type type) {
		//validateMethod(method);
		Body body=method.getActiveBody();
		Chain units=body.getUnits();
		List params=method.getParameterTypes();
		
		IdentityStmt lastIDStmt=null;
		if (params.isEmpty()) {
			lastIDStmt=(IdentityStmt)units.getFirst();
			if (! (lastIDStmt.getRightOp() instanceof ThisRef))
				if (!method.isStatic())
					throw new RuntimeException();
		} else {
		//	debug("param id: " + (params.size()-1));
			lastIDStmt=getParameterIdentityStatement(method, params.size()-1);
		}
		params.add(type);
		method.setParameterTypes(params);
		LocalGenerator lg=new LocalGenerator(body);
		Local l=lg.generateLocal(type);
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
		String accessMethodName,
		String interfaceName,
		List accessParameters,
		Type accessReturnType,
		SootClass accessInterface,
		SootMethod localAccessMethod) {
		
		Body accessBody=localAccessMethod.getActiveBody();
		Chain accessStatements=accessBody.getUnits();
		
		boolean bStatic=joinpointMethod.isStatic();
	
		Chain units=joinpointMethod.getActiveBody().getUnits();
		
		SootMethod adviceMethod=adviceAppl.advice.getImpl().getSootMethod();
		if (adviceMethod==null)
			throw new RuntimeException();
		
		State.AccessMethodInfo info=state.getMethodInfo(joinpointClass.getName(), interfaceName, bStatic);
		State.InterfaceInfo interfaceInfo=state.getInterfaceInfo(interfaceName);

		
		Body joinpointBody=joinpointMethod.getActiveBody();
		
		Chain joinpointChain=joinpointBody.getUnits();		
		
		Stmt begin=adviceAppl.shadowpoints.getBegin();
		Stmt end=adviceAppl.shadowpoints.getEnd();
		
							 
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
 

		if (!(adviceAppl instanceof StmtAdviceApplication)) {
			debug("NYI: advice application: " + adviceAppl);
			throw new CodeGenException(
					"Can only handle Statement-AdviceApplication "); 
		}
		StmtAdviceApplication stmtAppl=(StmtAdviceApplication)adviceAppl;
		Stmt applStmt=stmtAppl.stmt;
		
		if (!joinpointChain.contains(applStmt))
			throw new RuntimeException();
		
		List /*ValueBox*/ actuals=new LinkedList();
		List /*Type*/ actualsTypes=new LinkedList();
		
		Local returnedLocal=null;
		AssignStmt assignStmt=null;
		ValueBox invokeTarget=null;
		Stmt invokeStmt=null;
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
					
				 } else if (rightOp instanceof FieldRef) {
				 	// get
				 	// no actuals
				 }
			} else if (leftOp instanceof FieldRef && rightOp instanceof Local) {
				// set...
				// must be replaced by call to void
				actuals.add(assignStmt.getRightOpBox());
				actualsTypes.add(((FieldRef)leftOp).getType());
			} else if (leftOp instanceof FieldRef && rightOp instanceof Constant) {
				// set from constant.
				// Add an assignment to a local before stmt 
				LocalGeneratorEx lg=new LocalGeneratorEx(joinpointBody);
				Local l=lg.generateLocal(((Constant)rightOp).getType());
				AssignStmt s=Jimple.v().newAssignStmt(l, rightOp);
				joinpointChain.insertBefore(s, adviceAppl.shadowpoints.getBegin());
				assignStmt.setRightOp(l);
				actuals.add(assignStmt.getRightOpBox());
				actualsTypes.add(((FieldRef)leftOp).getType());
				//throw new CodeGenException("Can't handle assignment from constant yet");
			} else {
				// unexpected statement type
				throw new RuntimeException();
			}
		} else if (applStmt instanceof InvokeStmt) {
			// call (void)
			invokeStmt=(InvokeStmt) applStmt;
			InvokeExpr invokeEx=invokeStmt.getInvokeExpr();
			for (int i=0; i<invokeEx.getArgCount(); i++)
				actuals.add(invokeEx.getArgBox(i));
			invokeTarget=invokeStmt.getInvokeExprBox();
			actualsTypes=invokeEx.getMethod().getParameterTypes();
		} else {
			// unexpected statement type
			throw new RuntimeException();		
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
				 		throw new RuntimeException();
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
			SootMethod m=localAccessMethod;
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
				String name="abc$dynarg$" + argIndex[i];
				Local l=lg.generateLocal(type, name);
				AssignStmt s=Jimple.v().newAssignStmt(l, val);
				units.insertBefore(s, stmtAppl.shadowpoints.getBegin());	
				box.setValue(l);
				generatedLocals[i]=l;
				i++;
 			} 			
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
				newParams.addAll(addedDynArgs); /// should do deep copy?	
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
			 	Local l=addParameterToMethod(adviceMethod, type);
			 	addedParameterLocals.add(l);
			 }
		}
		
		{ // Modify the interface invokations. These must all be in the advice method.
			// This constraint is violated by adviceexecution() pointcuts.
			Iterator it=interfaceInfo.interfaceInvokationStmts.iterator();
			while (it.hasNext()) {
				Stmt stmt=(Stmt)it.next();
				if (!adviceStatements.contains(stmt))
					throw new RuntimeException();
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
		}
		
		// add parameters to all access method implementations
		{
			Set keys=interfaceInfo.accessMethodImplementations.keySet();
			Iterator it=keys.iterator();
			while (it.hasNext()) {
				State.AccessMethodInfo accessMethodInfo=
					(State.AccessMethodInfo)
						interfaceInfo.accessMethodImplementations.get((String)it.next());
				
				Iterator it2=addedDynArgsTypes.iterator();
				while (it2.hasNext()) {			
					Type type=(Type)it2.next();	
					Local l=addParameterToMethod(accessMethodInfo.method, type);
					accessMethodInfo.dynParamLocals.add(l);
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
				info.lookupStmt, returnedLocal, result);
			first=(Stmt)result.object;
			switchTarget=Jimple.v().newNopStmt();
			accessStatements.insertBefore(switchTarget, first);
		}
		{
			// find the corresponding statement in the access method
			Stmt newStmt=(Stmt)bindings.get(stmtAppl.stmt);
	
			// get last ID stmt in access method so we can insert statements after that
			Stmt insertionPoint=(Stmt)first; /*
				getParameterIdentityStatement(localAccessMethod, 
					localAccessMethod.getParameterCount()-1);*/	
			for (int i=0; i<generatedLocals.length; i++) {
				Local l=(Local)bindings.get(generatedLocals[i]);
				Local paramLocal=(Local)info.dynParamLocals.get(argIndex[i]);
				AssignStmt s=Jimple.v().newAssignStmt(l, paramLocal);
				accessStatements.insertBefore(s, insertionPoint);
				// maybe we have to cast from Object to the original type
				insertCast(localAccessMethod.getActiveBody(), s, s.getRightOpBox(), l.getType());
			}
		}
		//interfaceInfo.adviceMethodInvokationMethods.add(localAccessMethod);
		// just in case this is an advice method being modified.
		//interfaceInfo.interfaceInvokationMethods.add(localAccessMethod);
		updateSavedReferencesToStatements(bindings);
		
		// modify the lookup statement in the access method
		int intId=info.nextID++;
		info.lookupValues.add(IntConstant.v(intId));
		info.targets.add(switchTarget);
		// generate new lookup statement and replace the old one
		Stmt lookupStmt=Jimple.v().newLookupSwitchStmt(info.idParamLocal, 
			info.lookupValues, info.targets, info.defaultTarget);
		accessStatements.insertAfter(lookupStmt, info.lookupStmt);
		accessStatements.remove(info.lookupStmt);
		info.lookupStmt=lookupStmt;
		
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
		AssignStmt stmt1 =  
			Jimple.v().newAssignStmt( 
				aspectref, 
					Jimple.v().newStaticInvokeExpr(
						theAspect.getMethod("aspectOf", new ArrayList())));
		units.insertBefore(stmt1,applStmt);
 	
 	
 		// generate basic invoke statement (to advice method) and preparatory stmts
		Chain invokeStmts =  
					PointcutCodeGen.makeAdviceInvokeStmt 
										  (aspectref,adviceAppl,units,localgen);

		// copy all the statements before the actual call into the shadow
		InvokeExpr invokeEx= ((InvokeStmt)invokeStmts.getLast()).getInvokeExpr();
		invokeStmts.removeLast();
		for (Iterator stmtlist = invokeStmts.iterator(); stmtlist.hasNext(); ){
			Stmt nextstmt = (Stmt) stmtlist.next();
			units.insertBefore(nextstmt,applStmt);
		}
		
		// we need to add some of our own parameters to the invokation
		List params=new LinkedList();
		if (lThis==null) {
			params.add(NullConstant.v());
		} else {
			params.add(lThis); // pass the closure
		}
		params.add(IntConstant.v(intId));
		if (bStatic) {
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
				throw new RuntimeException(); // must/should never be reached
			// set()
			// replace old "fieldref=local" with invokation 
			invokeStmt=Jimple.v().newInvokeStmt(invokeEx2);
			units.insertAfter(invokeStmt, assignStmt);
			units.remove(assignStmt);			
			stmtAppl.stmt=invokeStmt;
			invokeTarget=invokeStmt.getInvokeExprBox();
			//Jimple.v().newEqExpr()
		}
		if (invokeStmt==null)
			throw new RuntimeException();
			
		interfaceInfo.adviceMethodInvokationStmts.add(invokeStmt);
		
		if (assignStmt!=null) {
			// perform cast if necessary
			insertCast(joinpointMethod.getActiveBody(), applStmt, assignStmt.getRightOpBox(), 
				assignStmt.getLeftOp().getType());
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
				throw new RuntimeException();
			
			if (!local.getType().equals(method.getDeclaringClass().getType()))
							throw new RuntimeException();
			
		}
		
		// idStmt.getRightOp() 
	
		
		
		Iterator it=params.iterator();
		int i=0;
		while (it.hasNext()) {
			Type type=(Type)it.next();
			Stmt stmt=(Stmt)itUnits.next();
			IdentityStmt id=(IdentityStmt)stmt;
			Local local=(Local)id.getLeftOp();
			ParameterRef ref=(ParameterRef)id.getRightOp();
		
			debug("  parameter " + i + ": " + type.toString() + ":" + local.getName());
				
			
			if (!(local).getType().equals(type)) {
				throw new RuntimeException();
			}
			if (ref.getIndex()!=i++) {
				throw new RuntimeException();
			}
			if (!ref.getType().equals(type)) {
				throw new RuntimeException();
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


		Chain localChain=dest.getLocals();

		// Clone local units.
		it = source.getLocals().iterator();
		while(it.hasNext()) {
			Local original = (Local) it.next();
			Local copy = (Local) original.clone();
    
    		if (original==lThisSource) {
				bindings.put(lThisSource, lThisDest);
    		} else {
				copy.setName(copy.getName() + "$abc$" + state.getUniqueID());
				// Add cloned unit to our local list.
				localChain.addLast(copy);

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
				throw new RuntimeException();
			
			ReturnStmt returnStmt=Jimple.v().newReturnStmt(newLocal);	
			unitChain.insertAfter(returnStmt, insertAfter);
			insertCast(dest, returnStmt, returnStmt.getOpBox(), dest.getMethod().getReturnType());
			//JasminClass
			insertAfter=returnStmt;
		} else {
			if (!dest.getMethod().getReturnType().equals(VoidType.v()))
				throw new RuntimeException();
			
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
		Type returnType,
		boolean bStatic, 
		String accessMethodName, 
		SootClass joinpointClass,
		SootClass theAspect,
		String interfaceName) {
		
		State.AdviceMethodInfo info=state.getAdviceMethodInfo(theAspect.getName(), adviceMethod.getName());
		//boolean bSet=!bGet;
		
					
		debug("modifying advice method: " + adviceMethod.toString());
		
		validateMethod(adviceMethod);
		
		Body aroundBody=adviceMethod.getActiveBody();
		Chain statements=aroundBody.getUnits();
		
		List aroundParameters=adviceMethod.getParameterTypes();
		
		aroundParameters.add(accessInterface.getType());
		int interfaceParam=aroundParameters.size()-1;
		aroundParameters.add(IntType.v());			
		int idParam=aroundParameters.size()-1;	
		aroundParameters.add(IntType.v());
		int staticDispatchParam=aroundParameters.size()-1;
		
		adviceMethod.setParameterTypes(aroundParameters);
		
		
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
				throw new RuntimeException();

			IdentityStmt id=(IdentityStmt)lastExistingIDStmt;
	
			Local local=(Local)id.getLeftOp();

			// local.

			if ( local!=aroundBody.getParameterLocal(0))
				throw new RuntimeException();

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

		validateMethod(adviceMethod);
		
		info.proceedParameters.add(l2);
		info.interfaceLocal=lInterface;
		info.idLocal=l2;
		info.staticDispatchLocal=l3;
		
		
		State.InterfaceInfo interfaceInfo=state.getInterfaceInfo(interfaceName);
		
		Iterator it=statements.snapshotIterator();
		while (it.hasNext()) { /// TODO: Check if all cases of proceed invokations are caught
			Stmt s=(Stmt)it.next();
			InvokeExpr invokeEx=null;
			try {
				invokeEx=s.getInvokeExpr();
			} catch(RuntimeException ex) {
				
			}
			
			if (invokeEx!=null) {
				if (invokeEx.getMethod().getName().startsWith("proceed$")) {
					
					State.AdviceMethodInfo.ProceedInvokation invokation=new 
											State.AdviceMethodInfo.ProceedInvokation();
					info.proceedInvokations.add(invokation);
					
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
