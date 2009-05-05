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
 * MERCHANTABILITY or FITNESS FOR InstanceKey PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received InstanceKey copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.da.fsanalysis.flowanalysis.ds;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.PointsToSet;
import soot.jimple.toolkits.pointer.InstanceKey;
import abc.da.fsanalysis.flowanalysis.AnalysisJob;
import abc.da.fsanalysis.flowanalysis.TMFlowAnalysis;

/**
 * InstanceKey disjunct represents InstanceKey mapping of variables (type {@link String}) to
 * creation sites (type {@link PointsToSet}).
 * Also it holds InstanceKey history, which is the set of shadow-ids of the shadows
 * of all edges in the program-graph that drove this disjunct into its current state.
 *
 * @param <InstanceKey> the abstraction type representing an object
 * 
 * Disjuncts are produced using the prototype pattern, i.e. via cloning. The prototype is
 * {@link #FALSE}. Other Disjuncts can then be created by calling
 * {@link #addBindingsForSymbol(Collection, Map, String, boolean)} and  
 * {@link #addNegativeBindingsForSymbol(Collection, Map, String)}.
 *
 * @author Eric Bodden
 */
public class Disjunct implements Cloneable {
	
	public static Disjunct TRUE;
	
	/** The unique FALSE disjunct. It holds no mapping and no history. */
	public static Disjunct FALSE;

	protected HashMap<String, HashSet<InstanceKey>> posVarBinding;
	protected HashMap<String, HashSet<InstanceKey>> negVarBinding;

