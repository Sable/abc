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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.PointsToSet;
import soot.jimple.toolkits.pointer.InstanceKey;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.tm.weaving.matching.SMNode;
import abc.tmwpopt.fsanalysis.mustalias.TMFlowAnalysis;

/**
 * Implements a single constraint. A constraint is normally associated with a state
 * and represents under which constraint a run may be in this state.
 * The easiest constraints are {@link #TRUE} and {@link #FALSE}, which mean that we
 * simply are in the state (or not).
 * 
 * We store constraints in disjunctive normal form (DNF), which means each constraint
 * is a set of disjuncts. So a constraint of the form {{v=a,w=b},{v=c}} means that
 * either the variable v points to creation side a and w to b or v points to c (and w
 * is unbound).
 * 
 * Constraints are produced using the prototype pattern, i.e. via cloning. The two prototypes are
 * {@link #TRUE} and {@link #FALSE}. Other Constraints can then be created by calling
 * {@link #addBindingsForSymbol(Collection, SMNode, Map, String, TMFlowAnalysis)} and  
 * {@link #addNegativeBindingsForSymbol(Collection, SMNode, Map, String, TMFlowAnalysis)}.
 *
 * @author Eric Bodden
 */
public class Constraint implements Cloneable {
	
	/** The set of disjuncts for this constraint (DNF). */
	protected HashSet<Disjunct<InstanceKey>> disjuncts;
	
	/** The unique false constraint. */
	public static Constraint FALSE;
	
	/** The unique true constraint. */
	public static Constraint TRUE;
	
	/**
	 * Initialized the prototype constraints TRUE and FALSE. 
	 * @param falseProtoType the prototype disjunct for {@link Disjunct#FALSE}.
	 */
	public static void initialize(Disjunct falseProtoType) {
		//initialize FALSE
		FALSE = new Constraint(new HashSet()) {			
			
			/**
			 * Returns this (FALSE).
			 */
			@Override
			public Constraint addBindingsForSymbol(Collection allVariables, SMNode from, SMNode to, Map bindings, Shadow shadow) {
				//FALSE stays FALSE
				return this;
			}
			
			/**
			 * Returns this (FALSE).
			 */
			@Override
			public Constraint addNegativeBindingsForSymbol(Collection allVariables, SMNode state, Map bindings, Shadow shadow, Configuration config) {
				//FALSE stays FALSE
				return this;
			}
			
			@Override
			protected Constraint clone() {
				return new Constraint(disjuncts);
			}
		};

		//initialize TRUE; this holds a single empty disjunct
		HashSet set = new HashSet();
		set.add(falseProtoType);		
		TRUE = new Constraint(set) {
			
			@Override
			protected Constraint clone() {
				return new Constraint(disjuncts);
			}
			
			@Override
			public Constraint or(Constraint other) {
				//TRUE || other == TRUE
				return this;
			}
		};
		
		Disjunct.FALSE = falseProtoType;
	}
	
	/**
	 * Constructs a new constraint holding a reference to the given disjuncts. 
	 * Only to be called from inside this class (prototype pattern). Other disjuncts can be created
	 * via {@link #addBindingsForSymbol(Collection, SMNode, Map, String, TMFlowAnalysis)} and
	 * {@link #addNegativeBindingsForSymbol(Collection, SMNode, Map, String, TMFlowAnalysis)}.
	 * @param disjuncts a set of {@link Disjunct}s
	 */
	private Constraint(HashSet disjuncts) {
		this.disjuncts = (HashSet) disjuncts.clone();
	}

	public Constraint(Disjunct onlyDisjunct) {
		this.disjuncts = new HashSet<Disjunct<InstanceKey>>();
		this.disjuncts.add(onlyDisjunct);
	}

