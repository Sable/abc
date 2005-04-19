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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.weaving.weaver.around.Util;

/**
 * @author Sascha Kuzins
 */
public class UnusedMethodsRemover {
	
	static boolean considerInstanceMethod(String methodName) {
		return 
			Util.isAroundAdviceMethodName(methodName) ||
			abc.weaving.weaver.AdviceInliner.isAfterBeforeAdviceMethod(methodName);
	}
	static boolean considerStaticMethod(String methodName) {
		return 
		Util.isProceedMethodName(methodName) ||
		methodName.startsWith("proceed$") ||
		methodName.startsWith("if$") ||
		methodName.startsWith("shadow$");
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
		while (removeUnusedMethodsPass()>0) 
			;		
	}
	// returns number of removed methods
	private static int removeUnusedMethodsPass() {
		
		int removedMethods=0;
		Set calledMethods=new HashSet();
		
        for( Iterator clIt = Scene.v().getApplicationClasses().iterator(); clIt.hasNext(); ) {
            soot.SootClass cl=(soot.SootClass)clIt.next();
			
            for( Iterator methodIt = cl.getMethods().iterator(); methodIt.hasNext(); ) {
                final SootMethod method = (SootMethod) methodIt.next();
                
                if (method.isAbstract())
                	continue;
                
                if (!method.hasActiveBody()) {
                	debug("method does not have active body!");
                	continue;
                }
                
                Body body=method.getActiveBody();
                
                Chain statements=body.getUnits();
                
                for (Iterator stmtIt=statements.iterator(); stmtIt.hasNext(); ) {
                	Stmt stmt=(Stmt)stmtIt.next();
                	if (stmt.containsInvokeExpr()) {
                		InvokeExpr expr=stmt.getInvokeExpr();
                		String name=expr.getMethodRef().name();
                		if (considerMethod(name)) {// considerMethod(expr.getMethodRef().name())) {
                			calledMethods.add(expr.getMethodRef().toString());
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
                	//debug("Looking at method method: " + method.toString());
                	if (!calledMethods.contains(method.makeRef().toString())) {
                		debug("Removing unused method: " + method.toString());
                		cl.removeMethod(method);
                		removedMethods++;
                	}
                }                
            }
        }
        return removedMethods;
	}
}
