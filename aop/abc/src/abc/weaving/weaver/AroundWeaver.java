package abc.weaving.weaver;

import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import soot.javaToJimple.LocalGenerator;
import sun.rmi.runtime.NewThreadAction;

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
	
		// find out what kind of pointcut 
		if (adviceappl instanceof StmtAdviceApplication) {
			arddebug("found statement advice application");
			StmtAdviceApplication stmtAdv=(StmtAdviceApplication) adviceappl;
			// is it an assignment?
			if (stmtAdv.stmt instanceof AssignStmt) {
				arddebug("found assignment statement");
				AssignStmt assignStmt=(AssignStmt)stmtAdv.stmt;
				
				Value rightOp=assignStmt.getRightOp();
				// is it a field access?
				if (rightOp instanceof InstanceFieldRef) { 
					InstanceFieldRef fieldRef=(InstanceFieldRef) rightOp;
					arddebug("found assignment from field " + fieldRef.getField().getName());
					
					String typeName=
						rightOp.getType().toString();// .getClass().getName();
						
					String interfaceName="abc$get$" + typeName;
					
					String methodName="abc$proceed$get$" + typeName;
					
					List /*type*/ getParameters=new LinkedList();
					getParameters.add(IntType.v());
					
					SootClass getInterface;
					SootMethod getMethod;
					// create "get" interface if it doesn't exist
					if (Scene.v().containsClass(interfaceName)) {
						arddebug("found get interface");
						getInterface=Scene.v().getSootClass(interfaceName);
						getMethod=getInterface.getMethodByName(methodName);
					} else {
						arddebug("generating get interface");
						getInterface=new SootClass(interfaceName, 
							Modifier.INTERFACE | Modifier.PUBLIC);						
						
						getInterface.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
						
						getMethod=
							new SootMethod(methodName, getParameters, rightOp.getType(),
									Modifier.ABSTRACT | 
									Modifier.PUBLIC);
						
						getInterface.addMethod(getMethod);
						//signature.setActiveBody(Jimple.v().newBody(signature));
						
						
						Scene.v().addClass(getInterface);
						
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
						arddebug("adding get interface to class");
						
						cl.addInterface(getInterface);
		
						// create new method					
						SootMethod localGetMethod=new SootMethod(methodName, getParameters, rightOp.getType(),
								Modifier.PUBLIC);
						
						Body getBody=Jimple.v().newBody(localGetMethod);
						
						localGetMethod.setActiveBody(getBody);		
						cl.addMethod(localGetMethod);
						
						Chain adviceChain=getBody.getUnits();
					
						
						// generate this := @this
						LocalGenerator lg=new LocalGenerator(getBody);
						Local lThis=lg.generateLocal(cl.getType());
						adviceChain.addFirst(
							Jimple.v().newIdentityStmt(lThis, 
								Jimple.v().newThisRef(
									RefType.v(cl))));
						//getBody.getThisLocal();
						// $i0 := @parameter0: int;
						Local p2=lg.generateLocal(IntType.v());
						Stmt paramIdStmt=Jimple.v().newIdentityStmt(p2, 
						Jimple.v().newParameterRef(IntType.v(),0));
						adviceChain.insertAfter(paramIdStmt,
							 adviceChain.getFirst());
												
						List lookupValues= new LinkedList();
						List targets= new LinkedList();
						Unit defaultTarget;
						
						Iterator it=relevantFields.iterator();
						int fieldID=0;
						while (it.hasNext()) {
							SootField field=(SootField)it.next();
							lookupValues.add(IntConstant.v(fieldID));
						
							// generate field access								
							Local l=lg.generateLocal(fieldRef.getType());
			
							Stmt getStmt=Jimple.v().newAssignStmt(l, 
									Jimple.v().newInstanceFieldRef(lThis ,field));			
							adviceChain.add(getStmt);
						
							ReturnStmt returnStmt=Jimple.v().newReturnStmt(l);	
							adviceChain.add(returnStmt);
							
							targets.add(getStmt);				
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
						adviceChain.add(newExceptStmt);
						adviceChain.add(initEx);
						adviceChain.add(throwStmt);
						defaultTarget=newExceptStmt;
						
						
						Stmt lookupStmt=Jimple.v().newLookupSwitchStmt(p2, 
							lookupValues, targets, defaultTarget);												
						
						// return
						
						//returnStmt.setOp(IntConstant.v());
						adviceChain.insertAfter(lookupStmt, paramIdStmt);
						
						
					}
					
					// add parameters to around() method if it doesn't exist
					// and replace proceed with call to get-interface
					if (adviceMethod.getParameterCount()==0 ||
						 !adviceMethod.getParameterType(0).toString().equals(interfaceName)) {
						arddebug("modifying around() method");
						List aroundParameters=adviceMethod.getParameterTypes();
						aroundParameters.add(0, IntType.v());
						aroundParameters.add(0, getInterface.getType());
						adviceMethod.setParameterTypes(aroundParameters);
						Body aroundBody=adviceMethod.getActiveBody();
						Chain statements=aroundBody.getUnits();
						LocalGenerator localgen2 = new LocalGenerator(aroundBody);
						Local l=localgen2.generateLocal(getInterface.getType());
						Stmt intRefIDstmt=Jimple.v().newIdentityStmt(l, 
								Jimple.v().newParameterRef(	
										getInterface.getType(),0));
						statements.insertAfter(intRefIDstmt, statements.getFirst());
						Local l2=localgen2.generateLocal(IntType.v());
						Stmt fieldIDStmt=Jimple.v().newIdentityStmt(l2, 
								Jimple.v().newParameterRef(IntType.v(),1));
						statements.insertAfter(fieldIDStmt, intRefIDstmt);
						Iterator it=statements.snapshotIterator();
						while (it.hasNext()) { /// TODO: Check if all cases of get are caught
							Stmt s=(Stmt)it.next();
							if (s instanceof InvokeStmt) {
								InvokeStmt i=(InvokeStmt)s;
								arddebug("found invoke statement in around() method" + 
									i.getInvokeExpr().getMethod().getName());
								if (i.getInvokeExpr().getMethod().getName().startsWith("proceed$")) {
									arddebug("modifying proceed call in around() method ");
									/*IdentityStmt id=(IdentityStmt) statements.getFirst();
									i.setInvokeExpr(Jimple.v().newVirtualInvokeExpr(
										id.getLeftOp(),   )
										*/
										/// TODO
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
												local, getMethod, l2);
										a.setRightOp(invokeExpr);
									}
								}
							}
						}
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
					 
					/* Local thisRef=localgen.generateLocal(cl.getType() );
//					smt1:  aspectref = <AspectType>.aspectOf();
					  AssignStmt stmt2 =  
						  Jimple.v().newAssignStmt( 
							  thisRef, 
								Jimple.v().newThisRef(RefType.v(cl)) );
					   arddebug("Generated stmt2: " + stmt2);
					 units.insertBefore(stmt2,assignStmt);
				 */
				 	units.insertBefore(stmt1,assignStmt);
				 
				    IdentityStmt id=(IdentityStmt) units.getFirst();
				    List params=new LinkedList();
				    params.add(id.getLeftOp());
				    params.add(IntConstant.v(fieldIndex));
				 	InvokeExpr invokeEx=
				 		Jimple.v().newVirtualInvokeExpr( aspectref, adviceMethod, params);
					assignStmt.setRightOp(invokeEx);
				} else if (rightOp instanceof StaticFieldRef) {
					arddebug("NYI: static field get " + adviceappl);
				}
				
			}			
		} else if (adviceappl instanceof ExecutionAdviceApplication) {
			arddebug("NYI: execution advice application: " + adviceappl);
		} else {
			arddebug("NYI: advice application: " + adviceappl);
		}
		
	
		

	} // method doWeave 
}
