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
import soot.jimple.IdentityStmt;import soot.jimple.InstanceFieldRef;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.util.Chain;
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
	public static boolean debug = true;

	private static void arddebug(String message) {
		if (debug)
			System.err.println("ARD *** " + message);
	}


	public static class State {
		private static class ClassInterfacePair {
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

		}
		public static class AccessMethodInfo {
			HashMap /*String, Integer*/ fieldIDs=new HashMap();
			
			List targets=new LinkedList();
			List lookupValues=new LinkedList();
			Unit defaultTarget;
			int nextID;
			
			
			public boolean implementsField(String field) {
				return fieldIDs.containsKey(field);
			}
		}
		HashMap /*ClassInterfacePair, AccessMethodInfo*/ accessInterfacesGet=new HashMap();
		
		/*public boolean hasInterface(String className, String interfaceName) {
			ClassInterfacePair key=new ClassInterfacePair(className, interfaceName);
			return accessInterfacesGet.containsKey(key);
		}*/
		public AccessMethodInfo getMethodInfo(String className, String interfaceName) {
			ClassInterfacePair key=new ClassInterfacePair(className, interfaceName);
			if (!accessInterfacesGet.containsKey(key)) {
				accessInterfacesGet.put(key, new AccessMethodInfo());
			}
			return (AccessMethodInfo)accessInterfacesGet.get(key);
		}
	}
	public static State state=new State();


	public static void doWeave(
					SootClass cl,
					SootMethod method,
					LocalGenerator localgen,
					AdviceApplication adviceappl) {
		arddebug("Handling aound: " + adviceappl);
		//if (cl!=null) return;
		SootClass cl2=cl;
		Body b = method.getActiveBody();
		Chain units = b.getUnits();
		AdviceDecl advicedecl = adviceappl.advice;
		AdviceSpec advicespec = advicedecl.getAdviceSpec();
		AroundAdvice aroundspec = (AroundAdvice) advicespec;
		SootClass theAspect =
			advicedecl.getAspect().getInstanceClass().getSootClass();
		SootMethod adviceMethod = advicedecl.getImpl().getSootMethod();
	
		Type adviceReturnType=adviceMethod.getReturnType();
	
		arddebug("Advice application - kind:" + adviceappl.sjpInfo.kind + 
				" signatureType: " + adviceappl.sjpInfo.signatureType +
				" signature: " + adviceappl.sjpInfo.signature);
	
		// find out what kind of pointcut 
		if (adviceappl instanceof StmtAdviceApplication) {
			arddebug("found statement advice application");
			StmtAdviceApplication stmtAdv=(StmtAdviceApplication) adviceappl;
			//stmtAdv.
			//advicedecl.
			// is it an assignment?
			if (stmtAdv.stmt instanceof AssignStmt) {
				arddebug("found assignment statement");
				AssignStmt assignStmt=(AssignStmt)stmtAdv.stmt;
				
				Value rightOp=assignStmt.getRightOp();
				Value leftOp=assignStmt.getLeftOp();
				// is it a field access?
				boolean bGetInstance=rightOp instanceof InstanceFieldRef;
				boolean bGetStatic=rightOp instanceof StaticFieldRef;
				boolean bSetInstance=leftOp instanceof InstanceFieldRef;
				boolean bSetStatic=leftOp instanceof StaticFieldRef;
				boolean bGet=bGetInstance || bGetStatic;
				boolean bStatic=bGetStatic || bSetStatic;
				if (bGetInstance || bGetStatic || bSetInstance || bSetStatic) { 
					weaveGetSet(
						cl,
						localgen,
						cl2,
						units,
						theAspect,
						adviceMethod,
						assignStmt,
						bGet ? rightOp : leftOp, 
						bGet,  
						bStatic
						 );
				}				
			} else {
				arddebug("NYI: type of stmt advice application " + adviceappl);
			}			
		} else if (adviceappl instanceof ExecutionAdviceApplication) {
			arddebug("NYI: execution advice application: " + adviceappl);
		} else {
			arddebug("NYI: advice application: " + adviceappl);
		}
	} // method doWeave 

	private static void weaveGetSet(
		SootClass cl,
		LocalGenerator localgen,
		SootClass cl2,
		Chain units,
		SootClass theAspect,
		SootMethod adviceMethod,
		AssignStmt assignStmt,
		Value accessFieldRef, boolean bGet, boolean bStatic) {
		
		boolean bSet=!bGet;
		boolean bDynamic=!bStatic;
		
		InstanceFieldRef fieldRef=(InstanceFieldRef) accessFieldRef;
		arddebug("found field access: " + fieldRef.getField().getName());
		
		Type adviceReturnType=adviceMethod.getReturnType();
		Type accessInterfaceType=adviceReturnType;
		
		String typeName=
			accessInterfaceType.toString();// .getClass().getName();
		
		String accessTypeString= bGet ? "get" : "set";
		
		String interfaceName="abc$" + accessTypeString + "$" + typeName;
		
		String methodName="abc$proceed$" +accessTypeString + "$" + typeName;
		
		List /*type*/ accessMethodParameters=new LinkedList();
		accessMethodParameters.add(IntType.v());
		if (bSet) {
			accessMethodParameters.add(accessInterfaceType);
		}
		
		SootClass accessInterface;
		SootMethod accessMethod;
		// create "get" interface if it doesn't exist
		if (Scene.v().containsClass(interfaceName)) {
			arddebug("found access interface");
			accessInterface=Scene.v().getSootClass(interfaceName);
			accessMethod=accessInterface.getMethodByName(methodName);
		} else {
			arddebug("generating access interface");
			accessInterface=new SootClass(interfaceName, 
				Modifier.INTERFACE | Modifier.PUBLIC);						
			
			accessInterface.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
			
			accessMethod=
				new SootMethod(methodName, accessMethodParameters, 
					bGet ? accessInterfaceType : VoidType.v(),
						Modifier.ABSTRACT | 
						Modifier.PUBLIC);
			
			accessInterface.addMethod(accessMethod);
			//signature.setActiveBody(Jimple.v().newBody(signature));
			
			
			Scene.v().addClass(accessInterface);
			
			GlobalAspectInfo.v().getGeneratedClasses().add(interfaceName);						 
		}
		
		// create list of fields of this type
		Collection /*SootField */ relevantFields=new LinkedList();
		int fieldIndex=0; // will store index of current field
		{
			int i=0;
			Chain fields=cl2.getFields();
			Iterator it=fields.iterator();
			while (it.hasNext()) {
				SootField field=(SootField) it.next();
				if (field.getType().toString().equals(typeName)) { /// use equals directly?
					arddebug("found relevant field: " + field.getName());
					relevantFields.add(field);
					if (fieldRef.getField().equals(field)) {
						fieldIndex=i;	
						arddebug("current field under consideration is " + field.getName());
					}
					i++;								
				}							
			}
		}
		
		
		
		// add interface to class if it doesn't exist.
		// implement the "get" method
		if (!cl.implementsInterface(interfaceName)){
			implementInterface(
				cl,
				accessFieldRef,
				fieldRef,
				methodName,
				accessMethodParameters,
				accessInterface,
				relevantFields,
				bGet, 
				bStatic);
			
			
		}
		
		// add parameters to around() method if it doesn't exist
		// and replace proceed with call to get-interface
		if (adviceMethod.getParameterCount()==0 ||
			 !adviceMethod.getParameterType(0).toString().equals(interfaceName)) {
			modifyAdviceMethod(adviceMethod, accessInterface, accessMethod, 
			    fieldRef.getType(),
				accessInterfaceType,
				bGet, bStatic
				);
		}
	
		arddebug("replacing field access with call to advice method");
		Local aspectref = localgen.generateLocal( theAspect.getType() );
	 	//smt1:  aspectref = <AspectType>.aspectOf();
     	AssignStmt stmt1 =  
	   		Jimple.v().newAssignStmt( 
		   		aspectref, 
		   			Jimple.v().newStaticInvokeExpr(
		      			theAspect.getMethod("aspectOf", new ArrayList())));
		 arddebug("Generated stmt1: " + stmt1);
	
	 	units.insertBefore(stmt1,assignStmt);
	 
	    IdentityStmt id=(IdentityStmt) units.getFirst();
	    List params=new LinkedList();
	    params.add(id.getLeftOp());
	    params.add(IntConstant.v(fieldIndex));
	    if (bSet) {
	    	// replace assignment to field with assignment to local
	    	Local lVal=localgen.generateLocal(fieldRef.getType());
	    	assignStmt.setLeftOp(lVal);
	    	params.add(lVal);
	    }
	 	InvokeExpr invokeEx=
	 		Jimple.v().newVirtualInvokeExpr( aspectref, adviceMethod, params);
	 	
	 	if (bGet) {
			assignStmt.setRightOp(invokeEx);
	 	} else {
	 		InvokeStmt invokeStmt=Jimple.v().newInvokeStmt(invokeEx);
	 		units.insertAfter(invokeStmt, assignStmt);
	 	}
		
	}

	private static void modifyAdviceMethod(
		SootMethod adviceMethod,
		SootClass accessInterface,
		SootMethod accessMethod,
		Type fieldType,
		Type accessInterfaceType,
		boolean bGet,
		boolean bStatic) {
		
		boolean bSet=!bGet;
			
			
		arddebug("modifying advice method: " + adviceMethod.toString());
		List aroundParameters=adviceMethod.getParameterTypes();
		// params are added in reverse order...
		if (bSet) {
			aroundParameters.add(0, fieldType);	
		}
		aroundParameters.add(0, IntType.v());
		aroundParameters.add(0, accessInterface.getType());		
		adviceMethod.setParameterTypes(aroundParameters);
		Body aroundBody=adviceMethod.getActiveBody();
		Chain statements=aroundBody.getUnits();
		LocalGenerator localgen2 = new LocalGenerator(aroundBody);
		Local l=localgen2.generateLocal(accessInterface.getType());
		// insert id for first param (interface reference)
		Stmt intRefIDstmt=Jimple.v().newIdentityStmt(l, 
				Jimple.v().newParameterRef(	
						accessInterface.getType(),0));
		statements.insertAfter(intRefIDstmt, statements.getFirst());
		// id for second param (id of field accessed)
		Local l2=localgen2.generateLocal(IntType.v());
		Stmt fieldIDStmt=Jimple.v().newIdentityStmt(l2, 
				Jimple.v().newParameterRef(IntType.v(),1));
		statements.insertAfter(fieldIDStmt, intRefIDstmt);
		// id for third param (value for set operation)
		Local l3=null;
		if (bSet) {
			l3=localgen2.generateLocal(fieldType);
			Stmt valueIDStmt=Jimple.v().newIdentityStmt(l3, 
					Jimple.v().newParameterRef(fieldType,2));
			statements.insertAfter(valueIDStmt, fieldIDStmt);
		}
		List proceedParams=new LinkedList();
		
		proceedParams.add(l2);
		if (bSet) {
			proceedParams.add(l3);
		}
		
		Iterator it=statements.snapshotIterator();
		while (it.hasNext()) { /// TODO: Check if all cases of proceed invokations are caught
			Stmt s=(Stmt)it.next();
			if (s instanceof InvokeStmt) {
				InvokeStmt i=(InvokeStmt)s;
				arddebug("found invoke statement in around() method " + 
					i.getInvokeExpr().getMethod().getName());
				if (i.getInvokeExpr().getMethod().getName().startsWith("proceed$")) {
					arddebug("modifying proceed call in around() method ");
					IdentityStmt id=(IdentityStmt) statements.getFirst();
					Local local= aroundBody.getParameterLocal(0);								
					InvokeExpr invokeExpr=
						Jimple.v().newInterfaceInvokeExpr( 
							local, accessMethod, proceedParams);
					i.setInvokeExpr(invokeExpr);
				}											
			} else if (s instanceof AssignStmt) {
				AssignStmt a=(AssignStmt)s;
				Value r=a.getRightOp();
				if (r instanceof InvokeExpr) {
					InvokeExpr invokeExpr=(InvokeExpr) r;									
					if (invokeExpr.getMethod().getName().startsWith("proceed$")) {
						arddebug("replacing proceed$ call (invoke expression) in advice method");		
						IdentityStmt id=(IdentityStmt) statements.getFirst();
						Local local= aroundBody.getParameterLocal(0);								
						invokeExpr=
							Jimple.v().newInterfaceInvokeExpr( 
								local, accessMethod, proceedParams);
						a.setRightOp(invokeExpr);
					}
				}
			}
		}
	}

	private static void implementInterface(
		SootClass cl,
		Value accessFieldReference,
		InstanceFieldRef fieldRef,
		String methodName,
		List accessParameters,
		SootClass accessInterface,
		Collection relevantFields,
		boolean bGet,
		boolean bStatic) {
		arddebug("adding interface to class");
		
		boolean bSet=!bGet;
		
		cl.addInterface(accessInterface);
		
		Type returnType;
		if (bGet) {
			returnType=accessInterface.getType() ;
		} else {
			returnType=VoidType.v();
		}
			
		// create new method					
		SootMethod localGetMethod=new SootMethod(
			methodName, accessParameters, returnType ,
				Modifier.PUBLIC);
		
		Body getBody=Jimple.v().newBody(localGetMethod);
		
		localGetMethod.setActiveBody(getBody);
		cl.addMethod(localGetMethod);
		
		Chain statements=getBody.getUnits();
		
		// generate this := @this
		LocalGenerator lg=new LocalGenerator(getBody);
		Local lThis=lg.generateLocal(cl.getType());
		statements.addFirst(
			Jimple.v().newIdentityStmt(lThis, 
				Jimple.v().newThisRef(
					RefType.v(cl))));
		//getBody.getThisLocal();
		// $i0 := @parameter0: int;
		Local p2=lg.generateLocal(IntType.v());
		Stmt paramIdStmt=Jimple.v().newIdentityStmt(p2, 
		Jimple.v().newParameterRef(IntType.v(),0));
		statements.insertAfter(paramIdStmt,
			 statements.getFirst());
		Local p3=null;
		Stmt lastIDStmt=paramIdStmt;
		if (bSet) {
			p3=lg.generateLocal(accessFieldReference.getType());
			Stmt valueIdStmt=Jimple.v().newIdentityStmt(p3, 
				Jimple.v().newParameterRef(
					accessFieldReference.getType(),1));
			statements.insertAfter(valueIdStmt,
				 paramIdStmt);		
			
			lastIDStmt=valueIdStmt;	
		}
								
		List lookupValues= new LinkedList();
		List targets= new LinkedList();
		Unit defaultTarget;
		
		Iterator it=relevantFields.iterator();
		int fieldID=0;
		while (it.hasNext()) {
			SootField field=(SootField)it.next();
			lookupValues.add(IntConstant.v(fieldID));
		
			// generate field access
			if (bGet) {								
				Local l=lg.generateLocal(fieldRef.getType());
				Stmt getStmt=Jimple.v().newAssignStmt(l, 
						Jimple.v().newInstanceFieldRef(lThis ,field));			
				statements.add(getStmt);
				ReturnStmt returnStmt=Jimple.v().newReturnStmt(l);	
				statements.add(returnStmt);
				targets.add(getStmt);		
			} else {
				Stmt setStmt=Jimple.v().newAssignStmt( 
					Jimple.v().newInstanceFieldRef(lThis ,field), p3);
				statements.add(setStmt);
				//ReturnStmt returnStmt=;	
				statements.add(Jimple.v().newReturnVoidStmt());
				targets.add(setStmt);
			}
			fieldID++;
		}
		
		//Local l=lg.generateLocal(fieldRef.getType());
		//ReturnStmt returnStmt=Jimple.v().newReturnStmt(l);	
		//adviceChain.add(returnStmt);
		//throw RuntimeException()
		SootClass exception=Scene.v().getSootClass("java.lang.RuntimeException");
		Local ex=lg.generateLocal(exception.getType());
		Stmt newExceptStmt = Jimple.v().newAssignStmt( ex, Jimple.v().newNewExpr( exception.getType() ) );
		Stmt initEx=Jimple.v().newInvokeStmt( Jimple.v().newSpecialInvokeExpr( ex, exception.getMethod( "<init>", new ArrayList()))) ;
		Stmt throwStmt=Jimple.v().newThrowStmt(ex);
		statements.add(newExceptStmt);
		statements.add(initEx);
		statements.add(throwStmt);
		defaultTarget=newExceptStmt;
		
		
		Stmt lookupStmt=Jimple.v().newLookupSwitchStmt(p2, 
			lookupValues, targets, defaultTarget);												
		
		// return
		
		//returnStmt.setOp(IntConstant.v());
		statements.insertAfter(lookupStmt, lastIDStmt);
	} 
}
