/*
 * Created on 08-Nov-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.weaving.weaver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.*;
import soot.Body;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.util.Chain;

/**
 * @author Sascha Kuzins
 */
public class UnusedMethodsRemover {

	static boolean considerInstanceMethod(String methodName) {
		return 
			AroundWeaver.Util.isAroundAdviceMethodName(methodName) ||
			abc.soot.util.AfterBeforeInliner.isAdviceMethodName(methodName);
	}
	static boolean considerStaticMethod(String methodName) {
		return 
		AroundWeaver.Util.isProceedMethodName(methodName) ||
		methodName.startsWith("proceed$");
	}
	static boolean considerMethod(String methodName) {
		return considerInstanceMethod(methodName) ||
		considerStaticMethod(methodName);
	}
	static private void debug(String message) {
		if (abc.main.Debug.v().unusedMethodsRemover)
			System.err.println("UMR*** " + message);
	}
	
	public static void removeUnusedMethods() {
//		 Retrieve all bodies
		Set methods=new HashSet();
		
        for( Iterator clIt = Scene.v().getApplicationClasses().iterator(); clIt.hasNext(); ) {
            //final AbcClass cl = (AbcClass) clIt.next();
            soot.SootClass cl=(soot.SootClass)clIt.next();
			
            for( Iterator methodIt = cl.getMethods().iterator(); methodIt.hasNext(); ) {
                final SootMethod method = (SootMethod) methodIt.next();
                
                if (method.isAbstract())
                	continue;
                
                if (!method.hasActiveBody())
                	continue;
                
                Body body=method.getActiveBody();
                
                Chain statements=body.getUnits();
                
                for (Iterator stmtIt=statements.iterator(); stmtIt.hasNext(); ) {
                	Stmt stmt=(Stmt)stmtIt.next();
                	if (stmt.containsInvokeExpr()) {
                		InvokeExpr expr=stmt.getInvokeExpr();
                		String name=expr.getMethodRef().name();
                		if (considerMethod(name)) {// considerMethod(expr.getMethodRef().name())) {
                			methods.add(expr.getMethodRef().toString());
                		}
                	}
                }
                
            }
        }
        
        for( Iterator clIt = Scene.v().getApplicationClasses().iterator(); clIt.hasNext(); ) {
            //final AbcClass cl = (AbcClass) clIt.next();
            SootClass cl=(SootClass)clIt.next();
            List clMethods=new ArrayList(cl.getMethods());
            for( Iterator methodIt = clMethods.iterator(); methodIt.hasNext(); ) {
                final SootMethod method = (SootMethod) methodIt.next();
                
                String name=method.getName();
                if ( (!method.isStatic() && considerInstanceMethod(name)) || 
                	 (method.isStatic() && considerMethod(name) )) {
                	if (!methods.contains(method.makeRef().toString())) {
                		debug("Removing unused method: " + method.toString());
                		cl.removeMethod(method);
                	}
                }                
            }
        }	
	}
}
