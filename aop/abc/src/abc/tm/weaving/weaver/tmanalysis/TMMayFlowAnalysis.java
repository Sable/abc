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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import abc.tm.ast.SymbolDecl_c;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.TMStateMachine;

/**
 * This analysis computes a "may control flow" analysis for tracematches.
 * The bit that is important here to understand is the form of the directed graph that we analyze
 * and the analysis information we use. Opposed to standard flow analyses where we have an in-set and
 * an out-set for each <i>node</i> in a directed graph, we want to say something about <i>edges</i> here.
 * Hence, we model edges as nodes and nodes as edges. This is done by the adapter
 * {@link TMStateMachineAsGraph}, which does this conversion on-the-fly.
 * Our analysis information in the fixed point is for each edge the set of possible
 * tracematch transition that <i>could possibly be taken</i> at runtime when triggering the symbol
 * that is associated with this edge.<br>
 * Hence, the out-set of an edge p -s-> q holds all transitions p' -s-> q' for which
 * a transition n' -t-> p' is already in the in-set for some symbol t'.<br>
 * Unused edges can then be accessed via {@link #unusedEdgeIterator()}.
 * @author Eric Bodden
 */
public class TMMayFlowAnalysis extends ForwardFlowAnalysis {

	/**
	 * The state machine to interpret.
	 */
	protected final TMStateMachine stateMachine;	
	
	/**
	 * The program graph to interpret over.
	 */
	protected final TMStateMachine programGraph;

	/**
	 * A list of artificial entry edges. 
	 */
	protected final FlowSet ENTRY_EDGES;
	
	/**
	 * The tracematch id. Used to identify the correct labels in the global analysis information.
	 */
	protected final String tracematchID;	
	
	/**
	 * Maps skip loops to symbols. In the fixed point, a mapping <i>(l,{s})</i> is in
	 * the map if when reading <i>s</i> at any time in the program, this may cause
	 * <i>l</i> to be triggered.
	 */
	protected final Map /*<SMEdge> -> Set<<String>>*/ mayBeCausedBySymbol;
	
	/**
	 * Constructs a new analysis, which is run immediately.
	 * @param tm a tracematch to interpret
	 * @param programGraph the program abstraction
	 */
	public TMMayFlowAnalysis(TraceMatch tm, TMStateMachine programGraph) {
		super(new TMStateMachineAsGraph(programGraph));
		this.stateMachine = (TMStateMachine) tm.getStateMachine();
		this.tracematchID = tm.getName();
		this.programGraph = programGraph;
		this.mayBeCausedBySymbol = new HashMap();
		
		//generate entry edges to all initial states of the state machine;
		//those represent the initial flow information for the entry points;
		//the edges should not (!) become part of the state machine
		FlowSet entryEdges = new ArraySparseSet();
		for (Iterator iter = stateMachine.getStateIterator(); iter.hasNext();) {
			SMNode state = (SMNode) iter.next();
			if(state.isInitialNode()) {
				entryEdges.add(new SMEdge(null,state,null));
			}
		}
		ENTRY_EDGES = entryEdges;
				
		//do the analysis
		doAnalysis();
	}

