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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import polyglot.util.ErrorInfo;
import soot.SootMethod;
import abc.da.HasDAInfo;
import abc.da.weaving.aspectinfo.AdviceDependency;
import abc.da.weaving.aspectinfo.DAInfo;
import abc.da.weaving.weaver.depadviceopt.ds.Shadow;
import abc.main.Debug;
import abc.main.Main;
import abc.main.options.OptionsParser;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.AfterAdvice;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.residues.NeverMatch;
import abc.weaving.weaver.AbstractReweavingAnalysis;
import abc.weaving.weaver.AdviceApplicationVisitor;


/**
 * Performs the Quick Check on dependent advice.
 * This disables dependent advice for which no dependency is fulfilled.
 * A dependency is fulfilled if all strong advice in the dependency match at least once
 * in the program.
 * @author Eric Bodden
 */
public class DependentAdviceQuickCheck extends AbstractReweavingAnalysis {

	protected int numEnabledDependentAdviceShadowsBefore = -1;
	protected int numEnabledDependentAdviceShadowsAfter = 0;

	/**
	 * Conducts the analysis, disabling shadows for tracematches that do not fulfill the quick check.
	 */
	public boolean analyze() {
		
		final GlobalAspectInfo gai = Main.v().getAbcExtension().getGlobalAspectInfo();
		final DAInfo dai = ((HasDAInfo) Main.v().getAbcExtension()).getDependentAdviceInfo();
		//perform "type checks" in the back-end (for dependencies that were added as aspect-info
		if(!dai.consistencyCheckForDependentAdvice()) {			
			//found error
			return false;
		}

		Set<AdviceDependency> adviceDependencies = dai.getAdviceDependencies();
		if(adviceDependencies.isEmpty()) return false;		
		
		if(Debug.v().debugDA) {
			System.err.println();
			System.err.println();
			for (AdviceDependency dep : adviceDependencies) {
				System.err.println(dep);
				System.err.println();
			}
			int largestNumberOfStrongShadows = 0;
			for (AdviceDependency dep : adviceDependencies) {
				largestNumberOfStrongShadows = Math.max(largestNumberOfStrongShadows, dep.numStrongShadows());
			}
			System.err.println("Largest number of strong shadows: "+largestNumberOfStrongShadows);
			System.err.println();
			System.err.println();
			System.err.println("da: Starting QuickCheck");
		}
		long timeBefore = System.currentTimeMillis(); 
		
		Set<AdviceDependency> fulfilledAdviceDependencies = new HashSet<AdviceDependency>(); 
		int currNumFulfilledDependencies = Integer.MAX_VALUE;
		
		final Set<SootMethod> methodsWithShadowsBeforeQC = new HashSet<SootMethod>();
		numEnabledDependentAdviceShadowsBefore = 0;
		//disable all advice applications that belong to "other" dependent advice
		AdviceApplicationVisitor.v().traverse(new AdviceApplicationVisitor.AdviceApplicationHandler() {

			public void adviceApplication(AdviceApplication aa, SootMethod m) {
				boolean isDependent = dai.isDependentAdvice(aa.advice);
				if (isDependent && !NeverMatch.neverMatches(aa.getResidue())) {
					String qualifiedNameOfAdvice = dai.qualifiedNameOfAdvice((AdviceDecl)aa.advice);
					String givenName = dai.replaceForHumanReadableName(qualifiedNameOfAdvice);
					if(givenName.endsWith("newDaCapoRun")) return;
					
					numEnabledDependentAdviceShadowsBefore++;
					if(aa.advice.getAdviceSpec() instanceof AfterAdvice) {
						//after advice generate two shadows, one for after-returning and
						//one for after-throwing
						numEnabledDependentAdviceShadowsBefore++;
					}
					methodsWithShadowsBeforeQC.add(m);
				}
			}			
		});
		numEnabledDependentAdviceShadowsAfter = numEnabledDependentAdviceShadowsBefore;

		
		final Set<SootMethod> methodsWithShadowsAfterQC = new HashSet<SootMethod>();
		/*
		 * we have to make a fixed-point iteration here (see abc-2008-2):
		 * assume the trivial NFA for "a b | b c", and assume a program where "a" does not match;
		 * if we do not iterate to the fixed point then the result depend on the order in which we
		 * process the dependencies for "a b" and "b c"
		 */
		while(true) {
		
			//find all fulfilled dependencies
			for (AdviceDependency dep : adviceDependencies) {
				if(dep.fulfillsQuickCheck()) {
					fulfilledAdviceDependencies.add(dep);
				}
			}
			
			//if we have no more optimization potential any more then break the while(true) loop
			if(fulfilledAdviceDependencies.size()==currNumFulfilledDependencies) {
				break;
			} 
			currNumFulfilledDependencies = fulfilledAdviceDependencies.size();
			
			//do we have optimization potential?
			if(fulfilledAdviceDependencies.size()<adviceDependencies.size()) {
				//determine all dependent advice that belong to dependencies that are fulfilled
				final Set<AbstractAdviceDecl> dependentAdviceToKeepAlive = new HashSet<AbstractAdviceDecl>();
				for (AbstractAdviceDecl ad : (List<AbstractAdviceDecl>)gai.getAdviceDecls()) {
					boolean isDependent = dai.isDependentAdvice(ad);
					if(isDependent) {
						String adviceName = dai.replaceForHumanReadableName(dai.qualifiedNameOfAdvice((AdviceDecl) ad));
						for (AdviceDependency dep : fulfilledAdviceDependencies) {
							if(ad.getAspect().equals(dep.getContainer()) && dep.containsAdviceNamed(adviceName)) {
								dependentAdviceToKeepAlive.add(ad);
							} 
						}
					}
				}
				
				//disable all advice applications that belong to "other" dependent advice
				AdviceApplicationVisitor.v().traverse(new AdviceApplicationVisitor.AdviceApplicationHandler() {
	
					public void adviceApplication(AdviceApplication aa, SootMethod m) {
						boolean isDependent = dai.isDependentAdvice(aa.advice);
						if (isDependent && !NeverMatch.neverMatches(aa.getResidue())) {
							String qualifiedNameOfAdvice = dai.qualifiedNameOfAdvice((AdviceDecl)aa.advice);
							String givenName = dai.replaceForHumanReadableName(qualifiedNameOfAdvice);
							if(givenName.endsWith("newDaCapoRun")) return;
							if (!dependentAdviceToKeepAlive.contains(aa.advice)) {

								numEnabledDependentAdviceShadowsAfter--;
								if(aa.advice.getAdviceSpec() instanceof AfterAdvice) {
									//after advice generate two shadows, one for after-returning and
									//one for after-throwing
									numEnabledDependentAdviceShadowsAfter--;
								}
								aa.setResidue(NeverMatch.v());
							} else {
								methodsWithShadowsAfterQC.add(m);							
							}
						}
					}			
				});
				
				if(!OptionsParser.v().warn_about_individual_shadows()) {
					//warn the user about each dependent advice that will not be executed
					final Set<AbstractAdviceDecl> warned = new HashSet<AbstractAdviceDecl>();			
					AdviceApplicationVisitor.v().traverse(new AdviceApplicationVisitor.AdviceApplicationHandler() {
		
						public void adviceApplication(AdviceApplication aa, SootMethod m) {
							boolean isDependent = dai.isDependentAdvice(aa.advice);
							if(isDependent && !dependentAdviceToKeepAlive.contains(aa.advice)) {
								String qualifiedNameOfAdvice = dai.qualifiedNameOfAdvice((AdviceDecl)aa.advice);
								String givenName = dai.replaceForHumanReadableName(qualifiedNameOfAdvice);
								if(givenName.endsWith("newDaCapoRun")) return;

								//enough to warn one time, even if the same advice has many shadows
								if(!warned.contains(aa.advice)) {
									warnAdvice(aa.advice);
									warned.add(aa.advice);
								}
							}
						}
					});
				} else {			
					//warn the user about each shadow that was removed
					AdviceApplicationVisitor.v().traverse(new AdviceApplicationVisitor.AdviceApplicationHandler() {
		
						public void adviceApplication(AdviceApplication aa, SootMethod m) {
							boolean isDependent = dai.isDependentAdvice(aa.advice);
							if(isDependent && !dependentAdviceToKeepAlive.contains(aa.advice)) {
								String qualifiedNameOfAdvice = dai.qualifiedNameOfAdvice((AdviceDecl)aa.advice);
								String givenName = dai.replaceForHumanReadableName(qualifiedNameOfAdvice);
								if(givenName.endsWith("newDaCapoRun")) return;

								//enough to warn one time, even if the same advice has many shadows
								warnShadow(aa);
							}
						}
					});
				}
			}
		}

		if(adviceDependencies.size()==fulfilledAdviceDependencies.size()) {
			methodsWithShadowsAfterQC.clear();
			methodsWithShadowsAfterQC.addAll(methodsWithShadowsBeforeQC);
		}
		
		if(Debug.v().debugDA) {
			System.err.println("da:    QuickCheck took:                           "+(System.currentTimeMillis()-timeBefore));
			System.err.println("da:    Active dependencies before QuickCheck:     "+adviceDependencies.size());  
			System.err.println("da:    Active dependencies after QuickCheck:      "+fulfilledAdviceDependencies.size());  
			System.err.println("da:    DA-Shadows enabled before QuickCheck:      "+numEnabledDependentAdviceShadowsBefore);  
			System.err.println("da:    DA-Shadows enabled after QuickCheck:       "+numEnabledDependentAdviceShadowsAfter);  
			System.err.println("da:    Methods with enabled DA-Shadows before QC: "+methodsWithShadowsBeforeQC.size());
			System.err.println("da:    Methods with enabled DA-Shadows after QC:  "+methodsWithShadowsAfterQC.size());
		}

		return false;
	}

	/**
	 * Issues a warning that a shadow at the given {@link AdviceApplication} was removed.
	 */
	protected void warnShadow(AdviceApplication aa) {
		Main.v().getAbcExtension().forceReportError(
				ErrorInfo.WARNING,
				"Dependent advice will not be executed at this position because none of its dependencies are fulfilled.",
				Shadow.extractPosition(aa.shadowmatch.getHost()));
		
	}

	/**
	 * Issues a warning that the given {@link AbstractAdviceDecl} will have no effect because none
	 * of its dependencies are fulfilled.
	 */
	protected void warnAdvice(AbstractAdviceDecl abstractAdviceDecl) {
		Main.v().getAbcExtension().forceReportError(
				ErrorInfo.WARNING,
				"Dependent advice will not be executed because none of its dependencies are fulfilled.",
				abstractAdviceDecl.getPosition());
	}	

}
