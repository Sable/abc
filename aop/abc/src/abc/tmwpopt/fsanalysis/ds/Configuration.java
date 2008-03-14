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
package abc.tmwpopt.fsanalysis.ds;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.jimple.toolkits.pointer.InstanceKey;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tmwpopt.fsanalysis.mustalias.TMFlowAnalysis;

/**
 * An abstract state machine configuration. It holds a mapping from states to
 * Constraints.
 *
 * @author Eric Bodden
 */
public class Configuration implements Cloneable {

	/** The mapping from states to constraints. */
	protected HashMap<SMNode,Constraint> stateToConstraint;
	
	protected int numHitFinal;
	
	protected boolean isTainted;
	
	protected final TraceMatch tm;

	protected final TMFlowAnalysis flowAnalysis;
	
	protected final boolean countFinalHits;

	//TODO should actually be part of a constraint, I guess, but that would mean that we
	//need multiple different FALSE constraints
	protected HashSet<Shadow> historyNoVar;   

	/**
	 * Creates a new configuration holding a mapping for the given states and registering active
	 * shadows with the given analysis.
	 */
	public Configuration(TMFlowAnalysis flowAnalysis, Set<State> additionalInitialStates, boolean countFinalHits) {
		this.flowAnalysis = flowAnalysis;
		this.countFinalHits = countFinalHits;
		this.tm = flowAnalysis.getTracematch();
		stateToConstraint = new HashMap<SMNode,Constraint>();
		numHitFinal = 0;
		isTainted = false;
		historyNoVar = new HashSet<Shadow>();

		//associate each initial state with a TRUE constraint and all other states with a FALSE constraint
		Iterator<State> stateIter = tm.getStateMachine().getStateIterator();
		while(stateIter.hasNext()) {
			SMNode state = (SMNode) stateIter.next();
			Constraint constraint;
			if(state.isInitialNode() || additionalInitialStates.contains(state))
				//initial states and such states that we assume as initial always hold TRUE
                constraint = Constraint.TRUE;
			else 
				//default is FALSE
				constraint = Constraint.FALSE; 
			stateToConstraint.put(state, constraint);
		}
	}
	
