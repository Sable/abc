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

package abc.tm.weaving.weaver.tmanalysis.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import abc.tm.weaving.weaver.tmanalysis.util.IdentityHashSet;

import soot.Body;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.UnitGraph;

/**
 * An abstracted version of a unit graph.
 * Retains only nodes which fulfill a given predicate.
 * There exists an edge between two nodes in the abstracted graph whenever
 * the starting and end node of this edge fulfill the predicate and
 * the end node is directly reachable from the starting node.
 * @author Eric Bodden
 */
public class AbstractedUnitGraph extends UnitGraph {

	/**
	 * The original unit graph.
	 */
	protected UnitGraph delegate;
	
	/**
	 * The unit predicate, which tells what nodes to retain. 
	 */
	protected UnitPredicate unitPredicate;
	
	/**
	 * Set of visited nodes. Needed internally for the algorithm.
	 */
	protected volatile Set visited;
	
	/**
     * Creates a new abstracted unit graph for a given unit graph under the given
     * predicate.
     * 
     * @param fullGraph a unit graph
	 * @param a predicate stating which units to retain
	 */
	public AbstractedUnitGraph(UnitGraph fullGraph, UnitPredicate unitPredicate) {
		//pass the body to make the compiler appy
		super(fullGraph.getBody());
		
		if(fullGraph == null) {
			throw new IllegalArgumentException("Delegate call graph null!");
		}
		if(unitPredicate == null) {
			throw new IllegalArgumentException("Node predicate null!");
		}
		
		this.delegate = fullGraph;
		this.unitPredicate = unitPredicate;
		
		super.unitToSuccs = new HashMap();
		super.unitToPreds = new HashMap();
		
		//build up the abstracted graph
		performAbstraction();
		
		//remove all units which are no longer referenced
		removeUnreferencedUnits();		
	}


	/**
	 * This builds up the abstracted graph, storing the relevant edges in the field {@link CallGraph#edges}.
	 */
	protected void performAbstraction() {
		visited = new IdentityHashSet();
		
		//for all entry points
		for (Iterator iter = delegate.getHeads().iterator(); iter.hasNext();) {
			Unit entryPoint = (Unit) iter.next();
			
			//for all edges out of this node
			for (Iterator edgeIter = delegate.getSuccsOf(entryPoint).iterator(); edgeIter.hasNext();) {
				Unit succ = (Unit) edgeIter.next();

				//perform the abstraction for the paths leaving the node
				//over this edge
				abstractPath(entryPoint, succ);				
			}			
		}
		
		visited.clear();
		visited = null;
	}

	/**
	 * Build the abstraction for a certain path in the graph.
	 * @param entryPoint the edge which led to the current node at the last marked
	 * node on this path
	 * @param succ the current node
	 */
	protected void abstractPath(Unit lastMarked, Unit curr) {		
		boolean currNodeMarked = false; 
		
		//if we want to keep this node
		if(unitPredicate.want(curr)) {
			
			
			//add an edge from the last marked node to this node
			super.addEdge(unitToSuccs,unitToPreds,lastMarked,curr);
			
			//memorize that we want to keep this node
			currNodeMarked = true;
		}
		
		//if not already visited before
		if(!visited.contains(curr)) {
			//mark as visited
			visited.add(curr);
			
			//recurse over all outgoing edges
			for (Iterator edgeIter = delegate.getSuccsOf(curr).iterator(); edgeIter.hasNext();) {
				Unit succ = (Unit) edgeIter.next();
				
				//if the current node was marked, this is now the last one marked
				if(currNodeMarked) {
					lastMarked = curr;			
				} 
				abstractPath(lastMarked, succ);
			}
		}		
	}


	/**
	 * Removes all units which are no longer referenced.
	 */
	protected void removeUnreferencedUnits() {
		Body old = body;
		body = (Body) body.clone();
		unitChain = body.getUnits();
		unitChain.clear();
		for (Iterator iter = old.getUnits().iterator(); iter.hasNext();) {
			Unit unit = (Unit) iter.next();
			if(unitToSuccs.containsKey(unit) || unitToPreds.containsKey(unit)) {
				unitChain.add(unit);
			}
		}
	}
    
    //Need to override the following two methods because during the processing
    //it can well happen that a NoSuchElementException would normally arise.
	
	/**
	 * @inheritdoc
	 */
	public List getPredsOf(Object u) {
		try {			
			return super.getPredsOf(u);
		} catch(NoSuchElementException e) {
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * @inheritdoc
	 */
	public List getSuccsOf(Object u) {
		try {			
			return super.getSuccsOf(u);
		} catch(NoSuchElementException e) {
			return Collections.EMPTY_LIST;
		}
	}
}
