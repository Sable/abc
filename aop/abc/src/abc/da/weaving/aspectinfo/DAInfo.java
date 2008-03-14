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
package abc.da.weaving.aspectinfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import polyglot.util.ErrorInfo;
import abc.main.Main;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.GlobalAspectInfo;

/**
 * An enhanced version of {@link GlobalAspectInfo} which keeps track of advice names
 * and advice dependencies.
 * @author Eric Bodden
 */
public class DAInfo {

	protected Map<String,String> adviceMethodNameToAdviceShortName = new HashMap<String,String>();
	protected Set<AdviceDependency> adviceDependencies = new HashSet<AdviceDependency>();
	
	/**
	 * Registers a new advice dependency.
	 */
	public void addAdviceDependency(AdviceDependency ad) {
		adviceDependencies.add(ad);
	}
	
	/**
	 * Returns the unmodifiable set of all advice dependencies.
	 */
	public Set<AdviceDependency> getAdviceDependencies() {
		return Collections.unmodifiableSet(adviceDependencies);		
	}
	
	/**
	 * Registers a dependent advice.
	 */
	public void registerDependentAdvice(String internalAdviceName) {
		registerDependentAdvice(internalAdviceName,internalAdviceName);
	}
	
	/**
	 * Registers a dependent advice and a human-readable name for it (the name given to it in the frontend and in the
	 * dependency declarations).
	 * Both names must be qualified by the aspect type's name.
	 */
	public void registerDependentAdvice(String internalAdviceName, String humanReadableName) {
		if(!internalAdviceName.contains(".")) {
			throw new IllegalArgumentException("Internal advice name has to be qualified!");
		}
		if(adviceMethodNameToAdviceShortName.containsKey(internalAdviceName)) {
			if(!adviceMethodNameToAdviceShortName.get(internalAdviceName).equals(humanReadableName))
				throw new RuntimeException("already registered different human readable name!");
		}
		
		
		if(!humanReadableName.contains(".")) {
			throw new IllegalArgumentException("Human readable advice name has to be qualified!");
		}
		adviceMethodNameToAdviceShortName.put(internalAdviceName, humanReadableName);
	}
	
	/**
	 * Returns for a fully qualified internal advice name the fully qualified human-readable name
	 * if such a name was previously registered. Otherwise, the original name is returned. 
	 */
	public String replaceForHumanReadableName(String internalAdviceName) {
		String hrn = adviceMethodNameToAdviceShortName.get(internalAdviceName);
		if(hrn!=null) {
			return hrn;
		} else {
			return internalAdviceName;
		}
	}
	
	/**
	 * Returns <code>true</code> if the given advice declaration resembles a dependent advice.
	 */
	public boolean isDependentAdvice(AbstractAdviceDecl ad) {
		//can only be a dependent advice if it is a proper AdviceDecl 
		if(ad instanceof AdviceDecl) {
			AdviceDecl adviceDecl = (AdviceDecl) ad;
			String fullName = qualifiedNameOfAdvice(adviceDecl);
			return adviceMethodNameToAdviceShortName.containsKey(fullName);
		}
		return false;
	}

	public String qualifiedNameOfAdvice(AdviceDecl adviceDecl) {
		return adviceDecl.getAspect().getName()+"."+adviceDecl.getImpl().getSootMethod().getName();
	}
	
	/**
	 * Performs a consistency check on dependent advice declarations. Each dependent advice must be mentioned in at
	 * least one dependency declaration. Also each dependency declaration may only refer to existing dependent advice. 
	 * @return <code>false</code> is an error was found
	 */
	public boolean consistencyCheckForDependentAdvice() {
		boolean foundError = false;
		
		GlobalAspectInfo gai = Main.v().getAbcExtension().getGlobalAspectInfo();
		
		Set<String> qualifiedDependentAdviceNamesDeclared = new HashSet<String>();
		for (AbstractAdviceDecl ad : gai.getAdviceDecls()) {
			if(isDependentAdvice(ad)) {				
				qualifiedDependentAdviceNamesDeclared.add(replaceForHumanReadableName(qualifiedNameOfAdvice((AdviceDecl) ad)));
			}
		}
		
		Set<String> qualifiedDependentAdviceNamesFound = new HashSet<String>();		
		//check that each advice mentioned really exists in code
		for (AdviceDependency dep : getAdviceDependencies()) {
			for (String qualifiedAdviceName  : dep.adviceNames()) {
				if(!qualifiedDependentAdviceNamesDeclared.contains(qualifiedAdviceName)) {
					Main.v().getAbcExtension().forceReportError(ErrorInfo.SEMANTIC_ERROR,
					"Advice with name '"+qualifiedAdviceName+
							"' mentioned in dependency is not found in aspect "+dep.getContainer()+"!", dep.getPosition());
					foundError = true;
				}
				qualifiedDependentAdviceNamesFound.add(qualifiedAdviceName);
			}
			
			if(dep.strongAdviceNameToVars.isEmpty()) {
				Main.v().getAbcExtension().forceReportError(ErrorInfo.SEMANTIC_ERROR,
						"Dependency group declares no strong advice!", dep.getPosition());
				foundError = true;
			}
		}
		
		for (AbstractAdviceDecl ad : gai.getAdviceDecls()) {
			if(isDependentAdvice(ad)) {
				String qualified = replaceForHumanReadableName(qualifiedNameOfAdvice((AdviceDecl) ad));
				if(!qualifiedDependentAdviceNamesFound.contains(qualified)) {
					Main.v().getAbcExtension().forceReportError(ErrorInfo.SEMANTIC_ERROR,
							"Dependent advice '"+qualified+"' is never " +
							"referenced in any dependency declaration.", ad.getPosition());
					foundError = true;
				}
			}
		}
		return !foundError;
	}
	
}
