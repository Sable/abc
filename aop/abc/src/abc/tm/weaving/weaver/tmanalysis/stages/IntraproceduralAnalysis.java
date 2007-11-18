/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Patrick Lam
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
package abc.tm.weaving.weaver.tmanalysis.stages;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.Kind;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.pointer.LocalMustNotAliasAnalysis;
import soot.jimple.toolkits.pointer.StrongLocalMustAliasAnalysis;
import soot.toolkits.graph.ExceptionalUnitGraph;
import abc.main.Debug;
import abc.main.Main;
import abc.main.options.OptionsParser;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.Statistics;
import abc.tm.weaving.weaver.tmanalysis.ShadowUtils;
import abc.tm.weaving.weaver.tmanalysis.query.ReachableShadowFinder;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroupRegistry;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.query.SymbolShadowWithPTS;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowTag;
import abc.tm.weaving.weaver.tmanalysis.subanalyses.UnnecessaryShadowsElimination;
import abc.tm.weaving.weaver.tmanalysis.subanalyses.RunOnceOptimization;
import abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow;
import abc.tm.weaving.weaver.tmanalysis.util.ShadowsPerTMSplitter;

/**
 * IntraproceduralAnalysis: This analysis propagates tracematch
 * automaton states through the method.
 *
 * @author Patrick Lam
 * @author Eric Bodden
 */
public class IntraproceduralAnalysis extends AbstractAnalysisStage {
	
	public static TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();
	
    protected CallGraph cg;

    protected int numShadowsBefore;
    
	/**
	 * {@inheritDoc}
	 */
	protected void doAnalysis() {
		cg = CallGraphAbstraction.v().abstractedCallGraph();
		long timeBefore = System.currentTimeMillis();
		
		final int MAX_ITERATIONS = OptionsParser.v().wp_tmopt_iterations();
		if(MAX_ITERATIONS<1) {
			throw new IllegalArgumentException("Invalie argument: wp-tmopt-iterations"+MAX_ITERATIONS);
		}
		
		for(int i=0;i<MAX_ITERATIONS;i++) {
	        oneIteration();
	        ShadowGroupRegistry.v().pruneShadowGroupsWhichHaveBecomeIncomplete();
	        if(!ShadowRegistry.v().wasShadowDisabled()) {
	            break;
	        }
		}
		
		if(Debug.v().tmShadowStatistics) {
			Statistics.v().totalIntraProceduralAnalysisTime = System.currentTimeMillis() - timeBefore;		
			Statistics.v().dump();
		}
	}

    /**
     * Executes a single analysis iteration.
     */
    protected void oneIteration() {
        Set reachableShadows = ReachableShadowFinder.v().reachableShadows(cg);
		numShadowsBefore = reachableShadows.size();
        Map tmNameToShadows = ShadowsPerTMSplitter.splitShadows(reachableShadows);
		
        boolean mayStartThreads = mayStartThreads();
        
        for (TraceMatch tm : (Collection<TraceMatch>)gai.getTraceMatches()) {
            if(mayStartThreads && !tm.isPerThread() && Debug.v().debugTmAnalysis) {
                System.err.println("#####################################################");
                System.err.println(" Application may start threads that execute shadows! ");
                System.err.println(" Tracematch "+tm.getName()+" is not per-thread!");
                System.err.println("#####################################################");
            }
            
        	Set<SootMethod> methodsWithShadows = new HashSet<SootMethod>();
        	Set<SymbolShadowWithPTS> thisTMsShadows = (Set<SymbolShadowWithPTS>) tmNameToShadows.get(tm.getName());
        	if(thisTMsShadows==null) {
        	    //no shadows left
        	    continue;
        	}
            for (SymbolShadowWithPTS s : thisTMsShadows) {
                SootMethod m = s.getContainer();
                methodsWithShadows.add(m);
            }

            for (SootMethod m : methodsWithShadows) {
                if(ShadowUtils.getAllActiveShadows(tm, m.getActiveBody().getUnits()).isEmpty()) return; //no active shadows any more
                
                if(Debug.v().debugTmAnalysis)
                	System.err.println("Analyzing: "+m+" on tracematch: "+tm.getName());
                
                ExceptionalUnitGraph g = new ExceptionalUnitGraph(m.retrieveActiveBody());
    			StrongLocalMustAliasAnalysis localMustAliasAnalysis = new StrongLocalMustAliasAnalysis(g);
				LocalMustNotAliasAnalysis localNotMayAliasAnalysis = new LocalMustNotAliasAnalysis(g);
                Map<Local,Stmt> tmLocalsToDefStatements = findTmLocalDefinitions(g,tm);

                boolean allRemoved = UnnecessaryShadowsElimination.apply(tm, g, tmLocalsToDefStatements, localMustAliasAnalysis, localNotMayAliasAnalysis);

                if(!allRemoved) {
                    if(Debug.v().useShadowMotion) {
                        RunOnceOptimization.apply(tm, g, tmLocalsToDefStatements, localMustAliasAnalysis, localNotMayAliasAnalysis);
                    }
                }

                if(Debug.v().debugTmAnalysis)
                	System.err.println("Done analyzing: "+m+" on tracematch: "+tm.getName());    			
	        }
		}
    }

