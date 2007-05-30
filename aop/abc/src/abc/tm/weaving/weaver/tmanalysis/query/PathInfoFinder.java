/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
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
package abc.tm.weaving.weaver.tmanalysis.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.StateMachine;
import abc.tm.weaving.weaver.tmanalysis.ds.Bag;
import abc.tm.weaving.weaver.tmanalysis.ds.HashBag;
import abc.tm.weaving.weaver.tmanalysis.util.Naming;

/**
 * Computes the labels of all edges that dominate a final state in the state machine.
 * This means that all those events must occur in some order to lead to a match.
 * 
 * @author Eric Bodden
 */
public class PathInfoFinder {

	protected transient Set allPaths;
	
	protected Set pathInfos;
	
	/**
	 * Constructs a new analysis for the given state machine.
	 * @param traceMatch a tracematch state machine.
	 */
	public PathInfoFinder(TraceMatch traceMatch) {
		pathInfos = new HashSet();
		
		//get the special state machine we require for the analysis
		//this state machine holds no cycles instead of skip-loops
		StateMachine sm = traceMatch.getNecessarySymbolsStateMachine();
		
		allPaths = new HashSet();
		//find all paths in SM:
		//recurse into the automaton starting at each initial nodel
		//this will fill "allPaths"
		for (Iterator nodeITer = sm.getStateIterator(); nodeITer.hasNext();) {
			SMNode node = (SMNode) nodeITer.next();
			if(node.isInitialNode()) {
				recurse(node,new ArrayList());
			}
		}
		
		//for every path, 
		for (Iterator pathIter = allPaths.iterator(); pathIter.hasNext();) {
			Collection path = (Collection) pathIter.next();
			Bag labSet = new HashBag();
			Set skipLoopLabelSet = new HashSet();
			for (Iterator edgeIter = path.iterator(); edgeIter.hasNext();) {
				SMEdge edge = (SMEdge) edgeIter.next();
				String lab = Naming.getSymbolShortName(edge.getLabel());
				labSet.add(lab);
				SMNode tgt = edge.getTarget();
				for (Iterator outIter = tgt.getOutEdgeIterator(); outIter.hasNext();) {
					SMEdge outEdge = (SMEdge) outIter.next();
					if(outEdge.isSkipEdge()) {
						skipLoopLabelSet.add(outEdge.getLabel());
					}
				}
			}
			skipLoopLabelSet = Collections.unmodifiableSet(skipLoopLabelSet);			
			pathInfos.add(new PathInfo(labSet,skipLoopLabelSet));
		}

		pathInfos = Collections.unmodifiableSet(pathInfos);
		
		allPaths = null;
	}

	/**
	 * Computes all paths which are contain <i>prefix</i> as a prefix and
	 * for which the remainder of the path startes at <i>node</i> and ends in a final state.
	 * The resulting paths are added to allPaths.
	 * @param node a node of a {@link StateMachine} which holds no cycles apart from skip-loops
	 * @param prefix any list of nodes
	 */
	private void recurse(SMNode node, List prefix) {
		boolean reachedEnd = true;
		for (Iterator outIter = node.getOutEdgeIterator(); outIter.hasNext();) {
			reachedEnd = false;
			SMEdge edge = (SMEdge) outIter.next();
			if(!edge.isSkipEdge()) {
				SMNode next = edge.getTarget();
				List subpath = new ArrayList(prefix);
				subpath.add(edge);
				recurse(next,subpath);
			}
		}
		if(reachedEnd) {
			allPaths.add(prefix);
		}
	}

	/**
	 * @return all path infos cumputed for the given tracematch
	 */
	public Set getPathInfos() {
		return pathInfos;
	}
	
	/**
	 * A path info holds a bag of dominating labels and a set of skip-labels.
	 * The dominating labels have to be visited (as often as they are contained in the bag)
	 * in order to reach a final state. Skip loops with the given skip loop labels could interfere
	 * with a match on the same path to the final state.
	 * @author Eric Bodden
	 */
	public static class PathInfo {
		
		protected Bag dominatingLabels;
		
		protected Set skipLoopLabels;

		public PathInfo(Bag dominatingLabels, Set skipLoopLabels) {
			this.dominatingLabels = dominatingLabels;
			this.skipLoopLabels = skipLoopLabels;
		}

		/**
		 * @return the dominating labels
		 */
		public Bag getDominatingLabels() {
			return new HashBag(dominatingLabels);
		}

		/**
		 * @return the skip loop labels
		 */
		public Set getSkipLoopLabels() {
			return new HashSet(skipLoopLabels);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public String toString() {
			return "<dom-labels="+dominatingLabels+",skip-labels="+skipLoopLabels+">";
		}

		/**
		 * {@inheritDoc}
		 */
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((dominatingLabels == null) ? 0 : dominatingLabels
							.hashCode());
			result = prime
					* result
					+ ((skipLoopLabels == null) ? 0 : skipLoopLabels.hashCode());
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final PathInfo other = (PathInfo) obj;
			if (dominatingLabels == null) {
				if (other.dominatingLabels != null)
					return false;
			} else if (!dominatingLabels.equals(other.dominatingLabels))
				return false;
			if (skipLoopLabels == null) {
				if (other.skipLoopLabels != null)
					return false;
			} else if (!skipLoopLabels.equals(other.skipLoopLabels))
				return false;
			return true;
		}
		
	}

}
 