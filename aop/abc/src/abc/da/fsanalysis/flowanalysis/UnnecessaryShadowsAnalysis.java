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
package abc.da.fsanalysis.flowanalysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soot.jimple.Stmt;
import soot.jimple.toolkits.pointer.InstanceKey;
import abc.da.fsanalysis.flowanalysis.ds.Configuration;
import abc.da.fsanalysis.flowanalysis.ds.Disjunct;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.tm.weaving.matching.SMNode;

/**
 * This analysis attempts to find an unnecessary shadow. A shadow is deemed unnecessary when it is guaranteed to transition
 * in a way that we move to a state from which we reach a final state if and only if we had reached the final state from
 * the transition's source state. 
 */
public class UnnecessaryShadowsAnalysis {


	protected final ReachingStatesAnalysis forwardAnalysis;
	protected final ReachingStatesAnalysis backwardsAnalysis;
	protected final Shadow unnecessaryShadow;
	protected final AnalysisJob job;

	public UnnecessaryShadowsAnalysis(AnalysisJob job, ReachingStatesAnalysis forwardAnalysis, ReachingStatesAnalysis backwardAnalysis) {
		this.job = job;
		this.forwardAnalysis = forwardAnalysis;
		this.backwardsAnalysis = backwardAnalysis;
		
		nextShadow:
		for (Shadow shadow : job.allEnabledTMShadowsInMethod()) {
			Stmt stmt = shadow.getAdviceBodyInvokeStmt();
			ConfigurationSet beforeFlow = forwardAnalysis.getFlowBefore(stmt);
			Set<Configuration> configsBefore = new HashSet<Configuration>(beforeFlow.getConfigurations());
			
			Map<String, Set<InstanceKey>> shadowBinding = job.shadowBindings(shadow);
			Disjunct shadowDisjunct = Disjunct.TRUE.addBindingsForSymbol(shadowBinding);
						
			//for every configuration that reaches the shadow's statement...
			for (Configuration configuration : configsBefore) {
				//if this configuration is relevant...
				if(configuration.getBinding().isCompatibleTo(shadowDisjunct)) {
					Set<Configuration> splitByState = splitByState(configuration);
					//for every sub-configuration of this configuration containing only a single state
					for(Configuration config: splitByState) {
						SMNode stateBeforeTransition = config.getStates().iterator().next();
						assert config.getStates().size()==1; //configuration was split, so there should be just one state!
						//compute the resulting configurations
						Set<Configuration> resultingConfigs = config.doTransition(shadow);
						//and the states that we can reach through the transition
						Set<SMNode> statesAfterTransition = new HashSet<SMNode>();
						for (Configuration resultingConfig : resultingConfigs) {
							if(resultingConfig.getBinding().isCompatibleTo(shadowDisjunct)) {
								statesAfterTransition.addAll(resultingConfig.getStates());
							}
						}
						//the shadow is necessary if it reaches a final state;
						//hence continue by investigating the next shadow
						if(hasFinalState(statesAfterTransition)) {
							//shadow is necessary
							continue nextShadow;
						}
						//next we compute the set of sets of states that are live after the shadow, i.e. from which
						//a final state can be reached; we use state numbers here, because the states
						//come from the backwards analyses, i.e. belong to a different state machine than the
						//one being used in forward analysis
						Set<Set<Integer>> liveStatesSetsAfterShadow = liveStatesSetsAfterShadow(shadow,shadowDisjunct);
						//for each such set...
						for (Set<Integer> liveSet : liveStatesSetsAfterShadow) {
							//if the set does not contain the source state, then we cannot reach the final state
							//without executing the shadow
							if(!liveSet.contains(stateBeforeTransition.getNumber())) {
								//if one of the target states is in the set, then we can reach the final state, assuming that the transition
								//*does* execute; hence, in this case the shadow is necessary
								for (SMNode stateAfterTransition : statesAfterTransition) {
									if(liveSet.contains(stateAfterTransition.getNumber())) {
										//shadow is necessary
										continue nextShadow;
									}
								}
							} else {
								//else if the set does contain the source state, then conversely, we need to keep the shadow
								//when there is a target state that is *not* in the set
								for (SMNode stateAfterTransition : statesAfterTransition) {
									if(!liveSet.contains(stateAfterTransition.getNumber())) {
										//shadow is necessary
										continue nextShadow;
									}
								}								
								//the following branch handles the special case where we move to "the empty set"; because we have no dedicated
								//state modelling this set, we have to take extra care here
								if(statesAfterTransition.isEmpty()) {
									//shadow is necessary
									continue nextShadow;
								}
							}
						}						
					}
				}
			}
			//if we got here then the shadow is unnecessary
			this.unnecessaryShadow = shadow;
			return;
		}
		this.unnecessaryShadow = null;		
	}

