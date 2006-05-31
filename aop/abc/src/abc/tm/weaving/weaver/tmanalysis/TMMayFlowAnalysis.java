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
package abc.tm.weaving.weaver.tmanalysis;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import soot.toolkits.scalar.ForwardFlowAnalysis;
import abc.tm.ast.SymbolDecl_c;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.TMStateMachine;

/**
 * @author Eric Bodden
 */
public class TMMayFlowAnalysis extends ForwardFlowAnalysis {

	protected final TMStateMachine stateMachine;
	protected final Collection ENTRY_EDGES;
	protected final TMStateMachine programGraph;
	protected final String tracematchID;
	

	/**
	 * @param programGraph
	 */
	public TMMayFlowAnalysis(TraceMatch tm, TMStateMachine programGraph) {
		super(new TMStateMachineAsGraph(programGraph));
		this.stateMachine = (TMStateMachine) tm.getStateMachine();
		this.tracematchID = tm.getName();
		this.programGraph = programGraph;
		//
		Collection entryEdges = new HashSet();
		for (Iterator iter = stateMachine.getStateIterator(); iter.hasNext();) {
			SMNode state = (SMNode) iter.next();
			if(state.isInitialNode()) {
				entryEdges.add(new SMEdge(null,state,null));
			}
		}
		ENTRY_EDGES = Collections.unmodifiableCollection(entryEdges);
				
		//do the analysis
		doAnalysis();
	}

	/** 
	 * {@inheritDoc}
	 */
	protected void flowThrough(Object in, Object d, Object out) {
		Collection cin = (Collection) in;
		SMEdge edge = (SMEdge) d;
		Collection cout = (Collection) out;
		
		cout.clear();

		if(edge.getLabel()==UGStateMachine.EPSILON) {
			copy(in, out);
		} else {

			for (Iterator iter = cin.iterator(); iter.hasNext();) {
				SMEdge transition = (SMEdge) iter.next();
				SMNode state = transition.getTarget();
				
				for (Iterator iterator = state.getOutEdgeIterator(); iterator.hasNext();) {
					SMEdge outTransition = (SMEdge) iterator.next();
					
					String transitionLabel = SymbolDecl_c.uniqueSymbolID(tracematchID, outTransition.getLabel());
					
					//check whether the two strings are properly interned
					//so that we can quickly compare them by reference comparison
					assert transitionLabel == transitionLabel.intern();
					assert edge.getLabel()== null || edge.getLabel() == edge.getLabel().intern();
					
					if(transitionLabel==edge.getLabel()) {
						cout.add(outTransition);
					}
				}
				
			}
			//always add all entry edges to model the implicit sigma-star loop at the initial state of the tracematch
			cout.addAll(ENTRY_EDGES);
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	protected void copy(Object source, Object dest) {
		Collection res = (Collection) dest;		
		res.clear();
		res.addAll((Collection) source);
	}

	/** 
	 * {@inheritDoc}
	 */
	protected Object entryInitialFlow() {
		return ENTRY_EDGES;
	}

	/** 
	 * {@inheritDoc}
	 */
	protected void merge(Object in1, Object in2, Object out) {
		Collection cin1 = (Collection) in1;
		Collection cin2 = (Collection) in2;
		Collection cout = (Collection) out;
		
		//cout.clear();
		cout.addAll(cin1);
		cout.addAll(cin2);
	}

	/** 
	 * {@inheritDoc}
	 */
	protected Object newInitialFlow() {
		return new HashSet();
	}
	
	public Iterator unusedEdgeIterator() {
		Collection usedEdges = new HashSet();
		
		//collect all usedEdges first
		for (Iterator iter = programGraph.getEdgeIterator(); iter.hasNext();) {
			SMEdge edge = (SMEdge) iter.next();
			usedEdges.addAll((Collection) getFlowAfter(edge));
		}
		
		Collection allEdges = new HashSet();
		for (Iterator iter = stateMachine.getEdgeIterator(); iter.hasNext();) {
			allEdges.add(iter.next());
		}
		
		Collection unusedEdges = allEdges;
		unusedEdges.removeAll(usedEdges);
		
		//remove skip edges for now
		for (Iterator iter = unusedEdges.iterator(); iter.hasNext();) {
			SMEdge edge = (SMEdge) iter.next();
			if(edge.isSkipEdge()) {
				iter.remove();
			}
		}

		return Collections.unmodifiableCollection(unusedEdges).iterator();
	}
}
