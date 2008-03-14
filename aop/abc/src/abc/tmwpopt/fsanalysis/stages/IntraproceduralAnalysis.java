/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
 * Copyright (C) 2007 Patrick Lam
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
package abc.tmwpopt.fsanalysis.stages;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Kind;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import abc.da.weaving.aspectinfo.AdviceDependency;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.main.Debug;
import abc.main.Main;
import abc.tmwpopt.AbcExtension;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tmwpopt.fsanalysis.Ranking;
import abc.tmwpopt.fsanalysis.Statistics;
import abc.tmwpopt.fsanalysis.SymbolNames;
import abc.tmwpopt.fsanalysis.callgraph.AbstractedCallGraph;
import abc.tmwpopt.fsanalysis.callgraph.NodePredicate;
import abc.tmwpopt.fsanalysis.subanalyses.UnnecessaryShadowsElimination;
import abc.tmwpopt.fsanalysis.util.ShadowsPerTMSplitter;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.weaver.AbstractReweavingAnalysis;

/**
 * IntraproceduralAnalysis: This analysis propagates tracematch
 * automaton states through the method.
 *
 * @author Patrick Lam
 * @author Eric Bodden
 */
public class IntraproceduralAnalysis extends AbstractReweavingAnalysis {
	
	protected long maxTime, averageTime, totalTime;
	protected int analysisCount;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean analyze() {
		
		TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();

		//nothing to do?
    	if(gai.getTraceMatches().size()==0) {
    		return false;
    	}
    	
		AbcExtension abcExtension = (AbcExtension) Main.v().getAbcExtension();
		try {
			Set<Shadow> dependentAdviceShadows =
				abcExtension.flowInsensitiveAnalysis().getDependentAdviceShadowsEnabledAfterThisStage();
			
			if(dependentAdviceShadows.isEmpty()) return false;

			final Set<SootMethod> methodsWithShadows = new HashSet<SootMethod>();
			for (Shadow shadow : dependentAdviceShadows) {
				methodsWithShadows.add(shadow.getContainer());
			}
			
			CallGraph cg = new AbstractedCallGraph(Scene.v().getCallGraph(), new NodePredicate() {

				/** 
				 * Returns <code>true</code> if the method can call back to weavable classes.
				 */
				public boolean visitChildren(MethodOrMethodContext curr) {
			    	SootMethod method = curr.method();
			    	//explicitly has no effects on base code
			    	return !MethodCategory.noEffectsOnBaseCode(method);
			    }

				public boolean want(MethodOrMethodContext node) {
			    	SootMethod method = node.method();			    	
			    	//explicitly has no effects on base code
			    	if(MethodCategory.noEffectsOnBaseCode(method)) {
			    		return false;
			    	}
			    	return methodsWithShadows.contains(node);
				}
				
			});
			
			Statistics.v().dump("after first flow-insensitive stage", dependentAdviceShadows, true);
			
	        oneIteration(dependentAdviceShadows,cg);

	        Statistics.v().dump("after flow-sensitive stage", dependentAdviceShadows, true);

	        AdviceDependency.disableShadowsWithNoStrongSupportByAnyGroup(dependentAdviceShadows);

			Statistics.v().dump("after second flow-insensitive stage", dependentAdviceShadows, false);
	        
	        System.err.println("=========================================");
	        if(UnnecessaryShadowsElimination.timesAborted>0) {
	        	System.err.println("SOME RUNS WERE ABORTED!");
	        }
        	System.err.println("Number of aborted attempts:    "+UnnecessaryShadowsElimination.timesAborted);
        	System.err.println("Limit was:                     "+UnnecessaryShadowsElimination.maxJobCount);
        	System.err.println("Max number of successful jobs: "+UnnecessaryShadowsElimination.maxJobCountOccurred);
	        System.err.println("=========================================");

		} catch(OutOfMemoryError e) {
			//in case we run out of memory, clean up right away
			e.printStackTrace();
			System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.err.println("Ran out of memory! Cleaning up...");
			System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			abcExtension.resetAnalysisDataStructures();
		} finally {
			Statistics.reset();
			Ranking.reset();
			SymbolNames.reset();
		}
		return false;
	}



