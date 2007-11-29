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
package abc.tm.weaving.weaver.tmanalysis.subanalyses;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.pointer.LocalMustNotAliasAnalysis;
import soot.jimple.toolkits.pointer.StrongLocalMustAliasAnalysis;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.weaver.tmanalysis.ShadowUtils;
import abc.tm.weaving.weaver.tmanalysis.Statistics;
import abc.tm.weaving.weaver.tmanalysis.ds.Configuration;
import abc.tm.weaving.weaver.tmanalysis.ds.FinalConfigsUnitGraph;
import abc.tm.weaving.weaver.tmanalysis.ds.PreciseSymmetricDisjunct;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolShadow;

/**
 * UnnecessaryShadowsElimination
 *
 * @author Eric Bodden
 */
public class UnnecessaryShadowsElimination {
    
    public static boolean apply(TraceMatch tm, UnitGraph g, Map<Local, Stmt> tmLocalsToDefStatements, StrongLocalMustAliasAnalysis localMustAliasAnalysis, LocalMustNotAliasAnalysis localNotMayAliasAnalysis) {
        
        System.err.println("Running optimization 'unncessary shadows elimination'...");

        //get all active shadows in the method
        Set<ISymbolShadow> allMethodShadows = ShadowUtils.getAllActiveShadows(tm,g.getBody().getUnits());
        
        //generate an augmented unit graph modelling all possible outgoing executions from the method
        DirectedGraph<Unit> augmentedGraph = new FinalConfigsUnitGraph(g,g.getBody().getMethod(),allMethodShadows,tm);
        
        Collection<Stmt> allStmts = new HashSet<Stmt>();
        for (Iterator<Unit> unitIter = augmentedGraph.iterator(); unitIter.hasNext();) {
            Stmt st = (Stmt)unitIter.next();
            allStmts.add(st);
        }
        
        Collection<Stmt> bodyStmts = new HashSet<Stmt>();
        for (Unit u : g.getBody().getUnits()) {
            Stmt st = (Stmt)u;
            bodyStmts.add(st);
        }
    
        Statistics.v().currAnalysis = UnnecessaryShadowsElimination.class;
        Statistics.v().currMethod = g.getBody().getMethod();
        
        IntraProceduralTMFlowAnalysis flowAnalysis = new IntraProceduralTMFlowAnalysis(
                tm,
                augmentedGraph,
                g.getBody().getMethod(),
                tmLocalsToDefStatements,
                new PreciseSymmetricDisjunct(g.getBody().getMethod(),tm),
                new HashSet<State>(),
                allStmts,
                localMustAliasAnalysis,
                localNotMayAliasAnalysis,
                false
        );
        
        Statistics.v().commitdataSet();
        
        Status status = flowAnalysis.getStatus();
        System.err.println("Analysis done with status: "+status);

        if(status.isAborted()) {
            return false;
        }

        //initialise to all shadows in the method
        Set<String> shadowsToDisable = new HashSet<String>(SymbolShadow.uniqueShadowIDsOf(allMethodShadows));

        if(status.hitFinal()) {
            //ReachingActiveShadowsAnalysis reachingShadows = new ReachingActiveShadowsAnalysis(augmentedGraph,tm);

        	Set<Configuration> allConfigs = new HashSet<Configuration>();
            for (Unit unit : augmentedGraph) {    
                Set<Configuration> flowBefore = flowAnalysis.getFlowBefore(unit).configurations;
                Set<Configuration> flowAfter = flowAnalysis.getFlowAfter(unit).configurations;
                allConfigs.addAll(flowBefore);
                allConfigs.addAll(flowAfter);
            }
            
            for (Configuration configuration : allConfigs) {
                if(configuration.isTainted()) {
                	shadowsToDisable.removeAll(configuration.getHistoryAtAllStates());
                } else if(configuration.hasHitFinal()) {
                	shadowsToDisable.removeAll(configuration.getHistoryAtFinalStates());
                }
			}
        } 
        
        for (String shadowId : shadowsToDisable) {
            ShadowRegistry.v().disableShadow(shadowId);
        }

        Statistics.v().shadowsUnnecessaryShadows += shadowsToDisable.size();
        
        if(shadowsToDisable.size()==allMethodShadows.size()) {
            System.err.println("Optimization 'unncessary shadows elimination' removed all shadows.");
            assert ShadowUtils.getAllActiveShadows(tm,g.getBody().getUnits()).isEmpty();
            
            return true;
        } else {
            return false;
        }
    }


}
