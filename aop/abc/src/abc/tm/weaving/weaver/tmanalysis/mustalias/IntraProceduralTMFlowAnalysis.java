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
package abc.tm.weaving.weaver.tmanalysis.mustalias;

import static abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status.ABORTED_HIT_FINAL;
import static abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status.ABORTED_MAX_NUM_CONFIGS;
import static abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status.ABORTED_MAX_NUM_ITERATIONS;
import static abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status.ABORTED_MAX_SIZE_CONFIG;
import static abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status.FINISHED;
import static abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status.FINISHED_HIT_FINAL;
import static abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status.RUNNING;
import static abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status.RUNNING_HIT_FINAL;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.RefLikeType;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.pointer.InstanceKey;
import soot.jimple.toolkits.pointer.LocalMustNotAliasAnalysis;
import soot.jimple.toolkits.pointer.StrongLocalMustAliasAnalysis;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.ShadowUtils;
import abc.tm.weaving.weaver.tmanalysis.Statistics;
import abc.tm.weaving.weaver.tmanalysis.ds.Configuration;
import abc.tm.weaving.weaver.tmanalysis.ds.Constraint;
import abc.tm.weaving.weaver.tmanalysis.ds.Disjunct;
import abc.tm.weaving.weaver.tmanalysis.ds.FinalConfigsUnitGraph;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroupRegistry;
import abc.tm.weaving.weaver.tmanalysis.stages.CallGraphAbstraction;
import abc.tm.weaving.weaver.tmanalysis.stages.FlowInsensitiveAnalysis;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowTag;
import abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow;

public class IntraProceduralTMFlowAnalysis extends ForwardFlowAnalysis<Unit,IntraProceduralTMFlowAnalysis.Abstraction> implements TMFlowAnalysis {

	public static class Abstraction {

		public Set<Configuration> configurations;
		
		public Set<InstanceKey> valuesInInitialState;

		protected Abstraction(Set<Configuration> configs,Set<InstanceKey> valuesInInitialState) {
			this.configurations = configs;
			this.valuesInInitialState = valuesInInitialState;
		}		
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
			* result
			+ ((configurations == null) ? 0 : configurations.hashCode());
			result = prime
			* result
			+ ((valuesInInitialState == null) ? 0
					: valuesInInitialState.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Abstraction other = (Abstraction) obj;
			if (configurations == null) {
				if (other.configurations != null)
					return false;
			} else if (!configurations.equals(other.configurations))
				return false;
			if (valuesInInitialState == null) {
				if (other.valuesInInitialState != null)
					return false;
			} else if (!valuesInInitialState.equals(other.valuesInInitialState))
				return false;
			return true;
		}
	}
	
	
	/**
     * AbortedException
     *
     * @author Eric Bodden
     */
    public static class AbortedException extends RuntimeException { }

