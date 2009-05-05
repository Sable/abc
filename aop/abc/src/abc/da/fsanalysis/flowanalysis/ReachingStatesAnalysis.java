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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.Unit;
import soot.jimple.Stmt;
import abc.da.fsanalysis.flowanalysis.ds.Configuration;
import abc.da.fsanalysis.flowanalysis.ds.Disjunct;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.tm.weaving.matching.SMNode;

/**
 * Computes for every statement the states that could be reached by the control flow leading up to this statement.
 * If reversed, it computes for every statement the set of states from which a final state could be reached
 * given the remaining control flow.
 */
public class ReachingStatesAnalysis extends WorklistBasedAnalysis {

	public ReachingStatesAnalysis(AnalysisJob job, boolean isForward) {
		super(job,isForward);
	}

	@Override
	protected Set<Job<Unit, ConfigurationSet>> initialJobs() {
		Set<Configuration> initialConfigs = new HashSet<Configuration>();;
		
		
		//first we add for every entry unit the set of configurations that transitively
		//reach this unit in at least one step
		Set<SMNode> initialStates = tracePattern().getStateMachine().getInitialStates();
		for (SMNode node : initialStates) {
			initialConfigs.add(new Configuration(this,Collections.singleton(node),Disjunct.TRUE,null));
		}
		Set<Job<Unit, ConfigurationSet>> jobs = new HashSet<Job<Unit, ConfigurationSet>>();
		
		if(isForward()) {
			Set<Configuration> configs = new HashSet<Configuration>();
			for (Configuration initialConfig : initialConfigs) {
				configs.addAll(configsInterprocedurallyReachedFromConfigThroughRestOfProgram(initialConfig));
				configs.add(initialConfig);
			}
			for(Unit entryUnit : unitGraph().getHeads()) {
				jobs.add(new WorklistAnalysis.Job<Unit, ConfigurationSet>(entryUnit,new ConfigurationSet(this,configs)));			
			}
		} else {
			//in backward mode, we also add an initial configuration just behind every unit that may lead into a final state;
			//this takes care of the semantics of dependency state machines: we execute the body after each matched *prefix*
			for(Unit unit : unitGraph()) {
				Set<Shadow> shadows = getJob().enabledShadowsOfStmt((Stmt) unit);
				if(!shadows.isEmpty()) {
					boolean isFinalTransition = false;
					for (Shadow shadow : shadows) {
						if(tracePattern().getInitialSymbols().contains(getJob().symbolNameForShadow(shadow))) {
							isFinalTransition = true;
							break;
						}
					}
					if(isFinalTransition) {
						jobs.add(new WorklistAnalysis.Job<Unit, ConfigurationSet>(unit,new ConfigurationSet(this,initialConfigs)));			
					}
				}
			}
			
			Set<Configuration> configs = new HashSet<Configuration>();
			//the following call actually performs a fixed-point iteration
			for (Configuration initialConfig : initialConfigs) {
				configs.addAll(configsInterprocedurallyReachedFromConfigThroughRestOfProgram(initialConfig));
			}
			for(Unit entryUnit : getJob().getTails()) {
				jobs.add(new WorklistAnalysis.Job<Unit, ConfigurationSet>(entryUnit,new ConfigurationSet(this,configs)));			
			}
			for(Unit entryUnit : getJob().getCallStmts()) {
				jobs.add(new WorklistAnalysis.Job<Unit, ConfigurationSet>(entryUnit,new ConfigurationSet(this,configs)));			
			}
		}	
		return jobs; 
	}
	
	protected Set<Configuration> configsInterprocedurallyReachedFromConfigThroughRestOfProgram(Configuration config) {
		return configsInterprocedurallyReachedFromConfigThroughRestOfProgram(config,null); 
	}
	
	/**
	 *  Computes the set of configurations that are inter-procedurally reached from the configurations in curr through the rest of the program.
	 */
	protected Set<Configuration> configsInterprocedurallyReachedFromConfigThroughRestOfProgram(Configuration config, Stmt s) {
		Set<Shadow> shadows;
		//if possible, refine the set of relevant shadows to only these that are transitively called
		if(s!=null && getJob().getCallStmts().contains(s)) {
			shadows = new HashSet<Shadow>(getJob().transitivelyCalledOverlappingShadowsFromOtherMethods(s));
		} else {
			shadows = new HashSet<Shadow>(getJob().enabledOverlappingSymbolShadowsFromOtherMethods());
		}
		
		Set<Configuration> reacheable = new HashSet<Configuration>(); 
		Set<Configuration> worklist = new HashSet<Configuration>(Collections.singleton(config));
		while(!worklist.isEmpty()) {
			Iterator<Configuration> iter = worklist.iterator();
			Configuration curr = iter.next(); iter.remove(); //pop element
			Set<Configuration> newSet = new HashSet<Configuration>();
			newSet.addAll(configsInterprocedurallyReachedFrom(curr, shadows));
			for (Configuration newState : newSet) {
				if(!reacheable.contains(newState)) {
					reacheable.add(newState);
					worklist.add(newState);
				}
			}
		}		
		
		return reacheable;
	}

	@Override
	protected void considerOutGoingCalls(Set<Configuration> configs, Stmt s) {
		Set<Unit> succs = externalSuccsOf(s);
		if(!succs.isEmpty()) {
			Set<Configuration> reachingConfigs = new HashSet<Configuration>();
			for(Configuration config: configs) {
				reachingConfigs.addAll(configsInterprocedurallyReachedFromConfigThroughRestOfProgram(config, s));
				reachingConfigs.add(config);
			}
			for (Unit u : succs) {
				worklist.add(new Job<Unit, ConfigurationSet>(u,new ConfigurationSet(this,reachingConfigs)));
			}
		}
	}
	
	protected Set<Unit> externalSuccsOf(Unit u) {
		Set<Unit> res = new HashSet<Unit>();
		Set<Unit> callStmts = getJob().getCallStmts();
		Set<Unit> recursiveCallStmts = getJob().getRecursiveCallStmts();
		Collection<Unit> heads = getHeads();
		Collection<Unit> tails = getTails();

		if(callStmts.contains(u)) {
			//instead of u's successors we here add u itself; this is ok because u
			//cannot have any shadow
			res.add(u);
		} 
		if(recursiveCallStmts.contains(u)) {
			res.addAll(heads);
		} 
		if(tails.contains(u)) {
			res.addAll(recursiveCallStmts);
			res.addAll(heads);
		}
		
		return res;
	}
}
