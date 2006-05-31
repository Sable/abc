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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import polyglot.util.Position;

import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.TMStateMachine;

import soot.toolkits.graph.DirectedGraph;

/**
 * This is an adaptor that shows a tracematch state machine as directed graph,
 * so that it can be used in Soot's flow analysis framework.
 * <i>Note that the <b>edges</b> of the state machine are interpreted as
 * <b>nodes</b> in the directed graph and vice versa!</i> 
 * @author Eric Bodden
 */
public class TMStateMachineAsGraph implements DirectedGraph {

	protected TMStateMachine delegate;
	protected final List HEADS, TAILS, ALL;
	
	/**
	 * Constructs a new adapter.
	 * @param delegate the state machine to adapt
	 */
	public TMStateMachineAsGraph(TMStateMachine delegate) {
		this.delegate = delegate;
		//construct a list of heads
		List heads = new ArrayList();
		for (Iterator iter = delegate.getStateIterator(); iter.hasNext();) {
			SMNode state = (SMNode) iter.next();
			if(state.isInitialNode()) {
				SMEdge startEdge = new SMEdge(null,state,null);
				state.addIncomingEdge(startEdge);
				heads.add(startEdge);
			}
		}
		HEADS = Collections.unmodifiableList(heads);
		//constuct a list of tails
		List tails = new ArrayList();
		for (Iterator iter = delegate.getStateIterator(); iter.hasNext();) {
			SMNode state = (SMNode) iter.next();
			if(state.isFinalNode()) {
				SMEdge endEdge = new SMEdge(state,null,null);
				state.addOutgoingEdge(endEdge);
				tails.add(endEdge);
			}
		}		
		TAILS = Collections.unmodifiableList(tails);
		//construct a reference to all edges
		List all = new ArrayList();
		all.addAll(getHeads());
		all.addAll(getTails());
		for (Iterator iter = delegate.getEdgeIterator(); iter.hasNext();) {
			all.add(iter.next());
		}
		ALL = Collections.unmodifiableList(all);
	}

	//the following methods implement the adaptor
	
	/** 
	 * {@inheritDoc}
	 */
	public List getHeads() {
		return HEADS;
	}

	/** 
	 * {@inheritDoc}
	 */
	public List getPredsOf(Object s) {		
		SMEdge edge = (SMEdge) s;
		List preds = new ArrayList();
		if(edge.getSource()!=null) {
			for (Iterator iter = edge.getSource().getInEdgeIterator(); iter.hasNext();) {
				preds.add(iter.next());
			}
		}
		return preds;
	}

	/** 
	 * {@inheritDoc}
	 */
	public List getSuccsOf(Object s) {
		SMEdge edge = (SMEdge) s;
		List succs = new ArrayList();
		if(edge.getTarget()!=null) {
			for (Iterator iter = edge.getTarget().getOutEdgeIterator(); iter.hasNext();) {
				succs.add(iter.next());
			}
		}
		return succs;
	}

	/** 
	 * {@inheritDoc}
	 */
	public List getTails() {
		return TAILS;
	}

	/** 
	 * {@inheritDoc}
	 */
	public Iterator iterator() {
		return ALL.iterator();
	}

	/** 
	 * {@inheritDoc}
	 */
	public int size() {
		int size = 0;
		for (Iterator iter = iterator(); iter.hasNext();iter.next()) {			
			size++;
		}
		return size;
	}

	//only delegate methods follow
	
	/**
	 * 
	 * @see abc.tm.weaving.matching.TMStateMachine#cleanup()
	 */
	public void cleanup() {
		delegate.cleanup();
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	/**
	 * @return
	 * @see abc.tm.weaving.matching.TMStateMachine#getNumberOfStates()
	 */
	public int getNumberOfStates() {
		return delegate.getNumberOfStates();
	}

	/**
	 * @param n
	 * @return
	 * @see abc.tm.weaving.matching.TMStateMachine#getStateByNumber(int)
	 */
	public SMNode getStateByNumber(int n) {
		return delegate.getStateByNumber(n);
	}

	/**
	 * @return
	 * @see abc.tm.weaving.matching.TMStateMachine#getStateIterator()
	 */
	public Iterator getStateIterator() {
		return delegate.getStateIterator();
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return delegate.hashCode();
	}

	/**
	 * @param source
	 * @param toInsert
	 * @param target
	 * @see abc.tm.weaving.matching.TMStateMachine#insertStateMachine(abc.tm.weaving.matching.State, abc.tm.weaving.matching.TMStateMachine, abc.tm.weaving.matching.State)
	 */
	public void insertStateMachine(State source, TMStateMachine toInsert, State target) {
		delegate.insertStateMachine(source, toInsert, target);
	}

	/**
	 * @return
	 * @see abc.tm.weaving.matching.TMStateMachine#newState()
	 */
	public State newState() {
		return delegate.newState();
	}

	/**
	 * @param from
	 * @param to
	 * @param s
	 * @see abc.tm.weaving.matching.TMStateMachine#newTransition(abc.tm.weaving.matching.State, abc.tm.weaving.matching.State, java.lang.String)
	 */
	public void newTransition(State from, State to, String s) {
		delegate.newTransition(from, to, s);
	}

	/**
	 * @param from
	 * @param to
	 * @param s
	 * @param toClone
	 * @see abc.tm.weaving.matching.TMStateMachine#newTransitionFromClone(abc.tm.weaving.matching.State, abc.tm.weaving.matching.State, java.lang.String, abc.tm.weaving.matching.SMEdge)
	 */
	public void newTransitionFromClone(State from, State to, String s, SMEdge toClone) {
		delegate.newTransitionFromClone(from, to, s, toClone);
	}

	/**
	 * @param tm
	 * @param formals
	 * @param notused
	 * @param pos
	 * @see abc.tm.weaving.matching.TMStateMachine#prepareForMatching(abc.tm.weaving.aspectinfo.TraceMatch, java.util.List, java.util.Collection, polyglot.util.Position)
	 */
	public void prepareForMatching(TraceMatch tm, List formals, Collection notused, Position pos) {
		delegate.prepareForMatching(tm, formals, notused, pos);
	}

	/**
	 * 
	 * @see abc.tm.weaving.matching.TMStateMachine#renumberStates()
	 */
	public void renumberStates() {
		delegate.renumberStates();
	}

	/**
	 * @return
	 * @see abc.tm.weaving.matching.TMStateMachine#toString()
	 */
	public String toString() {
		return delegate.toString();
	}

}
