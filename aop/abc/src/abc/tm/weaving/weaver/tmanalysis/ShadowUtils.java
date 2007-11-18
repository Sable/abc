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
package abc.tm.weaving.weaver.tmanalysis;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroupRegistry;
import abc.tm.weaving.weaver.tmanalysis.stages.CallGraphAbstraction;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowTag;
import abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow;

/**
 * Some utility methods for looking up shadows.
 *
 * @author Eric Bodden
 */
public class ShadowUtils {

    /**
     * Returns a set of all active shadows for the given {@link TraceMatch} at the given unit.
     */
    public static Set<ISymbolShadow> getAllActiveShadows(TraceMatch tm, Unit unit) {
    	return getAllActiveShadows(tm, Collections.singleton(unit));
    }
    
    /**
     * Returns a set of all active shadows for the given {@link TraceMatch} in the given list of units.
     */
    public static Set<ISymbolShadow> getAllActiveShadows(TraceMatch tm, Collection<? extends Unit> units) {
        Set<ISymbolShadow> allShadows = new HashSet<ISymbolShadow>();
    	for (Unit unit : units) {
            if(unit.hasTag(SymbolShadowTag.NAME)) {
            	SymbolShadowTag tag = (SymbolShadowTag) unit.getTag(SymbolShadowTag.NAME);
            	for (ISymbolShadow match : tag.getMatchesForTracematch(tm)) {
    				if(match.isEnabled()) {
    					allShadows.add(match);
    				}
    			}
            }
    	}
        return allShadows;
    }
    
    
    /**
     * Returns the collection of <code>ISymbolShadow</code>s triggered in transitive callees from <code>s</code>.
     * @param s any statement
     */
    protected static Collection<ISymbolShadow> transitivelyCalledShadows(Stmt s) {
        CallGraph abstractedCallGraph = CallGraphAbstraction.v().abstractedCallGraph();
        HashSet<ISymbolShadow> symbols = new HashSet<ISymbolShadow>();
        HashSet<SootMethod> calleeMethods = new HashSet<SootMethod>();
        LinkedList<MethodOrMethodContext> methodsToProcess = new LinkedList();

        // Collect initial edges out of given statement in methodsToProcess
        Iterator<Edge> initialEdges = abstractedCallGraph.edgesOutOf(s);
        while (initialEdges.hasNext()) {
            Edge e = initialEdges.next();
            methodsToProcess.add(e.getTgt());
            calleeMethods.add(e.getTgt().method());
        }

        // Collect transitive callees of methodsToProcess
        while (!methodsToProcess.isEmpty()) {
            MethodOrMethodContext mm = methodsToProcess.removeFirst();
            Iterator mIt = abstractedCallGraph.edgesOutOf(mm);

            while (mIt.hasNext()) {
                Edge e = (Edge)mIt.next();
                if (!calleeMethods.contains(e.getTgt().method())) {
                    methodsToProcess.add(e.getTgt());
                    calleeMethods.add(e.getTgt().method());
                }
            }
        }

        // Collect all shadows in calleeMethods
        for (SootMethod method : calleeMethods) {
            if(method.hasActiveBody()) {
                Body body = method.getActiveBody();
                
                for (Iterator iter = body.getUnits().iterator(); iter.hasNext();) {
                    Unit u = (Unit) iter.next();
                    if(u.hasTag(SymbolShadowTag.NAME)) {
                        SymbolShadowTag tag = (SymbolShadowTag) u.getTag(SymbolShadowTag.NAME);
                        for (ISymbolShadow match : tag.getAllMatches()) {
                            if(match.isEnabled()) {
                                symbols.add(match);
                            }
                        }
                    }
                }
            }
        }
        return symbols;
    }


    /**
     * Computes the set of all shadows that share a shadow group with one of the shadows in the input set.
     */
    public static Set<String> sameShadowGroup(Set<ISymbolShadow> shadows) {
        Set<String> overlappingShadows = new HashSet<String>();
        for (ISymbolShadow shadowHere : shadows) {
            Set<String> overlappingShadowsForShadow = ShadowGroupRegistry.v().getShadowIdsOfShadowsInSameGroups(shadowHere);
            overlappingShadows.addAll(overlappingShadowsForShadow);
        }
        return overlappingShadows;
    }

}
