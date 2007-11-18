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

package abc.tm.weaving.weaver.tmanalysis.ds;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.PointsToSet;
import soot.jimple.toolkits.pointer.InstanceKey;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.weaver.tmanalysis.mustalias.TMFlowAnalysis;

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
	
	/**
	 * a most-recently used cache to cache equal constraints; the idea is that equality checks
	 * are faster if performed on "interned" instances
	 * @see #intern()
	 * @see String#intern()
	 */
	protected static Map constraintToUniqueConstraint = new HashMap();//new MemoryStableMRUCache("constraint-intern",10*1024*1024,false);
	
	public static void reset() {
		constraintToUniqueConstraint.clear();
	}


	/** The set of disjuncts for this constraint (DNF). */
	protected HashSet<Disjunct> disjuncts;
	
	/** The unique constraint for the final states. */
	public static Constraint FINAL;

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
			public Constraint addBindingsForSymbol(Collection allVariables, SMNode from, SMNode to, Map bindings, String shadowId) {
				//FALSE stays FALSE
				return this;
			}
			
			/**
			 * Returns this (FALSE).
			 */
			@Override
			public Constraint addNegativeBindingsForSymbol(Collection allVariables, SMNode state, Map bindings, String shadowId, Configuration config) {
				//FALSE stays FALSE
				return this;
			}
			
			@Override
			protected Constraint clone() {
				return new Constraint(disjuncts);
			}
		};

		//initialize FINAL
		FINAL = new Constraint(new HashSet()) {			
			
			/**
			 * Returns this (FALSE).
			 */
			@Override
			public Constraint addBindingsForSymbol(Collection allVariables, SMNode from, SMNode to, Map bindings, String shadowId) {
				//FINAL stays FINAL
				return this;
			}
			
			/**
			 * Returns this (FALSE).
			 */
			@Override
			public Constraint addNegativeBindingsForSymbol(Collection allVariables, SMNode state, Map bindings, String shadowId, Configuration config) {
				//FINAL stays FINAL
				return this;
			}
			
			@Override
			protected Constraint clone() {
				return this;
			}
			
			@Override
			public String toString() {
				return "FINAL";
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

	/**
	 * Adds bindings for the case where the given symbol is read by taking an edge in the program graph.
	 * Also, this adds the shadow-ids of any edges that are on the path to a final state to the
	 * may-flow analysis. (see {@link TMFlowAnalysis#registerActiveShadows(Set)})
	 * @param allVariables the set of all variables bound by the symbol that is read
	 * @param node true if the source state of the transition is an initial state
	 * @param to the state the state machine is driven into by taking this transition
	 * @param bindings the bindings of that edge in form of a mapping {@link String} to {@link PointsToSet}
	 * @param shadowId the shadow-id of the shadow that triggered this edge
	 * @return the updated constraint; this is a fresh instance, 
	 * the disjuncts of this copy hold the history of the disjuncts of this constraint plus
	 * the shadowId that is passed in
	 */
	public Constraint addBindingsForSymbol(Collection allVariables, SMNode from, SMNode to, Map bindings, String shadowId) {
		//create a set for the resulting disjuncts
		HashSet<Disjunct> resultDisjuncts = new HashSet<Disjunct>();
		//for all current disjuncts
		for (Iterator iter = disjuncts.iterator(); iter.hasNext();) {
			Disjunct disjunct = (Disjunct) iter.next();

			//delegate to the disjunct
			Disjunct newDisjunct = disjunct.addBindingsForSymbol(allVariables,bindings,shadowId, from);
			assert newDisjunct!=null;
            
            //FALSE is a marker for "no match"; do not add it as it represents TRUE in the Constraint
            if(newDisjunct!= Disjunct.FALSE) {
                resultDisjuncts.add(newDisjunct);
            }
		}
		
		if(resultDisjuncts.isEmpty()) {
			//if no disjuncts are left, this means nothing else but FALSE
			return FALSE;	
		} else {
			//return an interned version of the the updated copy;
			//the disjuncts of this copy hold clones of the history of the original disjuncts
			//(plus the id of the shadow that triggered the current edge)
			return new Constraint(resultDisjuncts);
		}		
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
        HashSet<Disjunct> clonedDisjuncts = new HashSet<Disjunct>();
        for (Disjunct d : clone.disjuncts) {
            clonedDisjuncts.add(d.clone());
        }
        clone.disjuncts = clonedDisjuncts; 
        
        HashSet<Disjunct> newDisjuncts = new HashSet<Disjunct>(clone.disjuncts);
       
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
	            			if(d1Copy.equals(d2Copy)) {
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

        //store set with new hash codes
        clone.disjuncts = new HashSet<Disjunct>(newDisjuncts);
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
	 * @param shadowId the shadow-id of the shadow that triggered this edge
     * @param configuration the configuration holding this disjunct
	 * @param analysis the may-flow analysis; used as call-back to register active edges
	 * @return the updated constraint; this is a fresh instance or {@link #FALSE} 
	 */
	public Constraint addNegativeBindingsForSymbol(Collection allVariables, SMNode state, Map bindings, String shadowId, Configuration configuration) {
		HashSet resultDisjuncts = new HashSet();
		//for each disjunct
		for (Iterator iter = disjuncts.iterator(); iter.hasNext();) {
			Disjunct disjunct = (Disjunct) iter.next();
			
			resultDisjuncts.addAll(disjunct.addNegativeBindingsForSymbol(allVariables,bindings,shadowId, configuration));
		}
		
		//references to FALSE are useless in DNF
		resultDisjuncts.remove(Disjunct.FALSE);
		if(resultDisjuncts.isEmpty()) {
			//if no disjunts are left, this means nothing else but FALSE
			return FALSE;
		} else {
			//return an interned version of the updated copy;
			//the disjuncts of this copy hold clones of the history of the original disjuncts
			//(plus the id of the shadow that triggered the current edge)
			return new Constraint(resultDisjuncts);
		}		
	}


	
	/**
	 * Constructs and returns a constraint representing <code>this</code>
	 * <i>or</i> <code>other</code> by adding all disjuncts from other to a clone of this constraint.
	 * @param other some other constraint
	 * @return the disjoint constraint
	 */
	public Constraint or(Constraint other) {
		Constraint copy = (Constraint) clone();
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

	public Collection<String> getCurrentHistory() {
		Collection<String> res = new HashSet<String>();
		for (Disjunct d : disjuncts) {
			res.addAll(d.getCurrentHistory());
		}
		return res;
	}	
	
}