    /**
	 * Status
	 *
	 * @author Eric Bodden
	 */
	public enum Status {
		RUNNING{
			public boolean isAborted() { return false; }
			public boolean isFinishedSuccessfully() { return false; }
            public boolean hitFinal() { return false; }
			public String toString() { return "running"; }
	        public void countForStatistics() {
	            throw new IllegalStateException("only to be called after analysis finished");
	        }
		},
        RUNNING_HIT_FINAL{
            public boolean isAborted() { return false; }
            public boolean isFinishedSuccessfully() { return false; }
            public boolean hitFinal() { return true; }
            public String toString() { return "running, hit final state"; }
            public void countForStatistics() {
                throw new IllegalStateException("only to be called after analysis finished");
            }
        },
        ABORTED_HIT_FINAL{
            public boolean isAborted() { return true; }
            public boolean isFinishedSuccessfully() { return false; }
            public boolean hitFinal() { return true; }
            public String toString() { return "aborted, hit final state"; }
            public void countForStatistics() {
                Statistics.v().statusAbortedHitFinal++;
            }
        },
        ABORTED_MAX_NUM_ITERATIONS{
            public boolean isAborted() { return true; }
            public boolean isFinishedSuccessfully() { return false; }
            public boolean hitFinal() { return false; }
            public String toString() { return "aborted, exceeded maximal number of iterations ("+MAX_NUM_VISITED+")"; }
            public void countForStatistics() {
                Statistics.v().statusAbortedMaxNumIterations++;
            }
        },
        ABORTED_MAX_NUM_CONFIGS{
            public boolean isAborted() { return true; }
            public boolean isFinishedSuccessfully() { return false; }
            public boolean hitFinal() { return false; }
            public String toString() { return "aborted, exceeded maximal number of configurations ("+MAX_NUM_CONFIGS+")"; }
            public void countForStatistics() {
                Statistics.v().statusAbortedMaxNumConfigs++;
            }
        },
        ABORTED_MAX_SIZE_CONFIG{
            public boolean isAborted() { return true; }
            public boolean isFinishedSuccessfully() { return false; }
            public boolean hitFinal() { return false; }
            public String toString() { return "aborted, exceeded maximal size of configurations ("+MAX_SIZE_CONFIG+")"; }
            public void countForStatistics() {
                Statistics.v().statusAbortedMaxSizeConfig++;
            }
        },
        ABORTED_HIT_FINAL_OUTSIDE_METHOD {
            public boolean isAborted() { return true; }
            public boolean isFinishedSuccessfully() { return false; }
            public boolean hitFinal() { return true; }
            public String toString() { return "aborted, hit final state on synthetic final unit"; }
            public void countForStatistics() {
                Statistics.v().statusAbortedHitFinalOnSyntheticUnit++;
            }
        },
		FINISHED {
			public boolean isAborted() { return false; }
			public boolean isFinishedSuccessfully() { return true; }
            public boolean hitFinal() { return false; }
			public String toString() { return "finished"; }
            public void countForStatistics() {
                Statistics.v().statusFinished++;
            }
		},
        FINISHED_HIT_FINAL {
            public boolean isAborted() { return false; }
            public boolean isFinishedSuccessfully() { return true; }
            public boolean hitFinal() { return true; }
            public String toString() { return "finished, hit final state"; }
            public void countForStatistics() {
                Statistics.v().statusFinishedHitFinal++;
            }
        };
		public abstract boolean isAborted(); 
		public abstract boolean isFinishedSuccessfully();
        public abstract boolean hitFinal();
        public abstract void countForStatistics();
	}

	/**
	 * The state machine to interpret.
	 */
	protected final TMStateMachine stateMachine;	
	
	protected final TraceMatch tracematch;	
	
	protected final DirectedGraph<Unit> ug;

	protected final Set<Stmt> visited;
	
	protected final Map<Stmt,Set<Configuration>> stmtToFirstAfterFlowConfig;

	protected final CallGraph abstractedCallGraph;
	
	protected final Set<State> additionalInitialStates;

	protected final Collection<Stmt> stmtsToAnalyze;

    protected final Collection<String> overlappingShadowIDs;
    
    protected final StrongLocalMustAliasAnalysis lmaa;

    protected final LocalMustNotAliasAnalysis lmna;

    protected final SootMethod container;

    protected final Map<Local, Stmt> tmLocalDefs;

    protected final boolean abortWhenHittingFinal;
    
    protected final Map<Stmt,Integer> numberVisited;
    
    protected final static int MAX_NUM_VISITED = 20;

    protected final static int MAX_NUM_CONFIGS = 1000;

    protected static int MAX_SIZE_CONFIG;

    protected Status status;
    
    protected transient int maxNumVisitedInThisRun = 0;

    protected transient int maxNumConfigsInThisRun = 0;
    
    protected transient int maxSizeConfigInThisRun = 0;

	protected final Set<String> initialSymbols;

	protected final Set<InstanceKey> allReferencedInstanceKeys;

