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

package abc.tmwpopt;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.util.Position;
import abc.da.HasDAInfo;
import abc.da.weaving.aspectinfo.AdviceDependency;
import abc.da.weaving.aspectinfo.DAInfo;
import abc.da.weaving.weaver.depadviceopt.DependentAdviceFlowInsensitiveAnalysis;
import abc.da.weaving.weaver.depadviceopt.DependentAdviceQuickCheck;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.main.options.OptionsParser;
import abc.tm.weaving.aspectinfo.PerSymbolTMAdviceDecl;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.TMWeaver;
import abc.tmwpopt.fsanalysis.OutputDotGraphs;
import abc.tmwpopt.fsanalysis.Ranking;
import abc.tmwpopt.fsanalysis.Statistics;
import abc.tmwpopt.fsanalysis.SymbolNames;
import abc.tmwpopt.fsanalysis.ds.Constraint;
import abc.tmwpopt.fsanalysis.ds.Disjunct;
import abc.tmwpopt.fsanalysis.stages.IntraproceduralAnalysis;
import abc.tmwpopt.tmtoda.PathInfoFinder;
import abc.tmwpopt.tmtoda.PathInfoFinder.PathInfo;
import abc.weaving.weaver.ReweavingPass;
import abc.weaving.weaver.Weaver;
import abc.weaving.weaver.ReweavingPass.ID;

/**
 * Abc extension for static whole-program analysis of tracematches. This extension currently exists of three different
 * static analyses:
 * <ol>
 * <li> a quick check, (ECOOP 2007)
 * <li> a flow-insensitive analysis to detect orphan shadows, (ECOOP 2007) and 
 * <li> a flow-sensitive abstract interpretation of tracematches, which is largely intraprocedural.
 * </ol>
 * In this version of abc, the first two analyses are completely reused from the abc.da extension. We simply generate advice
 * dependencies from each tracematch and abc.da then simply performs the {@link DependentAdviceQuickCheck} and {@link DependentAdviceFlowInsensitiveAnalysis}
 * to resolve these dependencies. Therefore, this extension uses parts of both, abc.tm and abc.da.
 * 
 * <b>WARNING:</b> This extension only re-uses abc.da in the backend, not in the frontend! Therefore, users cannot define both dependent advice and
 * tracematches in source code right now!
 * 
 * @author Eric Bodden
 */
public class AbcExtension extends abc.tm.AbcExtension implements HasDAInfo
{
	
    protected static final ID TM_INTRA_FLOWSENS = new ReweavingPass.ID("flow-sensitive intraprocedural analysis for tracematches");
	
	/**
	 * The abc.da extension that we use to conduct the quick-check and flow-insensitive analysis.
	 */
	protected abc.da.AbcExtension daExtension = new abc.da.AbcExtension() {
		@Override
		public void resetAnalysisDataStructures() {
			super.resetAnalysisDataStructures();
			//when resetting data structures, make sure to reset our data structures, too
			AbcExtension.this.resetAnalysisDataStructures();
		}
		
		@Override
		protected DependentAdviceFlowInsensitiveAnalysis createFlowInsensitiveAnalysis() {
			return new DependentAdviceFlowInsensitiveAnalysis() {
				@Override
				public void warn(Shadow s, String msg) {
					//if this extension is enabled, only warn when removing shadows that belong to per-symbol advice
					//(otherwise we would report for sync/some/body advice as well)
					if(s.getAdviceDecl() instanceof PerSymbolTMAdviceDecl)
						super.warn(s, msg);
				}
			};
		}
		
		@Override
		protected DependentAdviceQuickCheck createQuickCheck() {
			return new DependentAdviceQuickCheck() {
				protected void warnShadow(abc.weaving.matching.AdviceApplication aa) {
					//if this extension is enabled, only warn when removing shadows that belong to per-symbol advice
					//(otherwise we would report for sync/some/body advice as well)
					if(aa.advice instanceof PerSymbolTMAdviceDecl) {
						super.warnShadow(aa);
					}
				}
			};
		}
	};

	/** The flow-sensitive abstract interpretation that we use. */
	protected IntraproceduralAnalysis flowSensitiveAnalysis;
	
	public AbcExtension() {
		//if this extension is enabled, we want to warn the user about each individual shadow being removed
		//by abc.da, and not just summary information
		OptionsParser.v().set_warn_about_individual_shadows(true);
	}

    protected void collectVersions(StringBuffer versions)
    {
        super.collectVersions(versions);
        versions.append(" with TraceMatching and Whole-Program Optimizations " +
                        new abc.tm.Version().toString() +
                        "\n");
    }
    
	/**
	 * Returns the {@link DAInfo} of abc.da.
	 */
	public DAInfo getDependentAdviceInfo() {
		return daExtension.getDependentAdviceInfo();
	}
	
