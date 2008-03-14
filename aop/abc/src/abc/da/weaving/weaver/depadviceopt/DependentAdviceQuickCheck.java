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
import abc.main.Main;
import abc.main.options.OptionsParser;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
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
		
		//find all fulfilled dependencies
		Set<AdviceDependency> fulfilledAdviceDependencies = new HashSet<AdviceDependency>(); 
		for (AdviceDependency dep : adviceDependencies) {
			if(dep.fulfillsQuickCheck()) {
				fulfilledAdviceDependencies.add(dep);
			}
		}

		//if we have optimization potential
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
					if(isDependent && !dependentAdviceToKeepAlive.contains(aa.advice)) {
						aa.setResidue(NeverMatch.v());
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
							//enough to warn one time, even if the same advice has many shadows
							warnShadow(aa);
						}
					}
				});
			}
			
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
