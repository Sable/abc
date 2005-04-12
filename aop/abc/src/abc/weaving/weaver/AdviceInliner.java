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
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;


import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StmtBody;
import soot.jimple.toolkits.invoke.AccessManager;
import soot.jimple.toolkits.invoke.InlinerSafetyManager;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.util.Chain;

/**
 * @author Sascha Kuzins
 *
 */
public abstract class AdviceInliner extends BodyTransformer {
	public int getAccessViolationCount(SootMethod container, SootMethod adviceMethod) 
	{
		int violations=0;
		Body body=adviceMethod.getActiveBody();
		Chain statements=body.getUnits();
		for (Iterator it=statements.iterator(); it.hasNext();) {
			Stmt stmt=(Stmt)it.next();
			if (!AccessManager.isAccessLegal(container, stmt))
				violations++;
		}
		return violations;
	}
	private void debug(String message) {
		if (abc.main.Debug.v().adviceInliner)
			System.err.println("AIL*** " + message);
	}
	final public static int MAX_DEPTH=6;
	
	// with  50, all cases pass with forced inlining.
	// 100 works as well
	// with 300, some run out of memory (@512M).
	final public static int MAX_CONTAINER_SIZE=100; //5000;
	
	public static interface InlineOptions {
		public boolean inline(SootMethod container, Stmt stmt, InvokeExpr expr);
	}
	
	protected boolean inlineMethods(Body body, Map options, InlineOptions inlineOptions) {
		StmtBody stmtBody = (StmtBody)body;
		
		
		
		Chain units = stmtBody.getUnits();
		
		if (units.size()>MAX_CONTAINER_SIZE) {
			debug("Method body exceeds maximum size. No inlining. " + body.getMethod());
			return false;
		}
		
        ArrayList unitList = new ArrayList(); unitList.addAll(units);

        boolean bDidInline=false;
        Iterator stmtIt = unitList.iterator();
        while (stmtIt.hasNext()) {
        	Stmt stmt = (Stmt)stmtIt.next();
        	
        	if (!stmt.containsInvokeExpr())
                continue;
        	
        	InvokeExpr expr=stmt.getInvokeExpr();
        	
        	
        	
            if (inlineOptions.inline(body.getMethod(),stmt, expr)) {
            	//debug(" Trying to inline " + expr.getMethodRef());
            	if (InlinerSafetyManager.ensureInlinability(
            			expr.getMethod(), stmt, body.getMethod(), "accessors")) { // "unsafe"
            		
            		Stmt before=null;
            		try { before=(Stmt)units.getPredOf(stmt);} catch(NoSuchElementException e){};
            		Stmt after=null;
            		try { after=(Stmt)units.getSuccOf(stmt);} catch(NoSuchElementException e){};
            		SiteInliner.inlineSite(expr.getMethod(), stmt, body.getMethod(), options);
            		
            		AccessManager.createAccessorMethods(body, before, after);           		
            		            		
            		bDidInline=true;
            		debug("  Succeeded.");
            	} else {
            		debug("  Failed.");
            	}
            } else {
            	// debug(" No inlining.");
            }
        }		
        return bDidInline;
	}
	
	public abstract boolean forceInline();
	
	protected class IfMethodInlineOptions implements InlineOptions {
		public boolean inline(SootMethod container, Stmt stmt, InvokeExpr expr) {
			SootMethod method=expr.getMethod();
			
			if (!expr.getMethodRef().name().startsWith("if$"))
				return false;
			
			if (!method.isStatic())
				return false;
			
			//if (!method.getDeclaringClass().equals(container.getDeclaringClass()))
			//	return false;
			
			debug("Trying to inline if method " + method);
			
			if (forceInline()) {
				debug("force inline on.");
				return true;
			}

			int accessViolations=getAccessViolationCount(container, method);
			if (accessViolations!=0) {
				debug("Access violations");
				debug(" Method: " + container);
				debug(" Advice method: " + method); 
				debug(" Violations: " + accessViolations);
				if (accessViolations>0)
					return false;					
			}
			
			Body body=method.getActiveBody();
			
			//if (info.proceedInvocations>1)
			int size=body.getUnits().size();
			debug(" Size of if method: " + size);
			int addedLocals=body.getLocalCount()-method.getParameterCount();
			debug(" Number of added locals (approximately): " + addedLocals);			
						
			if (size<6)
				return true;
			

			return false;
		}
	}

}
