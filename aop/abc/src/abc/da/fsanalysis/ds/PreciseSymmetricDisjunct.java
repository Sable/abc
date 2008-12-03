/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
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

package abc.da.fsanalysis.ds;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.jimple.toolkits.pointer.InstanceKey;
import abc.da.fsanalysis.analysis.AnalysisJob;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.StateMachine;

/**
 * A disjuncts making use of must and must-not alias information.
 * It is symmetric because it uses a set of positive and negative bindings with symmetric update rules.
 *
 * @author Eric Bodden
 */
public class PreciseSymmetricDisjunct extends Disjunct<InstanceKey> {
	

	private final AnalysisJob job;

	/**
	 * Constructs a new disjunct.
	 */
	public PreciseSymmetricDisjunct(AnalysisJob job) {
        this.job = job;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Disjunct addBindingsForSymbol(Collection allVariables, Map bindings, Shadow shadow, boolean sameState) {
		PreciseSymmetricDisjunct clone = clone();
		//for each TracePattern variable
		for (String tmVar : (Collection<String>)allVariables) {
			InstanceKey toBind = (InstanceKey) bindings.get(tmVar);

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
			 * Uniqueness check. See comment there.
			 */
			if(contradictionDueToUniquess(bindings,tmVar)) {
				return FALSE;
			}
			
			/*
			 * Rule 3: If we want to bind a value x=o1 but we already have a positive binding
			 *         x=o2, and we know that o1==o2 by the must-alias analysis, then we  
			 *         can just leave the constraint unchanged, because x=o1 && x=o2 is the same as just
			 *         x=o1.
			 *         However, we have to add the current shadow to the history of this binding. (but only if
			 *         we actually produce this binding on a new state, i.e. if we don't loop) 
			 */
			if(mustAliasedPositiveBinding(tmVar, toBind)) {
				if(!sameState)
					clone.posVarBinding.get(tmVar).get(toBind).add(shadow);
				continue;
			}
			
			/*
			 * Optimization rule introduced Feb 15th, 2008 by Eric Bodden:
			 * We never store non-local positive bindings. These bindings only *propagate* local bindings.
			 */
			if(toBind.haveLocalInformation()) {
				/*
				 * At this point we know that the positive binding is necessary because it does not clash with any
				 * of our other constraints and also it is not superfluous (rule 3). Hence, store it.
				 */
				addBinding(clone.posVarBinding, tmVar, toBind, shadow);
			}
			
			/*
			 * Rule 4: We just stored the positive binding x=o1. Now if there is a negative binding that says
			 *         x!=o2 and we know that o1!=o2 by our not-may-alias analysis, then we do not need to store that
			 *         negative binding. This is because x=o1 and o1!=o2 already implies x!=o2. 
			 */
			clone.pruneSuperfluousNegativeBinding(tmVar,toBind);
		}
		
		if(equals(clone)) {
			return this;
		}
		
		//assert clone.isHistoryConsistent();
		return clone;
	}
	
	public Set addNegativeBindingsForSymbol(Collection allVariables, Map<String,InstanceKey> bindings, Shadow shadow, Configuration config) {		
		
		//if there are no variables, there is nothing to do
		if(allVariables.isEmpty()) {
			return Collections.EMPTY_SET;
		}
		
		Set resultSet = new HashSet();
		
		for (Iterator varIter = allVariables.iterator(); varIter.hasNext();) {
			String varName = (String) varIter.next();

			resultSet.add( addNegativeBindingsForVariable(varName, bindings.get(varName), shadow) );
		}			

		return resultSet;
	}

	/**
	 * This method returns true if the incoming positive binding would contradict with the currently
	 * existing positive or negative binding (for the given variable), taking into account an interprocedural
	 * uniqueness test. 
	 * 
	 * For an incoming binding on variable x we first determine all symbols that can bind x in the state machine,
	 * i.e. all symbols that bind x, not match on x. Then we determine all shadows with these symbols in the entire
	 * program (we take into account all shadows of the current methods and all shadows overlapping with those).
	 * 
	 * If one of these shadows does not reside in the current method we abort, returning false. This is because we need
	 * precise pointer information for the subsequent checks. 
	 * 
	 * Then, the check progresses twofold, comparing on positive and negative bindings separately.
	 *
	 * First check: 
	 * 
	 * Assume an incoming positive binding x=o.
	 * 
	 * For each existing positive binding y=p.
	 * 
	 * 	If for each shadow that may bind x=o it holds that
	 * 		this shadow cannot bind y=p,
	 * 	then return true
	 * 
	 * else return false
	 * 
	 * Second check: 
	 * 
	 * Assume an incoming positive binding x=o.
	 * 
	 * For each existing negative binding y!=p.
	 * 
	 * 	If for each shadow that may bind x=o it holds that
	 * 		this shadow must bind y=p,
	 * 	then return true 
	 * 
	 * else return false
	 * 
	 * The second check requires must-alias information and
	 * we return false early if such information is not available because
	 * the binding shadows reside in multiple methods.
	 * 
	 * @param bindings a positive binding
	 * @param var the vaiable that is bound
	 * @return true if the incoming binding would contradict the currently existing binding
	 */
	protected boolean contradictionDueToUniquess(Map<String, InstanceKey> bindings, String var) {
		//quick check : do not need to compute anything more expensive
	    //if any bound value does not provide must-alias information 
	    for (Map.Entry<String, InstanceKey> entry : bindings.entrySet()) {
	    	InstanceKey toBind = entry.getValue();
	    	
		    if(!toBind.haveLocalInformation()) {
				//have no precise information on that value
				return false;
			}			
		    
		    if(!toBind.isOfReferenceType()) {
				//have no precise information on that value
		    	return false;
		    }

		}
	    
	
		Set<Shadow> overlappingAndMethodShadows = new HashSet<Shadow>(job.overlappingSymbolShadows());
		overlappingAndMethodShadows.addAll(job.allTMShadowsInMethod());

		boolean allShadowsBindingCurrentSymbolInCurrentMethod = true;
		Set<Shadow> shadowsBindingCurrentSymbol = new HashSet<Shadow>();
		for (String tmVar : bindings.keySet()) {
			Set<String> symbolsThatMayBindCurrentValue = new HashSet<String>();
			StateMachine sm = job.stateMachine();
			//for all edges in the tm state machine
			for (Iterator<SMEdge> edgeIter = sm.getEdgeIterator(); edgeIter.hasNext();) {
				SMEdge edge = edgeIter.next();
				//get the symbol/label of that edge and the variables the edge binds
				String edgeSymbol = edge.getLabel();
				assert edgeSymbol != null; //should have no epsilon edges at this point
				List<String> varsBoundByEdge = job.TracePattern().getVariableOrder(edgeSymbol);
				//if the edge may freshly bind the variable we care about...
				if(varsBoundByEdge.contains(tmVar) &&
				   !edge.getSource().boundVars.contains(tmVar)) {
					//add to the set
					symbolsThatMayBindCurrentValue.add(edgeSymbol);
				}
			}			
			
			for (Shadow shadow : overlappingAndMethodShadows) {
				if(symbolsThatMayBindCurrentValue.contains(job.symbolNameForShadow(shadow))) {					
					//if shadow from different method
					if(!shadow.getContainer().equals(job.method())) {
						//if shadow has symbol that may bind the value we care about
						allShadowsBindingCurrentSymbolInCurrentMethod = false;
					} else if(symbolsThatMayBindCurrentValue.contains(job.symbolNameForShadow(shadow))) {
						shadowsBindingCurrentSymbol.add(shadow);
					}
				}
			}
		}
				
		for (Entry<String, HashMap<InstanceKey, HashSet<Shadow>>> entry : this.posVarBinding.entrySet()) {
			String posVar = entry.getKey();
			Set<InstanceKey> posKeys = entry.getValue().keySet();
			boolean allShadowsBindPosVarToPosKey = true;
			for (Shadow shadow : shadowsBindingCurrentSymbol) {
				final Map<String,InstanceKey> varToInstanceKey = flowAnalysis.reMap(shadow.getAdviceFormalToSootLocal());
				//get binding for newly bound variable
				InstanceKey newKey = varToInstanceKey.get(var);
				//if the shadow may bind the new binding...
				if(newKey!=null && !newKey.mayNotAlias(bindings.get(var))) {
					//get binding for negative variable
					InstanceKey posKey = varToInstanceKey.get(posVar);
					if(posKey!=null) {
						for (InstanceKey key : posKeys) {
							if(!key.mayNotAlias(posKey)) {
								//found a shadow that may bind posVar to posKey
								allShadowsBindPosVarToPosKey = false;
								break;
							}
						}
					}
				}
			}			
			if(allShadowsBindPosVarToPosKey) {
				return true;
			}
		}

		/*
		 * For the following check we need must-alias information on all values.
		 * We don't have that info if a shadow comes from another method. Hence, just
		 * abort then.
		 */
		if(!allShadowsBindingCurrentSymbolInCurrentMethod) {
			return false;
		}

		for (Entry<String, HashMap<InstanceKey, HashSet<Shadow>>> entry : this.negVarBinding.entrySet()) {
			String negVar = entry.getKey();
			Set<InstanceKey> negKeys = entry.getValue().keySet();
			boolean allShadowsBindNegVarToNegKey = true;
			for (Shadow shadow : shadowsBindingCurrentSymbol) {
				final Map<String,InstanceKey> varToInstanceKey = flowAnalysis.reMap(shadow.getAdviceFormalToSootLocal());
				//get binding for newly bound variable
				InstanceKey newKey = varToInstanceKey.get(var);
				//if the shadow may bind the new binding...
				if(newKey!=null && !newKey.mayNotAlias(bindings.get(var))) {
					//get binding for negative variable
					InstanceKey negKey = varToInstanceKey.get(negVar);					
					if(negKey!=null && !negKeys.contains(negKey)) {
						//found a shadow that potentially binds negVar to a value
						//different from negKey
						allShadowsBindNegVarToNegKey = false;
						break;
					}
				}
			}			
			if(allShadowsBindNegVarToNegKey) {
				return true;
			}
		}

		return false;
	}

	
	
	/**
	 * {@inheritDoc}
	 */
	@Override      
	protected Disjunct addNegativeBindingsForVariable(String tmVar, InstanceKey toBind, Shadow shadow) {
		/*
		 * Rule 1: If we want to bind a value x!=o1 but we already have a positive binding
		 *         x=o2, and we know that o1!=o2 by the not-may-alias analysis, then we can
		 *         just leave the constraint unchanged, as x=o2 and o1!=o2 already implies x!=o1. 
		 */
		if(notMayAliasedPositiveBinding(tmVar, toBind)) {
			return this;
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
			return this;
		}
		
		/*
		 * Rule 3: If we want to bind a value x!=o1 but we already have a positive binding
		 *         x=o2, and we know that o1==o2 by the must-alias analysis, then we can
		 *         safely reduce to FALSE.
		 */
		if(mustAliasedPositiveBinding(tmVar, toBind)) {
			return FALSE;
		}

		/*
		 * At this point we know that the negative binding is necessary because it does not clash with any
		 * of our other constraints and also it is not superfluous (rule 2). Hence, store it.
		 * 
		 * HOWEVER: We only need to store negative bindings which originate from within the current methods.
		 * Other bindings (1) can never lead to clashes, because they can never must-alias a binding (we don't have
		 * inter-procedural must-alias information), neither (2) do we need it for looking up which shadows contribute to
		 * reaching a final state.
		 * 
		 * Also we don't need to store bindings of primitive values, for the same reason.
		 */
	    if(toBind.haveLocalInformation() && toBind.isOfReferenceType()) {
	    	PreciseSymmetricDisjunct clone = clone();
	    	addBinding(clone.negVarBinding, tmVar, toBind, shadow);
	    	//assert newDisjunct.isHistoryConsistent();
			return clone;
	    } else {
	    	//do not need to store negative bindings from other methods
	    	return this;
	    }
	}
	
	protected static void addBinding(HashMap<String, HashMap<InstanceKey, HashSet<Shadow>>> hashMap, String tmVar, InstanceKey toBind, Shadow shadow) {
		HashMap<InstanceKey, HashSet<Shadow>> posBindingForVar = hashMap.get(tmVar);
		if(posBindingForVar==null){
			posBindingForVar = new HashMap<InstanceKey,HashSet<Shadow>>();
			hashMap.put(tmVar, posBindingForVar);
		}
		HashSet<Shadow> history = new HashSet<Shadow>();
		history.add(shadow);
		posBindingForVar.put(toBind,history);
	}
	
	/**
	 * Returns <code>true</code> if there is a positive binding stored in this disjunct
	 * that must-aliases the given binding for the given variable.
	 */
	protected boolean mustAliasedPositiveBinding(String tmVar, InstanceKey toBind) {
		HashMap<InstanceKey, HashSet<Shadow>> posBindingsForVar = posVarBinding.get(tmVar);
		if(posBindingsForVar!=null) {
			for (InstanceKey posBinding : posBindingsForVar.keySet()) {
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
		HashMap<InstanceKey, HashSet<Shadow>> posBindingsForVar = posVarBinding.get(tmVar);
		if(posBindingsForVar!=null) {
			for (InstanceKey posBinding : posBindingsForVar.keySet()) {
				if(posBinding.mayNotAlias(toBind)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if there is a negative binding stored in this disjunct
	 * that must-aliases the given binding for the given variable.
	 */
	protected boolean mustAliasedNegativeBinding(String tmVar, InstanceKey toBind) {
		HashMap<InstanceKey, HashSet<Shadow>> negBindingsForVar = negVarBinding.get(tmVar);
		if(negBindingsForVar!=null) {
			for (InstanceKey negBinding : negBindingsForVar.keySet()) {
				if(negBinding.mustAlias(toBind)) {
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
		HashMap<InstanceKey, HashSet<Shadow>> negBindingsForVar = negVarBinding.get(tmVar);
		if(negBindingsForVar!=null) {
			for (Iterator<InstanceKey> iterator = negBindingsForVar.keySet().iterator(); iterator.hasNext();) {
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
	
	private Object bindingToString(
		HashMap<String, HashMap<InstanceKey, HashSet<Shadow>>> posVarBinding, boolean positive) {
		StringBuffer sb = new StringBuffer();
		for(Iterator<Entry<String, HashMap<InstanceKey, HashSet<Shadow>>>> iter = posVarBinding.entrySet().iterator();iter.hasNext();) {
			Entry<String, HashMap<InstanceKey, HashSet<Shadow>>> entry = iter.next();			
			sb.append(entry.getKey());
			if(positive)
				sb.append("=");
			else
				sb.append("!=");
			sb.append("{");
			for(Iterator<Entry<InstanceKey, HashSet<Shadow>>> iter2 = entry.getValue().entrySet().iterator();iter2.hasNext();) {
				Entry<InstanceKey, HashSet<Shadow>> innerEntry = iter2.next();
				InstanceKey ik = innerEntry.getKey();
				String ikString = job.localMustAliasAnalysis().instanceKeyString(ik.getLocal(), ik.getStmt());
				sb.append(ikString);
				sb.append("[");
				for(Iterator<Shadow> iter3 = innerEntry.getValue().iterator();iter3.hasNext();) {
					Shadow s = iter3.next();
					sb.append(s.getPosition().line());
					if(iter3.hasNext()) {
						sb.append(",");
					}
				}
				sb.append("]");
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

	protected PreciseSymmetricDisjunct clone() {
		return (PreciseSymmetricDisjunct) super.clone();
	}	
	

}