	/**
	 * Performs the tracematch-state flow analysis on the given tracematch and unitgraph.
	 * @param initialDisjunct Initial disjunct (functionally copied everywhere)
	 * @param stmtsToAnalyze Statements to consider for this analysis.
	 * @param patchingChain 
	 */
	public IntraProceduralTMFlowAnalysis(TraceMatch tm, DirectedGraph<Unit> ug, SootMethod container, Map<Local, Stmt> tmLocalDefs, Disjunct initialDisjunct, Set<State> additionalInitialStates, Collection<Stmt> stmtsToAnalyze, StrongLocalMustAliasAnalysis lmaa, LocalMustNotAliasAnalysis lmna, boolean abortWhenHittingFinal) {
		super(ug);
        this.container = container;
        this.tmLocalDefs = tmLocalDefs;
        this.lmaa = lmaa;
        this.lmna = lmna;
        this.abortWhenHittingFinal = abortWhenHittingFinal;
		this.stmtsToAnalyze = new HashSet(stmtsToAnalyze);
		MAX_SIZE_CONFIG = tm.getStateMachine().getNumberOfStates() * 50; //50 disjuncts per state should be enough
		
		initialDisjunct.setFlowAnalysis(this);
		Constraint.initialize(initialDisjunct);
		
		this.ug = ug;
		this.additionalInitialStates = additionalInitialStates;

		//since we are iterating over state machine edges, which implement equals(..)
		//we need to separate those edges by using identity hash maps
		this.filterUnitToAfterFlow = new IdentityHashMap();
		this.filterUnitToBeforeFlow = new IdentityHashMap();
		this.unitToAfterFlow = new IdentityHashMap();
		this.unitToBeforeFlow = new IdentityHashMap();
		
		this.stateMachine = (TMStateMachine) tm.getStateMachine();
		this.tracematch = tm;
		this.initialSymbols = tm.getInitialSymbols();

		this.visited = new HashSet<Stmt>();
		this.stmtToFirstAfterFlowConfig = new HashMap<Stmt,Set<Configuration>>();
		
		this.abstractedCallGraph = CallGraphAbstraction.v().abstractedCallGraph();

		//see which shadow groups are present in the code we look at;
        //also initialize invariantStatements to the set of all statements with a shadow
		Set<ISymbolShadow> allShadowsForTM = ShadowUtils.getAllActiveShadows(tm, stmtsToAnalyze);
        
        if(ShadowGroupRegistry.v().hasShadowGroupInfo()) {
            //store all IDs of shadows in those groups
            this.overlappingShadowIDs = ShadowUtils.sameShadowGroup(allShadowsForTM);
        } else {
            this.overlappingShadowIDs = null;
        }
        
		Set<ISymbolShadow> allActiveShadows = ShadowUtils.getAllActiveShadows(tracematch, stmtsToAnalyze);
		Set<InstanceKey> allReferencedInstanceKeysTemp = new HashSet<InstanceKey>();
		for (ISymbolShadow shadow : allActiveShadows) {
			Collection<InstanceKey> instanceKeysReferencedByShadow = reMap(shadow.getTmFormalToAdviceLocal()).values();
			allReferencedInstanceKeysTemp.addAll(instanceKeysReferencedByShadow);
		}
		allReferencedInstanceKeys = Collections.unmodifiableSet(allReferencedInstanceKeysTemp);
        
        this.numberVisited = new HashMap<Stmt,Integer>();
        
		//do the analysis
        Statistics.v().statusStarted++;
		this.status = RUNNING;		
		try {
		    doAnalysis();
		} catch(AbortedException e) {
		    //ignore
		} finally {
			//clear caches
			Disjunct.reset();
			Constraint.reset();
			Configuration.reset();
		}
		if(!this.status.isAborted()) {
	        Statistics.v().maxNumVisitedOnSuccessfulRun = Math.max(Statistics.v().maxNumVisitedOnSuccessfulRun, maxNumVisitedInThisRun); 
            Statistics.v().maxNumConfigsOnSuccessfulRun = Math.max(Statistics.v().maxNumConfigsOnSuccessfulRun, maxNumConfigsInThisRun); 
            Statistics.v().maxSizeConfigOnSuccessfulRun = Math.max(Statistics.v().maxSizeConfigOnSuccessfulRun, maxSizeConfigInThisRun); 
            if(this.status.hitFinal()) {
                this.status = FINISHED_HIT_FINAL;
            } else {
                this.status = FINISHED;
            }
        }
				
		this.status.countForStatistics();
	}

