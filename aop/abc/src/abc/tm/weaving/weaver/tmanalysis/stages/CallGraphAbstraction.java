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
package abc.tm.weaving.weaver.tmanalysis.stages;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import abc.main.Main;
import abc.tm.weaving.weaver.tmanalysis.callgraph.AbstractedCallGraph;
import abc.tm.weaving.weaver.tmanalysis.callgraph.NodePredicate;
import abc.tm.weaving.weaver.tmanalysis.query.ReachableShadowFinder;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger.SymbolShadowMatchTag;
import abc.tm.weaving.weaver.tmanalysis.util.Naming;
import abc.tm.weaving.weaver.tmanalysis.util.Timer;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolFinder.SymbolShadowMatch;
import abc.weaving.weaver.Weaver;

/**
 * This stage does not actually perform any real analysis. It merely applies the <i>cg</i> phase, constructing a call graph
 * and points-to sets and afterwards abstracts the call graph, pruning subgraphs which contain no methods of interest. 
 * However, afterwards it disables all unrechable shadows, which might lead to the fact that there are actually no enabled
 * shadows remaining (in the case where all active shadows were unreachable).
 *
 * @author Eric Bodden
 */
public class CallGraphAbstraction extends AbstractAnalysisStage {

    /** 
     * predicate used for call graph  abstraction
     * @see TaggedMethods
     */
    protected final NodePredicate ONLY_METHODS_WITH_MATCHED_UNITS = new TaggedMethods();
    
	/** the abstracted call graph */
	protected CallGraph abstractedCallGraph;
	
	/** set of reachable shadows */
	protected Set reachableShadowIDs;
	
	/** timer for cg-phase */
	protected Timer cgTimer = new Timer("cg-phase"); 

	/** timer for call graph abstraction */
	protected Timer cgAbstrTimer = new Timer("cg-abstraction"); 

	/**
	 * {@inheritDoc}
	 */
	protected void doAnalysis() {
        //set a main class from the options if none is set yet
        Scene.v().setMainClassFromOptions();
        
        cgTimer.startOrResume();
        
    	//build call graph
    	PackManager.v().getPack("cg").apply();

        cgTimer.stop();
		logToStatistics("cg-phase-time", cgTimer);

		CallGraph callGraph = Scene.v().getCallGraph();

		logToStatistics("cg-size-original", callGraph.size());

		cgAbstrTimer.startOrResume();
        
        //abstract the call-graph, i.e. only retain nodes which contain tagged units
        abstractedCallGraph = new AbstractedCallGraph(callGraph, ONLY_METHODS_WITH_MATCHED_UNITS);

        cgAbstrTimer.stop();
		logToStatistics("cg-abstraction-time", cgAbstrTimer);

		logToStatistics("cg-size-abstracted", abstractedCallGraph.size());

    	reachableShadowIDs = ReachableShadowFinder.v().reachableShadowIDs(abstractedCallGraph);
		logToStatistics("reachable-shadow-count", reachableShadowIDs.size());
		
    	//determine the unreachable ones by subtracting the reachable ones from all enabled ones
    	Set unreachableShadowsIDs = ShadowRegistry.v().enabledShadows();
    	unreachableShadowsIDs.removeAll(reachableShadowIDs);

    	//disable all unreachable shadows
    	disableAll(unreachableShadowsIDs);
	}
	
	/**
	 * Rebuilds the abstracted callgraph. This is desirable after additional shadows have been removed, because
	 * then after re-abstraction edges to methods containing those removed shadows can be removed as well.
	 */
	public void rebuildAbstractedCallGraph() {
		CallGraph callGraph = Scene.v().getCallGraph();
        abstractedCallGraph = new AbstractedCallGraph(callGraph, ONLY_METHODS_WITH_MATCHED_UNITS);
	}
	
	/**
	 * Returns the abstracted call graph. This graph holds no edges for methods which are not of interest to the analysis. 
	 * @return the abstracted call graph
	 */
	public CallGraph abstractedCallGraph() {
		return abstractedCallGraph;
	}
	
	/**
	 * @return the set of shadow IDs reachable via the abstracted call graph
	 * @see Naming#uniqueShadowID(String, int)
	 * @see Naming#uniqueShadowID(String, String, int)
	 */
	public Set getReachableShadowIDs() {
		return new HashSet(reachableShadowIDs);
	}
	
    /**
     * This is a predicate over call graph nodes which accepts
     * only methods which contain at least one unit which is tagged
     * with matching tracematch symbols.
     */
	private class TaggedMethods implements NodePredicate {
		
		protected Weaver weaver; 
		
		/** 
	     * @return <code>true</code> if the method is tagged with matching symbols or it 
	     * contains a unit that is tagged with matching symbols.
	     */
	    public boolean want(MethodOrMethodContext curr) {
	    	if(weaver==null) weaver = Main.v().getAbcExtension().getWeaver();    	
	    	
	    	SootMethod method = curr.method();

	        if(method.hasActiveBody()) {
	            Body body = method.getActiveBody();
	            
	            for (Iterator iter = body.getUnits().iterator(); iter.hasNext();) {
	                Unit u = (Unit) iter.next();
	                //if we have a tag
	                if(u.hasTag(SymbolShadowMatchTag.NAME)) {
	                	SymbolShadowMatchTag tag = (SymbolShadowMatchTag) u.getTag(SymbolShadowMatchTag.NAME);
						//if any shadows in the tag are still enabled 
	                	for (SymbolShadowMatch match : tag.getAllMatches()) {
							if(match.isEnabled()) {
								return true;
							}
						}
	                }
	            }
	        }
	        
	        return false;
	    }
	}
	
	//singleton pattern
	
	protected static CallGraphAbstraction instance;

	private CallGraphAbstraction() {}
	
	public static CallGraphAbstraction v() {
		if(instance==null) {
			instance = new CallGraphAbstraction();
		}
		return instance;		
	}
	
	/**
	 * Frees the singleton object. 
	 */
	public static void reset() {
		instance = null;
	}


}
