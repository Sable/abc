/* abc - The AspectBench Compiler
 * Copyright (C) 2009 Eric Bodden
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
 
package abc.da.weaving.aspectinfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.SootClass;
import soot.SootMethod;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.SimpleStateMachine;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.StateMachine;
import abc.tm.weaving.matching.TMStateMachine;
import abc.weaving.aspectinfo.Aspect;

public class InvertedTracePattern implements TracePattern {
	
	protected TracePattern delegate;
	
	protected SimpleStateMachine invertedStateMachine;

	public InvertedTracePattern(TracePattern toBeInverted) {
		this.delegate = toBeInverted;
		
		StateMachine sm = delegate.getStateMachine();
		invertedStateMachine = new TMStateMachine();
		Map<SMNode,SMNode> stateToNewState = new HashMap<SMNode, SMNode>();
		for(Iterator<State> stateIterator = sm.getStateIterator();stateIterator.hasNext();) {
			SMNode state = (SMNode) stateIterator.next();
			SMNode newState = (SMNode) invertedStateMachine.newState();
			stateToNewState.put(state, newState);
		}
		
		for (SMNode state : stateToNewState.keySet()) {
			if(state.isInitialNode()) {
				SMNode newState = stateToNewState.get(state);
				newState.setFinal(true);
			}
			if(state.isFinalNode()) {
				SMNode newState = stateToNewState.get(state);
				newState.setInitial(true);
			}
		}
		
		for(Iterator<SMEdge> edgeIter = sm.getEdgeIterator();edgeIter.hasNext();) {
			SMEdge edge = edgeIter.next();
			SMNode newSource = stateToNewState.get(edge.getTarget());
			SMNode newTarget = stateToNewState.get(edge.getSource());
			if(edge.isSkipEdge()) {
				assert newSource == newTarget;
				invertedStateMachine.newSkipLoop(newSource, edge.getLabel());
			} else {
				invertedStateMachine.newTransition(newSource, newTarget, edge.getLabel());
			}
			
		}

		invertedStateMachine.renumberStates();
				
        Map<String, Collection<String>> symToVars = new HashMap<String, Collection<String>>();
        for(String sym: getSymbols()) {
        	symToVars.put(sym,getVariableOrder(sym));
        }
        invertedStateMachine.initBoundVars(getFormals());
		invertedStateMachine.fixBoundVars(symToVars);
	}
	
	public Collection<String> getFormals() {
		return delegate.getFormals();
	}

	public Aspect getContainer() {
		return delegate.getContainer();
	}

	public SootClass getContainerClass() {
		return delegate.getContainerClass();
	}

	public Set<String> getFinalSymbols() {
		//invert
		return delegate.getInitialSymbols();
	}

	public Set<String> getInitialSymbols() {
		//invert
		return delegate.getFinalSymbols();
	}

	public String getName() {
		return delegate.getName();
	}

	public SimpleStateMachine getStateMachine() {
		return invertedStateMachine;
	}

	public SootMethod getSymbolAdviceMethod(String symbol) {
		return delegate.getSymbolAdviceMethod(symbol);
	}

	public Set<String> getSymbols() {
		return delegate.getSymbols();
	}

	public List<String> getVariableOrder(String symbol) {
		return delegate.getVariableOrder(symbol);
	}

}