	private final AnalysisJob job;
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	protected Disjunct clone() {		
		try {
			Disjunct clone = (Disjunct) super.clone();
			//deep clone positive bindings
			clone.posVarBinding = (HashMap) posVarBinding.clone();
			for (Entry<String, HashSet<InstanceKey>> entry : clone.posVarBinding.entrySet()) {
				HashSet<InstanceKey> clonedSet = (HashSet<InstanceKey>) entry.getValue().clone();
				entry.setValue(clonedSet);
			}
			//deep clone negative bindings
			clone.negVarBinding = (HashMap) negVarBinding.clone();
			for (Entry<String, HashSet<InstanceKey>> entry : clone.negVarBinding.entrySet()) {
				HashSet<InstanceKey> clonedSet = (HashSet<InstanceKey>) entry.getValue().clone();
				entry.setValue(clonedSet);
			}
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
		result = prime * result
				+ ((negVarBinding == null) ? 0 : negVarBinding.hashCode());
		result = prime * result
				+ ((posVarBinding == null) ? 0 : posVarBinding.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		final Disjunct other = (Disjunct) obj;
		if (negVarBinding == null) {
			if (other.negVarBinding != null)
				return false;
		} else if (!negVarBinding.equals(other.negVarBinding))
			return false;
		if (posVarBinding == null) {
			if (other.posVarBinding != null)
				return false;
		} else if (!posVarBinding.equals(other.posVarBinding))
			return false;
		return true;
	}

	public static void reset() {
		FALSE = null;
		TRUE = null;
	}

	public boolean isCompatibleWithPositiveBindings(Disjunct other) {
		for (String label : other.posVarBinding.keySet()) {
			HashSet<InstanceKey> posBindings = this.posVarBinding.get(label);
			if(posBindings!=null && !posBindings.isEmpty()) {
				for (InstanceKey posBinding : posBindings) {
					HashSet<InstanceKey> otherPosBindings = other.posVarBinding.get(label);
					for (InstanceKey otherPosBinding : otherPosBindings) {
						if(posBinding.mayNotAlias(otherPosBinding)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	protected boolean isCompatibleWithNegativeBindings(Disjunct other) {
		for (String label : other.posVarBinding.keySet()) {
			HashSet<InstanceKey> negBindings = this.negVarBinding.get(label);
			if(negBindings!=null && !negBindings.isEmpty()) {
				for (InstanceKey negBinding : negBindings) {
					HashSet<InstanceKey> otherPosBindings = other.posVarBinding.get(label);
					for (InstanceKey posBinding : otherPosBindings) {
						if(negBinding.equals(posBinding)) {
							return false;
						}
					}
				}
			}
		}		
		return true;
	}

	public boolean isCompatibleTo(Disjunct other) {
		return isCompatibleWithNegativeBindings(other) && other.isCompatibleWithNegativeBindings(this) && isCompatibleWithPositiveBindings(other) && other.isCompatibleWithPositiveBindings(this);
	}

	
	protected static void addBinding(HashMap<String, HashSet<InstanceKey>> hashMap, String tmVar, InstanceKey toBind) {
		HashSet<InstanceKey> bindingForVar = hashMap.get(tmVar);
		if(bindingForVar==null){
			bindingForVar = new HashSet<InstanceKey>();
			hashMap.put(tmVar, bindingForVar);
		}
		bindingForVar.add(toBind);
	}

	/**
	 * Creates a new disjunct with empty bindings.
	 * Only to be called form this class (prototype pattern.) 
	 * Other disjuncts can be created using
	 * {@link #addBindingsForSymbol(Collection, Map, String, boolean)} and 
	 * {@link #addNegativeBindingsForSymbol(Collection, Map, String)}.
	 */
	public Disjunct(AnalysisJob job, TMFlowAnalysis flowAnalysis) {
	    this.job = job;
		this.posVarBinding = new HashMap<String, HashSet<InstanceKey>>();
		this.negVarBinding = new HashMap<String, HashSet<InstanceKey>>();
	}

	/**
	 * Adds bindings to this disjunct, adding the shadowId to the history of the new disjunct.
	 * @param allVariables the set of all variables bound by the symbol that is read
	 * @param bindings the bindings of that edge in form of InstanceKey mapping {@link String} to {@link PointsToSet}
	 * @param shadow the shadow that triggered this edge
	 * @param sameState true if the disjunct will be placed on the same state as the original disjunct, i.e. if we loop
	 * @return the updated disjunct; this is InstanceKey fresh instance, 
	 * the disjuncts of this copy hold the history of the disjuncts of this constraint plus
	 * the shadowId that is passed in
	 */
	public Disjunct addBindingsForSymbol(Map<String,? extends Set<InstanceKey>> bindings) {
		
		Disjunct clone = clone();
		//for each bound variable
		for (String tmVar : (Collection<String>)bindings.keySet()) {
			Set<InstanceKey> toBinds = bindings.get(tmVar);
			
			for (InstanceKey toBind : toBinds) {
				
				/*
				 * Rule 1: If we want to bind a value x=o1 but we already have a positive binding
				 *         x=o2, and we know that o1!=o2 by the not-may-alias analysis, then we can
				 *         safely reduce to FALSE.
				 */
				if(notMayAliasedPositiveBinding(tmVar, toBind)) {
					return FALSE;
				}
				
				/*
				 * Rule 2: If we want to bind a value x=o1 but we already have a negative binding
				 *         x=o2, and we know that o1==o2 by the must-alias analysis, then we can
				 *         safely reduce to FALSE.
				 */
				if(mustAliasedNegativeBinding(tmVar, toBind)) {
					return FALSE;
				}
				
				/*
				 * Rule 3: If we want to bind a value x=o1 but we already have a positive binding
				 *         x=o2, and we know that o1==o2 by the must-alias analysis, then we  
				 *         can just leave the constraint unchanged, because x=o1 && x=o2 is the same as just
				 *         x=o1.
				 */
				if(mustAliasedPositiveBinding(tmVar, toBind)) {
					continue;
				}
				
				/*
				 * At this point we know that the positive binding is necessary because it does not clash with any
				 * of our other constraints and also it is not superfluous (rule 3). Hence, store it.
				 */
				addBinding(clone.posVarBinding, tmVar, toBind);
				
				/*
				 * Rule 4: We just stored the positive binding x=o1. Now if there is a negative binding that says
				 *         x!=o2 and we know that o1!=o2 by our not-may-alias analysis, then we do not need to store that
				 *         negative binding. This is because x=o1 and o1!=o2 already implies x!=o2. 
				 */
				clone.pruneSuperfluousNegativeBinding(tmVar,toBind);			
			}
	
		}
		
		//let's safe some memory if we can
		if(equals(clone)) {
			return this;
		}
	
		return clone;
	}

	/**
	 * Adds negative bindings for the case where the given symbol is read by taking InstanceKey <i>skip</i> edge in the program graph.
	 * Effectively this deletes all bindings which adhere to the binding which is passed in.
	 * Note that unlike in {@link #addBindingsForSymbol(Collection, Map, int, boolean)}
	 * here we do not need to update the history of the disjuncts, because we know that no skip-loop
	 * can ever possibly lead to InstanceKey final node.
	 * @param allVariables the set of all variables bound by the symbol that is read
	 * @param bindings the bindings of that skip-edge in form of InstanceKey mapping {@link String} to {@link PointsToSet}
	 * @param shadow the shadow of the shadow that triggered this edge
	 * @param configuration 
	 * @param analysis 
	 * @return the updated constraint; this is InstanceKey fresh instance or {@link #FALSE} 
	 */
	public Set<Disjunct> addNegativeBindingsForSymbol(Map<String,? extends Set<InstanceKey>> bindings) {		
		Collection<String> allVariables = bindings.keySet();
		
		//if there are no variables, there is nothing to do
		if(allVariables.isEmpty()) {
			return Collections.emptySet();
		}
		
		Set<Disjunct> resultSet = new HashSet<Disjunct>();
		
		for (String varName : allVariables) {
			resultSet.add( addNegativeBindingsForVariable(varName, bindings.get(varName)) );
		}		
		
		resultSet.remove(Disjunct.FALSE);
	
		return resultSet;
	}

	/**
	 * Currently this just returns InstanceKey clone of <code>this</code>. We need InstanceKey must-alias and must--flow analysis
	 * in order to do anything more clever.
	 * @param varName the name of the variable for which the binding is to be updated
	 * @param negBinding the negative binding this variable should be updated with
	 * @param shadow the shadow-id of the shadow that triggered this edge
	 * @return
	 */     
	protected Disjunct addNegativeBindingsForVariable(String tmVar, Set<InstanceKey> toBinds) {
		Disjunct clone = clone();
		for (InstanceKey toBind : toBinds) {
			
			/*
			 * Rule 1: If we want to bind a value x!=o1 but we already have a positive binding
			 *         x=o2, and we know that o1!=o2 by the not-may-alias analysis, then we can
			 *         just leave the constraint unchanged, as x=o2 and o1!=o2 already implies x!=o1. 
			 */
			if(notMayAliasedPositiveBinding(tmVar, toBind)) {
				continue;
			}
			
			/*
			 * Rule 2: If we want to bind a value x!=o1 but we already have a negative binding
			 *         x!=o2, and we know that o1==o2 by the must-alias analysis, then we 
			 *         can just leave the constraint unchanged, because x!=o1 && x!=o2 is the same
			 *         as just x=o1 in that case.
			 *         Note that unlike in the positive case we do not need to add the current
			 *         shadow to the history here because we know that we are generating the constraint
			 *         on the same state as the origin state and the constraint does not change. 
			 */
			if(mustAliasedNegativeBinding(tmVar, toBind)) {
				continue;
			}
			
			/*
			 * Rule 3: If we want to bind a value x!=o1 but we already have a positive binding
			 *         x=o2, and we know that o1==o2 by the must-alias analysis, then we can
			 *         safely reduce to FALSE.
			 */
			if(mustAliasedPositiveBinding(tmVar, toBind)) {
				return FALSE;
			}
	
	    	addBinding(clone.negVarBinding, tmVar, toBind);
		}
		
		//let's safe some memory if we can
		if(this.equals(clone)) {
			return this;
		}		
		
		return clone;
	}

	private Object bindingToString(
		HashMap<String, HashSet<InstanceKey>> posVarBinding, boolean positive) {
		StringBuffer sb = new StringBuffer();
		for(Iterator<Entry<String, HashSet<InstanceKey>>> iter = posVarBinding.entrySet().iterator();iter.hasNext();) {
			Entry<String, HashSet<InstanceKey>> entry = iter.next();			
			sb.append(entry.getKey());
			if(positive)
				sb.append("=");
			else
				sb.append("!=");
			sb.append("{");
			for(Iterator<InstanceKey> iter2 = entry.getValue().iterator();iter2.hasNext();) {
				InstanceKey ik = iter2.next();
				String ikString = job.localMustAliasAnalysis().instanceKeyString(ik.getLocal(), ik.getStmt());
				sb.append(ikString);
				if(iter2.hasNext()) {
					sb.append(",");
				}
			}
			sb.append("}");
			if(iter.hasNext()) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	/**
	 * Returns <code>true</code> if there is a negative binding stored in this disjunct
	 * that must-aliases the given binding for the given variable.
	 */
	protected boolean mustAliasedNegativeBinding(String tmVar, InstanceKey toBind) {
		HashSet<InstanceKey> negBindingsForVar = negVarBinding.get(tmVar);
		if(negBindingsForVar!=null) {
			for (InstanceKey negBinding : negBindingsForVar) {
				if(negBinding.mustAlias(toBind)) {
						return true;
					}
				}
			}
		return false;
	}

	/**
	 * Returns <code>true</code> if there is a positive binding stored in this disjunct
	 * that must-aliases the given binding for the given variable.
	 */
	protected boolean mustAliasedPositiveBinding(String tmVar, InstanceKey toBind) {
		HashSet<InstanceKey> posBindingsForVar = posVarBinding.get(tmVar);
		if(posBindingsForVar!=null) {
			for (InstanceKey posBinding : posBindingsForVar) {
				if(posBinding.mustAlias(toBind)) {
						return true;
					}
				}
			}
		return false;
	}

	/**
	 * Returns <code>true</code> if there is a positive binding stored in this disjunct
	 * that not-may-aliases the given binding for the given variable.
	 */
	protected boolean notMayAliasedPositiveBinding(String tmVar, InstanceKey toBind) {
		HashSet<InstanceKey> posBindingsForVar = posVarBinding.get(tmVar);
		if(posBindingsForVar!=null) {
			for (InstanceKey posBinding : posBindingsForVar) {
				if(posBinding.mayNotAlias(toBind)) {
						return true;
					}
				}
			}
		return false;
	}

	/**
	 * Prunes any negative bindings for the variable tmVar that not may-alias the
	 * positive binding that is passed in for that variable.
	 * This is because those negative bindings are superfluous. 
	 */ 
	protected void pruneSuperfluousNegativeBinding(String tmVar, InstanceKey toBind) {
		HashSet<InstanceKey> negBindingsForVar = negVarBinding.get(tmVar);
		if(negBindingsForVar!=null) {
			for (Iterator<InstanceKey> iterator = negBindingsForVar.iterator(); iterator.hasNext();) {
				InstanceKey negBinding = iterator.next();
				if(negBinding.mayNotAlias(toBind)) {
					iterator.remove();
				}
			}
			if(negBindingsForVar.isEmpty()) {
				negVarBinding.remove(tmVar);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<");
	    sb.append(bindingToString(posVarBinding,true));
	    if(!posVarBinding.isEmpty() && !negVarBinding.isEmpty())
	    	sb.append(",");
	    sb.append(bindingToString(negVarBinding,false));
		sb.append(">");			
		return sb.toString();
	}
	
	public Map<String, HashSet<InstanceKey>> getPosVarBinding() {
		return Collections.unmodifiableMap(posVarBinding);
	}
	
	public Map<String, HashSet<InstanceKey>> getNegVarBinding() {
		return Collections.unmodifiableMap(negVarBinding);
	}
	
	public boolean implies(Disjunct other) {
		for(Iterator<Entry<String,HashSet<InstanceKey>>> entryIter=posVarBinding.entrySet().iterator();entryIter.hasNext();) {
			Entry<String,HashSet<InstanceKey>> entry = entryIter.next();
			String var = entry.getKey();
			HashSet<InstanceKey> keys = entry.getValue();
			HashSet<InstanceKey> otherKeys = other.posVarBinding.get(var);
			if(otherKeys==null) {
				return false;
			} else {
				if(!otherKeys.containsAll(keys)) {
					return false;
				}
			}
		}
		for(Iterator<Entry<String,HashSet<InstanceKey>>> entryIter=negVarBinding.entrySet().iterator();entryIter.hasNext();) {
			Entry<String,HashSet<InstanceKey>> entry = entryIter.next();
			String var = entry.getKey();
			HashSet<InstanceKey> keys = entry.getValue();
			HashSet<InstanceKey> otherKeys = other.negVarBinding.get(var);
			if(otherKeys==null) {
				return false;
			} else {
				if(!otherKeys.containsAll(keys)) {
					return false;
				}
			}
		}
		return true;
	}
}