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

import java.util.Iterator;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.queue.QueueReader;

import abc.tm.weaving.weaver.tmanalysis.callgraph.AbstractedCallGraph;
import abc.tm.weaving.weaver.tmanalysis.callgraph.NodePredicate;
import abc.weaving.weaver.AbstractReweavingAnalysis;

/**
 * @author Eric Bodden
 */
public class TracematchAnalysis extends AbstractReweavingAnalysis {

    /**
     * This is a predicate over call graph nodes which accepts
     * only methods which contain at least one unit which is tagged
     * with matching tracematch symbols.
     */
    protected final NodePredicate ONLY_METHODS_WITH_MATCHED_UNITS = new NodePredicate() {

        /** 
         * @return <code>true</code> if the method contains a unit that is tagged
         * with matching symbols
         */
        public boolean want(MethodOrMethodContext curr) {
            SootMethod method = curr.method();
            if(method.hasActiveBody()) {
                Body body = method.getActiveBody();
                
                for (Iterator iter = body.getUnits().iterator(); iter.hasNext();) {
                    Unit u = (Unit) iter.next();
                    if(u.hasTag(MatchingTMSymbolTag.NAME)) {
                        return true;
                    }
                }
            }
            
            return false;
        }
        
    };
    
    /**
     * The abstracted call graph we operate on.
     */
    protected transient CallGraph abstractedCallGraph;

    /** 
     * TODO comment
     */
    public boolean analyze() {
        
        //for convenience, in a first step, add tags to all "first" units
        //at which at least tracematch symbol matches;
        //the tag holds IDs for all symbols that match this unit
        new MatchingTMSymbolTagger().performTagging();
        
        //build the abstracted call graph
        buildAbstractedCallGraph();
        
        //build a state machine reflecting the transition
        //structure of a method for each method
        buildUnitGraphStateMachines();
        
        //we do not need to reweave right away
        return false;
    }

    /**
     * Builds an abstracted call graph holding only nodes which
     * contain matches for symbols.
     */
    protected void buildAbstractedCallGraph() {
        //build call graph
        PackManager.v().getPack("cg").apply();
        CallGraph callGraph = Scene.v().getCallGraph();
        //abstract it, i.e. only retain nodes which contain tagged units
        abstractedCallGraph = new AbstractedCallGraph(callGraph, ONLY_METHODS_WITH_MATCHED_UNITS);        
    }
    
    /**
     * For each method in the abstract call graph,
     * builds a state machine reflecting the transition structure
     * of the graph. This state machine is then attached
     * to the method with a tag.
     */
    protected void buildUnitGraphStateMachines() {
        QueueReader reader = abstractedCallGraph.listener();        
        while(reader.hasNext()) {
            Edge edge = (Edge) reader.next();
            abstractUnitGraph(edge.getSrc().method());
            abstractUnitGraph(edge.getTgt().method());
        }
    }

    /**
     * For a given method,
     * builds a state machine reflecting the transition structure
     * of the graph. This state machine is then attached
     * to the method with a tag.
     * @param method the method to process
     */
    protected void abstractUnitGraph(SootMethod method) {
        //TODO check can be speeded up by using a set of methods
        if(!method.hasTag(UGStateMachineTag.NAME)) {
            //build an initial unit graph
            UnitGraph eg = new ExceptionalUnitGraph(method.getActiveBody());
            
            //build a state machine reflecting its transition structure
            UGStateMachine sm = new UGStateMachine(eg);
            
            //UnitGraph ag = new AbstractedUnitGraph(eg, pred);
            method.addTag(new UGStateMachineTag(sm));
        }
    }

}