	private Set<Set<Integer>> liveStatesSetsAfterShadow(Shadow shadow, Disjunct shadowDisjunct) {
		Stmt stmt = shadow.getAdviceBodyInvokeStmt();
		ConfigurationSet dualInfo = backwardsAnalysis.getFlowBefore(stmt);
		Set<Set<Integer>> res = new HashSet<Set<Integer>>();
		for (Configuration config : dualInfo.getConfigurations()) {
			if(config.getBinding().isCompatibleTo(shadowDisjunct)) {
				Set<Integer> stateSet = new HashSet<Integer>();
				for(SMNode node: config.getStates()) {
					stateSet.add(node.getNumber());
				}
				res.add(stateSet);
			}			
		}
		return res;
	}

	private boolean hasFinalState(Set<SMNode> statesAfterTransition) {
		for (SMNode node : statesAfterTransition) {
			if(node.isFinalNode()) {
				return true;
			}
		}
		return false;
	}

	private Set<Configuration> splitByState(Configuration config) {
		if(config.getStates().size()==1) {
			return Collections.singleton(config);
		}
		Set<Configuration> split = new HashSet<Configuration>();
		for (SMNode state : config.getStates()) {
			split.add(new Configuration(forwardAnalysis, Collections.singleton(state), config.getBinding(), null));
		}	
		return split;
	}
	
	public Shadow getUnnecessaryShadow() {
		return unnecessaryShadow;
	}
	
	public boolean foundUnnnecessaryShadow() {
		return unnecessaryShadow!=null;
	}
	
	public Set<Integer> statesBeforeTransition(Shadow shadow) {
		Stmt stmt = shadow.getAdviceBodyInvokeStmt();
		ConfigurationSet beforeFlow = forwardAnalysis.getFlowBefore(stmt);
		Set<Configuration> configsBefore = new HashSet<Configuration>(beforeFlow.getConfigurations());
		
		Map<String, Set<InstanceKey>> shadowBinding = job.shadowBindings(shadow);
		Disjunct shadowDisjunct = Disjunct.TRUE.addBindingsForSymbol(shadowBinding);

		Set<Integer> states = new HashSet<Integer>();
		for (Configuration config : configsBefore) {
			if(config.getBinding().isCompatibleTo(shadowDisjunct)) {
				for (SMNode node : config.getStates()) {
					states.add(node.getNumber());
				}
			}
		}
		return states;
	}
	
	public Set<Set<Integer>> liveStatesSetsAfterTransition(Shadow shadow) {
		Stmt stmt = shadow.getAdviceBodyInvokeStmt();
		Map<String, Set<InstanceKey>> shadowBinding = job.shadowBindings(shadow);
		Disjunct shadowDisjunct = Disjunct.TRUE.addBindingsForSymbol(shadowBinding);

		ConfigurationSet dualInfo = backwardsAnalysis.getFlowBefore(stmt);
		Set<Set<Integer>> res = new HashSet<Set<Integer>>();
		for (Configuration config : dualInfo.getConfigurations()) {
			if(config.getBinding().isCompatibleTo(shadowDisjunct)) {
				Set<Integer> stateSet = new HashSet<Integer>();
				for(SMNode node: config.getStates()) {
					stateSet.add(node.getNumber());
				}
				res.add(stateSet);
			}			
		}
		return res;
	}
	
	public Map<Integer,Set<Integer>> transitions(Shadow shadow) {
		Stmt stmt = shadow.getAdviceBodyInvokeStmt();
		ConfigurationSet beforeFlow = forwardAnalysis.getFlowBefore(stmt);
		Set<Configuration> configsBefore = new HashSet<Configuration>(beforeFlow.getConfigurations());
		
		Map<String, Set<InstanceKey>> shadowBinding = job.shadowBindings(shadow);
		Disjunct shadowDisjunct = Disjunct.TRUE.addBindingsForSymbol(shadowBinding);
		
		Map<Integer,Set<Integer>> res = new HashMap<Integer, Set<Integer>>();

		for (Configuration configuration : configsBefore) {
			Set<Configuration> splitByState = splitByState(configuration);
			for(Configuration config: splitByState) {
				if(config.getBinding().isCompatibleTo(shadowDisjunct)) {
					SMNode stateBeforeTransition = config.getStates().iterator().next();
					assert config.getStates().size()==1; //configuration was split, after all
					Set<Configuration> resultingConfigs = config.doTransition(shadow);
					Set<Integer> statesAfterTransition = new HashSet<Integer>();
					for (Configuration resultingConfig : resultingConfigs) {
						if(resultingConfig.getBinding().isCompatibleTo(shadowDisjunct)) {
							for(SMNode node: resultingConfig.getStates()) {
								statesAfterTransition.add(node.getNumber());
							}
						}
					}
					res.put(stateBeforeTransition.getNumber(), statesAfterTransition);
				}
			}
		}
		return res;
	}
}