	/**
	 * Returns the successor configuration of this configuration under edge.
	 * @return the successor configuration under edge
	 */
	@SuppressWarnings("unchecked")
	public Configuration doTransition(Shadow shadow) {
		//the skip-copy has to be initialized as a copy of this configuration
		Configuration skip = (Configuration) clone();
		//the tmp-copy needs to be initialized to false on all states,
		//(we initialize it to true for initial states but that does not matter
		//because they are all the time true anyway)
		Configuration tmp = getCopyResetToInitial();
		
		//get the current symbol name
		final String symbolName = flowAnalysis.getJob().symbolNameForShadow(shadow);
		//and the variable binding
		final Map<String,InstanceKey> bindings = flowAnalysis.reMap(shadow.getAdviceFormalToSootLocal());
		//all variables of the state machine
		final TMStateMachine sm = (TMStateMachine) tm.getStateMachine();
		final Collection<String> allVariables =
			Collections.unmodifiableCollection(tm.getVariableOrder(symbolName));

		
		//for all transitions in the state machine
		for (Iterator<SMEdge> transIter = sm.getEdgeIterator(); transIter.hasNext();) {
			SMEdge transition = transIter.next();
			
			//if the labels coincide
			if(transition.getLabel().equals(symbolName)) {
				
				if(transition.isSkipEdge()) {
					//if we have a skip transition
					assert transition.getSource()==transition.getTarget(); //must be a loop
					
					//get the state of this skip loop
					SMNode skipState = transition.getTarget();
					assert !skipState.isFinalNode(); 		   //only nonfinal nodes should have skip edges
					assert getStates().contains(skipState);    //assert consistency
					
					//get the old constraint at the state
					Constraint oldConstraint = skip.getConstraintFor(skipState);
					
					//add negative bindings
					Constraint newConstraint = oldConstraint.addNegativeBindingsForSymbol(
							allVariables,
							skipState,
							bindings,
							shadow,
							this
					);
					
					/*
					 * If a symbol has no variable binding, its shadow ID will not be stored
					 * within the changed disjunct. This is because in disjuncts shadow IDs are stored
					 * with the bindings. Hence, in order to be sound, we store the shadow ID here, if the
					 * transition changed the constraint. It would actually make more sense to store 
					 * them on the constraint itself but that would mean that we would need to allow for multiple
					 * different FALSE constraints.
					 */
					if(bindings.isEmpty() && !oldConstraint.equals(newConstraint)) {
						tmp.historyNoVar.add(shadow);						
					}
					
					//store the result at the original (=target) state
					skip.stateToConstraint.put(skipState, newConstraint);
				} else {
					//a "normal" transition					
					
					//get constraint at source state
					Constraint oldConstraint = getConstraintFor(transition.getSource());
					
					//add bindings
					Constraint newConstraint = oldConstraint.addBindingsForSymbol(
							allVariables, 
							transition.getSource(),
							transition.getTarget(), 
							bindings, 
							shadow
					); 
					
					if(transition.getTarget().isFinalNode() && !newConstraint.equals(Constraint.FALSE)) {
						flowAnalysis.hitFinal();
						if(countFinalHits)
							tmp.numHitFinal++;
					}
	
					/*
					 * If a symbol has no variable binding, its shadow ID will not be stored
					 * within the changed disjunct. This is because in disjuncts shadow IDs are stored
					 * with the bindings. Hence, in order to be sound, we store the shadow ID here, if the
					 * transition changed the constraint. It would actually make more sense to store 
					 * them on the constraint itself but that would mean that we would need to allow for multiple
					 * different FALSE constraints.
					 */
					if(bindings.isEmpty() && !oldConstraint.equals(newConstraint)) {
						tmp.historyNoVar.add(shadow);						
					}
	
					//put the new constraint on the target state
					//via a disjoint update
					tmp.disjointUpdateFor(transition.getTarget(), newConstraint);
				}
			}
		}

		//disjointly merge the constraints of tmp and skip
		tmp = tmp.getJoinWith(skip);
		
        //filter unnecessary negative bindings by applying the distributive law
		tmp = tmp.applyDistributiveLaw();
		
		//try to reduce to TRUE
		tmp = tmp.tryReduceToTrue();

		//reduce constraints TRUE || D to TRUE
		tmp.optimizeStatesWithTrue();

		return tmp;
	}	
	
    /**
     * Returns <code>true</code> if the disjunct bearing the given bindings must be in an initial state, i.e.
     * if one of its instance keys must-alias an instance key in valuesInInitialState. 
     */
    protected boolean mustBeInInitial(Map<String, InstanceKey> bindings, Set<InstanceKey> valuesInInitialState) {
    	for (InstanceKey instanceKey : bindings.values()) {
    		if(valuesInInitialState.contains(instanceKey)) {
    			return true;
    		}			
		}
		return false;
	}

	/**
	 * Merges the constraint disjointly with the one currently associated with the state,
	 * updating this constraint of state.
	 * @param state any state in {@link #getStates()}
	 * @param constraint the constraint to merge
	 */
	public void disjointUpdateFor(SMNode state, Constraint constraint) {
		assert getStates().contains(state);		
		Constraint currConstraint = (Constraint) stateToConstraint.get(state);		
		stateToConstraint.put(state, currConstraint.or(constraint));
	}

	/**
	 * Joins this configuration with the other one and returns the result.
	 * This implies a disjoint update of all associated constraints and a merge
	 * of the associated thread edges. 
	 * The sets {@link #historyNoVar} are joined.
	 * @param other another configuration 
	 * @return the joined configuration
	 */
	public Configuration getJoinWith(Configuration other) {
		assert other.getStates().equals(getStates());
		
		Configuration clone = (Configuration) clone();
		for (Iterator<SMNode> stateIter = getStates().iterator(); stateIter.hasNext();) {
			SMNode state = stateIter.next();
			clone.disjointUpdateFor(state, other.getConstraintFor(state));
		}
		clone.historyNoVar.addAll(other.historyNoVar);
		return clone;
	}
	
