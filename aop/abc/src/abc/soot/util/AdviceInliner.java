/*
 * Created on 04-Nov-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.soot.util;

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
 * @author kuzins
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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
	final public static int MAX_DEPTH=4;
	final public static int MAX_CONTAINER_SIZE=5000;
	
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

}
