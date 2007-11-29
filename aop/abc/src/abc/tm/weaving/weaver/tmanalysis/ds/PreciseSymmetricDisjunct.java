/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.PointsToSet;
import soot.SootMethod;
import soot.jimple.toolkits.pointer.InstanceKey;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroup;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroupRegistry;
import abc.tm.weaving.weaver.tmanalysis.query.SymbolShadowWithPTS;
import abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow;

/**
 * A disjuncts making use of must and must-not alias information.
 * It is symmetric because it uses a set of positive and negative bindings with symmetric update rules.
 *
 * @author Eric Bodden
 */
public class PreciseSymmetricDisjunct extends Disjunct<InstanceKey> {
	

    private final SootMethod container;
    private final TraceMatch tm;
    /**
	 * Constructs a new disjunct.
	 */
	public PreciseSymmetricDisjunct(SootMethod container, TraceMatch tm) {
        this.container = container;
        this.tm = tm;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Disjunct addBindingsForSymbol(Collection allVariables, Map bindings, String shadowId, SMNode from) {
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
			clone.addPositiveBinding(tmVar, toBind, shadowId);
			
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
	
	public Set addNegativeBindingsForSymbol(Collection allVariables, Map<String,InstanceKey> bindings, String shadowId, Configuration config) {		
		
		//if there are no variables, there is nothing to do
		if(allVariables.isEmpty()) {
			return Collections.EMPTY_SET;
		}
		
		Set resultSet = new HashSet();
		
		if(bindingIsUnique(bindings,config)) {
			Disjunct disjunct = this;
			//for each tracematch variable, add the negative bindings for that variable
			for (Iterator varIter = allVariables.iterator(); varIter.hasNext();) {
				String varName = (String) varIter.next();

				disjunct = disjunct.addNegativeBindingsForVariable(varName, bindings.get(varName), shadowId);
			}
			resultSet.add(disjunct);
		} else {
			//for each tracematch variable, add the negative bindings for that variable
			for (Iterator varIter = allVariables.iterator(); varIter.hasNext();) {
				String varName = (String) varIter.next();

				resultSet.add( addNegativeBindingsForVariable(varName, bindings.get(varName), shadowId) );
			}			
		}

		
		return resultSet;
	}

	private boolean bindingIsUnique(Map<String, InstanceKey> bindings, Configuration config) {
	    if(!ShadowGroupRegistry.v().hasShadowGroupInfo()) {
	        return false;
	    }
	    
	    if(bindings.size()<2) {
	    	return false;
	    }
	    
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
	    
	
		Set<SymbolShadowWithPTS> overlaps = new HashSet<SymbolShadowWithPTS>();

		Set<ShadowGroup> allShadowGroups = ShadowGroupRegistry.v().getAllShadowGroups();
		for (ShadowGroup shadowGroup : allShadowGroups) {
			if(shadowGroup.getTraceMatch().equals(tm)) {
				boolean hasCompatibleBindings = true;
				for (String tmVar : bindings.keySet()) {
			    	InstanceKey toBind = bindings.get(tmVar);
			    	
				    if(!toBind.haveLocalInformation()) {
						//have no precise information on that value
						return false;
					}			
				    
					PointsToSet toBindPts = toBind.getPointsToSet();
					
					if(!shadowGroup.hasCompatibleBinding(tmVar, toBindPts)) {
						hasCompatibleBindings = false;
						break;
					}
				}
				if(hasCompatibleBindings) {
					overlaps.addAll(shadowGroup.getAllShadows());
				}
			}
		}
		
		//exclude all artificial shadows
		for (Iterator<SymbolShadowWithPTS> shadowIter = overlaps.iterator(); shadowIter.hasNext();) {
		    SymbolShadowWithPTS shadow = (SymbolShadowWithPTS) shadowIter.next();
            if(shadow.isArtificial()) {
                shadowIter.remove();
            }
        }
		
		boolean allShadowsBindingCurrentSymbolInCurrentMethod = true;
		Set<ISymbolShadow> shadowsBindingCurrentSymbolInCurrentMethod = new HashSet<ISymbolShadow>();
		for (String tmVar : bindings.keySet()) {
			Set<String> symbolsThatMayBindCurrentValue = new HashSet<String>();
			TMStateMachine sm = (TMStateMachine) tm.getStateMachine();
			//for all edges in the tm state machine
			for (Iterator<SMEdge> edgeIter = sm.getEdgeIterator(); edgeIter.hasNext();) {
				SMEdge edge = edgeIter.next();
				//get the symbol/label of that edge and the variables the edge binds
				String edgeSymbol = edge.getLabel();
				assert edgeSymbol != null; //should have no epsilon edges at this point
				List<String> varsBoundByEdge = tm.getVariableOrder(edgeSymbol);
				//if the edge may freshly bind the variable we care about...
				if(varsBoundByEdge.contains(tmVar) &&
				   !edge.getSource().boundVars.contains(tmVar)) {
					//add to the set
					symbolsThatMayBindCurrentValue.add(edgeSymbol);
				}
			}			
			
			for (ISymbolShadow shadow : overlaps) {
				if(symbolsThatMayBindCurrentValue.contains(shadow.getSymbolName())) {					
					//if shadow from different method
					if(!shadow.getContainer().equals(container)) {
						//if shadow has symbol that may bind the value we care about
						allShadowsBindingCurrentSymbolInCurrentMethod = false;
						break;
					} else if(symbolsThatMayBindCurrentValue.contains(shadow.getSymbolName())) {
						shadowsBindingCurrentSymbolInCurrentMethod.add(shadow);
					}
				}
			}
		}
		
		if(!allShadowsBindingCurrentSymbolInCurrentMethod) {
			return false;
		}

		Map<String,Set<InstanceKey>> tmVarToPossibleInstanceKeys = new HashMap<String, Set<InstanceKey>>();
		for (ISymbolShadow shadow : shadowsBindingCurrentSymbolInCurrentMethod) {
			final Map<String,InstanceKey> varToInstanceKey = flowAnalysis.reMap(shadow.getTmFormalToAdviceLocal());
			for (String tmVar : bindings.keySet()) {
				InstanceKey keyAtShadow = varToInstanceKey.get(tmVar);
				Set<InstanceKey> keysForVar = tmVarToPossibleInstanceKeys.get(tmVar);
				if(keysForVar==null) {
					keysForVar = new HashSet<InstanceKey>();
					tmVarToPossibleInstanceKeys.put(tmVar, keysForVar);					
				}
				keysForVar.add(keyAtShadow);
			}
		}
		
		boolean bindingUnique = true;
		for (String tmVar : bindings.keySet()) {
			if(tmVarToPossibleInstanceKeys.get(tmVar).size()!=1) {
				bindingUnique = false;
			}			
		}
		
		return bindingUnique;
	}

	
	
	/**
	 * {@inheritDoc}
	 */
	@Override      
	protected Disjunct addNegativeBindingsForVariable(String tmVar, InstanceKey toBind, String shadowId) {
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
		 * inter-procedural must-alias information), neither do we need it for looking up which shadows contribute to
		 * reaching a final state.
		 */
	    if(toBind.haveLocalInformation()) {
	    	PreciseSymmetricDisjunct newDisjunct = (PreciseSymmetricDisjunct) addNegativeBinding(tmVar, toBind, shadowId);
	    	assert newDisjunct.isHistoryConsistent();
			return newDisjunct;
	    } else {
	    	//do not need to store negative bindings from other methods
	    	return this;
	    }
	}

	protected Disjunct addNegativeBinding(String tmVar, InstanceKey negBinding, String shadowId) {	
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
		clone.registerShadowIdInNegHistory(negBinding, shadowId);
		return clone;
	}
	
	protected void addPositiveBinding(String tmVar, InstanceKey toBind, String shadowId) {
		Set<InstanceKey> posBindingForVar = posVarBinding.get(tmVar);
		if(posBindingForVar==null){
			posBindingForVar = new HashSet<InstanceKey>();
			posVarBinding.put(tmVar, posBindingForVar);
		}
		posBindingForVar.add(toBind);
		if(toBind.haveLocalInformation())
			registerShadowIdInPosHistory(toBind,shadowId);
	}
	
	
	protected void registerShadowIdInPosHistory(InstanceKey toBind, String shadowId) {
		assert toBind.haveLocalInformation();
		posHistory.put(toBind,shadowId);
	}

	protected void registerShadowIdInNegHistory(InstanceKey toBind, String shadowId) {
		assert toBind.haveLocalInformation();
		negHistory.put(toBind,shadowId);
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
        sb.append(getCurrentHistory());
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
