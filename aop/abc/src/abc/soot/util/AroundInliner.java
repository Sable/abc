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
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.*;
import soot.jimple.StmtBody;
import soot.jimple.toolkits.invoke.AccessManager;
import soot.jimple.toolkits.invoke.InlinerSafetyManager;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.Evaluator;
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
	
	
	private boolean forceInline() {
		return true;
	}
	
	public static AroundInliner v() { return instance; }
	
	/* (non-Javadoc)
	 * @see soot.BodyTransformer#internalTransform(soot.Body, java.lang.String, java.util.Map)
	 */
	private void foldSwitches(Body body) {
		ConstantPropagatorAndFolder.v().transform(body);
		SwitchFolder.v().transform(body); // TODO: phaseName etc.?
		UnreachableCodeEliminator.v().transform(body);
	}
	final private static int MAX_DEPTH=4;
	protected void internalTransform(Body body, String phaseName, Map options) {
		
		// remove dead code from the dynamic residues.
		// this is important because the dead code may contain a call
		// to the proceed method.
		UnreachableCodeEliminator.v().transform(body);
		
		// do this in a loop:
		// after inlining, additional advice method calls may be present
		// (if the same joinpoint was advised multiple times, or in the case
		// of nested joinpoints)
		int depth=0;
		while (inlineMethods(body, options, new AdviceMethodInlineOptions())) {
			foldSwitches(body);
			inlineMethods(body, options, new ProceedMethodInlineOptions(body));
			foldSwitches(body);
			depth++;
			if (depth>=MAX_DEPTH)
				break;
		}
	}

	private static interface InlineOptions {
		public boolean inline(SootMethod container, Stmt stmt, InvokeExpr expr);
	}
	private class AdviceMethodInlineOptions implements InlineOptions {
		private int getAccessViolationCount(SootMethod container, SootMethod adviceMethod) 
		{
			int violations=0;
			Body body=adviceMethod.getActiveBody();
			Chain statements=body.getUnits();
			for (Iterator it=statements.iterator(); it.hasNext();) {
				Stmt stmt=(Stmt)it.next();
				if (stmt.containsInvokeExpr()) {
					if (!AccessManager.isAccessLegal(container, stmt.getInvokeExpr().getMethod()))
						violations++;
				} else if (stmt instanceof AssignStmt) {
					AssignStmt as=(AssignStmt)stmt;
					if (as.getRightOp() instanceof FieldRef) {
						FieldRef r=(FieldRef)as.getRightOp();
						if (!AccessManager.isAccessLegal(container, r.getField()))
								violations++;
					}
					if (as.getLeftOp() instanceof FieldRef) {
						FieldRef r=(FieldRef)as.getLeftOp();
						if (!AccessManager.isAccessLegal(container, r.getField()))
								violations++;
					}
				}
			}
			return violations;
		}
		public boolean inline(SootMethod container, Stmt stmt, InvokeExpr expr) {
			SootMethod method=expr.getMethod();
			if (!AroundWeaver.Util.isAroundAdviceMethodName(expr.getMethodRef().name()))
				return false;
			
			if (forceInline())
				return true;
			
			
			AroundWeaver.AdviceMethodInlineInfo info=
					AroundWeaver.state.getAdviceMethodInlineInfo(method);
			
			int accessViolations=getAccessViolationCount(container, method);
			
			if (info.nestedClasses)
				return false;
			
			//if (info.proceedInvocations>1)
				
			if (info.originalSize<4)
				return true;
			
			if (info.applications==1)
				return true;
			
			return false;
		}
	}
	private class ProceedMethodInlineOptions implements InlineOptions {
		public ProceedMethodInlineOptions(Body body) {
			
		}
		public boolean inline(SootMethod container, Stmt stmt, InvokeExpr expr) {
			SootMethod method=expr.getMethod();
			
			if (!AroundWeaver.Util.isProceedMethodName(expr.getMethodRef().name()))
				return false;
			
			if (!method.isStatic())
				return false;
			
			if (!method.getDeclaringClass().equals(container.getDeclaringClass()))
				return false;
			
			if (forceInline())
				return true;
			
			AroundWeaver.ProceedMethodInlineInfo info=					
				AroundWeaver.state.getProceedMethodInlineInfo(method);
			
			int shadowSize=-1;
			if (stmt.hasTag("AroundShadowInfoTag"))	{
				AroundShadowInfoTag tag=
					(AroundShadowInfoTag)stmt.getTag("AroundShadowInfoTag");
				
				shadowSize=tag.shadowSize;
			} else {
				soot.Value v=expr.getArg(info.shadowIDParamIndex);
				if (Evaluator.isValueConstantValued(v)) {
                    v = Evaluator.getConstantValueOf(v);
                    int shadowID=((IntConstant) v).value;
                  
                    shadowSize=((Integer)info.shadowSizes.get(new Integer(shadowID))).intValue();                    	
                    
                    stmt.addTag(new AroundShadowInfoTag(shadowSize));
				}
			}
			
			if (shadowSize!=-1 && shadowSize<4)
				return true;

			return false;
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
        	
        	
        	
            if (inlineOptions.inline(body.getMethod(),stmt, expr)) {
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
