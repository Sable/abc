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
import java.util.NoSuchElementException;

import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
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
public class AroundInliner extends AdviceInliner {

	private void debug(String message) {
		if (abc.main.Debug.v().aroundInliner)
			System.err.println("ARI*** " + message);
	}
	private static AroundInliner instance = 
		new AroundInliner();
	public static void reset() { instance = new AroundInliner(); }
	
	
	private boolean forceInline() {
		return abc.main.options.OptionsParser.v().around_force_inlining();
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

	
	private class AdviceMethodInlineOptions implements InlineOptions {
		
		public boolean inline(SootMethod container, Stmt stmt, InvokeExpr expr) {
			SootMethod method=expr.getMethod();
			if (!AroundWeaver.Util.isAroundAdviceMethodName(expr.getMethodRef().name()))
				return false;
			
			debug("Trying to inline advice method " + method);
			
			if (forceInline()) {
				debug("force inline on.");
				return true;	
			}
			
			
			AroundWeaver.AdviceMethodInlineInfo info=
					AroundWeaver.state.getAdviceMethodInlineInfo(method);
			
			int accessViolations=getAccessViolationCount(container, method);
			if (accessViolations!=0) {
				debug("Access violations");
				debug(" Method: " + container);
				debug(" Advice method: " + method); 
				debug(" Violations: " + accessViolations);
				if (accessViolations>3)
					return false;					
			}
			
			if (info.nestedClasses) {
				debug(" Skipped (nested classes)");
				return false;
			}
			
			//if (info.proceedInvocations>1)
			debug(" Size of advice method: " + info.originalSize);
			debug(" Number of applications: " + info.applications);
			debug(" Number of added locals (approximately): " + info.internalLocalCount);
			debug(" Proceed invocations: " + info.proceedInvocations);
			
						
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
			
			debug("Trying to inline proceed method " + method);
			
			if (forceInline()) {
				debug("force inline on.");
				return true;
			}
			
						
			AroundWeaver.ProceedMethodInlineInfo info=					
				AroundWeaver.state.getProceedMethodInlineInfo(method);
			
			AroundWeaver.ShadowInlineInfo shadowInfo=null;
			debug("Proceed method: " + method);
			
			if (stmt.hasTag("AroundShadowInfoTag"))	{
				AroundShadowInfoTag tag=
					(AroundShadowInfoTag)stmt.getTag("AroundShadowInfoTag");
			
				debug(" Found tag.");
				shadowInfo=tag.shadowInfo;
			} else {
				soot.Value v=expr.getArg(info.shadowIDParamIndex);
				if (Evaluator.isValueConstantValued(v)) {
                    v = Evaluator.getConstantValueOf(v);
                    int shadowID=((IntConstant) v).value;
                  
                    shadowInfo=
                    	(AroundWeaver.ShadowInlineInfo) info.shadowInformation.get(new Integer(shadowID));                 	
                    
                    stmt.addTag(new AroundShadowInfoTag(
                    		shadowInfo));
				}
			}
			if (shadowInfo!=null) {
				debug(" Shadow size: " + shadowInfo.size);
				debug(" Number of additional locals (approximately): " + shadowInfo.internalLocals);
			} else {
				debug(" Could not find shadow information.");				
			}
			if (shadowInfo!=null && shadowInfo.size<6)
				return true;
			
			

			return false;
		}
	}
	
}
