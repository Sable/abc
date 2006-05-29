/* Soot - a J*va Optimization Framework
 * Copyright (C) 2006 Eric Bodden
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package abc.tm.weaving.weaver.tmanalysis.callgraph;

import java.util.Iterator;
import java.util.Set;

import abc.tm.weaving.weaver.tmanalysis.util.IdentityHashSet;

import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

/**
 * An abstracted version of a call graph.
 * The constructor takes the original graph as input plus a predicate which tells what nodes
 * to retain in the abstracted graph.
 * The abstracted graph then contains an edge <i>(src,srcUnit,kind,target)</i>
 * <i>src</i> and <i>target</i> are to be retained and
 * in the original call graph there is a <i>path</i> from <i>src</i> to
 * <i>target</i> over the unit <i>srcUnit</i> of kind <i>kind</i>.
 *
 * @author Eric Bodden
 */
public class AbstractedCallGraph extends CallGraph {
	
	/**
	 * The original callgraph.
	 */
	protected CallGraph delegate;	

	/**
	 * The node predicate, which tells what nodes to retain. 
	 */
	protected NodePredicate nodePredicate;	

	/**
	 * Set of visited nodes. Needed internally for the algorithm.
	 */
	protected volatile Set visited;

	/**
	 * Constructs an abstracttion of the given call graph under the given node predicate.  
	 * @param fullGraph the original call graph
	 * @param nodePredicate a predicate telling what nodes to retain in the abstracted graph
	 */
	public AbstractedCallGraph(CallGraph fullGraph, NodePredicate nodePredicate) {
		super();

		if(fullGraph == null) {
			throw new IllegalArgumentException("Delegate call graph null!");
		}
		if(nodePredicate == null) {
			throw new IllegalArgumentException("Node predicate null!");
		}
		
		this.delegate = fullGraph;
		this.nodePredicate = nodePredicate;
		
		//build up the abstracted graph
		performAbstraction();
	}

	/**
	 * This builds up the abstracted graph, storing the relevant edges in the field {@link CallGraph#edges}.
	 */
	protected void performAbstraction() {
		visited = new IdentityHashSet();
		
		//for all entry points
		for (Iterator iter = Scene.v().getEntryPoints().iterator(); iter.hasNext();) {
			MethodOrMethodContext entryPoint = (SootMethod) iter.next();
			
			//for all edges out of this node
			for (Iterator edgeIter = delegate.edgesOutOf(entryPoint); edgeIter.hasNext();) {
				Edge edge = (Edge) edgeIter.next();

				//perform the abstraction for the paths leaving the node
				//over this edge
				abstractPath(edge, edge.getTgt());				
			}			
		}
		
		visited.clear();
		visited = null;
	}

	/**
	 * Build the abstraction for a certain path in the graph.
	 * @param lastMarked the edge which led to the current node at the last marked
	 * node on this path
	 * @param curr the current node
	 */
	protected void abstractPath(Edge lastMarked, MethodOrMethodContext curr) {		
		boolean currNodeMarked = false; 
		
		//if we want to keep this node
		if(nodePredicate.want(curr)) {
			
			//add an edge from the last marked node to this node
			super.addEdge(new Edge(
					lastMarked.getSrc(),
					lastMarked.srcUnit(),
					curr,
					lastMarked.kind()
			));
			
			//memorize that we want to keep this node
			currNodeMarked = true;
		}
		
		//if not already visited before
		if(!visited.contains(curr)) {
			//mark as visited
			visited.add(curr);
			
			//recurse over all outgoing edges
			for (Iterator iter = delegate.edgesOutOf(curr); iter.hasNext();) {
				Edge succEdge = (Edge) iter.next();
				
				//if the current node was marked, this is now the last one marked
				if(currNodeMarked) {
					lastMarked = succEdge;			
				} 
				abstractPath(lastMarked, succEdge.getTgt());
			}
		}		
	}
}
