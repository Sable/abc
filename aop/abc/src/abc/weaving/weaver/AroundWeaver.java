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
			//HashMap /*String, Integer*/ fieldIDs=new HashMap();
			
			List targets=new LinkedList();
			List lookupValues=new LinkedList();
			Unit defaultTarget;
			Stmt lookupStmt;
			int nextID;
			Local idParamLocal;
			
			/*public boolean implementsField(String field) {
				return fieldIDs.containsKey(field);
			}*/
		}
		private HashMap /*ClassInterfacePair, AccessMethodInfo*/ accessInterfacesGet=new HashMap();
		
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
		public int getUniqueID() {
			return currentUniqueID++;
		}
		int currentUniqueID;
	}
	public static State state=new State();


	public static void doWeave(
					SootClass cl,
					SootMethod method,
					LocalGenerator localgen,
					AdviceApplication adviceappl) {
		debug("Handling aound: " + adviceappl);
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
	
		debug("Advice application - kind:" + adviceappl.sjpInfo.kind + 
				" signatureType: " + adviceappl.sjpInfo.signatureType +
				" signature: " + adviceappl.sjpInfo.signature);
	
		// find out what kind of pointcut 
		if (adviceappl instanceof StmtAdviceApplication) {
			debug("found statement advice application");
			StmtAdviceApplication stmtAdv=(StmtAdviceApplication) adviceappl;
			//stmtAdv.
			//advicedecl.
			// is it an assignment?
			if (adviceappl.sjpInfo.kind.equals("field-get")) {
				debug("found field-get");
				if (!(stmtAdv.stmt instanceof AssignStmt)) {
					throw new CodeGenException(
						"StmtAdviceApplication.stms is expected to be instanceof AssignStmt"); // TODO: 
				}
				debug("found assignment statement");
				AssignStmt assignStmt=(AssignStmt)stmtAdv.stmt;
				
				Value rightOp=assignStmt.getRightOp();
				Value leftOp=assignStmt.getLeftOp();
				// is it a field access?
				/*boolean bGetInstance=rightOp instanceof InstanceFieldRef;
				boolean bGetStatic=rightOp instanceof StaticFieldRef;
				boolean bSetInstance=leftOp instanceof InstanceFieldRef;
				boolean bSetStatic=leftOp instanceof StaticFieldRef;
				boolean bGet=bGetInstance || bGetStatic;
				boolean bStatic=bGetStatic || bSetStatic;*/
				 
				weaveGetSet(
					adviceappl,
					cl,
					localgen,
					method,
					theAspect,
					adviceMethod,
					assignStmt,
					true // bGet  
					 );
								
			} else {
				debug("NYI: type of stmt advice application " + adviceappl);
			}			
		} else if (adviceappl instanceof ExecutionAdviceApplication) {
			debug("NYI: execution advice application: " + adviceappl);
		} else {
			debug("NYI: advice application: " + adviceappl);
		}
	} // method doWeave 

	private static void weaveGetSet(
		AdviceApplication adviceAppl,
		SootClass cl,
		LocalGenerator localgen,
		SootMethod method,
		SootClass theAspect,
		SootMethod adviceMethod,
		AssignStmt assignStmt,
		boolean bGet) {
		
		boolean bSet=!bGet;
		//boolean bDynamic=!bStatic;
		
		
		Chain units=method.getActiveBody().getUnits();
		
		//InstanceFieldRef fieldRef=(InstanceFieldRef) accessFieldRef;
		//arddebug("found field access: " + fieldRef.getField().getName());
		
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
			debug("found access interface in scene");
			accessInterface=Scene.v().getSootClass(interfaceName);
			accessMethod=accessInterface.getMethodByName(methodName);
		} else {
			debug("generating access interface for scene");
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
		/*Collection relevantFields=new LinkedList();
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
		}*/
						
		// and replace proceed with call to get-interface
		if (adviceMethod.getParameterCount()==0 ||
			 !adviceMethod.getParameterType(0).toString().equals(interfaceName)) {
			modifyAdviceMethod(adviceMethod, accessInterface, accessMethod, 
				accessInterfaceType,
				bGet
				);
		}
	

		implementInterface(
			theAspect,
			method,
			adviceAppl,
			assignStmt,
			 cl,
			 methodName,
			 interfaceName,
			 accessMethodParameters,
			 accessInterface,
			 bGet);
		
	}
	
	private static void implementInterface(
		SootClass theAspect,
		SootMethod method,
		AdviceApplication adviceAppl,
		AssignStmt assignStmt,
		SootClass cl,
		String accessMethodName,
		String interfaceName,
		List accessParameters,
		SootClass accessInterface,
		boolean bGet) {
		
	
		boolean bSet=!bGet;
	
		Chain units=method.getActiveBody().getUnits();
		
		SootMethod adviceMethod=adviceAppl.advice.getImpl().getSootMethod();
		
	
		// add interface to class if it doesn't exist.
		// add the method with arguments and identitiy statements
		SootMethod localGetMethod=null;
		Body accessBody=null;
		Chain accessStatements=null;
		
		State.AccessMethodInfo info=state.getMethodInfo(cl.getName(), interfaceName);
		
		if (cl.implementsInterface(interfaceName)){
			debug("found interface " + interfaceName + " in class: " + cl.getName());
			SootClass cl2=cl;
			localGetMethod=cl2.getMethodByName(accessMethodName);
			accessBody=localGetMethod.getActiveBody();
			accessStatements=accessBody.getUnits();
		} else {
			debug("adding interface " + interfaceName + " to class " + cl.getName());
			
			cl.addInterface(accessInterface);
	
			Type returnType;
			if (bGet) {
				returnType=adviceMethod.getReturnType() ;
			} else {
				returnType=VoidType.v();
			}
			
			// create new method					
			localGetMethod=new SootMethod(
				accessMethodName, accessParameters, returnType ,
					Modifier.PUBLIC);
		
			accessBody=Jimple.v().newBody(localGetMethod);
		
			localGetMethod.setActiveBody(accessBody);
			debug("adding method " + localGetMethod.getName() + " to class " + cl.getName());
			cl.addMethod(localGetMethod);
			
			accessStatements=accessBody.getUnits();
	
			// generate this := @this
			LocalGenerator lg=new LocalGenerator(accessBody);
			Local lThis=lg.generateLocal(cl.getType());
			accessStatements.addFirst(
				Jimple.v().newIdentityStmt(lThis, 
					Jimple.v().newThisRef(
						RefType.v(cl))));
			//lThis.setName("this");
			//getBody.getThisLocal();
			// $i0 := @parameter0: int;
			info.idParamLocal=lg.generateLocal(IntType.v());
			Stmt paramIdStmt=Jimple.v().newIdentityStmt(info.idParamLocal, 
			Jimple.v().newParameterRef(IntType.v(),0));
			accessStatements.insertAfter(paramIdStmt,
				 accessStatements.getFirst());
			Local p3=null;
			Stmt lastIDStmt=paramIdStmt;
			/*if (bSet) {
				p3=lg.generateLocal(accessFieldReference.getType());
				Stmt valueIdStmt=Jimple.v().newIdentityStmt(p3, 
					Jimple.v().newParameterRef(
						accessFieldReference.getType(),1));
				statements.insertAfter(valueIdStmt,
					 paramIdStmt);		

				lastIDStmt=valueIdStmt;	
			}*/
		
			// generate exception code (default target)
			SootClass exception=Scene.v().getSootClass("java.lang.RuntimeException");	
			Local ex=lg.generateLocal(exception.getType());
			Stmt newExceptStmt = Jimple.v().newAssignStmt( ex, Jimple.v().newNewExpr( exception.getType() ) );
			Stmt initEx=Jimple.v().newInvokeStmt( Jimple.v().newSpecialInvokeExpr( ex, exception.getMethod( "<init>", new ArrayList()))) ;
			Stmt throwStmt=Jimple.v().newThrowStmt(ex);
			accessStatements.add(newExceptStmt);
			accessStatements.add(initEx);
			accessStatements.add(throwStmt);
			info.defaultTarget=newExceptStmt;
			
			// just generate a nop for now.
			info.lookupStmt=Jimple.v().newNopStmt();
			
			accessStatements.insertAfter(info.lookupStmt, lastIDStmt);		
		}
		
		//adviceAppl;
		
		SootMethod joinpointMethod=method;
		Body joinpointBody=joinpointMethod.getActiveBody();
		
		Chain joinpointChain=joinpointBody.getUnits();		
		
		Stmt begin=adviceAppl.shadowpoints.getBegin();
		Stmt end=adviceAppl.shadowpoints.getEnd();
		
		if (!joinpointChain.contains(assignStmt))
			throw new RuntimeException();
		
		Unit first=CopyStmtSequence(joinpointBody, begin, end, accessBody, 
				info.lookupStmt, (Local)assignStmt.getLeftOp());
		
		int intId=info.nextID++;
		info.lookupValues.add(IntConstant.v(intId));
		info.targets.add(first);
		
		// generate new lookup statement and replace the old one
		Stmt lookupStmt=Jimple.v().newLookupSwitchStmt(info.idParamLocal, 
			info.lookupValues, info.targets, info.defaultTarget);
		accessStatements.insertAfter(lookupStmt, info.lookupStmt);
		accessStatements.remove(info.lookupStmt);
		info.lookupStmt=lookupStmt;
		
		// remove statements except original assignment
		RemoveStatements(joinpointBody, begin, end, assignStmt);
		
		Local lThis=joinpointBody.getThisLocal();
		//lThis.setName("this");
		
		LocalGenerator localgen=new LocalGenerator(joinpointBody);	
		
		debug("replacing former code with call to advice method");
		// add another this id statement.
		// why? have to keep this code independent of the rest of the method.
		/*Local lThis=localgen.generateLocal(cl.getType());
					units.insertBefore(
						Jimple.v().newIdentityStmt(lThis, 
							Jimple.v().newThisRef(
								RefType.v(cl))), assignStmt);
								*/
		Local aspectref = localgen.generateLocal( theAspect.getType() );
		//smt1:  aspectref = <AspectType>.aspectOf();
		AssignStmt stmt1 =  
			Jimple.v().newAssignStmt( 
				aspectref, 
					Jimple.v().newStaticInvokeExpr(
						theAspect.getMethod("aspectOf", new ArrayList())));
		debug("Generated stmt1: " + stmt1);

		units.insertBefore(stmt1,assignStmt);
 
		//IdentityStmt id=(IdentityStmt) units.getFirst();
		List params=new LinkedList();
		params.add(lThis);//id.getLeftOp());
		params.add(IntConstant.v(intId));
		/*if (bSet) {
				// replace assignment to field with assignment to local
			Local lVal=localgen.generateLocal(fieldRef.getType());
			assignStmt.setLeftOp(lVal);
			params.add(lVal);
		}*/
		InvokeExpr invokeEx=
			Jimple.v().newVirtualInvokeExpr( aspectref, adviceMethod, params);

		if (bGet) {
			assignStmt.setRightOp(invokeEx);
		} else {
			InvokeStmt invokeStmt=Jimple.v().newInvokeStmt(invokeEx);
			units.insertAfter(invokeStmt, assignStmt);
		}
		
	
		/*Iterator it=relevantFields.iterator();
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
		}*/
		
	
		//Local l=lg.generateLocal(fieldRef.getType());
		//ReturnStmt returnStmt=Jimple.v().newReturnStmt(l);	
		//adviceChain.add(returnStmt);
		//throw RuntimeException()
		/*
		
	
	
												
	
		// return
	
		//returnStmt.setOp(IntConstant.v());
		statements.insertAfter(lookupStmt, lastIDStmt);
		
		
		
		*/
		
		

		
	} 
	/**
	 * Removes statements between begin and end, excluding these and skip.
	 */
	private static void RemoveStatements(Body body, Unit begin, Unit end, Unit skip) {
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
	 * Returns first inserted unit in the destination method.
	 * 
	 * If returnedLocal is not null, the corresponding new local is returned after the 
	 * copy of the block.
	 * 
	 * The former local "this" is mapped to the new "this". 
	 * 
	 * This is a modified version of Body.importBodyContentsFrom()
	 * */
	private static Unit CopyStmtSequence(Body source, Unit begin, Unit end, 
			Body dest, Unit insertAfter,
				Local returnedLocal) {
		
		Local lThisSource=source.getThisLocal();
		Local lThisDest=dest.getThisLocal();			
		
		HashMap bindings = new HashMap();

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
				copy.setName(copy.getName() + "abc" + state.getUniqueID());
				// Add cloned unit to our trap list.
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

		if (returnedLocal!=null) {
			Local newLocal=(Local)bindings.get(returnedLocal);
			if (newLocal==null)
				throw new RuntimeException();
			
			ReturnStmt returnStmt=Jimple.v().newReturnStmt(newLocal);	
			unitChain.insertAfter(returnStmt, insertAfter);
			insertAfter=returnStmt;
		}
		
//		rename all locals that were newly introduced 
		 /*it = dest.getUseAndDefBoxes().iterator();
		 while(it.hasNext()) {
			 ValueBox vb = (ValueBox) it.next();
			 if(vb.getValue() instanceof Local) {
				 Value newLocal=(Value) bindings.get(vb.getValue());
				 if (newLocal!=null) {
				 	Local local=(Local)vb.getValue();
				 	local.setName(local.getName() + "_abc_" + state.getUniqueID()); 
					vb.setValue(local);
				 }					 
			 }
			
		 }*/
		
		return firstCopy;
	}
	
	private static void modifyAdviceMethod(
		SootMethod adviceMethod,
		SootClass accessInterface,
		SootMethod accessMethod,
		Type accessInterfaceType,
		boolean bGet) {
		
		boolean bSet=!bGet;
			
			
		debug("modifying advice method: " + adviceMethod.toString());
		List aroundParameters=adviceMethod.getParameterTypes();
		// params are added in reverse order...
		/*if (bSet) {
			aroundParameters.add(0, fieldType);	
		}*/
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
		/*if (bSet) {
			l3=localgen2.generateLocal(fieldType);
			Stmt valueIDStmt=Jimple.v().newIdentityStmt(l3, 
					Jimple.v().newParameterRef(fieldType,2));
			statements.insertAfter(valueIDStmt, fieldIDStmt);
		}*/
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
				debug("found invoke statement in around() method " + 
					i.getInvokeExpr().getMethod().getName());
				if (i.getInvokeExpr().getMethod().getName().startsWith("proceed$")) {
					debug("modifying proceed call in around() method ");
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
						debug("replacing proceed$ call (invoke expression) in advice method");		
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

	
}
