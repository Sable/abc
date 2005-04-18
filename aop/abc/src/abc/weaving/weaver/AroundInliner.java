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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import polyglot.util.InternalCompilerError;
import soot.Body;
import soot.SootMethod;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.util.Chain;
import abc.main.options.OptionsParser;
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
	
	public Set adviceMethodsNotInlined=new HashSet();
	
	
	private static boolean methodContainsSwitch(Body body) {
		Chain statements=body.getUnits();
		for (Iterator it=statements.iterator();it.hasNext();) {
			Stmt s=(Stmt)it.next();
			if (s instanceof TableSwitchStmt || s instanceof LookupSwitchStmt)
				return true;
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see soot.BodyTransformer#internalTransform(soot.Body, java.lang.String, java.util.Map)
	 */
	private void foldSwitches(Body body) {
		if (!methodContainsSwitch(body))
			return;
		ConstantPropagatorAndFolder.v().transform(body);
		SwitchFolder.v().transform(body); // TODO: phaseName etc.?
		UnreachableCodeEliminator.v().transform(body);
	}

	// Called for all methods that contained shadows.
	// Whenever a proceed method or an advice method is inlined, 
	// the method calls itself recursively.
	protected void internalTransform(Body body, String phaseName, Map options) {
		internalTransform(body, phaseName, options, 0, 4);
	}
	/*private int getMaxDepth() {
		if (forceInline())
			return MAX_DEPTH;
		else
			return 1;
	}*/
	public void transform(Body b, int MAX_DEPTH) {
		HashMap dummyOptions = new HashMap();
        dummyOptions.put( "enabled", "true" );
		internalTransform(b, "",dummyOptions, 0, MAX_DEPTH);
	}
	protected void internalTransform(Body body, String phaseName, Map options, int depth, int MAX_DEPTH) {
		depth++;
		if(depth>MAX_DEPTH)
			return;
		
		// remove dead code from the dynamic residues.
		// this is important because the dead code may contain a call
		// to the proceed method.
		ConstantPropagatorAndFolder.v().transform(body);
		UnreachableCodeEliminator.v().transform(body);
		
		// inline if methods from the dynamic residue
		if (inlineMethods(body, options, new IfMethodInlineOptions(), depth)) {
			// 	process the inlined if 
			ConstantPropagatorAndFolder.v().transform(body);
			UnreachableCodeEliminator.v().transform(body);
		}
		
		boolean bDidInline=false;
		// for the failed-case of the dynamic residue
		if (inlineMethods(body, options, new ProceedMethodInlineOptions(body), depth)) {
			foldSwitches(body);
			bDidInline=true;
		}
		
		// do this recursively:
		// after inlining, additional advice method calls may be present
		// (if the same joinpoint was advised multiple times, or in the case
		// of nested joinpoints)		
		if (inlineMethods(body, options, new AdviceMethodInlineOptions(), depth)) {
			foldSwitches(body);
			bDidInline=true;
		}
		
		if (inlineMethods(body, options, new ExtractedShadowMethodInlineOptions(body), depth)) {
			bDidInline=true;
		}	
		
		//BoxingRemover.removeUnnecessaryCasts(body);
		
		if (bDidInline) { // recurse
			internalTransform(body, phaseName, options, depth, MAX_DEPTH);
			return;
		}
	}

	
	private class AdviceMethodInlineOptions implements InlineOptions {
		
		public int inline(SootMethod container, Stmt stmt, InvokeExpr expr, int depth) {
			SootMethod method=expr.getMethod();
			if (!Util.isAroundAdviceMethodName(expr.getMethodRef().name()))
				return InlineOptions.DONT_INLINE;
			
			int bDidInline=internalInline(container, stmt, expr, depth);
			if (bDidInline!=InlineOptions.INLINE_DIRECTLY) {
				//adviceMethodsNotInlined.add(method);
			}
			return bDidInline;
		}
		private int internalInline(SootMethod container, Stmt stmt, InvokeExpr expr, int depth) {
			SootMethod method=expr.getMethod();
			
			debug("Trying to inline advice method " + method);
			
			if (forceInline()) {
				debug("force inline on.");
				return InlineOptions.INLINE_DIRECTLY;	
			} else if (true) {				
				if (container.getName().startsWith("inline$")) {
					//if (true)throw new InternalCompilerError("");
					return DONT_INLINE;
				} else if (Util.isAroundAdviceMethodName(container.getName())) {
						return DONT_INLINE;
				} else {
					//if (true)throw new InternalCompilerError("");
					debug("container: " + container.getName());
					return InlineOptions.INLINE_STATIC_METHOD;
				}
			}
			// unreachable code below.
			
			AroundWeaver.AdviceMethodInlineInfo info=
					AroundWeaver.v().getAdviceMethodInlineInfo(method);
			
			AroundWeaver.ShadowInlineInfo shadowInfo=null;
			debug("Proceed method: " + method);
			
			if (stmt.hasTag("AroundShadowInfoTag"))	{
				AroundShadowInfoTag tag=
					(AroundShadowInfoTag)stmt.getTag("AroundShadowInfoTag");
			
				debug(" Found tag.");
				shadowInfo=tag.shadowInfo;
			}
			if (shadowInfo!=null) {
				if (shadowInfo.weavingRequiredUnBoxing) {
					debug(" (Un-)Boxing detected. Inlining.");
					return InlineOptions.INLINE_STATIC_METHOD;
				}
			}
			
			int accessViolations=getAccessViolationCount(container, method);
			if (accessViolations!=0) {
				debug("Access violations");
				debug(" Method: " + container);
				debug(" Advice method: " + method); 
				debug(" Violations: " + accessViolations);
				if (accessViolations>1)
					return InlineOptions.DONT_INLINE;					
			}
			
			if (info.nestedClasses) {
				debug(" Skipped (nested classes)");
				return InlineOptions.DONT_INLINE;
			}
			
			//if (info.proceedInvocations>1)
			debug(" Size of advice method: " + info.originalSize);
			debug(" Number of applications: " + info.applications);
			debug(" Number of added locals (approximately): " + info.internalLocalCount);
			debug(" Proceed invocations: " + info.proceedInvocations);
			
			
			if (info.originalSize< (20 >> (depth-1)))
				return InlineOptions.INLINE_STATIC_METHOD;
			
			//if (info.internalLocalCount==0)
			//	return true;
			//if (info.applications==1)
			//	return true;
			
			return InlineOptions.DONT_INLINE;
		}
	}
	private class ProceedMethodInlineOptions implements InlineOptions {
		public ProceedMethodInlineOptions(Body body) {
			
		}
		public int inline(SootMethod container, Stmt stmt, InvokeExpr expr, int depth) {
			SootMethod method=expr.getMethod();
			
			//debug("PROCEED: " + method);
			if (!Util.isProceedMethodName(expr.getMethodRef().name()))
				return InlineOptions.DONT_INLINE;
			
			if (!method.isStatic())
				return InlineOptions.DONT_INLINE;
			
			if (!method.getDeclaringClass().equals(container.getDeclaringClass()) &&
				OptionsParser.v().around_inlining() &&
				OptionsParser.v().around_force_inlining())
				return InlineOptions.DONT_INLINE;
			
			debug("Trying to inline proceed method " + method);
			
//			 we now *always* inline proceed 
			// because the shadow is always tiny due to the extraction.
		
			if (true)
				return InlineOptions.INLINE_DIRECTLY;
			// unreachable code below
			
			if (forceInline()) {
				debug("force inline on.");
				return InlineOptions.INLINE_DIRECTLY;
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
				if (shadowInfo.weavingRequiredUnBoxing) {
					debug(" (Un-)Boxing detected. Inlining.");
					return INLINE_DIRECTLY;
				}
				
				if (shadowInfo.size<10)
					return INLINE_DIRECTLY;
				
				//if (shadowInfo.internalLocals==0)
				//	return true;
			}
				
			
			

			return DONT_INLINE;
		}
	}
	private class ExtractedShadowMethodInlineOptions implements InlineOptions {
		public ExtractedShadowMethodInlineOptions(Body body) {
			
		}
		public int inline(SootMethod container, Stmt stmt, InvokeExpr expr, int depth) {
			SootMethod method=expr.getMethod();
			
			//debug("PROCEED: " + method);
			if (!expr.getMethodRef().name().startsWith("shadow$"))
				return InlineOptions.DONT_INLINE;
			
			if (!method.isStatic())
				throw new InternalCompilerError("");
			
			
			if (!method.getDeclaringClass().equals(container.getDeclaringClass())) {
				int accessViolations=getAccessViolationCount(container, method);
				if (accessViolations>0)
					return DONT_INLINE;
			}  
				
				
			
			debug("Trying to inline shadow method " + method);
			
//			 we now *always* inline proceed 
			// because the shadow is always tiny due to the extraction.
		
			if (forceInline()) {
				debug("force inline on.");
				return InlineOptions.INLINE_DIRECTLY;
			}
	
			int size=method.getActiveBody().getUnits().size()
				- method.getParameterCount();
			debug("  size: " + size);
			if (size<3)
				return INLINE_DIRECTLY;
			
			return DONT_INLINE;
		}
	}
	
}
