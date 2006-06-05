/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Eric Bodden
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
package abc.tm.weaving.weaver.tmanalysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;

import abc.weaving.aspectinfo.Var;
import abc.weaving.residues.WeavingVar;

/**
 * TODO comment
 * @author Eric Bodden
 */
public class MatchingTMSymbolWithVarsTag extends MatchingTMSymbolTag {

	protected final Map /*<SymbolName,Map<Var,Set<WeavingVar>>>*/ symbolToVarToWeavingVars;
	

	/**
	 * @param matchingSymbolIDs
	 */
	public MatchingTMSymbolWithVarsTag(Map/*<String,Map<Var,WeavingVar>>*/ symbolToFreeVars) {
		super(initializeSuper(symbolToFreeVars));
		symbolToVarToWeavingVars = symbolToFreeVars;		
	}

	private static List initializeSuper(Map symbolToFreeVars) {
		return new ArrayList(symbolToFreeVars.keySet());
	}

	/** 
	 * {@inheritDoc}
	 */
	public String toString() {
		return symbolToVarToWeavingVars.toString();
	}
	
	public boolean hasNonEmptyIntersection(MatchingTMSymbolWithVarsTag other) {
		if(equals(other)) {
			return true;
		}
		
		//TODO should we cache the results here?
		
		PointsToAnalysis pta = Scene.v().getPointsToAnalysis();		
		assert pta!=DumbPointerAnalysis.v(); // has the points-to analysis be run?
		
		for (Iterator symIter = symbolToVarToWeavingVars.keySet().iterator(); symIter.hasNext();) {
			String symId = (String) symIter.next();
			
			Map otherVarToWeavingVars = (Map) other.symbolToVarToWeavingVars.get(symId);
			if(otherVarToWeavingVars!=null) {
				Map varToWeavingVars = (Map) symbolToVarToWeavingVars.get(symId);
				
				for (Iterator varIter = varToWeavingVars.keySet().iterator(); varIter.hasNext();) {
					Var var = (Var) varIter.next();
					
					Set otherWeavingVars = (Set) otherVarToWeavingVars.get(var);
					if(otherWeavingVars!=null) {
						Set weavingVars = (Set) varToWeavingVars.get(var);
						
						Set thisPointsToSets = new HashSet();
						for (Iterator wvIter = weavingVars.iterator(); wvIter.hasNext();) {
							WeavingVar wv = (WeavingVar) wvIter.next();
							Local local = wv.get();
							PointsToSet pointsToSet = pta.reachingObjects(local);
							thisPointsToSets.add(pointsToSet);
						}
						
						for (Iterator wvIter = otherWeavingVars.iterator(); wvIter.hasNext();) {
							WeavingVar wv = (WeavingVar) wvIter.next();
							Local local = wv.get();
							PointsToSet otherPointsToSet = pta.reachingObjects(local);
							
							for (Iterator iter = thisPointsToSets.iterator(); iter
									.hasNext();) {
								PointsToSet pointsToSet = (PointsToSet) iter.next();
								if(pointsToSet.hasNonEmptyIntersection(otherPointsToSet)) {
									return true;
								}
							}
						}
						
					}					
				}
				
			}
		}
		return false;
	}
}
