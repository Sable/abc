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
import abc.da.ast.DAAdviceDecl;
import abc.da.weaving.aspectinfo.AdviceDependency;
import abc.da.weaving.aspectinfo.DAGlobalAspectInfo;
import abc.main.Main;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
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
	 * {@inheritDoc}
	 */
	public boolean analyze() {
		
		final DAGlobalAspectInfo gai = (DAGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();
		//perform "type checks" in the back-end (for dependencies that were added as aspect-info
		if(!gai.consistencyCheckForDependentAdvice()) {			
			//found error
			return false;
		}

		Set<AdviceDependency> adviceDependencies = gai.getAdviceDependencies();
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
				boolean isDependent = ad.getFlags().intersects(DAAdviceDecl.DEPENDENT);
				if(isDependent) {
					String adviceName = gai.replaceForHumanReadableName(ad.getQualifiedAdviceName());
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
					boolean isDependent = aa.advice.getFlags().intersects(DAAdviceDecl.DEPENDENT);
					if(isDependent && !dependentAdviceToKeepAlive.contains(aa.advice)) {
						aa.setResidue(NeverMatch.v());
					}
				}			
			});
			
			//warn the user about each dependency that is not fulfilled 
			Set<AdviceDependency> nonFulfilled = new HashSet<AdviceDependency>(adviceDependencies);
			nonFulfilled.removeAll(fulfilledAdviceDependencies);
			for (AdviceDependency ad : nonFulfilled) {
				warn(ad);
			}
			
			//and warn the user about each removed shadow
			final Set<AbstractAdviceDecl> warned = new HashSet<AbstractAdviceDecl>();			
			AdviceApplicationVisitor.v().traverse(new AdviceApplicationVisitor.AdviceApplicationHandler() {

				public void adviceApplication(AdviceApplication aa, SootMethod m) {
					boolean isDependent = aa.advice.getFlags().intersects(DAAdviceDecl.DEPENDENT);
					if(isDependent && !dependentAdviceToKeepAlive.contains(aa.advice)) {
						//enough to warn one time, even if the same advice has many shadows
						if(!warned.contains(aa.advice)) {
							warn(aa);
							warned.add(aa.advice);
						}
					}
				}
			});		
			
		}
		
		
		return false;
	}

	protected void warn(AdviceDependency ad) {
		//by default, do nothing
	}

	protected void warn(AdviceApplication aa) {
		Main.v().getAbcExtension().forceReportError(
				ErrorInfo.WARNING,
				"Dependent advice will not be executed because none of its dependencies are fulfilled.",
				aa.advice.getPosition());
	}	
	
	@Override
	public boolean isEnabled() {
		abc.da.AbcExtension abcExtension = (abc.da.AbcExtension) Main.v().getAbcExtension();
		return abcExtension.foundDependencyKeyword() || abcExtension.forceEnableQuickCheck();
	}

}
