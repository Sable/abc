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

import java.util.Map;

import soot.Body;
import soot.SootMethod;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import abc.soot.util.AroundShadowInfoTag;
import abc.soot.util.SwitchFolder;
import abc.weaving.weaver.around.AroundWeaver;
import abc.weaving.weaver.around.Util;
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
	
	
	public boolean forceInline() {
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
		
		inlineMethods(body, options, new IfMethodInlineOptions());
			
		// do this in a loop:
		// after inlining, additional advice method calls may be present
		// (if the same joinpoint was advised multiple times, or in the case
		// of nested joinpoints)
		int depth=0;
		while (inlineMethods(body, options, new AdviceMethodInlineOptions())) {
			foldSwitches(body);
			inlineMethods(body, options, new ProceedMethodInlineOptions(body));
			foldSwitches(body);
			
			inlineMethods(body, options, new IfMethodInlineOptions());
			
			depth++;
			if (depth>=MAX_DEPTH)
				break;
		}
	}

	
	private class AdviceMethodInlineOptions implements InlineOptions {
		
		public boolean inline(SootMethod container, Stmt stmt, InvokeExpr expr) {
			SootMethod method=expr.getMethod();
			if (!Util.isAroundAdviceMethodName(expr.getMethodRef().name()))
				return false;
			
			debug("Trying to inline advice method " + method);
			
			if (forceInline()) {
				debug("force inline on.");
				return true;	
			}
			
			
			AroundWeaver.AdviceMethodInlineInfo info=
					AroundWeaver.v().getAdviceMethodInlineInfo(method);
			
			int accessViolations=getAccessViolationCount(container, method);
			if (accessViolations!=0) {
				debug("Access violations");
				debug(" Method: " + container);
				debug(" Advice method: " + method); 
				debug(" Violations: " + accessViolations);
				if (accessViolations>1)
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
			
						
			if (info.originalSize<6)
				return true;
			
			//if (info.internalLocalCount==0)
			//	return true;
			//if (info.applications==1)
			//	return true;
			
			return false;
		}
	}
	private class ProceedMethodInlineOptions implements InlineOptions {
		public ProceedMethodInlineOptions(Body body) {
			
		}
		public boolean inline(SootMethod container, Stmt stmt, InvokeExpr expr) {
			SootMethod method=expr.getMethod();
			
			if (!Util.isProceedMethodName(expr.getMethodRef().name()))
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
				AroundWeaver.v().getProceedMethodInlineInfo(method);
			
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
			if (shadowInfo!=null) {
				if (shadowInfo.size<6)
					return true;
				
				//if (shadowInfo.internalLocals==0)
				//	return true;
			}
				
			
			

			return false;
		}
	}
	
	
}