	@Override
	public Weaver createWeaver() {
		return new TMWeaver() {
			@Override
			public void weaveGenerateAspectMethods() {
				super.weaveGenerateAspectMethods();
				//for each tracematch, register appropriate advice dependencies with abc.da
				for (TraceMatch tm : ((TMGlobalAspectInfo)getGlobalAspectInfo()).getTraceMatches()) {
					registerAdviceDependencies(tm);
				}
			}
		};
	}
	
    /**
     * Registers all necessary advice dependencies for the given tracematch with the abc.da extension.
     * @param tm a tracematch
     */
    protected void registerAdviceDependencies(TraceMatch tm) {
    	DAInfo dai = getDependentAdviceInfo();
    	
    	Set<PathInfo> pathInfos = new PathInfoFinder(tm).getPathInfos();
    	for (PathInfo pathInfo : pathInfos) {
			Map<String,List<String>> strongAdviceNameToVars = new HashMap<String, List<String>>();
			Set<String> strongSymbols = new HashSet<String>(pathInfo.getDominatingLabels());
			for (String strongSymbol : strongSymbols) {
				List<String> variableOrder = tm.getVariableOrder(strongSymbol);
				String adviceName = tm.getSymbolAdviceMethod(strongSymbol).getName();
				strongAdviceNameToVars.put(adviceName, variableOrder);
				dai.registerDependentAdvice(tm.getContainer().getName()+"."+adviceName);
			}
			
			Map<String,List<String>> weakAdviceNameToVars = new HashMap<String, List<String>>();
			Set<String> weakSymbols = new HashSet<String>(pathInfo.getSkipLoopLabels());
			weakSymbols.removeAll(strongSymbols);	//symbols that are strong, don't need to be declared weak as well
			for (String weakSymbol : weakSymbols) {
				List<String> variableOrder = tm.getVariableOrder(weakSymbol);
				String adviceName = tm.getSymbolAdviceMethod(weakSymbol).getName();
				weakAdviceNameToVars.put(adviceName, variableOrder);
				dai.registerDependentAdvice(tm.getContainer().getName()+"."+adviceName);
			}
			
			//synch, some and body advice are also weak; they take no parameters
			weakAdviceNameToVars.put(tm.getSynchAdviceMethod().getName(),Collections.<String>emptyList());
			weakAdviceNameToVars.put(tm.getSomeAdviceMethod().getName(),Collections.<String>emptyList());
			weakAdviceNameToVars.put(tm.getName()+"$body",Collections.<String>emptyList());
			dai.registerDependentAdvice(tm.getContainer().getName()+"."+tm.getSynchAdviceMethod().getName());
			dai.registerDependentAdvice(tm.getContainer().getName()+"."+tm.getSomeAdviceMethod().getName());
			dai.registerDependentAdvice(tm.getContainer().getName()+"."+tm.getName()+"$body");
			
			AdviceDependency adviceDependency = new AdviceDependency(
					strongAdviceNameToVars,
					weakAdviceNameToVars,
					tm.getContainer(),
					Position.compilerGenerated()
			);
			dai.addAdviceDependency(adviceDependency);			
		}
    }

    
	/**
	 * Adds a reweaving pass for the flow-sensitive abstract interpretation (if enabled).
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void createReweavingPasses(List passes) {
		super.createReweavingPasses(passes);
		daExtension.createReweavingPasses(passes);
		
		if(!OptionsParser.v().laststage().equals("quick")
		&& !OptionsParser.v().laststage().equals("flowins")) {
			passes.add(new ReweavingPass(TM_INTRA_FLOWSENS,flowSensitiveAnalysis()));			
		}
	}
	
	/**
	 * Returns the quick check of abc.da.
	 */
    	public DependentAdviceQuickCheck quickCheck() {
		return daExtension.quickCheck();
	}

	/**
	 * Returns the flow-insensitive analysis of abc.da.
	 */
	public DependentAdviceFlowInsensitiveAnalysis flowInsensitiveAnalysis() {
		return daExtension.flowInsensitiveAnalysis();
	}
	
	/**
	 * Creates a new {@link IntraproceduralAnalysis}. Other extensions may override this method
	 * to create subclasses of {@link IntraproceduralAnalysis} instead.
	 */
	protected IntraproceduralAnalysis createFlowSensitiveAnalysis() {
		return new IntraproceduralAnalysis();
	}
	
	/**
	 * Returns the singleton of the flow-sensitive abstract interpretation for tracematches.  
	 */
	public IntraproceduralAnalysis flowSensitiveAnalysis() {
		if(flowSensitiveAnalysis==null)
			flowSensitiveAnalysis = createFlowSensitiveAnalysis();
		return flowSensitiveAnalysis;
	}

	/**
	 * @inheritDoc
	 */
	public void resetAnalysisDataStructures() {
		Ranking.reset();
		Statistics.reset();
		SymbolNames.reset();
		Disjunct.reset();
		Constraint.reset();
		OutputDotGraphs.reset();
	}
}
