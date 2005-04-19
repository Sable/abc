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
public class AfterBeforeInliner { //extends AdviceInliner {
	
	/*private static AfterBeforeInliner instance = 
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
	
	
	
	final private static int MAX_DEPTH=4;
	protected void transform(Body body) {
	
		int depth=0;
		
		inlineMethods(body, new IfMethodInlineOptions(), depth);
		ConstantPropagatorAndFolder.v().transform(body);
		UnreachableCodeEliminator.v().transform(body);
		
		// do this in a loop:
		// after inlining, additional advice method calls may be present
		// (if the same joinpoint was advised multiple times, or in the case
		// of nested joinpoints)

		while (inlineMethods(body, new AfterBeforeMethodInlineOptions(), depth)) {
			
			// TODO: maybe should run whole jop pack here
			// to reduce method size between inlining passes
			ConstantPropagatorAndFolder.v().transform(body);
			UnreachableCodeEliminator.v().transform(body);
			
			inlineMethods(body, new IfMethodInlineOptions(), depth);
			
			ConstantPropagatorAndFolder.v().transform(body);
			UnreachableCodeEliminator.v().transform(body);
			
			depth++;
			if (depth>=MAX_DEPTH)
				break;
		}
	}
	*/
}