    protected void flowThrough(Abstraction in, Unit u, Abstraction out) {
    	Set<Configuration> inConfigs = in.configurations;
    	Set<Configuration> outConfigs = out.configurations;
        Stmt stmt = (Stmt) u;
        //if not yet visited, initialize
        if(!numberVisited.containsKey(stmt)) {
            numberVisited.put(stmt, 0);
        }
        
        //increment counter
        int numVisited = numberVisited.get(stmt);
        numVisited++;
        numberVisited.put(stmt, numVisited);
        maxNumVisitedInThisRun = Math.max(maxNumVisitedInThisRun , numVisited);

        //if visited too often, abort
        if(numVisited>MAX_NUM_VISITED) {
            status = ABORTED_MAX_NUM_ITERATIONS;
        }

        if(status.isAborted()) throw new AbortedException();

        //check for side-effects
        boolean mightHaveSideEffects = mightHaveSideEffects(stmt);
        
        boolean foundEnabledShadow = false;
        if (stmtsToAnalyze.contains(stmt)) {
            
            boolean isSyntheticFinalUnit = FinalConfigsUnitGraph.isASyntheticFinalUnit(stmt);
            int lengthOfLongestPathInfo = FlowInsensitiveAnalysis.v().lengthOfLongestPathFor(tracematch);
            
            if(!(isSyntheticFinalUnit && numVisited>lengthOfLongestPathInfo)) {
                //if we are visiting the synthetic final unit of unncessary-shadows and we have already propagated through it
                //as often as the longest path info, we can ignore further iterations, as they will never hit the final state

                //if we care about the shadow and it has a tag...
                if(stmt.hasTag(SymbolShadowTag.NAME)) {
    
                	//retrieve matches for the current tracematch
                	SymbolShadowTag tag = (SymbolShadowTag) stmt.getTag(SymbolShadowTag.NAME);		
                	Set<ISymbolShadow> matchesForThisTracematch = tag.getMatchesForTracematch(tracematch);
    
                    outConfigs.clear();
                    
                    //for each match, if it is still active, compute the successor and join 
                    for (ISymbolShadow shadow : matchesForThisTracematch) {
                        if(shadow.isEnabled()) {                
                            foundEnabledShadow = true;
                            for (Configuration oldConfig : inConfigs) {
                                Configuration newConfig = oldConfig.doTransition(shadow,isSyntheticFinalUnit,in.valuesInInitialState);
                                if(mightHaveSideEffects) {
                                    newConfig = newConfig.taint();
                                }
                                outConfigs.add(newConfig);
    
                                int numNewConfigs = outConfigs.size();
                                maxNumConfigsInThisRun = Math.max(maxNumConfigsInThisRun , numNewConfigs);
                                if(numNewConfigs>MAX_NUM_CONFIGS) {
                                    status = ABORTED_MAX_NUM_CONFIGS;
                                    return;
                                }
                                int configSize = newConfig.size();
                                maxSizeConfigInThisRun = Math.max(maxSizeConfigInThisRun , configSize);
                                if(configSize>MAX_SIZE_CONFIG) {
                                    status = ABORTED_MAX_SIZE_CONFIG;
                                    return;
                                }
                            }
                            
                            processValuesInInitialState(shadow,out.valuesInInitialState);
                        }
                    }
                    
                }
            }
        }
		
        if(!foundEnabledShadow) {
            //if we not actually computed a join, copy instead 
            copy(in,out);
        }
        
        processNewExpression(stmt,out.valuesInInitialState);
        
        if(mightHaveSideEffects) {
        	Set<Configuration> outCopy = new HashSet<Configuration>(outConfigs);
        	outConfigs.clear();
        	for (Configuration outConf : outCopy) {
				outConfigs.add(outConf.taint());
			}
        }
        
        //if visited for the first time
        if(numVisited==1) {
            //...record this after-flow for comparison
            stmtToFirstAfterFlowConfig.put(stmt, new HashSet<Configuration>(outConfigs));
        }
    }
	
    /**
     * This method removes an instance key k from valuesInInitialState in the case where shadow might have moved
     * k out of the initial state. This is assumed to be the case if (1) the shadow's symbol is an initial
     * symbol of the tracematch and (2) any instance keys referred to by the shadow may alias k.
     */
    protected void processValuesInInitialState(ISymbolShadow shadow,Set<InstanceKey> valuesInInitialState) {
    	if(initialSymbols.contains(shadow.getSymbolName())) {
        	Map<String, InstanceKey> varToInstanceKey = reMap(shadow.getTmFormalToAdviceLocal());
    		for (InstanceKey possiblyTransitionedInstanceKey : varToInstanceKey.values()) {
				for (Iterator initialValuesIter = valuesInInitialState.iterator(); initialValuesIter.hasNext();) {
					InstanceKey instanceKeyInInitialState = (InstanceKey) initialValuesIter.next();
					if(!instanceKeyInInitialState.mayNotAlias(possiblyTransitionedInstanceKey)) {
						initialValuesIter.remove();
					}
				}
			}
    	}
	}