    private boolean mayStartThreads() {
        CallGraph callGraph = CallGraphAbstraction.v().abstractedCallGraph();
        for (Iterator iterator = callGraph.listener(); iterator.hasNext();) {
            Edge edge = (Edge) iterator.next();
            if(edge.kind().equals(Kind.THREAD)) {
                return true;
            }
        }
        return false;
    }
    
	private Map<Local, Stmt> findTmLocalDefinitions(ExceptionalUnitGraph g, TraceMatch tm) {
		
		Body b = g.getBody();
		
		Set<Local> boundLocals = new HashSet<Local>();
		
		//find all localc bound by shadows of the given tracematch		
		for (Unit u: b.getUnits()) {
            Stmt stmt = (Stmt)u;
			if(stmt.hasTag(SymbolShadowTag.NAME)) {
				SymbolShadowTag tag = (SymbolShadowTag) stmt.getTag(SymbolShadowTag.NAME);
				Set<ISymbolShadow> matchesForTracematch = tag.getMatchesForTracematch(tm);
				for (ISymbolShadow shadow : matchesForTracematch) {
					boundLocals.addAll(shadow.getAdviceLocals());
				}
			}
		}
		
		Map<Local,Stmt> localToStmtAfterDefStmt = new HashMap<Local, Stmt>();
		
        for (Unit u: b.getUnits()) {
            Stmt stmt = (Stmt)u;
            for (soot.ValueBox vb : (Collection<soot.ValueBox>)stmt.getDefBoxes()) {
                soot.Value v = vb.getValue();
                if(boundLocals.contains(v)) {
                    //have a definition of v already!
                    if(localToStmtAfterDefStmt.containsKey(v)) {
                        throw new RuntimeException("Multiple defs! Has LocalSplitter been applied?");
                    }
                    
                	//we know that such def statements always have the form "adviceLocal = someLocal;",
                	//hence taking the first successor is always sound
                	localToStmtAfterDefStmt.put((Local)v, (Stmt)g.getUnexceptionalSuccsOf(stmt).get(0));
                }
            }			
		}
		
		return localToStmtAfterDefStmt;		
	}

	/**
	 * {@inheritDoc}
	 */
	protected void defaultStatistics() {
        if(Debug.v().tmShadowStatistics) {
            //recompute reachable shadows 
            ReachableShadowFinder.reset();
            Set reachableShadows = ReachableShadowFinder.v().reachableShadows(cg);
            int numRemainingShadows = ShadowRegistry.v().enabledShadows().size();
                
            logToStatistics("shadows-removed", numShadowsBefore-reachableShadows.size()+"");
            logToStatistics("shadows-retained", "0");
            logToStatistics("shadows-remaining", numRemainingShadows+"");
            logToStatistics("stage-time", stageTimer);
            logToStatistics("shadow-update-time", shadowUpdateTimer);
        } else {
            super.defaultStatistics();
        }
	}
	
	//singleton pattern
	
	protected static IntraproceduralAnalysis instance;

	private IntraproceduralAnalysis() {}
	
	public static IntraproceduralAnalysis v() {
		if(instance==null) {
			instance = new IntraproceduralAnalysis();
		}
		return instance;		
	}
	
	/**
	 * Frees the singleton object. 
	 */
	public static void reset() {
		instance = null;
	}

}