	/**
	 * Adds bindings for the case where the given symbol is read by taking an edge in the program graph.
	 * Also, this adds the shadow-ids of any edges that are on the path to a final state to the
	 * may-flow analysis. (see {@link TMFlowAnalysis#registerActiveShadows(Set)})
	 * @param allVariables the set of all variables bound by the symbol that is read
	 * @param node true if the source state of the transition is an initial state
	 * @param to the state the state machine is driven into by taking this transition
	 * @param bindings the bindings of that edge in form of a mapping {@link String} to {@link PointsToSet}
	 * @param shadow the shadow that triggered this edge
	 * @return the updated constraint; this is a fresh instance, 
	 * the disjuncts of this copy hold the history of the disjuncts of this constraint plus
	 * the shadowId that is passed in
	 */
	public Constraint addBindingsForSymbol(Collection allVariables, SMNode from, SMNode to, Map bindings, Shadow shadow) {
		Constraint newConstraint = FALSE;

		//for all current disjuncts
		for (Iterator iter = disjuncts.iterator(); iter.hasNext();) {
			Disjunct disjunct = (Disjunct) iter.next();

			//delegate to the disjunct
			Disjunct newDisjunct = disjunct.addBindingsForSymbol(allVariables,bindings,shadow, from);
			assert newDisjunct!=null;
            
            //FALSE is a marker for "no match"; do not add it as it represents TRUE in the Constraint
            if(newDisjunct!= Disjunct.FALSE) {
    			newConstraint = newConstraint.or(new Constraint(newDisjunct));
            }
			
		}

		return newConstraint;
	}
	
    /**
     * Applies a special version of the distributive law for Boolean algebra. In general, this law states (A && B) || (A && C) = A && (B || C).
     * The special case we are interested in is the one where B is the negation of C. Then it holds that: (A && x=o) || (A && x!=o) = A && (x=o || x!=o) = A.
     * This is the reduction we perform.
     * 
     * TODO can we extend this to take multiple equal bindings into account: c=c1&&i=i1 || c!=c1&&i!=i1 ?
     */
    public Constraint applyDistributiveLaw() {
        if(disjuncts.isEmpty()) {
            //nothing to do
            return this;
        }

        //deep clone this constraint
        Constraint clone = clone();
        HashSet<Disjunct<InstanceKey>> clonedDisjuncts = new HashSet<Disjunct<InstanceKey>>();
        for (Disjunct<InstanceKey> d : clone.disjuncts) {
            clonedDisjuncts.add(d.clone());
        }
        clone.disjuncts = clonedDisjuncts; 
        
        HashSet<Disjunct<InstanceKey>> newDisjuncts = new HashSet<Disjunct<InstanceKey>>(clone.disjuncts);
       
        /*
         * This loop tries to find a binding superFlousVar=superFlousKey, which appears positive in one
         * disjunct but negative in a second disjunct. Furthermore, those two disjuncts must be equal except for this binding.
         * (The equal part represents the "A" in the equations above.) If such a binding is found, superFlousVar etc. is set
         * and the reduction is performed. We iterate until a fixed point is reached.
         */
        while(true) {
	    	String superFlousVar=null;
	    	InstanceKey superFlousKey=null;
	    	Disjunct first=null, second=null;
	    	
	    	outer:
	        for (Disjunct d1 : newDisjuncts) {
	        	for (Disjunct d2 : newDisjuncts) {        		 
	            	for (Iterator<Map.Entry<String,Set>> posIter = ((Map<String,Set>)d1.posVarBinding).entrySet().iterator(); posIter.hasNext();) {
	            		Map.Entry<String,Set> entry = posIter.next();
	            		String var = entry.getKey();
	            		Set<InstanceKey> posBindingsD1 = entry.getValue();
	            		if(posBindingsD1==null) posBindingsD1 = new HashSet<InstanceKey>();
	            		Set<InstanceKey> negBindingsD2 = (Set<InstanceKey>) d2.negVarBinding.get(var);
	            		if(negBindingsD2==null) negBindingsD2 = new HashSet<InstanceKey>();
	            		Set<InstanceKey> intersection = new HashSet<InstanceKey>(posBindingsD1);
	            		intersection.retainAll(negBindingsD2);
	            		if(intersection.size()==1) {
	            			Disjunct d1Copy = d1.clone();
	            			Set<InstanceKey> posBindD1 = (Set<InstanceKey>) d1Copy.posVarBinding.get(var);	            			
	            			posBindD1.removeAll(intersection);
	            			if(posBindD1.isEmpty()) {
	            				d1Copy.posVarBinding.remove(var);
	            			}
	            			Disjunct d2Copy = d2.clone();
	            			Set<InstanceKey> negBindD2 = (Set<InstanceKey>) d2Copy.negVarBinding.get(var);	            			
	            			negBindD2.removeAll(intersection);
	            			if(negBindD2.isEmpty()) {
	            				d2Copy.negVarBinding.remove(var);
	            			}
	            			if(d1Copy.cloneWithoutHistory().equals(d2Copy.cloneWithoutHistory())) {
		            			superFlousKey = intersection.iterator().next();
		            			superFlousVar = var;
		            			first = d1;
		            			second = d2;
		            			break outer;
	            			}
	            		}            		
	            	}
	            }
	        }
	    	
	    	if(first!=null) {
	    		assert second!=null;
	    		assert superFlousKey!=null;
	    		assert superFlousVar!=null;
	    		
	    		newDisjuncts.remove(first);
	    		newDisjuncts.remove(second);
	    		Disjunct cloneOfFirst = first.clone();
	    		Set<InstanceKey> iks = (Set<InstanceKey>) cloneOfFirst.posVarBinding.get(superFlousVar);
	    		iks.remove(superFlousKey);
	    		if(iks.isEmpty()) {
	    			cloneOfFirst.posVarBinding.remove(superFlousVar);
	    		}
	    		
	    		newDisjuncts.add(cloneOfFirst);
	    	} else {
	    		break;
	    	}
        }

        for (Disjunct disjunct : newDisjuncts) {
			disjunct.reconcileHistory();
		}
        
        //store set with new hash codes
        clone.disjuncts = new HashSet<Disjunct<InstanceKey>>(newDisjuncts);
        return clone;
    }

