/*
 * Created on 29-Oct-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.soot.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StmtBody;
import soot.jimple.toolkits.invoke.InlinerSafetyManager;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.util.Chain;
import abc.weaving.weaver.AroundWeaver;

/**
 * @author Sascha Kuzins
 *
 */
public class AroundInliner extends BodyTransformer {

	private static AroundInliner instance = 
		new AroundInliner();
	public static void reset() { instance = new AroundInliner(); }
	
	
	public static AroundInliner v() { return instance; }
	
	/* (non-Javadoc)
	 * @see soot.BodyTransformer#internalTransform(soot.Body, java.lang.String, java.util.Map)
	 */
	private void foldSwitches(Body body) {
		ConstantPropagatorAndFolder.v().transform(body);
		SwitchFolder.v().transform(body); // TODO: phaseName etc.?
		UnreachableCodeEliminator.v().transform(body);
	}
	protected void internalTransform(Body body, String phaseName, Map options) {
		if (inlineMethods(body, options, new AdviceMethodInlineOptions())) {
			foldSwitches(body);
			inlineMethods(body, options, new ProceedMethodInlineOptions());
			foldSwitches(body);
		}
	}

	private static interface InlineOptions {
		public boolean inline(SootMethod container, InvokeExpr expr);
	}
	private static class AdviceMethodInlineOptions implements InlineOptions {
		public boolean inline(SootMethod container, InvokeExpr expr) {
			return AroundWeaver.Util.isAroundAdviceMethodName(expr.getMethodRef().name());
		}
	}
	private static class ProceedMethodInlineOptions implements InlineOptions {
		public boolean inline(SootMethod container, InvokeExpr expr) {
			SootMethod method=expr.getMethod();
			 
			return //method.isStatic() && 
				AroundWeaver.Util.isProceedMethodName(expr.getMethodRef().name());
		}
	}
	private boolean inlineMethods(Body body, Map options, InlineOptions inlineOptions) {
		StmtBody stmtBody = (StmtBody)body;
		
		Chain units = stmtBody.getUnits();
        ArrayList unitList = new ArrayList(); unitList.addAll(units);

        boolean bDidInline=false;
        Iterator stmtIt = unitList.iterator();
        while (stmtIt.hasNext()) {
        	Stmt stmt = (Stmt)stmtIt.next();
        	
        	if (!stmt.containsInvokeExpr())
                continue;
        	
        	InvokeExpr expr=stmt.getInvokeExpr();
        	
        	
        	
            if (inlineOptions.inline(body.getMethod(), expr)) {
            	if (InlinerSafetyManager.ensureInlinability(
            			expr.getMethod(), stmt, body.getMethod(), "unsafe")) {
            		SiteInliner.inlineSite(expr.getMethod(), stmt, body.getMethod(), options);
            		bDidInline=true;
            	}
            }           
        }		
        return bDidInline;
	}

}
