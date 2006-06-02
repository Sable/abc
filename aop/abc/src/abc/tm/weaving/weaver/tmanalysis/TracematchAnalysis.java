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
import java.util.List;

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
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.TMStateMachine;
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
	 * The state machine reflecting the transition structure
	 * of the complete program.
	 */
	protected TMStateMachine completeStateMachine;
    
    /** 
     * Performs the static analysis for tracematches. This currently consists of the following
     * steps:
     * <ol>
     * <li> Tag each first unit that is matched by a tracematch symbol at each shadow with
     *      the symbols that match it.
     * <li> Build an abstracted call graph. This graph holds only nodes (i.e. methods) which
     *      hold at least one unit that was tagged in the previous step. Edges are over-
     *      approximated in a sound way.
     * <li> For each method in the abstracted call graph and for each entry point build
     *      a finite state machine reflecting its transition structure w.r.t. all tracematches.
     *      This means that in the state machine there exists an edge <i>(q,l,p)</i> if
     *      the program can move from its global state <i>q</i> to <i>p</i> by causing
     *      an event that is matched by the symbol <i>l</i>.
     *      Further they hold a special edge <i>(q,i,p)</i> for each invoke statment <i>i</i>
     *      that leads from a program state <i>q</i> before the invocation to a state
     *      <i>p</i> after the invocation.
     *      Those state machines further have the special form that they have a unique
     *      starting and end state. (by means of epsilon transitions)
     * <li> Using the abstracted call graph, interprocedurally combine those automata by
     *      inlining the invoke edges: An edge <i>(q,i,p)</i> is replaced by epsilon edges
     *      leading from <i>q</i> to the unique starting nodes of the automata of 
     *      all possible callees of <i>i</i> plus epsilon transitions leading from all
     *      final states in those callee automata to <i>p</i>.
     * </ol>
     * Epsilon transitions and unreachable states are removed immediately whenever appropriate.
     */
    public boolean analyze() {
        
    	//this performs a quick test that can always be applied:
    	//we see if actually all of the per-symbol matched at some point;
    	//if one of them did not match, we remove all edges that
    	//hold this symbol; also, if then the final state becomes unreachable,
    	//we remove the tracematch entirely
    	//TODO maybe we should actually immediately reweave, when edges were eliminated
    	//that way in order to speed up the subsequent analysis
    	removeNonMatchingSymbols();
    	
        //for convenience, in a first step, add tags to all "first" units
        //at which at least tracematch symbol matches;
        //the tag holds IDs for all symbols that match this unit
        new MatchingTMSymbolTagger().performTagging();
        
        //build the abstracted call graph
        buildAbstractedCallGraph();
        
        //build a state machine reflecting the transition
        //structure of a method for each method
        buildUnitGraphStateMachines();
        
        //fold all state machines interprocedurally
        buildInterproceduralAbstraction();
        
        //specialize the state machine w.r.t. the interprocedural abstraction
        //FIXME Reenable
        specializeStateMachines();
        
        //TODO needs still some work
        //freeVariables();
        
        //we do not need to reweave right away
        return false;
    }

	/**
	 * For all tracematches, checks if all their symbols actually applied anywhere.
	 * If not, edges with that symbol are removed and if by doing so the final states
	 * become unreachable, we remove the tracematch entirely.
	 */
	protected void removeNonMatchingSymbols() {		
		TMGlobalAspectInfo gai = (TMGlobalAspectInfo) abc.main.Main.v().getAbcExtension().getGlobalAspectInfo();
		
		//for all tracematches
		for (Iterator iter = gai.getTraceMatches().iterator(); iter.hasNext();) {
			TraceMatch tm = (TraceMatch) iter.next();

			//remove non matching symbols
			tm.removeNonMatchingSymbols();			
		}
	}
	
	/**
	 * Specializes all state machines with respect to the global analysis information.
	 */
	protected void specializeStateMachines() {		
		TMGlobalAspectInfo gai = (TMGlobalAspectInfo) abc.main.Main.v().getAbcExtension().getGlobalAspectInfo();
		
		//for all tracematches
		for (Iterator iter = gai.getTraceMatches().iterator(); iter.hasNext();) {
			TraceMatch tm = (TraceMatch) iter.next();

			tm.removeUnusedEdges(completeStateMachine);			
		}
	}