    /**
     * Calls {@link Constraint#applyDistributiveLaw()} on all constraints and returns the result.
     */
	public Configuration applyDistributiveLaw() {
        Configuration clone = (Configuration) clone();
		for (Iterator<SMNode> stateIter = getStates().iterator(); stateIter.hasNext();) {
            SMNode state = (SMNode) stateIter.next();
            clone.stateToConstraint.put(state, stateToConstraint.get(state).applyDistributiveLaw());
        }
        return clone;
    }
    
    /**
     * Calls {@link Constraint#tryReduceToTrue()} on all constraints and returns the result.
     */
	public Configuration tryReduceToTrue() {
        Configuration clone = (Configuration) clone();
		for (Iterator<SMNode> stateIter = getStates().iterator(); stateIter.hasNext();) {
            SMNode state = (SMNode) stateIter.next();
            clone.stateToConstraint.put(state, stateToConstraint.get(state).tryReduceToTrue());
        }
        return clone;
    }
    
    /**
     * If a state holds {@link Disjunct#FALSE}, set it to {@link Constraint#TRUE}, as this is equivalent.
     */
	public void optimizeStatesWithTrue() {
		for (Iterator<SMNode> stateIter = getStates().iterator(); stateIter.hasNext();) {
            SMNode state = (SMNode) stateIter.next();
            Constraint constraint = stateToConstraint.get(state);
            boolean isTrue = false;
            for (Disjunct<?> d : constraint.disjuncts) {
                if(d.equals(Disjunct.FALSE)) {
                    isTrue = true;
                    break;
                }
            }
            if(isTrue) {
                stateToConstraint.put(state, Constraint.TRUE);
            }
        }
    }

	/**
	 * Returns a copy of this configuration but with all constraints reset
	 * to the ones of the initial configuration.
	 * @return a configuration where each state <i>s</i> is mapped to <code>{@link Constraint#TRUE}</code>
	 * if it is initial and {@link Constraint#FALSE} otherwise.
	 */
	public Configuration getCopyResetToInitial() {
		Configuration copy = (Configuration) clone();
		for (Iterator<Map.Entry<SMNode,Constraint>> iter = copy.stateToConstraint.entrySet().iterator(); iter.hasNext();) {
			Map.Entry<SMNode,Constraint> entry = iter.next();
			SMNode state = entry.getKey();			
			entry.setValue(state.isInitialNode() ? Constraint.TRUE : Constraint.FALSE);
		}		
		return copy;
	}
	
	/**
	 * Returns the state set of this configuration.
	 * @return
	 */
	public Set<SMNode> getStates() {
		return new HashSet<SMNode>(stateToConstraint.keySet()); 
	}
	
	/**
	 * Returns the constraint currently associated with the state. 
	 * @param state any state from {@link #getStates()}
	 * @return the constraint currently associated with this state
	 */
	public Constraint getConstraintFor(SMNode state) {
		assert getStates().contains(state);
		return (Constraint) stateToConstraint.get(state);
	}
	
	/**
	 * @return the number of disjuncts in this configuration
	 */
	public int size() {
		int res = 0;
		for (Iterator<Constraint> constIter = stateToConstraint.values().iterator(); constIter.hasNext();) {
			Constraint constr = constIter.next();
			res += constr.size();
		}
		return res;
	}
	
