package abc.weaving.weaver;

import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import soot.javaToJimple.LocalGenerator;

/** Handle after throwing weaving.
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
			if (stmtAdv.stmt instanceof AssignStmt) {
				arddebug("found assignment statement");
				AssignStmt assignStmt=(AssignStmt)stmtAdv.stmt;
				Value rightOp=assignStmt.getRightOp();
				if (rightOp instanceof InstanceFieldRef) {
					arddebug("found assignment from field"); 
					InstanceFieldRef fieldRef=(InstanceFieldRef) rightOp;
					String typeName=
						rightOp.getType().toString();// .getClass().getName();
						
					String interfaceName="abc$get$" + typeName;
					
					String methodName="abc$proceed$get$" + typeName;
					
					List /*type*/ getParameters=new LinkedList();
					getParameters.add(IntType.v());
					
					SootClass getInterface;
					// create "get" interface if it doesn't exist
					if (Scene.v().containsClass(interfaceName)) {
						arddebug("found get interface");
						getInterface=Scene.v().getSootClass(interfaceName);
					} else {
						arddebug("generating get interface");
						getInterface=new SootClass(interfaceName, 
							Modifier.INTERFACE | Modifier.PUBLIC);						
						
						SootMethod signature=
							new SootMethod(methodName, getParameters, rightOp.getType(),
									Modifier.ABSTRACT | Modifier.PUBLIC);
						
						getInterface.addMethod(signature);
						//signature.setActiveBody(Jimple.v().newBody(signature));
						
						
						Scene.v().addClass(getInterface);
						
						GlobalAspectInfo.v().getGeneratedClasses().add(interfaceName);						 
					}
					
					// add interface to class if it doesn't exist.
					// implement the "get" method
					if (!cl.implementsInterface(interfaceName)){
						arddebug("adding get interface to class");
						cl.addInterface(getInterface);
						SootMethod getMethod=new SootMethod(methodName, getParameters, rightOp.getType(),
								Modifier.PUBLIC);
						
						Body getBody=Jimple.v().newBody(getMethod);
						Chain adviceChain=getBody.getUnits();
						
						ReturnStmt returnStmt=Jimple.v().newReturnStmt(IntConstant.v(42));
						//returnStmt.setOp(IntConstant.v());
						adviceChain.add(returnStmt);
						getMethod.setActiveBody(getBody);		
						cl.addMethod(getMethod);
					}
					
					// add parameter to around() method if it doesn't exist
					if (adviceMethod.getParameterCount()==0 ||
						 !adviceMethod.getParameterType(0).toString().equals(interfaceName)) {
						arddebug("modifying around() method");
						List aroundParameters=adviceMethod.getParameterTypes();
						aroundParameters.add(0, getInterface.getType());
						adviceMethod.setParameterTypes(aroundParameters);
						Body aroundBody=adviceMethod.getActiveBody();
						Chain statements=aroundBody.getUnits();
						Iterator it=statements.snapshotIterator();
						while (it.hasNext()) {
							Stmt s=(Stmt)it.next();
							if (s instanceof InvokeStmt) {
								InvokeStmt i=(InvokeStmt)s;
								if (i.getInvokeExpr().getMethod().getName().equals("proceed")) {
									/// 
								}											
							}
						}
					}
					
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