    /**
	 * Adds negative bindings for the case where the given symbol is read by taking a <i>skip</i> edge in the program graph.
	 * Effectively this deletes all bindings which adhere to the binding which is passed in.
	 * Note that unlike in {@link #addBindingsForSymbol(Collection, SMNode, Map, int, TMFlowAnalysis)}
	 * here we do not need to update the history of the disjuncts, because we know that no skip-loop
	 * can ever possibly lead to a final node.
	 * @param allVariables the set of all variables bound by the symbol that is read
	 * @param state the state in the state-machine which the skip-loop which is taken is connected to 
	 * @param bindings the bindings of that skip-edge in form of a mapping {@link String} to {@link PointsToSet}
	 * @param shadow the shadow that triggered this edge
     * @param configuration the configuration holding this disjunct
	 * @param analysis the may-flow analysis; used as call-back to register active edges
	 * @return the updated constraint; this is a fresh instance or {@link #FALSE} 
	 */
	public Constraint addNegativeBindingsForSymbol(Collection allVariables, SMNode state, Map bindings, Shadow shadow, Configuration configuration) {
		Constraint newConstraint = FALSE;
		//for each disjunct
		for (Iterator iter = disjuncts.iterator(); iter.hasNext();) {
			Disjunct disjunct = (Disjunct) iter.next();
			
			HashSet newDisjuncts = new HashSet(disjunct.addNegativeBindingsForSymbol(allVariables,bindings,shadow, configuration));
			//references to FALSE have to be removed, as {FALSE} actually represents TRUE, not FALSE (in DNF)
			newDisjuncts.remove(Disjunct.FALSE);

			newConstraint = newConstraint.or(new Constraint(newDisjuncts));
		}

		return newConstraint;
	}


	
	/**
	 * Constructs and returns a constraint representing <code>this</code>
	 * <i>or</i> <code>other</code> by adding all disjuncts from other to a clone of this constraint.
	 * @param other some other constraint
	 * @return the disjoint constraint
	 */
	public Constraint or(Constraint other) {
		//of both are equal, just reuse the current one
		if(equals(other)) {
			return this;
		}
		//if both are equal not taking their history into account it should be safe to just preserve one of them
		//Note to self: It seems sound to do that on a single transition but it should be unsound to do that at
		//merge points (and we don't do it at merge points).
		if(cloneWithoutHistory().equals(other.cloneWithoutHistory())) {
			return this;
		}
		Constraint copy = clone();
		copy.disjuncts.addAll(other.disjuncts);
		return copy;
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected Constraint clone() {
		try {
			Constraint clone = (Constraint) super.clone();
			clone.disjuncts = (HashSet) disjuncts.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected Constraint cloneWithoutHistory() {
		try {
			Constraint clone = (Constraint) super.clone();
			clone.disjuncts = new HashSet();
			for (Disjunct d : disjuncts) {
				clone.disjuncts.add(d.cloneWithoutHistory());
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @return the number of disjuncts in this constraint
	 */
	public int size() {		
		return disjuncts.size();
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((disjuncts == null) ? 0 : disjuncts.hashCode());
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
		final Constraint other = (Constraint) obj;
		if (disjuncts == null) {
			if (other.disjuncts != null)
				return false;
		} else if (!disjuncts.equals(other.disjuncts))
			return false;
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return disjuncts.toString();
	}

	public Collection<Shadow> getCurrentHistory() {
		Collection<Shadow> res = new HashSet<Shadow>();
		for (Disjunct d : disjuncts) {
			res.addAll(d.getCurrentHistory());
		}
		return res;
	}

	/**
	 * This method pattern matches on constraints such as (A && B) || !A || !B || C,
	 * which is the same as TRUE || C, which is the same as TRUE. Therefore we return {@link #TRUE}
	 * if there is a disjunct with positive bindings only (A && B), and for each positive binding
	 * there exists a disjunct with no positive binding and which has the original positive binding
	 * as negative binding. 
	 */
	public Constraint tryReduceToTrue() {
        if(disjuncts.size()<2) {
            //nothing to do
            return this;
        }

        for (Disjunct d1 : disjuncts) {
        	if(!d1.negVarBinding.isEmpty()) continue;
        	if(d1.posVarBinding.isEmpty()) continue;
        	
        	boolean allPosBindingsHaveCorrespondingNegativeBinding = true;
    		for (Iterator<Map.Entry<String,Set>> posIter = ((Map<String,Set>)d1.posVarBinding).entrySet().iterator(); posIter.hasNext();) {
        		Map.Entry<String,Set> entry = posIter.next();
        		String var = entry.getKey();
        		Set<InstanceKey> posBindingsD1 = entry.getValue();
        		boolean foundCorrespondingNegativeBinding = false;
        		
            	for (Disjunct<InstanceKey> d2 : disjuncts) {        		 
                	if(!d2.posVarBinding.isEmpty()) continue;
            		
                	Set<InstanceKey> negBindingsD2 = d2.negVarBinding.get(var);
                	if(posBindingsD1.equals(negBindingsD2)) {
                		foundCorrespondingNegativeBinding = true;
	                	break;
                	}
            	}
            	
            	if(!foundCorrespondingNegativeBinding) {
            		allPosBindingsHaveCorrespondingNegativeBinding = false;
            		break;
            	}
        	}
    		
    		if(allPosBindingsHaveCorrespondingNegativeBinding) {
    			return TRUE;
    		}
        }
        
       	return this;
	}	
	
	public static void reset() {
		FALSE = null;
		TRUE = null;
	}
	
}
