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
package abc.da.fsanalysis.flowanalysis.ds;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.jimple.toolkits.pointer.InstanceKey;
import abc.da.fsanalysis.flowanalysis.AnalysisJob;
import abc.da.fsanalysis.flowanalysis.TMFlowAnalysis;
import abc.da.fsanalysis.flowanalysis.WorklistBasedAnalysis;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;

/**
 * An abstract state machine configuration. It holds a mapping from states to
 * Constraints.
 *
 * @author Eric Bodden
 */
public class Configuration implements Cloneable {

	protected final WorklistBasedAnalysis flowAnalysis;
	
	protected final Set<SMNode> states;
	
	protected final SMNode sourceState;
	
	protected Disjunct binding;
	
	public static int counter;
	
	/**
	 * worst case in experiments was:
	 * bloat-FailSafeIter: <EDU.purdue.cs.bloat.cfg.VerifyCFG: void visitBlock(EDU.purdue.cs.bloat.cfg.Block)>: 8828
	 */
	private final static int MAX_CONFIG_COUNT = 15000;	
	
	@SuppressWarnings("serial")
	public static class MaxConfigException extends RuntimeException {
		public MaxConfigException(String msg) {
			super(msg);
		}		
	}
	
	/**
	 * Creates a new configuration holding a mapping for the given states and registering active
	 * shadows with the given analysis.
	 */
	public Configuration(TMFlowAnalysis flowAnalysis, Set<SMNode> states, Disjunct binding, SMNode sourceState) {
		this.flowAnalysis = (WorklistBasedAnalysis) flowAnalysis;
		this.states = Collections.unmodifiableSet(states);
		this.binding = binding;
		this.sourceState = sourceState;
		counter++;
		if(counter==MAX_CONFIG_COUNT) {
			throw new MaxConfigException("Exceeded maximum number of allowed configurations in "+flowAnalysis.getJob().method()+": "+MAX_CONFIG_COUNT);
		}
	}
	
	public Set<Configuration> doTransition(Shadow shadow) {
		AnalysisJob job = flowAnalysis.getJob();
		
		//get the variable binding
		final Map<String, Set<InstanceKey>> bindings = flowAnalysis.getJob().shadowBindings(shadow);		

		Set<Configuration> res = new HashSet<Configuration>();

		//get the current symbol name
		final String symbolName = job.symbolNameForShadow(shadow);
		
		Set<SMNode> targetStates = new HashSet<SMNode>();
		for (SMNode state : states) {
			for (@SuppressWarnings("unchecked")
				Iterator<SMEdge> transIter = state.getOutEdgeIterator(); transIter.hasNext();) {
				SMEdge transition = transIter.next();
				
				//if the labels coincide
				if(transition.getLabel().equals(symbolName) && !transition.isSkipEdge()) {
					targetStates.add(transition.getTarget());
				}
			}
		}

		//add to the result sets these configurations that are propagated along automaton edges
		{
			Disjunct newBinding = binding.addBindingsForSymbol(bindings);			
			if(newBinding!=Disjunct.FALSE) {				
				Configuration newConfig = new Configuration(flowAnalysis,targetStates,newBinding,sourceState);						
				res.add(newConfig);
			}
		}
		

		//if moved out of the source state set, we have to add
		//configurations for this source state set with updated negative bindings
		Set<Disjunct> newBindings = binding.addNegativeBindingsForSymbol(bindings);
		
		for (Disjunct newBinding : newBindings) {
			res.add(new Configuration(flowAnalysis,states,newBinding,sourceState));
		}				

		
		if(res.isEmpty()) {
			throw new InternalError();
		}
		return res;		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		String res = "("+states+","+binding+(sourceState!=null?(",source:"+sourceState.getNumber()):"")+")";		
		return res;
	}
	
    /**
	 * {@inheritDoc}
	 */
	protected Configuration clone() {
		Configuration clone;
		try {
			clone = (Configuration) super.clone();
			clone.binding = (Disjunct) binding.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((binding == null) ? 0 : binding.hashCode());
		result = prime * result + ((states == null) ? 0 : states.hashCode());
		result = prime * result + ((sourceState == null) ? 0 : sourceState.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Configuration other = (Configuration) obj;
		if (binding == null) {
			if (other.binding != null)
				return false;
		} else if (!binding.equals(other.binding))
			return false;
		if (states == null) {
			if (other.states != null)
				return false;
		} else if (!states.equals(other.states))
			return false;
		if (sourceState == null) {
			if (other.sourceState != null)
				return false;
		} else if (!sourceState.equals(other.sourceState))
			return false;
		return true;
	}

	public Set<SMNode> getStates() {
		return states;
	}
	
	public Disjunct getBinding() {
		return binding;
	}	

	protected boolean sameStateNumbers(Configuration other) {
		Set<Integer> stateNumbers = new HashSet<Integer>();
		for (SMNode state : states) {
			stateNumbers.add(state.getNumber());
		}
		
		Set<Integer> otherStateNumbers = new HashSet<Integer>();
		for (SMNode state : other.states) {
			otherStateNumbers.add(state.getNumber());
		}
		
		boolean sameStateNumbers = otherStateNumbers.equals(stateNumbers);
		return sameStateNumbers;
	}
	
	public SMNode getSourceState() {
		if(sourceState==null) {
			throw new IllegalStateException("no source state given");
		}
		return sourceState;
	}
	
	public boolean hasSourceState() {
		return sourceState!=null;
	}
	
	public boolean implies(Configuration other) {		
		return getStates().equals(other.getStates()) && getBinding().implies(other.getBinding());
	}

}
