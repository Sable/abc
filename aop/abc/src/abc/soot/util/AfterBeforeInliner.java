/*
 * Created on 04-Nov-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.soot.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.SootMethod;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import abc.weaving.weaver.AroundWeaver;

/**
 * @author kuzins
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AfterBeforeInliner extends AdviceInliner {
	
	private static AfterBeforeInliner instance = 
		new AfterBeforeInliner();
	public static void reset() { instance = new AfterBeforeInliner(); }
	public static AfterBeforeInliner v() { return instance; }
	
	
	public void doInlining() {
		for (Iterator it=shadowMethods.iterator(); it.hasNext(); ) {
			final SootMethod m=(SootMethod)it.next();
			transform(m.getActiveBody());
		}
	}
	
	private void debug(String message) {
		if (abc.main.Debug.v().afterBeforeInliner)
			System.err.println("ABI*** " + message);
	}

	boolean forceInline() {
		return abc.main.options.OptionsParser.v().before_after_force_inlining();
	}
	
	private Set shadowMethods=new HashSet();
	public void addShadowMethod(SootMethod m) {
		shadowMethods.add(m);
	}
	
	public static boolean isAdviceMethodName(String name) {
		return name.startsWith("before$") || name.startsWith("after$") ||
		name.startsWith("afterReturning$") ||
		name.startsWith("afterThrowing$");
	}
	
	private class AdviceMethodInlineOptions implements InlineOptions {
		
		public boolean inline(SootMethod container, Stmt stmt, InvokeExpr expr) {
			SootMethod method=expr.getMethod();
			if (!isAdviceMethodName(expr.getMethodRef().name()))
				return false;
			
			debug("Trying to inline advice method " + method);
			
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
				if (accessViolations>3)
					return false;					
			}
			Body body=method.getActiveBody();
			
			//if (info.proceedInvocations>1)
			int size=body.getUnits().size();
			debug(" Size of advice method: " + size);
			int addedLocals=body.getLocalCount()-method.getParameterCount();
			debug(" Number of added locals (approximately): " + addedLocals);			
						
			if (size<6)
				return true;
			
			
			return false;
		}
	}
	
	protected void internalTransform(Body body, String phaseName, Map options) {
		// do this in a loop:
		// after inlining, additional advice method calls may be present
		// (if the same joinpoint was advised multiple times, or in the case
		// of nested joinpoints)
		int depth=0;
		while (inlineMethods(body, options, new AdviceMethodInlineOptions())) {
			
			// TODO: maybe should run whole jop pack here
			// to reduce method size between inlining passes
			
			depth++;
			if (depth>=MAX_DEPTH)
				break;
		}
	}
	
}
