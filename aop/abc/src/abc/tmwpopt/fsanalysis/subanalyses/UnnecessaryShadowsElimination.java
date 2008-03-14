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
package abc.tmwpopt.fsanalysis.subanalyses;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import polyglot.util.ErrorInfo;
import soot.Unit;
import soot.jimple.Stmt;
import abc.da.weaving.aspectinfo.AdviceDependency;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.main.Debug;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.PerSymbolTMAdviceDecl;
import abc.tm.weaving.matching.State;
import abc.tmwpopt.fsanalysis.Abstraction;
import abc.tmwpopt.fsanalysis.Ranking;
import abc.tmwpopt.fsanalysis.SymbolNames;
import abc.tmwpopt.fsanalysis.TMWorklistBasedAnalysis;
import abc.tmwpopt.fsanalysis.WorklistAnalysis.TimeOutException;
import abc.tmwpopt.fsanalysis.ds.Configuration;
import abc.tmwpopt.fsanalysis.stages.AnalysisJob;
import abc.tmwpopt.tmtoda.PathInfoFinder;
import abc.tmwpopt.tmtoda.PathInfoFinder.PathInfo;

/**
 * Performs the actual transformation for our static abstract interpretation for tracematches.
 * It applies our analysis to each method in question and then disables unnecessary shadows.
 *
 * @author Eric Bodden
 */
public class UnnecessaryShadowsElimination {
    
	public static int maxJobCount = 3000;

	public static int maxJobCountOccurred = 0;
	
	public static int timesAborted = 0;

    public static boolean apply(AnalysisJob job) {
    	
		if(Debug.v().debugTmAnalysis)
			System.err.println("Running optimization 'unncessary shadows elimination'...");

        long before = System.currentTimeMillis();
        
        TMWorklistBasedAnalysis flowAnalysis;
		try {
			flowAnalysis = new TMWorklistBasedAnalysis(job,maxJobCount);
			
			maxJobCountOccurred = Math.max(maxJobCountOccurred, flowAnalysis.getJobCount());

			if(Debug.v().debugTmAnalysis)
				System.err.println("Intraprocedural analysis took: "+(System.currentTimeMillis()-before));
	        
			before = System.currentTimeMillis();

	        Set<Shadow> shadowsToBeKeptAlive = propagateOverlappingShadowsFromOtherMethods(flowAnalysis,job);        

			if(Debug.v().debugTmAnalysis)
				System.err.println("Interprocedural propagation for soundness took: "+(System.currentTimeMillis()-before));

	        Set<Shadow> shadowsToDisable = new HashSet<Shadow>(job.allTMShadowsInMethod());

	        int numTainted = 0;
	        for (Unit unit : job.unitGraph()) {
				Set<Abstraction> flowAfter = flowAnalysis.getFlowAfter(unit);
				Set<Configuration> configs = new HashSet<Configuration>();
				boolean hasTainted = false;
				for (Abstraction abstraction : flowAfter) {
					Configuration c = abstraction.getConfiguration();
					hasTainted |= c.isTainted();
					configs.add(c);
				}				
				if(hasTainted) {
					Set<Shadow> shadowsOfStmt = job.shadowsOfStmt((Stmt) unit);
					shadowsToDisable.removeAll(shadowsOfStmt);
					Ranking.v().addShadowsRetainedBecauseOfTainting(shadowsOfStmt);
					numTainted++;
				}
				for (Configuration c : configs) {
					Collection<Shadow> historyAtFinalStates = c.getHistoryAtFinalStates();
					shadowsToDisable.removeAll(historyAtFinalStates);
				}
			}
	        
			if(Debug.v().debugTmAnalysis)
				System.err.println("Tainted statements: "+numTainted);
			
			Set<Shadow> shadowsOnlyInterprocedurallyRetained = new HashSet<Shadow>(shadowsToBeKeptAlive);
			shadowsOnlyInterprocedurallyRetained.retainAll(shadowsToDisable);	//take out all that were already intraprocedurally disabled
			Ranking.v().addShadowsRetainedInterprocedurally(shadowsOnlyInterprocedurallyRetained);
			
			shadowsToDisable.removeAll(shadowsToBeKeptAlive);
			
	        for (Shadow s : shadowsToDisable) {        	
	        	//keep artificial shadow alive:
	        	//FIXME actually there should be no exception necessary for this one!
	        	if(!SymbolNames.v().isArtificialShadow(s)) {
		            s.disable();
		            warn(s,job);
	        	}
	        }

	        if(shadowsToDisable.size()==job.allTMShadowsInMethod().size()) {
				if(Debug.v().debugTmAnalysis)
					System.err.println("Optimization 'unncessary shadows elimination' removed all shadows.");
	            return true;
	        } else {
	            return false;
	        }
		} catch (TimeOutException e) {
			Ranking.v().addMethodWithCutOffComputation(job.method());
			timesAborted++;
			if(Debug.v().debugTmAnalysis) {
				System.err.println("ABORTING ANALYSIS for method: "+job.method());
				System.err.println(e.getMessage());
			}
			return false;
		}
        
    }

	protected static Set<Shadow> propagateOverlappingShadowsFromOtherMethods(TMWorklistBasedAnalysis flowAnalysis, AnalysisJob job) {
		Set<Configuration> allEndConfigs = new HashSet<Configuration>();
		for (Unit u : job.unitGraph().getTails()) {
			Set<Abstraction> abstractions = flowAnalysis.getFlowAfter(u);
			for (Abstraction abstraction : abstractions) {
				allEndConfigs.add(abstraction.getConfiguration());
			}
		}

		Set<Shadow> shadowsToBeKeptAlive = new HashSet<Shadow>();
		
		//for each configuration c at a tail unit
		for (Configuration c : allEndConfigs) {
			//for each non-initial, non-final state q
			for (Iterator<State> stateIter = job.stateMachine().getStateIterator(); stateIter.hasNext();) {
				State q = stateIter.next();
				if(!q.isInitialNode() && !q.isFinalNode()) {
					//compute all path infos starting at q
					PathInfoFinder pathInfoFinder = new PathInfoFinder(job.traceMatch(),Collections.singleton(q));
					Set<PathInfo> pathInfos = pathInfoFinder.getPathInfos();

					Collection<Shadow> historyAtStateQ = c.getHistoryAtState(q);
					Set<Shadow> overlaps = AdviceDependency.getAllEnabledShadowsOverlappingWith(historyAtStateQ);
					overlaps.removeAll(job.allTMShadowsInMethod());
					Set<String> overlappingLabels = new HashSet<String>();
					for (Shadow overlap : overlaps) {
						if(overlap.getAdviceDecl() instanceof PerSymbolTMAdviceDecl) {
							overlappingLabels.add(job.symbolNameForShadow(overlap));
						}
					}
					
					boolean satisfied = false;
					for (PathInfo pathInfo : pathInfos) {
						if(overlappingLabels.containsAll(pathInfo.getDominatingLabels())) {
							satisfied = true;
							break;
						}
					}
					
					if(satisfied) {
						shadowsToBeKeptAlive.addAll(historyAtStateQ);
					}
					
				}			
			}
		}
		
		return shadowsToBeKeptAlive;
	}

	protected static void warn(Shadow s, AnalysisJob job) {	
		Main.v().getAbcExtension().forceReportError(ErrorInfo.WARNING, "Shadow was disabled because it is unnecessary: "+
				job.traceMatch().getName()+"."+job.symbolNameForShadow(s), s.getPosition());
	}


}
