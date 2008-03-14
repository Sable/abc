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

package abc.tmwpopt.fsanalysis.ds;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.jimple.toolkits.pointer.InstanceKey;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tmwpopt.fsanalysis.stages.AnalysisJob;

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
	public Disjunct addBindingsForSymbol(Collection allVariables, Map bindings, Shadow shadow, SMNode from) {
		PreciseSymmetricDisjunct clone = clone();
		//for each tracematch variable
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
			 */
			if(mustAliasedPositiveBinding(tmVar, toBind)) {
				continue;
			}
			
			/*
			 * Optimization rule introduced Feb 15th, 2008 by Eric Bodden:
			 * We never store non-local positive bindings. These bindings only propagate local bindings.
			 */
			if(toBind.haveLocalInformation()) {
				/*
				 * At this point we know that the positive binding is necessary because it does not clash with any
				 * of our other constraints and also it is not superfluous (rule 3). Hence, store it.
				 */
				clone.addPositiveBinding(tmVar, toBind, shadow);
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
		
		assert clone.isHistoryConsistent();
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
			TMStateMachine sm = job.tmStateMachine();
			//for all edges in the tm state machine
			for (Iterator<SMEdge> edgeIter = sm.getEdgeIterator(); edgeIter.hasNext();) {
				SMEdge edge = edgeIter.next();
				//get the symbol/label of that edge and the variables the edge binds
				String edgeSymbol = edge.getLabel();
				assert edgeSymbol != null; //should have no epsilon edges at this point
				List<String> varsBoundByEdge = job.traceMatch().getVariableOrder(edgeSymbol);
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
				
		for (Map.Entry<String, Set<InstanceKey>> entry : this.posVarBinding.entrySet()) {
			String posVar = entry.getKey();
			Set<InstanceKey> posKeys = entry.getValue();
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

		for (Map.Entry<String, Set<InstanceKey>> entry : this.negVarBinding.entrySet()) {
			String negVar = entry.getKey();
			Set<InstanceKey> negKeys = entry.getValue();
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
	    	PreciseSymmetricDisjunct newDisjunct = (PreciseSymmetricDisjunct) addNegativeBinding(tmVar, toBind, shadow);
	    	assert newDisjunct.isHistoryConsistent();
			return newDisjunct;
	    } else {
	    	//do not need to store negative bindings from other methods
	    	return this;
	    }
	}

	protected Disjunct addNegativeBinding(String tmVar, InstanceKey negBinding, Shadow shadow) {	
		//check if we need to add...
		//we do *not* need to add a mapping v->l is there is already a mapping
		//v->m with mustAlias(l,m)
		Set<InstanceKey> thisNegBindingsForVariable = negVarBinding.get(tmVar);
		if(thisNegBindingsForVariable!=null && thisNegBindingsForVariable.contains(negBinding)) {
			return this;
		}
        
		//else clone and actually add the binding...
		
		PreciseSymmetricDisjunct clone = (PreciseSymmetricDisjunct) clone();
		Set<InstanceKey> negBindingsForVariable = clone.negVarBinding.get(tmVar);
		//initialise if necessary
		if(negBindingsForVariable==null) {
			negBindingsForVariable = new HashSet<InstanceKey>();
			clone.negVarBinding.put(tmVar, negBindingsForVariable);
		}
		negBindingsForVariable.add(negBinding);
		clone.registerShadowIdInNegHistory(negBinding, shadow);
		return clone;
	}
	
	protected void addPositiveBinding(String tmVar, InstanceKey toBind, Shadow shadow) {
		Set<InstanceKey> posBindingForVar = posVarBinding.get(tmVar);
		if(posBindingForVar==null){
			posBindingForVar = new HashSet<InstanceKey>();
			posVarBinding.put(tmVar, posBindingForVar);
		}
		posBindingForVar.add(toBind);
		if(toBind.haveLocalInformation())
			registerShadowIdInPosHistory(toBind,shadow);
	}
	
	
	protected void registerShadowIdInPosHistory(InstanceKey toBind, Shadow shadow) {
		assert toBind.haveLocalInformation();
		posHistory.put(toBind,shadow);
	}

	protected void registerShadowIdInNegHistory(InstanceKey toBind, Shadow shadow) {
		assert toBind.haveLocalInformation();
		negHistory.put(toBind,shadow);
	}

	/**
	 * Returns <code>true</code> if there is a positive binding stored in this disjunct
	 * that must-aliases the given binding for the given variable.
	 */
	protected boolean mustAliasedPositiveBinding(String tmVar, InstanceKey toBind) {
		Set<InstanceKey> posBindingsForVar = posVarBinding.get(tmVar);
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
		Set<InstanceKey> posBindingsForVar = posVarBinding.get(tmVar);
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
	 * Returns <code>true</code> if there is a negative binding stored in this disjunct
	 * that must-aliases the given binding for the given variable.
	 */
	protected boolean mustAliasedNegativeBinding(String tmVar, InstanceKey toBind) {
		Set<InstanceKey> negBindingsForVar = negVarBinding.get(tmVar);
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
	 * Prunes any negative bindings for the variable tmVar that not may-alias the
	 * positive binding that is passed in for that variable.
	 * This is because those negative bindings are superfluous. 
	 */ 
	protected void pruneSuperfluousNegativeBinding(String tmVar, InstanceKey toBind) {
		Set<InstanceKey> negBindingsForVar = negVarBinding.get(tmVar);
		if(negBindingsForVar!=null) {
			for (Iterator<InstanceKey> iterator = negBindingsForVar.iterator(); iterator.hasNext();) {
				InstanceKey negBinding = iterator.next();
				if(negBinding.mayNotAlias(toBind)) {
					iterator.remove();
					removeFromNegShadowHistory(negBinding);
				}
			}
			if(negBindingsForVar.isEmpty()) {
				negVarBinding.remove(tmVar);
			}
		}
	}

	public void removeFromPosShadowHistory(InstanceKey binding) {
		assert binding.haveLocalInformation();
		super.removeFromPosShadowHistory(binding);
	}
	
	public void removeFromNegShadowHistory(InstanceKey binding) {
		assert binding.haveLocalInformation();
		super.removeFromNegShadowHistory(binding);
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[pos(");
        sb.append(posVarBinding.toString());
		sb.append(")-neg(");			
        sb.append(negVarBinding.toString());
		sb.append(")-hist(");
		for (Iterator<Shadow> iter = getCurrentHistory().iterator(); iter.hasNext();) {
			Shadow s = iter.next();
			sb.append("s:"+s.getID());
			if(iter.hasNext())
				sb.append(",");
		}
		sb.append(")]");			
		return sb.toString();
	}
	
	protected PreciseSymmetricDisjunct clone() {
		return (PreciseSymmetricDisjunct) super.clone();
	}	
	
	private boolean isHistoryConsistent() {
		Set<InstanceKey> allLocalKeys = new HashSet<InstanceKey>();
		for (Map.Entry<String,Set<InstanceKey>> binding : ((Map<String,Set<InstanceKey>>)posVarBinding).entrySet()) {
			Set<InstanceKey> keys = binding.getValue();
			for (InstanceKey instanceKey : keys) {
				if(instanceKey.haveLocalInformation()) {
					allLocalKeys.add(instanceKey);
				}
			}
		}
		for (Map.Entry<String,Set<InstanceKey>> binding : ((Map<String,Set<InstanceKey>>)negVarBinding).entrySet()) {
			Set<InstanceKey> keys = binding.getValue();
			for (InstanceKey instanceKey : keys) {
				if(instanceKey.haveLocalInformation()) {
					allLocalKeys.add(instanceKey);
				}
			}
		}
		
		Set<InstanceKey> allHistoryKeys = new HashSet<InstanceKey>(posHistory.keySet());
		allHistoryKeys.addAll(negHistory.keySet());
		
		return allHistoryKeys.equals(allLocalKeys);
	}

}