	/**
     * At any assignment of a new-expression we know that the instance key representing that
     * new-expression has to be in its initial state. We can exploit that for tracematches.
     * This method analyses stmt. If it is an assign statement and the RHS is a new-expression
     * then we add the instance key for that expression to valuesInInitialState. 
     */
    protected void processNewExpression(Stmt stmt,Set<InstanceKey> valuesInInitialState) {
    	if(stmt instanceof AssignStmt) {
			AssignStmt assignStmt = (AssignStmt) stmt;
    		Value rightOp = assignStmt.getRightOp();
    		if(rightOp instanceof NewExpr) {
    			Local assignedLocal = (Local) assignStmt.getLeftOp();
    			Stmt succ = (Stmt) ug.getSuccsOf(stmt).get(0);//there is only one successor of any new-statement
    			valuesInInitialState.add(new InstanceKey(assignedLocal,succ,container,lmaa,lmna));
    		}
    	}		
	}

	protected boolean mightHaveSideEffects(Stmt s) {
        Collection<ISymbolShadow> shadows = transitivelyCalledShadows(s);
        filterNewDacapoRun(shadows);
        if(!ShadowGroupRegistry.v().hasShadowGroupInfo()) {
            //do not have any info on shadow groups; hence we say that the statement can have side effects
            //if it calls any shadow at all
            return !shadows.isEmpty();
        } else {
    		for (ISymbolShadow shadow : shadows) {
    			if(overlappingShadowIDs.contains(shadow.getUniqueShadowId())) {
    				return true;
    			}
    		}
    		return false;
        }
	}

	/**
     * Removes shadows that have the symbol name newDaCapoRun, as those
     * symbols are just an artefact of our measurements. They can safely be ignored, as
     * they can only bring the automaton back to its initial configuration.  
     */
    private void filterNewDacapoRun(Collection<ISymbolShadow> shadows) {
        for (Iterator shadowIter = shadows.iterator(); shadowIter.hasNext();) {
            ISymbolShadow shadow = (ISymbolShadow) shadowIter.next();
            if(shadow.isArtificial()) {
                shadowIter.remove();
            }
        }        
    }

    /**
	 * Returns the collection of <code>ISymbolShadow</code>s triggered in transitive callees from <code>s</code>.
	 * @param s any statement
	 */
	protected Collection<ISymbolShadow> transitivelyCalledShadows(Stmt s) {
        HashSet<ISymbolShadow> symbols = new HashSet<ISymbolShadow>();
        HashSet<SootMethod> calleeMethods = new HashSet<SootMethod>();
        LinkedList<MethodOrMethodContext> methodsToProcess = new LinkedList();

        // Collect initial edges out of given statement in methodsToProcess
        Iterator<Edge> initialEdges = abstractedCallGraph.edgesOutOf(s);
        while (initialEdges.hasNext()) {
            Edge e = initialEdges.next();
            methodsToProcess.add(e.getTgt());
            calleeMethods.add(e.getTgt().method());
        }

        // Collect transitive callees of methodsToProcess
        while (!methodsToProcess.isEmpty()) {
            MethodOrMethodContext mm = methodsToProcess.removeFirst();
            Iterator mIt = abstractedCallGraph.edgesOutOf(mm);

            while (mIt.hasNext()) {
                Edge e = (Edge)mIt.next();
                if (!calleeMethods.contains(e.getTgt().method())) {
                    methodsToProcess.add(e.getTgt());
                    calleeMethods.add(e.getTgt().method());
                }
            }
        }

        // Collect all shadows in calleeMethods
        for (SootMethod method : calleeMethods) {
	        if(method.hasActiveBody()) {
	            Body body = method.getActiveBody();
	            
	            for (Iterator iter = body.getUnits().iterator(); iter.hasNext();) {
	                Unit u = (Unit) iter.next();
	                if(u.hasTag(SymbolShadowTag.NAME)) {
	                	SymbolShadowTag tag = (SymbolShadowTag) u.getTag(SymbolShadowTag.NAME);
	                	for (ISymbolShadow match : tag.getAllMatches()) {
							if(match.isEnabled()) {
								symbols.add(match);
							}
						}
	                }
	            }
	        }
        }
        return symbols;
	}

