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
package abc.da.weaving.weaver.depadviceopt;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import abc.da.HasDAInfo;
import abc.da.fsanalysis.EnabledShadowSet;
import abc.da.fsanalysis.callgraph.AbstractedCallGraph;
import abc.da.fsanalysis.callgraph.NodePredicate;
import abc.da.fsanalysis.flowanalysis.AnalysisJob;
import abc.da.fsanalysis.ranking.PFGs;
import abc.da.fsanalysis.ranking.Statistics;
import abc.da.fsanalysis.util.ShadowsPerTMSplitter;
import abc.da.weaving.aspectinfo.AdviceDependency;
import abc.da.weaving.aspectinfo.TracePattern;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.main.Debug;
import abc.main.Main;
import abc.main.options.OptionsParser;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.weaver.AbstractReweavingAnalysis;

/**
 * DependentAdviceIntraproceduralAnalysis: This analysis propagates TracePattern
 * automaton states through the method.
 *
 * @author Patrick Lam
 * @author Eric Bodden
 */
public class DependentAdviceIntraproceduralAnalysis extends AbstractReweavingAnalysis {
	
	protected long maxTime, averageTime, totalTime;
	protected int analysisCount;
	
	protected Map<SootMethod,AnalysisJob> methodToJob = new HashMap<SootMethod, AnalysisJob>();
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean analyze() {
		
		HasDAInfo abcExtension = (HasDAInfo) Main.v().getAbcExtension();

		//nothing to do?
    	if(abcExtension.getDependentAdviceInfo().getTracePatterns().size()==0) {
    		return false;
    	}
    	
		try {
			Set<Shadow> dependentAdviceShadows =
				abcExtension.getDependentAdviceInfo().flowInsensitiveAnalysis().getDependentAdviceShadowsEnabledAfterThisStage();
			
			if(dependentAdviceShadows.isEmpty()) return false;

			CallGraph cg = buildAbstractedCallGraph(dependentAdviceShadows);
			
			if(Debug.v().outputPFGs)
				PFGs.v().dump("after first flow-insensitive stage", dependentAdviceShadows, true);

			File traceFile = retrieveTraceFileHandle();

			int iteration = 1;
			//the set of shadows enabled just before the current itration
			Set<Shadow> enabledShadowsBeforeIteration = new HashSet<Shadow>(dependentAdviceShadows);
			//the set of shadow enabled just after the last iteration
			Set<Shadow> enabledShadowsAfterIteration = enabledShadowsBeforeIteration;
	        do {
	        	//the shadows enabled before this iterations are the ones that remained enabled after the last one
	        	enabledShadowsBeforeIteration = enabledShadowsAfterIteration;
		        System.err.println("da:    DA-Shadows enabled before FlowSens iteration "+iteration+": "+enabledShadowsBeforeIteration.size());		        
	        	
		        //do the work...
		        oneIteration(enabledShadowsBeforeIteration,cg,traceFile);
	        	
		        //create the set of all still-enabled shadows
		        enabledShadowsAfterIteration = new HashSet<Shadow>(enabledShadowsBeforeIteration);
	        	for (Iterator<Shadow> shadowIter = enabledShadowsAfterIteration.iterator(); shadowIter.hasNext();) {
					Shadow shadow = shadowIter.next();
					if(!shadow.isEnabled()) {
						shadowIter.remove();
					}
				}
		        
	        	iteration++;
	        	//we iterate until no shadows are disabled any more
	        } while(!enabledShadowsBeforeIteration.equals(enabledShadowsAfterIteration));

			if(Debug.v().outputPFGs)
				PFGs.v().dump("after flow-sensitive stage", enabledShadowsAfterIteration, false);
	        
	        System.err.println("da:    DA-Shadows enabled after last FlowSens iteration: "+enabledShadowsAfterIteration.size());  
		} finally {
			abcExtension.getDependentAdviceInfo().resetAnalysisDataStructures();
		}
		return false;
	}



