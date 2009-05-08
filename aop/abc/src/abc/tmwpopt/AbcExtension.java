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

import java.util.List;

import soot.Scene;
import soot.SootClass;
import soot.tagkit.Host;

import abc.da.HasDAInfo;
import abc.da.weaving.aspectinfo.DAInfo;
import abc.da.weaving.weaver.depadviceopt.DependentAdviceFlowInsensitiveAnalysis;
import abc.da.weaving.weaver.depadviceopt.DependentAdviceQuickCheck;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.main.Debug;
import abc.main.options.OptionsParser;
import abc.tm.weaving.aspectinfo.PerSymbolTMAdviceDecl;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.TMWeaver;
import abc.tmwpopt.tmtoda.TracePatternFromTM;
import abc.weaving.matching.SJPInfo;
import abc.weaving.weaver.ReweavingPass;
import abc.weaving.weaver.Weaver;

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
	
	/**
	 * The abc.da extension that we use to conduct the various static whole-program analyses for tracematches.
	 */
	protected abc.da.AbcExtension daExtension = new abc.da.AbcExtension() {
		@Override
		protected DAInfo createDependentAdviceInfo() {
			return new DAInfo() {
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
		}
	};

	public AbcExtension() {
		//if this extension is enabled, we want to warn the user about each individual shadow being removed
		//by abc.da, and not just summary information
		OptionsParser.v().set_warn_about_individual_shadows(true);
		
		if(Debug.v().printTMAdviceDeps) Debug.v().printIndices = true;
	}

    public void collectVersions(StringBuffer versions)
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
				DAInfo daInfo = getDependentAdviceInfo();
				//for each tracematch, register appropriate advice dependencies with abc.da
				for (TraceMatch tm : ((TMGlobalAspectInfo)getGlobalAspectInfo()).getTraceMatches()) {
					//register advice names
					for(String sym: tm.getSymbols()) {
						String adviceName = tm.getSymbolAdviceMethod(sym).getName();
						daInfo.registerDependentAdvice(tm.getContainer().getName()+"."+adviceName,
								tm.getContainer().getName()+"."+tm.getName()+"."+sym
						);
					}

					//create dependency for tracematch pattern
					daInfo.registerTracePattern(new TracePatternFromTM(tm));					
				}
				super.weaveGenerateAspectMethods();
			}
		};
	}
	
    /**
     * Registers the reweaving passes of the dependent-advice abc extension.
     */
    @Override
	public void createReweavingPasses(List<ReweavingPass> passes) {
    	super.createReweavingPasses(passes);
    	daExtension.createReweavingPasses(passes);
    }
    
    @Override
    public void addBasicClassesToSoot() {
    	super.addBasicClassesToSoot();
    	daExtension.addBasicClassesToSoot();
        if(Debug.v().traceExecution) {
        	Scene.v().addBasicClass("org.aspectbench.tm.runtime.internal.Dumper", SootClass.SIGNATURES);
        }
    }
    
}