	/**
	 * @return the tracematch
	 */
	public TraceMatch getTracematch() {
		return tracematch;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	protected void copy(Abstraction source, Abstraction dest) {
		Set<Configuration> destConfigs = dest.configurations;
		destConfigs.clear();
		destConfigs.addAll(source.configurations);
		Set<InstanceKey> destValuesInInitialState = dest.valuesInInitialState;
		destValuesInInitialState.clear();
		destValuesInInitialState.addAll(source.valuesInInitialState);
	}

	/** 
	 * {@inheritDoc}
	 */
	protected Abstraction entryInitialFlow() {
        Set<Configuration> configs = new HashSet<Configuration>();
        for (Iterator stateIter = stateMachine.getStateIterator(); stateIter.hasNext();) {
            State state = (State) stateIter.next();
            if(!state.isFinalNode()) {

                Configuration entryInitialConfiguration = new Configuration(
                		this,
                		Collections.singleton(state),
                		!abortWhenHittingFinal //count final-hits, if not aborting when hitting final
                );
                configs.add(entryInitialConfiguration);
            
            }
        }
        //be conservative at the beginning: know nothing about any values being in the initial state        
		HashSet<InstanceKey> valuesInInitialState = new HashSet<InstanceKey>();
		return new Abstraction(configs,valuesInInitialState);
	}

	/** 
	 * {@inheritDoc}
	 */
	protected Abstraction newInitialFlow() {
		return new Abstraction(new HashSet<Configuration>(),new HashSet<InstanceKey>(allReferencedInstanceKeys));
	}
	
	/** 
	 * {@inheritDoc}
	 */
	protected void merge(Abstraction in1, Abstraction in2, Abstraction out) {
		//merge sets of configurations by union		
		Set<Configuration> inConfigs1 = in1.configurations;
		Set<Configuration> inConfigs2 = in2.configurations;
		Set<Configuration> outConfigs = out.configurations;
        outConfigs.clear();
        outConfigs.addAll(inConfigs1);
        outConfigs.addAll(inConfigs2);
        
        //merge sets of values in first state by intersection
        Set<InstanceKey> outValuesInInitialState = out.valuesInInitialState;
		outValuesInInitialState.clear();
        outValuesInInitialState.addAll(in1.valuesInInitialState);
        outValuesInInitialState.retainAll(in2.valuesInInitialState);
	}
	
	/**
	 * @return the stateMachine
	 */
	public TMStateMachine getStateMachine() {
		return stateMachine;
	}

	/**
	 * Returns all statements for which at least one active shadow exists.
	 */
	public Set<Stmt> statemementsWithActiveShadows() {
		return stmtToFirstAfterFlowConfig.keySet();
	}
	
	public Set<Configuration> getFirstAfterFlowConfigs(Stmt stmt) {
		assert stmtToFirstAfterFlowConfig.containsKey(stmt);
		return stmtToFirstAfterFlowConfig.get(stmt);
	}
	
	public UnitGraph getUnitGraph() {
		return (UnitGraph) graph;
	}

	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}
    
    public void hitFinal() {
        if(abortWhenHittingFinal) {
            status = ABORTED_HIT_FINAL;
        } else {
            status = RUNNING_HIT_FINAL;
        }
    }

    /**
     * Determines all statements that are in scope but for which it is guaranteed that
     * one loop iteration suffices to reach the fixed point.
     * @return
     */
    public Set<Stmt> statementsReachingFixedPointAtOnce() {
        
        Set<Stmt> result = new HashSet<Stmt>();
        
        //for each statement 
        for (Stmt stmt : stmtsToAnalyze) {
            //if the first after-flow is equal to the final one
            Set<Configuration> firstAfterFlow = getFirstAfterFlowConfigs(stmt);
            Set<Configuration> finalAfterFlow = getFlowAfter(stmt).configurations;
            assert firstAfterFlow!=null && finalAfterFlow!=null;
            //is the first after-flow equal to the last?
            if(firstAfterFlow.equals(finalAfterFlow)) {
                result.add(stmt);
            }
        }       
        return result;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }
    
    /**
     * Rewrites the mapping from tracematch variables to locals to a mapping from
     * tracematch variables to instance keys.
     */
    public Map<String,InstanceKey> reMap(Map<String,Local> bindings) {
        Map<String,Local> origBinding = bindings;
        Map<String,InstanceKey> newBinding = new HashMap<String, InstanceKey>();
        for (Map.Entry<String,Local> entry : origBinding.entrySet()) {
            String tmVar = entry.getKey();
            Local adviceLocal = entry.getValue();
            Stmt stmt = tmLocalDefs.get(adviceLocal); //may be null, if adviceLocal is not part of this method
            InstanceKey instanceKey = (adviceLocal.getType() instanceof RefLikeType) ?
              new InstanceKey(adviceLocal,stmt,container,lmaa,lmna) :
              new InstanceKeyNonRefLikeType(adviceLocal,stmt,container,lmaa,lmna);
            newBinding.put(tmVar, instanceKey);
        }
        return newBinding;
    }

}
