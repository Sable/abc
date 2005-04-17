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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.SootMethod;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;

/**
 * @author Sascha Kuzins
 *
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

	public boolean forceInline() {
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
		
		public int inline(SootMethod container, Stmt stmt, InvokeExpr expr, int depth) {
			SootMethod method=expr.getMethod();
			if (!isAdviceMethodName(expr.getMethodRef().name()))
				return InlineOptions.DONT_INLINE;
			
			debug("Trying to inline advice method " + method);
			
			if (forceInline()) {
				debug("force inline on.");
				return InlineOptions.INLINE_DIRECTLY;	
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
			Body body=method.getActiveBody();
			
			//if (info.proceedInvocations>1)
			int size=body.getUnits().size();
			debug(" Size of advice method: " + size);
			int addedLocals=body.getLocalCount()-method.getParameterCount();
			debug(" Number of added locals (approximately): " + addedLocals);			
						
			if (size<6)
				return InlineOptions.INLINE_STATIC_METHOD;
			
			
			return InlineOptions.DONT_INLINE;
		}
	}
	final private static int MAX_DEPTH=4;
	protected void internalTransform(Body body, String phaseName, Map options) {
	
		int depth=0;
		
		inlineMethods(body, options, new IfMethodInlineOptions(), depth);
		ConstantPropagatorAndFolder.v().transform(body);
		UnreachableCodeEliminator.v().transform(body);
		
		// do this in a loop:
		// after inlining, additional advice method calls may be present
		// (if the same joinpoint was advised multiple times, or in the case
		// of nested joinpoints)

		while (inlineMethods(body, options, new AdviceMethodInlineOptions(), depth)) {
			
			// TODO: maybe should run whole jop pack here
			// to reduce method size between inlining passes
			ConstantPropagatorAndFolder.v().transform(body);
			UnreachableCodeEliminator.v().transform(body);
			
			inlineMethods(body, options, new IfMethodInlineOptions(), depth);
			
			ConstantPropagatorAndFolder.v().transform(body);
			UnreachableCodeEliminator.v().transform(body);
			
			depth++;
			if (depth>=MAX_DEPTH)
				break;
		}
	}
	
}