	/**
     * Executes a single analysis iteration.
     * @param shadows 
     * @param cg 
     */
    protected void oneIteration(Set<Shadow> shadows, CallGraph cg) {
		TMGlobalAspectInfo gai = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();

		/*
		 * First, split shadows by tracematch. This will ignore shadows that are not symbol shadows. 
		 */
		Map<TraceMatch,Set<Shadow>> tmToShadows = ShadowsPerTMSplitter.splitSymbolShadows(shadows);
		
        boolean mayStartThreads = mayStartThreads(cg);
        
        for (TraceMatch tm : (Collection<TraceMatch>)gai.getTraceMatches()) {
            if(mayStartThreads && !tm.isPerThread() && Debug.v().debugTmAnalysis) {
                System.err.println("#####################################################");
                System.err.println(" Application may start threads that execute shadows! ");
                System.err.println(" Tracematch "+tm.getName()+" is not per-thread!");
                System.err.println("#####################################################");
            }
            
        	Set<Shadow> thisTMsShadows = tmToShadows.get(tm);
        	if(thisTMsShadows==null) {
        	    //no shadows left
        	    continue;
        	}
        	
        	/*
        	 * Build a mapping from methods to the shadows in these methods.
        	 * We only take account shadows for this tracematch.
        	 */
        	Map<SootMethod,Set<Shadow>> methodToEnabledTMShadows = new HashMap<SootMethod, Set<Shadow>>();
            for (Shadow s : thisTMsShadows) {
            	if(s.isEnabled()) {
	                SootMethod m = s.getContainer();
	                Set<Shadow> shadowsInMethod = methodToEnabledTMShadows.get(m);
	                if(shadowsInMethod==null) {
	                	shadowsInMethod = new HashSet<Shadow>();
	                	methodToEnabledTMShadows.put(m, shadowsInMethod);                	
	                }
	                shadowsInMethod.add(s);
            	}
            }

            /*
             * For each method in the mapping...
             */
            for (SootMethod m : methodToEnabledTMShadows.keySet()) {
            	Set<Shadow> shadowsInMethod = methodToEnabledTMShadows.get(m);
                if(shadowsInMethod.isEmpty()) return; //no active shadows any more
                
                if(Debug.v().debugTmAnalysis)
                	System.err.println("Analyzing: "+m+" on tracematch: "+tm.getName());
                
                long before = System.currentTimeMillis();

                /*
                 * Set up and run supporting analyses.
                 */
                AnalysisJob job = new AnalysisJob(m,tm,shadowsInMethod,cg,methodToEnabledTMShadows);
                
                /*
                 * Run UnnecessaryShadowsElimination.
                 */
                UnnecessaryShadowsElimination.apply(job);

                long duration = System.currentTimeMillis()-before;

                if(Debug.v().debugTmAnalysis) {
                	System.err.println("Done analyzing: "+m+" on tracematch: "+tm.getName());
					System.err.println("Analysis took: "+duration);
                }
                
                maxTime = Math.max(maxTime, duration);
                totalTime += duration;
                analysisCount++;
	        }
		}
        averageTime = Math.round(totalTime / (analysisCount+0.0));
        
        System.err.println("Number of analysis runs: "+analysisCount);
        System.err.println("Maximal analysis time:   "+maxTime);
        System.err.println("Average analysis time:   "+averageTime);
        System.err.println("Total analysis time:     "+totalTime);
    }

    private boolean mayStartThreads(CallGraph cg) {
        for (Iterator<Edge> iterator = cg.listener(); iterator.hasNext();) {
            Edge edge = iterator.next();
            if(edge.kind().equals(Kind.THREAD)) {
                return true;
            }
        }
        return false;
    }

}
