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
package abc.da.weaving.weaver.depadviceopt.ds;

import java.util.List;
import java.util.Map;

import abc.da.weaving.aspectinfo.DAGlobalAspectInfo;
import abc.main.Main;

import soot.PointsToSet;

/**
 * A comparator that compares two shadows via an indirection on their variable names.
 * In a dependency declarations, advice formal names can differ from the actual advice formal names.
 * This comparator takes this difference into account.
 */
public class ShadowComparator {

	protected Map<String,List<String>> adviceNameToVars; 
	
	public ShadowComparator(Map<String, List<String>> adviceNameToVarsFilter) {
		this.adviceNameToVars = adviceNameToVarsFilter;
	}

	public boolean compatibleBindings(Shadow s1, Shadow s2) {

		//assert that both shadows actually belong to a dependent advice
		final DAGlobalAspectInfo gai = (DAGlobalAspectInfo) Main.v().getAbcExtension().getGlobalAspectInfo();
		assert gai.isDependentAdvice(s1.getAdviceDecl());
		assert gai.isDependentAdvice(s2.getAdviceDecl());
		
		String adviceName1 = gai.replaceForHumanReadableName(gai.qualifiedNameOfAdvice(s1.getAdviceDecl()));
		String adviceName2 = gai.replaceForHumanReadableName(gai.qualifiedNameOfAdvice(s2.getAdviceDecl()));
		List<String> filter1 = adviceNameToVars.get(adviceName1);
		List<String> filter2 = adviceNameToVars.get(adviceName2);
		assert filter1!=null && filter2!=null;
		
		//walk through the filter for the first shadow
		for(int i1 = 0; i1<filter1.size(); i1++) {
			//extract variable name
			String abstractVariableName1 = filter1.get(i1);
			//walk through the filter of the other shadows...
			for(int i2 = 0; i2<filter2.size(); i2++) {
				//...extracting the variable names there...
				String abstractVariableName2 = filter2.get(i2);
				//...and look for positions where we find the same variable
				if(abstractVariableName1.equals(abstractVariableName2)) {
					//extract both points-to sets (in terms of the original
					//variable names)
					PointsToSet pts1 = s1.pointsToSetOf(s1.variableNames().get(i1));
					PointsToSet pts2 = s2.pointsToSetOf(s2.variableNames().get(i2));
					//if they do not overlap, return false
					if(!pts1.hasNonEmptyIntersection(pts2)) {
						return false;
					}
				}
			}				
		}
		
		return true;
	}

}
