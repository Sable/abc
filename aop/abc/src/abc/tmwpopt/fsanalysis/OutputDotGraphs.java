/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Patrick Lam
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
package abc.tmwpopt.fsanalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.Scene;
import soot.SootMethod;
import abc.da.weaving.aspectinfo.AdviceDependency;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.TMStateMachine;

/**
 * Produces clickable dot graphs of tracematch automata.
 * 
 * We present all PFGs, in increasing size. When the user clicks on a PFG this
 * brings him to the PPF. From there he then should be able to navigate to all
 * overlapping shadows. Ideally, only overlapping shadows would be highlighted
 * in the code. (i.e. the display is contextual on the PFG).
 * 
 * @author Patrick Lam
 */
public class OutputDotGraphs {

	public void apply(TraceMatch tm) {
		PrintWriter out;

		try {
			String tmName = tm.getContainerClass().getShortName();
			tmName += "." + tm.getName();

			String bmName = Scene.v().getMainClass().getName();
			String fileName = bmName + "-" + tmName + ".dot";
			new File(fileName).delete();

			out = new PrintWriter(new FileOutputStream(fileName));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		final Set<Shadow> reachableActiveShadows = Shadow
				.reachableActiveShadows();

		// Pick out a shadow and its overlapping set.
		while (!reachableActiveShadows.isEmpty()) {
			Shadow s = reachableActiveShadows.iterator().next();
			Collection<Shadow> ss = new java.util.LinkedList<Shadow>();
			ss.add(s);
			Collection<Shadow> sg = AdviceDependency
					.getAllEnabledShadowsOverlappingWith(ss);
			reachableActiveShadows.removeAll(sg);
			outputGraph(out, tm, sg);
		}

		out.close();
	}

	private void removeNotSymbolAdvice(TraceMatch tm, Collection<Shadow> g) {
		Set<SootMethod> symbolAdviceMethods = new HashSet<SootMethod>();
		for (String sym : tm.getSymbols()) {
			SootMethod adviceMethod = tm.getSymbolAdviceMethod(sym);
			symbolAdviceMethods.add(adviceMethod);
		}

		for (Iterator<Shadow> iter = g.iterator(); iter.hasNext();) {
			Shadow s = iter.next();
			if (!symbolAdviceMethods.contains(s.getAdviceDecl().getImpl()
					.getSootMethod())) {
				iter.remove();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void outputGraph(PrintWriter out, TraceMatch tm,
			Collection<Shadow> g) {
		TMStateMachine sm = (TMStateMachine) tm.getStateMachine();

		removeNotSymbolAdvice(tm, g);

		out.println("digraph " + tm.getName().replace('$', '_') + " {");
		Iterator si = sm.getStateIterator();
		while (si.hasNext()) {
			SMNode n = (SMNode) si.next();
			out.println("  q" + n.getNumber() + ";");
		}

		// How do we know if a shadow corresponds to a given edge?
		// Naive approach: match only by edge names
		// Smarter approach: use intraprocedural results to reduce number of
		// shadows at farther edges

		// How do we get line number info out of the shadows?
		// call getPosition() on the shadow.

		Iterator ei = sm.getEdgeIterator();
		while (ei.hasNext()) {
			SMEdge e = (SMEdge) ei.next();
			if (e.getLabel().equals("newDaCapoRun"))
				continue;
			String label = " ";
			label = label + "label=\"" + e.getLabel() + "\" ";
			if (e.isSkipEdge())
				label = label + "style=dashed ";
			label = label + "URL=\"#" + e.getLabel() + "\"";
			out.println("  q" + e.getSource().getNumber() + " -> q"
					+ e.getTarget().getNumber() + " [" + label + "];");
		}

		out.println("}");
	}

	// singleton pattern

	protected static OutputDotGraphs instance;

	private OutputDotGraphs() {
	}

	public static OutputDotGraphs v() {
		if (instance == null) {
			instance = new OutputDotGraphs();
		}
		return instance;
	}

	/**
	 * Frees the singleton object.
	 */
	public static void reset() {
		instance = null;
	}
}
