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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import soot.Local;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.pointer.LocalMustNotAliasAnalysis;
import soot.jimple.toolkits.pointer.StrongLocalMustAliasAnalysis;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.UnitGraph;
import soot.util.IdentityHashSet;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.weaver.tmanalysis.Statistics;
import abc.tm.weaving.weaver.tmanalysis.ShadowUtils;
import abc.tm.weaving.weaver.tmanalysis.ds.Configuration;
import abc.tm.weaving.weaver.tmanalysis.ds.PreciseSymmetricDisjunct;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis;
import abc.tm.weaving.weaver.tmanalysis.mustalias.IntraProceduralTMFlowAnalysis.Status;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.util.ISymbolShadow;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolShadow;
import abc.weaving.residues.OnceResidue;
import abc.weaving.weaver.Weaver;

/**
 * ShadowMotion
 *
 * @author Eric Bodden
 */
public class ShadowMotion {

    public static void apply(TraceMatch tm, UnitGraph g, Map<Local, Stmt> tmLocalsToDefStatements, StrongLocalMustAliasAnalysis localMustAliasAnalysis, LocalMustNotAliasAnalysis localNotMayAliasAnalysis) {
        System.err.println("Loop optimization...");

        //build a loop nest tree
        LoopNestTree loopNestTree = new LoopNestTree(g.getBody());
        if(loopNestTree.hasNestedLoops()) {
            System.err.println("Method has nested loops.");
        }
    
        if(loopNestTree.isEmpty()) {
            System.err.println("Method has no loops.");
        }
        
        //for each loop, in ascending order (inner loops first) 
        for (Loop loop : loopNestTree) {
        	if(loop.getLoopExits().size()==1) {
            	optimizeLoop(tm, g, tmLocalsToDefStatements, localMustAliasAnalysis,localNotMayAliasAnalysis, loop);
        	} else {
                System.err.println("Loop has multiple exists or no exit. Skipping.");
        	}
        }
    }

    public static void optimizeLoop(TraceMatch tm, UnitGraph g, Map<Local, Stmt> tmLocalsToDefStatements, StrongLocalMustAliasAnalysis localMustAliasAnalysis, LocalMustNotAliasAnalysis localNotMayAliasAnalysis,
            Loop loop) {
        System.err.println("Optimizing loop...");
        
        //find all active shadows in the method
        Collection<Stmt> loopStatements = loop.getLoopStatements();
        
        if(ShadowUtils.getAllActiveShadows(tm, loopStatements).isEmpty()) {
            System.err.println("Loop has no shadows.");
            return;
        }
        
        Collection<Stmt> bodyStmts = new HashSet<Stmt>();
        for (Unit u : g.getBody().getUnits()) {
            Stmt st = (Stmt)u;
            bodyStmts.add(st);
        }

        Statistics.v().currAnalysis = ShadowMotion.class;
        Statistics.v().currMethod = g.getBody().getMethod();
        
        IntraProceduralTMFlowAnalysis flowAnalysis = new IntraProceduralTMFlowAnalysis(
                tm,
                g,
                g.getBody().getMethod(),
                tmLocalsToDefStatements,
                new PreciseSymmetricDisjunct(g.getBody().getMethod(),tm),
                new HashSet<State>(),
                bodyStmts/*loopStatements*/,
                localMustAliasAnalysis,
                localNotMayAliasAnalysis,
                false
        );
        
        Statistics.v().commitdataSet();
        
        Status status = flowAnalysis.getStatus();
        System.err.println("Analysis done with status: "+status);

        //if we abort once, we are going to abort for the other additional initial states too, so
        //just return, to proceed with the next loop
        
        if(status.isAborted()) return;
        
        assert status.isFinishedSuccessfully();
    
        //check how often we had to iterate...
        for (Stmt loopExit : loop.getLoopExits()) {
            if(!flowAnalysis.statementsReachingFixedPointAtOnce().contains(loopExit)) {
                System.err.println("FP not reached after one iteration. Cannot optimize.");
                return;
            }
        }
        for (Stmt loopStmt : loop.getLoopStatements()) {
            Set<Configuration> flowBefore = flowAnalysis.getFlowBefore(loopStmt);
            //if there is an active shadow at this unit
            if(!ShadowUtils.getAllActiveShadows(tm, Collections.singleton(loopStmt)).isEmpty()) {
                if(Configuration.hasTainted(flowBefore)) {
                    System.err.println("Aborting because shadow could have been affected by calls to other methods with shadows.");
                    return;
                }
            }
        }
        
        Weaver weaver = Main.v().getAbcExtension().getWeaver();
        
        assert loop.getLoopExits().size()==1;
        Stmt loopExit = loop.getLoopExits().iterator().next();
        //walk through all statements in the loop, recording all shadows up to the loop exit;
        //we store a set for each statement that is annotated with shadows;
        //we start at the loop exit and then walk backwards through the loop in a depth-first fashion
        Set<ISymbolShadow> shadowsbeforeLoopExit = new HashSet<ISymbolShadow>();
        Set<Unit> visited = new IdentityHashSet<Unit>();
        Queue<Unit> worklist = new LinkedList<Unit>();
        worklist.add(loopExit);
        Stmt loopHead = loop.getHead();
		while(!worklist.isEmpty()) {
            Unit curr = worklist.remove();
            visited.add(curr);
            
            Set<ISymbolShadow> shadows = ShadowUtils.getAllActiveShadows(tm,Collections.singleton(curr));
            if(!shadows.isEmpty()) {
                //add to the front
                shadowsbeforeLoopExit.addAll(shadows);
            }
            //stop at the loop head
            if(curr.equals(loopHead)) {
                break;
            }
            
            //get all predecessors in the loop and add them to the worklist
            List<Unit> preds = new ArrayList<Unit>(g.getPredsOf(curr));
            preds.retainAll(loopStatements);     
            preds.removeAll(visited);
            worklist.addAll(preds);
        }            
        
        //debug output
        System.err.println("Shadows for this loop exit:");
        System.err.println(SymbolShadow.uniqueShadowIDsOf(shadowsbeforeLoopExit));
        
        Set<Stmt> initPositions = new HashSet<Stmt>(); 
        Unit reboundLoopHead = weaver.reverseRebind(loopHead);
        if(reboundLoopHead!=loopHead) {
        	initPositions.add((Stmt) reboundLoopHead);
        } else {
        	Set<Unit> preds = new HashSet<Unit>(g.getPredsOf(loopHead)); 
        	preds.removeAll(loopStatements);
            for (Unit pred : preds) {
    			Unit reboundPred = weaver.reverseRebind(pred);
    			if(reboundPred!=pred) {
    				initPositions.add((Stmt) reboundPred);
    			} else {
    				System.out.println("WARNING: Could not find a statement suitable to place initialization code. Not optimizing loop. (1)");
    				return;
    			}
    		}
        }

        if(initPositions.isEmpty()) {
			System.out.println("WARNING: Could not find a statement suitable to place initialization code. Not optimizing loop. (2)");
			return;
        }
        
        for (ISymbolShadow shadow : shadowsbeforeLoopExit) {
            Statistics.v().shadowsOnlyExecuteOnce++;
            System.err.println("Executing shadow once: "+shadow.getUniqueShadowId());
            ShadowRegistry.v().conjoinShadowWithResidue(shadow.getUniqueShadowId(), new OnceResidue(initPositions));
        }
        
    }
    
}