	/**
	 * Depending on the command-line setting returns either a handle to a file which
	 * the compile-time trace for cross-validation should be writen to, or <code>null</code>.
	 */
	private File retrieveTraceFileHandle() {
		File traceFile = null;
		if(OptionsParser.v().produce_compile_time_trace()) {
			try {
				traceFile = new File("abc.da.cttrace");
				if(traceFile.exists()) {
					traceFile.delete();
				}
				traceFile.createNewFile();
			} catch (IOException e) {
				System.err.println("Error creating trace file abc.da.cttrace:");
				e.printStackTrace();
				traceFile = null;
			}
		}
		return traceFile;
	}

	/**
	 * Returns an abstracted call-graph. This call-graph is different from the full call-graph
	 * in that it does not contain any outgoing call edges for which it holds that transitively,
	 * through this call, no method containing an enabled shadow may be reached.
	 */
	private CallGraph buildAbstractedCallGraph(Set<Shadow> dependentAdviceShadows) {
		final Set<SootMethod> methodsWithShadows = new HashSet<SootMethod>();
		for (Shadow shadow : dependentAdviceShadows) {
			if(shadow.isEnabled())
				methodsWithShadows.add(shadow.getContainer());
		}
		
		CallGraph cg = new AbstractedCallGraph(Scene.v().getCallGraph(), new NodePredicate() {

			/** 
			 * Returns <code>true</code> if the method can call back to weaveable classes.
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
		return cg;
	}



	/**
     * Executes a single analysis iteration.
     * @param shadows the set of all shadows in the program that are still enabled
     * @param cg the abstracted call-graph
	 * @param traceFile <code>null</code> or the handle to a file which the compile-time
	 * trace should be written to (for cross-validation)
     */
    protected void oneIteration(Set<Shadow> shadows, CallGraph cg, File traceFile) {
		HasDAInfo gai = (HasDAInfo) Main.v().getAbcExtension();

		/*
		 * First, split shadows by TracePattern. This will ignore shadows that are not symbol shadows. 
		 */
		Map<TracePattern,Set<Shadow>> tmToShadows = ShadowsPerTMSplitter.splitSymbolShadows(shadows);
		
        for (TracePattern tm : (Collection<TracePattern>)gai.getDependentAdviceInfo().getTracePatterns()) {
            
        	Set<Shadow> thisTMsShadows = tmToShadows.get(tm);
        	if(thisTMsShadows==null) {
        	    //no shadows left
        	    continue;
        	}
        	
        	/*
        	 * Build a mapping from methods to the shadows in these methods.
        	 * We only take account shadows for this TracePattern.
        	 */
        	Map<SootMethod,EnabledShadowSet> methodToEnabledTMShadows = new HashMap<SootMethod, EnabledShadowSet>();
            for (Shadow s : thisTMsShadows) {
            	if(s.isEnabled()) {
	                SootMethod m = s.getContainer();
	                EnabledShadowSet shadowsInMethod = methodToEnabledTMShadows.get(m);
	                if(shadowsInMethod==null) {
	                	shadowsInMethod = new EnabledShadowSet();
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
                
                if(Debug.v().debugDA)
                	System.err.println("Analyzing: "+m+" on TracePattern: "+tm.getName());
                
                long before = System.currentTimeMillis();

                AnalysisJob job = methodToJob.get(m);
                if(job==null) {
                    /*
                     * Set up supporting analyses.
                     */
                	job = new AnalysisJob(m,tm,shadowsInMethod,cg,methodToEnabledTMShadows);
                	methodToJob.put(m, job);
                }

                /*
                 * Run the analysis job.
                 */
                job.compute(traceFile);

                long duration = System.currentTimeMillis()-before;

                if(Debug.v().debugDA) {
                	System.err.println("Done analyzing: "+m+" on TracePattern: "+tm.getName());
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
        
        //perform flow-insensitive analysis again
        AdviceDependency.disableShadowsWithNoStrongSupportByAnyGroup(shadows);
    }

}
