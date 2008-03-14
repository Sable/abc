/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Eric Bodden
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
package abc.tmwpopt.fsanalysis.callgraph;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

/**
 * A abstracted version of a call graph.
 * This call graph contains only those edges on paths to any method that is matched by the {@link NodePredicate} that is passed in. 
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
	 * A result cache. Necessary to break cycles due to recursion.
	 */
	protected transient Map<MethodOrMethodContext,Integer> resultCache;

	/** Status flag used in the computation. TRUE means <i>have to keep this method</i>, FALSE means <i>can leave out this method</i> and <i>COMPUTING</i>
	 * means that we are currently computing this status for this method. (This is used to avoid infinite recursion.) */
	protected static final int TRUE=0, FALSE=1, COMPUTING=2;

	/**
	 * Constructs an abstraction of the given call graph under the given {@link NodePredicate}.  
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
		resultCache = new IdentityHashMap<MethodOrMethodContext, Integer>();
		
		//for all entry points
		for (Iterator<SootMethod> iter = Scene.v().getEntryPoints().iterator(); iter.hasNext();) {
			MethodOrMethodContext entryPoint = (SootMethod) iter.next();
			
			//for all edges out of this node
			for (Iterator<Edge> edgeIter = delegate.edgesOutOf(entryPoint); edgeIter.hasNext();) {
				Edge edge = edgeIter.next();

				//perform the abstraction for the paths leaving the node
				//over this edge
				int keep = abstractPath(edge);
				//at this stage, the status should always be fully determined, not "COMPUTING"
				assert keep==TRUE || keep==FALSE;
				if(keep==TRUE) {
					//found something in callee
					addEdgeToThis(edge);
				}				
			}			
		}
		
		resultCache = null;
	}

	/**
	 * Build the abstraction for a certain path in the graph.
	 * @param currEdge the edge pointing to the current node
	 * @result {@link #TRUE} if currEdge must be retained, {@link #FALSE} if it can safely be dropped
	 * and {@link #COMPUTING} is the result is already being computed further up the call stack
	 */
	protected int abstractPath(Edge currEdge) {		
		MethodOrMethodContext curr = currEdge.getTgt();
		Integer cached = (Integer) resultCache.get(curr);

		if(cached==null) {

			//set status of curr to "COMPUTING" to avoid infinite recursion
			//when processing recursive programs
			resultCache.put(curr, new Integer(COMPUTING));
			
			//is the current node interesting
			boolean currInteresting = nodePredicate.want(curr);

			boolean childrenInteresting = false;
			
			//if children are to be visited
			if(nodePredicate.visitChildren(curr)) {
				//recurse over all outgoing edges
				//no shortcut evaluation allowed here (i.e. no 'break') because
				//the recursion might have to add edges deeper down the call stack 
				for (Iterator<Edge> iter = delegate.edgesOutOf(curr); iter.hasNext();) {
					Edge succEdge = iter.next();
					
					if(abstractPath(succEdge)==TRUE) {
						//found something in callee
						addEdgeToThis(succEdge);
						childrenInteresting = true;
					}
					
				}
			}
			
			//keep this node if it is interesting in itself or if its children are
			boolean keep = currInteresting || childrenInteresting;
			int result = keep ? TRUE : FALSE;
						
			cached = new Integer(result);
			resultCache.put(curr,cached);
		}
		
		return cached.intValue();		
	}

	/**
	 * Copies the edge correctly into "this". 
	 * @param edge
	 */
	protected void addEdgeToThis(Edge edge) {
		this.addEdge(
				new Edge(
						edge.getSrc(),
						edge.srcUnit(),
						edge.getTgt(),
						edge.kind()
				)
		);
	}
}
