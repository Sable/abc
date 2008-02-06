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
package abc.da.weaving.weaver.depadviceopt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.util.ErrorInfo;
import soot.PackManager;
import soot.Scene;
import abc.da.weaving.aspectinfo.AdviceDependency;
import abc.da.weaving.aspectinfo.AdviceDependency.DependentShadowGroup;
import abc.da.weaving.aspectinfo.DAGlobalAspectInfo;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.main.Main;
import abc.main.options.OptionsParser;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.weaver.AbstractReweavingAnalysis;


/**
 * Performs the flow-insensitive pointer analysis for dependent advice.
 * First it produces a set of consistent shadow groups. A consistent shadow group for an advice dependency dep
 * returns strong shadows of strong advice in dep and weak shadows of weak advice in dep, but only if
 * those shadows have overlapping points-to sets at those positions where dep uses the same variable.
 * 
 * A shadow of a dependent advice can be disabled if this shadow is not part of any consistent shadow group.
 * @author Eric Bodden
 */
public class DependentAdviceFlowInsensitiveAnalysis extends AbstractReweavingAnalysis {

	
	/**
	 * {@inheritDoc}
	 */
	public boolean analyze() {
		
		final DAGlobalAspectInfo gai = (DAGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();
		final Set<AdviceDependency> adviceDependencies = new HashSet<AdviceDependency>(gai.getAdviceDependencies());
		//prune dependencies that already fail the quick check; those we don't care about
		for (Iterator<AdviceDependency> depIter = adviceDependencies.iterator(); depIter.hasNext();) {
			AdviceDependency ad = (AdviceDependency) depIter.next();
			if(!ad.fulfillsQuickCheck()) {
				depIter.remove();
			}
		}		
		if(adviceDependencies.isEmpty()) return false;
		
		//perform pointer analysis if necessary
		if(!Scene.v().hasCallGraph() || !Scene.v().hasPointsToAnalysis()) {
			runCGPhase();
		}

		//compute all active shadows reachable from entry points
		final Set<Shadow> reachableActiveShadows = Shadow.reachableActiveShadows(true);

		//compute consistent shadow groups and build the set of shadows to retain
		Set<Shadow> shadowsToRetain = new HashSet<Shadow>();
		for (AdviceDependency dep : adviceDependencies) {
			Set<DependentShadowGroup> shadowGroups = dep.computeOrUpdateConsistentShadowGroups(reachableActiveShadows);
			for (DependentShadowGroup shadowGroup : shadowGroups) {
				shadowsToRetain.addAll(shadowGroup.allShadows());
			}
		}

		//compute all active shadows in the program (including unreachable ones)
		final Set<Shadow> allActiveShadows = Shadow.allActiveShadows(true);
		
		//disable all shadows that are contained in no consistent shadow group
		final Map<AdviceDecl,Integer> adviceToNumTotal  = new HashMap<AdviceDecl, Integer>();
		final Map<AdviceDecl,Integer> adviceToNumDisabled = new HashMap<AdviceDecl, Integer>();
		for (Shadow shadow : allActiveShadows) {
			//count total number
			Integer num = adviceToNumTotal.get(shadow.getAdviceDecl());
			if(num==null) num = 0;
			num++;
			adviceToNumTotal.put(shadow.getAdviceDecl(),num);
			
			//if shadow can be disabled
			if(!shadowsToRetain.contains(shadow)) {
				//count
				num = adviceToNumDisabled.get(shadow.getAdviceDecl());
				if(num==null) num = 0;
				num++;
				adviceToNumDisabled.put(shadow.getAdviceDecl(),num);
				//disable
				shadow.disable();
			}
		}
		
		//give warnings
		for (AdviceDecl ad : adviceToNumTotal.keySet()) {
			Integer disabled = adviceToNumDisabled.get(ad);
			if(disabled!=null) {
				Integer total = adviceToNumTotal.get(ad);
				
				warn(ad, total, disabled);
			}
		}		

		return false;
	}

	protected void runCGPhase() {
		PackManager.v().getPack("cg").apply();
	}

	protected void warn(AdviceDecl ad, int total, int disabled) {
		String msg = disabled + " out of " + total;
		
		Main.v().getAbcExtension().forceReportError(
				ErrorInfo.WARNING,
				msg + " shadows of dependent advice will not be executed because " +
					  "their dependencies are not fulfilled.",
				ad.getPosition()
		);
	}
	
	/** 
     * {@inheritDoc}
     */
	@Override
    public void defaultSootArgs(List sootArgs) {
        //keep line numbers
        sootArgs.add("-keep-line-number");
    	//enable whole program mode
        sootArgs.add("-w");
        //disable all packs we do not need
        sootArgs.add("-p");
        sootArgs.add("wjtp");
        sootArgs.add("enabled:false");
        sootArgs.add("-p");
        sootArgs.add("wjop");
        sootArgs.add("enabled:false");
        sootArgs.add("-p");
        sootArgs.add("wjap");
        sootArgs.add("enabled:false");
        
    	//enable points-to analysis
        sootArgs.add("-p");
        sootArgs.add("cg");
        sootArgs.add("enabled:true");

        //enable Spark
        sootArgs.add("-p");
        sootArgs.add("cg.spark");
        sootArgs.add("enabled:true");

        //use on-demand points-to analysis within Spark
        sootArgs.add("-p");
        sootArgs.add("cg.spark");
        sootArgs.add("cs-demand:true");
        
        OptionsParser.v().set_tag_instructions(true);
    }
	
	@Override
	public boolean isEnabled() {
		abc.da.AbcExtension abcExtension = (abc.da.AbcExtension) Main.v().getAbcExtension();
		return abcExtension.foundDependencyKeyword() || abcExtension.forceEnableFlowInsensitiveOptimizations();
	}

}