//	/**
//	 * TODO move the computation of this mapping to TraceMatch itself;
//	 * also store the mapping there.
//	 */
//	private void freeVariables() {
//		
//		TMGlobalAspectInfo gai = (TMGlobalAspectInfo) abc.main.Main.v().getAbcExtension().getGlobalAspectInfo();
//		
//		PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
//
//		//for all advice applications of all methods in all weavable classes...
//		for (Iterator classIter = gai.getWeavableClasses().iterator(); classIter.hasNext();) {
//			SootClass clazz = ((AbcClass) classIter.next()).getSootClass();
//			
//			for (Iterator methodIter = clazz.getMethods().iterator(); methodIter.hasNext();) {
//				SootMethod method = (SootMethod) methodIter.next();
//				
//				MethodAdviceList adviceList = gai.getAdviceList(method);
//				if(adviceList!=null) {
//					for (Iterator iter = gai.getAdviceList(method).allAdvice().iterator(); iter.hasNext();) {
//						AdviceApplication aa = (AdviceApplication) iter.next();
//						//TODO filter for TM advice?
//						Set weavingVariables = aa.getFreeVariablesAsSootLocals();
//						
//						for (Iterator varIter = weavingVariables.iterator(); varIter
//								.hasNext();) {
//							Local local = (Local) varIter.next();
//							
//							System.out.println("LOCAL: " + local.getName());
//							System.out.println(pta.reachingObjects(local));
//							
//						}
//					}
//				}
//			}
//		}
//	}
	
	/**
	 * Folds/inlines all state machines interprocedurally.
	 */
	protected void buildInterproceduralAbstraction() {
		completeStateMachine = new TMStateMachine();
		//create an initial state; this reflects the initial
		//program state
		State initialState = completeStateMachine.newState();
		initialState.setInitial(true);
		
		//and a final state; this reflects the final program state
		State finalState = completeStateMachine.newState();
		finalState.setFinal(true);
		
		//for all entry points
		for (Iterator iter = Scene.v().getEntryPoints().iterator(); iter.hasNext();) {
			MethodOrMethodContext entryPoint = (MethodOrMethodContext) iter.next();
			
			//at this point every method should have an associated state machine
			assert entryPoint.method().hasTag(UGStateMachineTag.NAME);
			//get the state machine
			UGStateMachineTag smTag = (UGStateMachineTag) entryPoint.method().getTag(UGStateMachineTag.NAME);
			UGStateMachine stateMachine = smTag.getStateMachine();
			
			//fold it, using the call graph for context information
			stateMachine = stateMachine.fold(abstractedCallGraph);

			//insert the state machine into the one for the complete program
			completeStateMachine.insertStateMachine(initialState, stateMachine, finalState);			
		}
		
		completeStateMachine.cleanup();
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
        //for all edges in the abstracted call graph
        while(reader.hasNext()) {
            Edge edge = (Edge) reader.next();
            //build state machine for source method
            buildUnitGraphStateMachine(edge.getSrc().method());
            //build state machine for target method
            buildUnitGraphStateMachine(edge.getTgt().method());
        }
        
        //also have to build those for all entry points due
        //to peculiarities of the call graph abstraction
        //algorithm
        
		//for all entry points
		for (Iterator iter = Scene.v().getEntryPoints().iterator(); iter.hasNext();) {
			MethodOrMethodContext entryPoint = (MethodOrMethodContext) iter.next();
			//build state machine
			buildUnitGraphStateMachine(entryPoint.method());
		}        
    }

    /**
     * For a given method,
     * builds a state machine reflecting the transition structure
     * of the graph. This state machine is then attached
     * to the method with a tag.
     * @param method the method to process
     */
    protected void buildUnitGraphStateMachine(SootMethod method) {
        //TODO check can be speeded up by using a set of methods
    	//if no state machine is associated yet
        if(!method.hasTag(UGStateMachineTag.NAME)) {
            //build an initial unit graph
            UnitGraph eg = new ExceptionalUnitGraph(method.getActiveBody());
            
            //build a state machine reflecting its transition structure
            UGStateMachine sm = new UGStateMachine(eg);
            
            //UnitGraph ag = new AbstractedUnitGraph(eg, pred);
            method.addTag(new UGStateMachineTag(sm));
        }
    }
    
    /** 
     * {@inheritDoc}
     */
    public void defaultSootArgs(List sootArgs) {
    	//enable paddle points-to analysis
//        sootArgs.add("-p");
//        sootArgs.add("cg");
//        sootArgs.add("enabled:true");
//        sootArgs.add("-p");
//        sootArgs.add("cg.paddle");
//        sootArgs.add("enabled:true");
//        sootArgs.add("-p");
//        sootArgs.add("cg.paddle");
//        sootArgs.add("backend:javabdd");
    }

}
