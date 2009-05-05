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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.pointer.InstanceKey;
import soot.toolkits.graph.DirectedGraph;
import abc.da.fsanalysis.flowanalysis.ds.Configuration;
import abc.da.fsanalysis.flowanalysis.ds.Disjunct;
import abc.da.fsanalysis.util.SymbolNames;
import abc.da.weaving.aspectinfo.TracePattern;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.main.Debug;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;

/**
 * A worklist algorithm for the flow-sensitive abstract interpretation of TracePatterns.
 * The algorithm exhaustively generates all possible automaton configurations at all statements
 * in a given method body. It is parameterized by an {@link AnalysisJob}.
 */
public abstract class WorklistBasedAnalysis extends WorklistAnalysis<Unit, ConfigurationSet> implements TMFlowAnalysis {

	/**
	 * The analysis job that parameterizes this analysis.
	 */
	protected final AnalysisJob job;
	
	protected final Set<Shadow> necessaryShadows = new HashSet<Shadow>();

	protected final boolean forward;
	
	/**
	 * Creates a new analysis.
	 * @param job the analysis job to process
	 * @param maxJobCount the maximal numbers of jobs to process from the worklist
	 */
	public WorklistBasedAnalysis(AnalysisJob job, boolean forward) {
		super(extractUnitGraph(job,forward));
		this.job = job;
		this.forward = forward;
		
		Disjunct falseDisjunct = new Disjunct(job,this) {
			@Override
			public String toString() {
				return "<NONE>";
			}
			
			@Override
			public Disjunct addBindingsForSymbol(Map<String, ? extends Set<InstanceKey>> bindings) {
				return this;//avoid cloning here so that we can compare to FALSE by identity
			}
			
			@Override
			protected Disjunct addNegativeBindingsForVariable(String tmVar,
					Set<InstanceKey> toBinds) {
				return this;//avoid cloning here so that we can compare to FALSE by identity
			}
			
			@Override
			protected Disjunct clone() {
				//should never clone FALSE so that we can compare to FALSE by identity
				throw new UnsupportedOperationException();
			}
		};

		Disjunct trueDisjunct = new Disjunct(job,this);

		Disjunct.FALSE = falseDisjunct;
		Disjunct.TRUE = trueDisjunct;
	}
	
	@Override
	protected void doAnalysis() {
		if(Debug.v().debugDA) {
			Configuration.counter = 0;
		}
		super.doAnalysis();
		if(Debug.v().debugDA) {
			System.err.println("Configuration counter for "+getJob().method()+": "+Configuration.counter);
		}
	}


	/**
	 * @inheritDoc
	 */
	public AnalysisJob getJob() {
		return job;
	}

	public void registerNecessaryShadow(Shadow interdependentShadows) {
		necessaryShadows.add(interdependentShadows);
	}
	
	public Set<Shadow> getNecessaryShadows() {		
		return Collections.unmodifiableSet(necessaryShadows);
	}	
	
	@Override
	protected void checkInitialJobs(Set<WorklistAnalysis.Job<Unit, ConfigurationSet>> initialJobs) {
		for (Job<Unit, ConfigurationSet> job : initialJobs) {
			Set<Configuration> configurations = job.getAbstraction().getConfigurations();
			for (Configuration configuration : configurations) {
				Set<SMNode> states = configuration.getStates();
				for (SMNode state : states) {
					if(!tracePattern().getStateMachine().getStates().contains(state)) {
						throw new InternalError("State not member of state machine!");
					}
				}
			}
		}
	}
	
	@Override
	protected ConfigurationSet initialAbstraction() {
		return new ConfigurationSet(this,new HashSet<Configuration>());
	}
	
	public TracePattern tracePattern() {
		if(forward)
			return job.tracePattern();
		else
			return job.invertedTracePattern();
	}

	public DirectedGraph<Unit> unitGraph() {
		if(forward)
			return job.unitGraph();
		else
			return job.invertedUnitGraph();
	}

	private static DirectedGraph<Unit> extractUnitGraph(AnalysisJob job, boolean forward) {
		if(forward)
			return job.unitGraph();
		else
			return job.invertedUnitGraph();
	}


	public ConfigurationSet transition(ConfigurationSet abstraction, Stmt stmt) {
		
		Set<Configuration> configurations = abstraction.getConfigurations();
		
		Set<Configuration> newConfigs = new HashSet<Configuration>();
		Set<Shadow> shadows = getJob().enabledShadowsOfStmt(stmt);

		if(shadows.isEmpty()) {
			//for statements without shadows, just copy everything over
			newConfigs.addAll(configurations);
		} else {		
			//otherwise compute the transition
			for(Shadow shadow: shadows) {
				for (Configuration configuration : configurations) {
					newConfigs.addAll(configuration.doTransition(shadow));
				}
			}
		}

		//consider outgoing calls
		considerOutGoingCalls(newConfigs, stmt);
		
		return new ConfigurationSet(this,newConfigs);
	}
	
	public boolean isForward() {
		return forward;
	}
	
	public Collection<Unit> getHeads() {
		if(forward)
			return getJob().getHeads();
		else
			return getJob().getTails();
	}

	public Collection<Unit> getTails() {
		if(forward)
			return getJob().getTails();
		else
			return getJob().getHeads();
	}


	/**
	 *  Computes the set of configurations that are inter-procedurally reached from the configurations in curr through the given shadows.
	 */	
	protected Set<Configuration> configsInterprocedurallyReachedFrom(Configuration curr, Set<Shadow> shadows) {
		Set<Configuration> configs = new HashSet<Configuration>();
		
		Set<SMNode> states = curr.getStates();		
		Map<String,Set<SMNode>> targetStatesForSymbol = new HashMap<String,Set<SMNode>>();
		for (SMNode state : states) {
			for (@SuppressWarnings("unchecked")
				Iterator<SMEdge> transIter = state.getOutEdgeIterator(); transIter.hasNext();) {
				SMEdge outEdge = transIter.next();
	
				//if the labels coincide
				if(!outEdge.isSkipEdge()) {				
					String symbol = outEdge.getLabel();
					Set<SMNode> targetStates = targetStatesForSymbol.get(symbol);
					if(targetStates==null) {
						targetStates = new HashSet<SMNode>();
						targetStatesForSymbol.put(symbol, targetStates);
					}
					targetStates.add(outEdge.getTarget());
				}
			}
		}
		
		for (Entry<String,Set<SMNode>> entry : targetStatesForSymbol.entrySet()) {
			String symbol = entry.getKey();
			
			boolean foundSymbol = false;
			for(Shadow shadow: shadows) {
				if(symbol.equals(SymbolNames.v().symbolNameForShadow(shadow))) {
					Map<String, Set<InstanceKey>> bindings = getJob().shadowBindings(shadow);
					Disjunct disj = Disjunct.TRUE.addBindingsForSymbol(bindings);
					if(disj.isCompatibleTo(curr.getBinding())) {
						foundSymbol = true;
						break;
					}
				}
			}
			if(foundSymbol) {
				SMNode sourceState = curr.hasSourceState() ? curr.getSourceState() : null;
				Set<SMNode> targetStates = entry.getValue();
				configs.add(new Configuration(this,targetStates,curr.getBinding(),sourceState));
			}
		}
			
		return configs;
	}


	protected abstract void considerOutGoingCalls(Set<Configuration> newConfigs, Stmt s);

}