	/**
	 * Creates a new constraint representing the <i>maximally worst assumption</i> that we
	 * are in all states but the final state, i.e. all constraints but the ones for final states
	 * are set to {@link Constraint#TRUE} (the ones for final states are set to {@link Constraint#FALSE}).
	 */
	public Configuration getMaximalAssumption() {
		Configuration clone = clone();
		for (Entry<SMNode,Constraint> entry : clone.stateToConstraint.entrySet()) {
			State state = entry.getKey();
			entry.setValue(state.isFinalNode() ? Constraint.FALSE : Constraint.TRUE);
		}		
		return clone;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		SMNode[] sorted = new SMNode[stateToConstraint.size()];
		//sort all states
		for (Iterator<SMNode> stateIter = stateToConstraint.keySet().iterator(); stateIter.hasNext();) {
			SMNode state = stateIter.next();
			sorted[state.getNumber()] = state;
		}

		String res = "[\n";
		for (int i = 0; i < sorted.length; i++) {
			SMNode state = sorted[i];
			res += "\t" + state.getNumber() + " -> " + stateToConstraint.get(state) + "\n";			
		}
		res += "]\n";
		
		if(countFinalHits)
			res += "hit final "+numHitFinal+" times\n";
        if(isTainted)
            res += "configuration is tainted\n";
		
		return res;
	}
	
	public Configuration taint() {
		Configuration clone = clone(); 
	    clone.isTainted = true;
	    return clone;
	}
	
    public boolean isTainted() {
        return isTainted;
    }
    
    public static boolean hasTainted(Collection<Configuration> configurations) {
	    for (Configuration configuration : configurations) {
            if(configuration.isTainted) {
                return true;
            }
        }
	    return false;
	}
	
    public static boolean hasHitFinal(Collection<Configuration> configurations) {
        for (Configuration configuration : configurations) {
            if(configuration.hasHitFinal()) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasHitFinal() {
    	for (State state : stateToConstraint.keySet()) {
			if(state.isFinalNode()) {
				Constraint c = stateToConstraint.get(state);
				if(!c.equals(Constraint.FALSE)) {
					return true;
				}
			}
		}
    	return false;
    }
    
    /**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	protected Configuration clone() {
		Configuration clone;
		try {
			clone = (Configuration) super.clone();
			clone.stateToConstraint = (HashMap<SMNode,Constraint>) stateToConstraint.clone();
			clone.historyNoVar = (HashSet<Shadow>) historyNoVar.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((stateToConstraint == null) ? 0 : stateToConstraint
						.hashCode());
		result = prime
				* result
				+ ((historyNoVar == null) ? 0 : historyNoVar
						.hashCode());
		result = prime * result + numHitFinal;
        result = prime * result + (isTainted ? 1 : 0);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Configuration other = (Configuration) obj;
		if (stateToConstraint == null) {
			if (other.stateToConstraint != null)
				return false;
		} else if (!stateToConstraint.equals(other.stateToConstraint))
			return false;
		if(numHitFinal!=other.numHitFinal) {
			return false;
		}
        if(isTainted!=other.isTainted) {
            return false;
        }
		if (historyNoVar == null) {
			if (other.historyNoVar != null)
				return false;
		} else if (!historyNoVar.equals(other.historyNoVar))
			return false;
		assert this.tm.equals(other.tm);
		return true;
	}
	
	/**
	 * Returns the shadow history at all states of this configurations.
	 */
	public Collection<Shadow> getHistoryAtAllStates() {
		return getHistoryAtStates(false);
	}

	/**
	 * Returns the shadow history at final states of this configurations.
	 */
	public Collection<Shadow> getHistoryAtFinalStates() {
		return getHistoryAtStates(true);
	}

	/**
	 * Returns the shadow history at state <i>s</i>.
	 */
	public Collection<Shadow> getHistoryAtState(State s) {
		return stateToConstraint.get(s).getCurrentHistory();
	}

	protected Collection<Shadow> getHistoryAtStates(boolean onlyAtFinalStates) {
		Collection<Shadow> res = new HashSet<Shadow>();
		for (State s : stateToConstraint.keySet()) {
			if(onlyAtFinalStates && !s.isFinalNode()) continue;
			Constraint c = stateToConstraint.get(s);
			res.addAll(c.getCurrentHistory());
		}
		res.addAll(historyNoVar);
		return res;
	}

}