	/** 
	 * Computes the transformation of analysis information when flowing through an edge.
	 * The out-set of an edge p -s-> q holds all transitions p' -s-> q' for which
	 * a transition n' -t-> p' is in the in-set for some symbol t'.
	 * @param in the in-set
	 * @param d the current edge
	 * @param out the out-set
	 */
	protected void flowThrough(Object in, Object d, Object out) {
		FlowSet cin = (FlowSet) in;
		SMEdge edge = (SMEdge) d;
		FlowSet cout = (FlowSet) out;		
		cout.clear();

		//if we have an epsilon transition, nothing changes
		if(edge.getLabel()==UGStateMachine.EPSILON) {
			copy(in, out);
		} else {
			//for each automaton-transition p --> q in the in-set
			for (Iterator iter = cin.iterator(); iter.hasNext();) {
				SMEdge transition = (SMEdge) iter.next();
				SMNode state = transition.getTarget();
				
				SMEdge currSkipLoop = null;				
				//for each automaton-transition q --> r
				for (Iterator iterator = state.getOutEdgeIterator(); iterator.hasNext();) {
					SMEdge outTransition = (SMEdge) iterator.next();
					
					//get the fuly qualified label for this transition
					String transitionLabel = SymbolDecl_c.uniqueSymbolID(tracematchID, outTransition.getLabel());
					
					//check whether the two strings are properly interned
					//so that String comparison runs faster
					assert transitionLabel == transitionLabel.intern();
					assert edge.getLabel()== null || edge.getLabel() == edge.getLabel().intern();
					
					//if it is the same as for the current edge, this means, we can take this
					//transition; hence add it to the out-set; all other transitions cannot be taken
					if(transitionLabel.equals(edge.getLabel())) {
						cout.add(outTransition);
					} else if(outTransition.isSkipEdge()) {
						//assert that we see only one outgoing skip loop for this state
						assert currSkipLoop == null;
						currSkipLoop = outTransition;
					}
				}				
				if(cout.isEmpty() && currSkipLoop!=null) {
					//if we cannot take any "labeled" edge in the trace match automaton,
					//this means we take the skip loop
					cout.add(currSkipLoop);
					
					//memorize that this skip loop was triggered by reading that symbol
					
					//TODO this all would be unnecessary if just SKIP loops could store
					//the label that they skip; we should do that at some point!

					//get the currently known set of symbols that may trigger this loop
					Set symbolsTriggeringThisLoop = (Set) mayBeCausedBySymbol.get(currSkipLoop);
					//initialize if necessary
					if(symbolsTriggeringThisLoop==null) {
						symbolsTriggeringThisLoop = new HashSet();
						mayBeCausedBySymbol.put(currSkipLoop, symbolsTriggeringThisLoop);
					}
					//add the mapping
					symbolsTriggeringThisLoop.add(edge.getLabel());
				}
				
			}
			
			//always add all entry edges to model the implicit sigma-star loop
			//at the initial state of the tracematch
			cout.union(ENTRY_EDGES);
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	protected void copy(Object source, Object dest) {
		FlowSet src = (FlowSet) source;		
		src.copy((FlowSet) dest);
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
		FlowSet cin1 = (FlowSet) in1;
		FlowSet cin2 = (FlowSet) in2;
		FlowSet cout = (FlowSet) out;
		
		cin1.copy(cout);
		cout.union(cin2);
	}

	/** 
	 * {@inheritDoc}
	 */
	protected Object newInitialFlow() {
		return new ArraySparseSet();
	}
	
	/**
	 * Returns all edges, which are certainly not used in the tracematch, i.e. for which
	 * the analysis computed that they can never be executed during runtime.
	 * @return an iterator over all unused tracematch edges
	 */
	public Iterator unusedEdgeIterator() {
		Collection usedEdges = new HashSet();
		
		//collect all usedEdges first
		for (Iterator iter = programGraph.getEdgeIterator(); iter.hasNext();) {
			SMEdge edge = (SMEdge) iter.next();
			usedEdges.addAll(((FlowSet) getFlowAfter(edge)).toList());
		}
		
		//get all edges
		Collection allEdges = new HashSet();
		for (Iterator iter = stateMachine.getEdgeIterator(); iter.hasNext();) {
			allEdges.add(iter.next());
		}
		
		//remove the used ones, retaining the unused ones
		Collection unusedEdges = allEdges;
		unusedEdges.removeAll(usedEdges);
		
		return Collections.unmodifiableCollection(unusedEdges).iterator();
	}
	
	/**
	 * Returns the set of all symbols which may trigger a skip loop
	 * during runtime. 
	 * @return the set of string labels for symbols which could cause a skip loop to be triggered 
	 */
	public Set/*<String>*/ symbolsWhichMayTriggerSkipLoops() {
		Set result = new HashSet();
		
		for (Iterator iter = mayBeCausedBySymbol.values().iterator(); iter.hasNext();) {
			Set currSet = (Set) iter.next();
			result.addAll(currSet);
		}
		
		return result;
	}
	
	/**
	 * Returns <code>true</code> if the given symbol may trigger the given skip loop.
	 * @param symbol a symbol of the tracematch of the associated state machine
	 * @param skipLoop a skip loop of the associated state machine 
	 * @return
	 */
	public boolean mayThisSymbolTriggerThisSkipLoop(String symbol, SMEdge skipLoop) {
		assert skipLoop.isSkipEdge();
		assert symbol != UGStateMachine.EPSILON && symbol != SMEdge.SKIP_LABEL;
		
		Set symbolsWhichMayTriggerThisLoop = (Set) mayBeCausedBySymbol.get(skipLoop);
		if(symbolsWhichMayTriggerThisLoop!=null) {
			return symbolsWhichMayTriggerThisLoop.contains(symbol);
		}
		return false;
	}
	
}
