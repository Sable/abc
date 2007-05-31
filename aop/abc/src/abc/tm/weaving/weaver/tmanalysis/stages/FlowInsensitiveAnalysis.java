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
package abc.tm.weaving.weaver.tmanalysis.stages;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import abc.main.Main;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.query.ConsistentShadowGroupFinder;
import abc.tm.weaving.weaver.tmanalysis.query.PathInfoFinder;
import abc.tm.weaving.weaver.tmanalysis.query.ReachableShadowFinder;
import abc.tm.weaving.weaver.tmanalysis.query.Shadow;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroup;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroupRegistry;
import abc.tm.weaving.weaver.tmanalysis.util.ShadowsPerTMSplitter;
import abc.tm.weaving.weaver.tmanalysis.util.Timer;

/**
 * This stage applies a flow-insensitive analysis to all shadows still remaining at this stage.
 * 
 * @author Eric Bodden
 */
public class FlowInsensitiveAnalysis extends AbstractAnalysisStage {

	protected Timer domEdgesTimer = new Timer("dominating-edges");
	protected Timer groupShadowsTimer = new Timer("group-shadows");

	/**
	 * {@inheritDoc}
	 */
	protected void doAnalysis() {
		
		//fetch all shadows reachable over the abstracted call graph
        Set reachableShadows = ReachableShadowFinder.v().reachableShadows(CallGraphAbstraction.v().abstractedCallGraph());
        
        //remove and disable all shadows that have an empty variable mapping
        removeShadowsWithEmptyMappings(reachableShadows);
        
        //split all remaining shadows by tracematch
        Map tmNameToShadows = ShadowsPerTMSplitter.splitShadows(reachableShadows);
        
        Set allConsistentShadowGroups = new LinkedHashSet();
        
        TMGlobalAspectInfo globalAspectInfo = (TMGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();
        
        //for each "tracematch-name to shadows" mapping 
        for (Iterator keyIter = tmNameToShadows.keySet().iterator(); keyIter.hasNext();) {
			String tmName = (String) keyIter.next();
			TraceMatch traceMatch = globalAspectInfo.traceMatchByName(tmName);
			
			//find the sets of labels that dominate final states along each path
			domEdgesTimer.startOrResume();
			Set pathInfos = new PathInfoFinder(traceMatch).getPathInfos();
			domEdgesTimer.stop();
			
			Set thisTMsShadows = (Set) tmNameToShadows.get(tmName);
			assert thisTMsShadows!=null;
			
			groupShadowsTimer.startOrResume();
			Set shadowsGroups = ConsistentShadowGroupFinder.v().consistentShadowGroups(traceMatch,thisTMsShadows, pathInfos);
			groupShadowsTimer.stop();
			
			//store shadow groups for later reuse
			allConsistentShadowGroups.addAll(shadowsGroups);
			
			//disable all shadows which are not in a group
			Set shadowsToDisable = new HashSet();
			shadowsToDisable.addAll(thisTMsShadows);
			for (Iterator groupIter = shadowsGroups.iterator(); groupIter.hasNext();) {
				ShadowGroup group = (ShadowGroup) groupIter.next();
				shadowsToDisable.removeAll(group.getAllShadows());
			}			
			disableShadows(shadowsToDisable);			
		}
        
        //register all remaining shadow groups (those are now known to be consistent)
        ShadowGroupRegistry.v().registerShadowGroups(allConsistentShadowGroups);
        
        logToStatistics("cum-dominating-edges-time", domEdgesTimer);
        logToStatistics("cum-group-shadows-time", groupShadowsTimer);
	}

	/**
	 * remove and disable all shadows in <i>shadows</i> that have an empty variable mapping
	 * @param shadows a set of {@link Shadow}s
	 */
	protected void removeShadowsWithEmptyMappings(Set shadows) {
        int emptyMappingCount=0;
        for (Iterator shadowIter = shadows.iterator(); shadowIter.hasNext();) {
			Shadow shadow = (Shadow) shadowIter.next();
			if(shadow.hasEmptyMapping()) {
				shadowIter.remove();
				//such a shadow can safely be disabled due to the tracematch semantics which say
				//that the advice would not execute for an empty binding anyway
				disableShadow(shadow.getUniqueShadowId());
				
				emptyMappingCount++;
			}
		}
        logToStatistics("shadows-removed-due-to-empty-variable-mappings", emptyMappingCount);
	}
	
	/**
	 * Disables all given shadows.
	 * @param shadows a set os {@link Shadow}s
	 */
	protected void disableShadows(Set shadows) {
		Set shadowIDsToDisable = new HashSet();
		for (Iterator shadowIter = shadows.iterator(); shadowIter.hasNext();) {
			Shadow shadow = (Shadow) shadowIter.next();
			shadowIDsToDisable.add(shadow.getUniqueShadowId());
		}
		disableAll(shadowIDsToDisable);
	}
	
	//singleton pattern

	protected static FlowInsensitiveAnalysis instance;

	private FlowInsensitiveAnalysis() {}
	
	public static FlowInsensitiveAnalysis v() {
		if(instance==null) {
			instance = new FlowInsensitiveAnalysis();
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

