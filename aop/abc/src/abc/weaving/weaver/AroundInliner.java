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

/**
 * @author Sascha Kuzins
 *
 */
public class AroundInliner { //extends AdviceInliner {
/*
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
	
	
	
	

	// Called for all methods that contained shadows.
	// Whenever a proceed method or an advice method is inlined, 
	// the method calls itself recursively.
	protected void internalTransform(Body body, String phaseName, Map options) {
		internalTransform(body, phaseName, options, 0, 4);
	}
	
	
	
	protected void inline(Body body, Set visitedBodies) {
		
		if (visitedBodies.contains(body))
			return;
		
		if (!bodyHasRelevantCalls(body))
			return; // to prevent the constant prop if it's not necessary
		
		// remove dead code from the dynamic residues.
		// this is important because the dead code may contain a call
		// to the proceed method.
		ConstantPropagatorAndFolder.v().transform(body);
		UnreachableCodeEliminator.v().transform(body);
		
		// inline if methods from the dynamic residue
		if (inlineMethods(body, new IfMethodInlineOptions(), visitedBodies)) {
			// 	process the inlined if 
			ConstantPropagatorAndFolder.v().transform(body);
			UnreachableCodeEliminator.v().transform(body);
		}
		
		boolean bDidInline=false;
		// for the failed-case of the dynamic residue
		if (inlineMethods(body, new ProceedMethodInlineOptions(body), depth)) {
			foldSwitches(body);
			bDidInline=true;
		}
		
		// do this recursively:
		// after inlining, additional advice method calls may be present
		// (if the same joinpoint was advised multiple times, or in the case
		// of nested joinpoints)		
		if (inlineMethods(body, new AroundAdviceMethodInlineOptions(), depth)) {
			foldSwitches(body);
			bDidInline=true;
		}
		
		if (inlineMethods(body, new ExtractedShadowMethodInlineOptions(body), depth)) {
			bDidInline=true;
		}	
		
		//BoxingRemover.removeUnnecessaryCasts(body);
		
		if (bDidInline) { // recurse
			internalTransform(body, phaseName, options, depth, MAX_DEPTH);
			return;
		}
	}

	*/
	
	
}
