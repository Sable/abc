/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Pavel Avgustinov
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

package abc.tm.weaving.matching;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.util.ErrorInfo;
import polyglot.util.Position;
import abc.main.Debug;
import abc.tm.weaving.aspectinfo.CollectSetSet;
import abc.tm.weaving.aspectinfo.TraceMatch;

/**
 * Implementation of the StateMachine interface for tracematch matching
 * @author Pavel Avgustinov
 * @author Eric Bodden
 */

public class TMStateMachine extends SimpleStateMachine {

	public State newState() {
	    SMNode n = new SMNode(this,false, false);
	    nodes.add(n);
	    return n;
	}

    // for creating new nodes without adding them to the nodes collection, e.g. while
    // iterating over it
    protected State newStateDontAdd() {
        SMNode n = new SMNode(this, false, false);
        return n;
    }

    /**
	 * Eliminates epsilon transitions and unreachable states,
	 * then renumbers the states.
	 */
	public void cleanup() {
		eliminateEpsilonTransitions();
		compressStates();
		renumberStates();
	}
	
	/**
     * Accumulates, for each state, information about which tracematch vars must be
     * stored using a strong reference. We want to use a weak reference for a variable X in
     * state S if and only if every path from the S to a final state F binds X.
     * Conversely, we must keep a strong reference if and only if there is some path
     * from S to a final state that does not bind X. 
     * 
     * @param formals variables declared in tracematch
     * @param symtovar mapping from symbols to sets of bound variables
     * @param notused variables not used in tracematch body
     */
    protected void collectBindingInfo(List formals,TraceMatch tm,Collection notused,Position pos) {
    	// New and improved space leak elimination analysis.
    	computeCollectSets(tm, notused);
    	fillInCollectableWeakRefs(tm);
        collectableWeakRefsToOtherRefs(formals,notused,tm);
        initBoundVars(formals);
        Map<String, Collection<String>> symToVars = new HashMap<String, Collection<String>>();
        for(String sym: tm.getSymbols()) {
        	symToVars.put(sym,tm.getVariableOrder(sym));
        }        
        fixBoundVars(symToVars);
        if (!formals.isEmpty() && Debug.v().generateLeakWarnings)
        	generateLeakWarnings(pos);
    }
    
    /**
     * New algorithm/analysis to compute collectable weakrefs (and some more general information):
     * 
     * Inspired by the observation that if the final state has two incoming transitions, one binding
     * x and one binding y, then neither x nor y will be collectable, and yet if both of them
     * expire, we know we can never complete the match, we introduce the concept of "collect-sets".
     * For an edge, a collect-set is a set of tracematch formals which, when completely expired,
     * will prevent that edge from ever being taken again. For each non-skip edge, we compute the set
     * of collect-sets. Naturally, this is just the set of singleton sets of every formal bound by 
     * the symbol labelling the transition.
     * 
     * For a pair of states (i,j), a collect-set is a set of tracematch formals which, when completely
     * expired, prevents any direct transition from state i to state j. We can obtain the set of such
     * collect-sets for two states, collectSetsTransition(i, j), simply by taking the cross-union (x) 
     * over the collect-set-sets of all edges leading from i to j. Here, we use the term cross-union  
     * to denote the following operation:
     * 
     *  A (x) B = { a U b | a \in A && b \in B }
     *  
     *  The motivation for this is that we're looking for sufficient collect-sets that would prevent
     *  *any* of the transitions from i to j; each set in the collect-set-set constructed above contains
     *  some collect-set for each transition, and so is a collect-set for *all* transitions.
     *  
     *  To summarise,
     *  
     *  collectSetsTransition(i, j) = (X) : (i, s, j) \in Delta : collectSets((i, s, j)).
     *  
     *  Note that to keep sets small, we apply *minimisation*, defined as follows:
     *  
     *  min(A) = { a \in A | there exists no b \in A such that b is a proper subset of A }.
     *  
     *  Intuitively, if both {x, y} and {x} are collect-sets, then we don't need to remember {x, y},
     *  since whenever that has fully expired, the smaller set {x} has also fully expired.
     *  
     *  Now let us define collectSetsVia(i, j); this is the set of all collect-sets which would prevent
     *  a partial match on state i from reaching the final state via a transition to j, and can be
     *  computed simply as
     *  
     *  collectSetsVia(i, j) = collectSets(j) U collectSetsTransition(i, j).
     *  
     *  Intuitively, this says that we will not be able to reach the final state via j if either enough
     *  variables expire for the final state to be unreachable from j (collectSets(j)), or enough
     *  variables expire for any transition from i to j to be impossible.
     *  
     *  Finally, we can give an expression for the collect-set of a particular state i, collectSets(i).
     *  This is just the cross-union over all immediately reachable next-states j of collectSetsVia(i, j):
     *  
     *  collectSets(i) = (X) : (i, _, j) \in Delta : collectSetsVia(i, j).
     *  
     *  Now, collectSets(i) is recursive, and so we need a fixpoint iteration to compute it. We start off
     *  the final state with an empty collectSetSet, and all other states with universal collectSetSets (the
     *  universal set is just the powerset of the set of tracematch formals). Pavel and Julian have proved
     *  that the function in the above recursive equation is monotonic, so we will reach a fixpoint
     *  (details available upon request). Moreover, we apply minimisation at various stages during the
     *  computation, and so we also need the following to be true:
     *  
     *   min(A (x) B) = min(min(A) (x) min(B))
     *   
     *   This has also been proved by Pavel and Julian.
     *   
     *   Observe that the traditional concept of collectableWeakRef corresponds precisely to a singleton
     *   collect-set. Also, we can do advanced disjunct cleanup by checking any other collect-sets that
     *   may exist for a state. Note that if we have a collect-set of {x,y}, then x and y are still
     *   strongRefs if they are used in the tracematch body and weakRefs otherwise.
     */
    private void computeCollectSets(TraceMatch tm, Collection notUsed) {
    	int numStates = nodes.size();
    	
    	// Current and previous values for the fixpoint computation. Initialise...
    	CollectSetSet[] oldState = new CollectSetSet[numStates];
    	CollectSetSet[] newState = new CollectSetSet[numStates];
    	for(int i = 0; i < numStates; i++) {
    		if(getStateByNumber(i).isFinalNode()) {
    			oldState[i] = new CollectSetSet();
    			newState[i] = new CollectSetSet();
    		} else {
    			oldState[i] = CollectSetSet.universalSet();
    		}
    	}
    	
    	// Init collectSets for transitions between two particular states
    	CollectSetSet[][] trans = new CollectSetSet[numStates][numStates];
    	for(int i = 0; i < numStates; i++) {
			for(Iterator edgeIt = getStateByNumber(i).getOutEdgeIterator(); edgeIt.hasNext(); ) {
				SMEdge edge = (SMEdge) edgeIt.next();
				if(edge.isSkipEdge())
					continue;
				int j = edge.getTarget().getNumber();
				Collection<String> vars = new LinkedList<String>(tm.getVariableOrder(edge.getLabel()));
				vars.retainAll(tm.getNonPrimitiveFormalNames());
				CollectSetSet tmp = new CollectSetSet(vars);
				trans[i][j] = (trans[i][j] == null? tmp : trans[i][j].cross(tmp));
			}
    	}
    	
    	// Do the fixpoint iteration
    	boolean changed = true;
    	while(changed) {
    		changed = false;
    		for(int i = 0; i < numStates; i++) {
    			if(getStateByNumber(i).isFinalNode())
    				continue;
    			newState[i] = null;
    			for(int j = 0; j < numStates; j++) {
    				if(trans[i][j] != null) {
    					CollectSetSet tmp = oldState[j].union(trans[i][j]);
    					if(newState[i] == null) {
    						newState[i] = tmp;
    					} else {
    						newState[i] = newState[i].cross(tmp).minimise();
    					}
    				}
    			}
    			if(!newState[i].equals(oldState[i]))
    				changed = true;
    		}
    		CollectSetSet[] tmp = newState; newState = oldState; oldState = tmp;
    	}
    	
    	// Now we have the final configuration in oldState...
    	for(Iterator nodeIt = getStateIterator(); nodeIt.hasNext(); ) {
    		SMNode state = (SMNode) nodeIt.next();
    		state.collectSets = oldState[state.getNumber()].retainSingletonsAndSubsetsOf(notUsed);
    	}
    }
    
    private void fillInCollectableWeakRefs(TraceMatch tm) {
    	for(Iterator nodeIt = getStateIterator(); nodeIt.hasNext(); ) {
    		SMNode state = (SMNode) nodeIt.next();
    		LinkedHashSet<String> collWeakRefs = new LinkedHashSet<String>();
    		for(Iterator varIt = tm.getNonPrimitiveFormalNames().iterator(); varIt.hasNext(); ) {
    			String var = (String) varIt.next();
    			if(state.collectSets.hasSingleton(var)) {
    				collWeakRefs.add(var);
    			}
    		}
    		state.collectableWeakRefs = collWeakRefs;
    	}
    }

	 /**
	  * compute for each node n, n.needStrongRefs := complement(n.collectableWeakRefs);
	 * @param formals variables declared in the tracematch
	 */
	private void collectableWeakRefsToOtherRefs(Collection formals, Collection notUsed, TraceMatch tm) {
		// for codegen we really need the complement of src.collectableWeakRefs
        // so compute that in 
		for (Iterator stateIter = getStateIterator(); stateIter.hasNext(); ) {
			SMNode node = (SMNode) stateIter.next();
			// start with the set of all declared formals
			node.needStrongRefs = new LinkedHashSet(formals);
			if (Debug.v().onlyStrongRefs) {
				//use only strong references
				node.collectableWeakRefs.clear();
				node.weakRefs = new LinkedHashSet();
			} else if (Debug.v().noCollectableWeakRefs) {
				//use no collectable weak refs
				//i.e. make them all "usual" weak refs
				node.weakRefs.addAll(node.collectableWeakRefs);
				node.collectableWeakRefs.clear();
			} else {
			// and remove those that are in node.weakRefs and those that are not used
			// everything else is a non-collectable weakRef
			node.weakRefs = new LinkedHashSet(tm.getNonPrimitiveFormalNames());
                        node.weakRefs.removeAll(node.collectableWeakRefs);
                        node.weakRefs.retainAll(notUsed);
                        node.needStrongRefs.removeAll(node.collectableWeakRefs);
                        node.needStrongRefs.removeAll(node.weakRefs);
			}
		}
	}
	
	/**
	 * generate warnings for potential space leaks; ignoring possible null bindings for now.
	 * there ought to be a check that the weak references are not bound to null, or we
	 * should completely rule out null bindings in tracematches.
	 *
	 */
	private void generateLeakWarnings(Position pos) {
		boolean hasWarned = false;
		for (Iterator it = getStateIterator(); it.hasNext() && !hasWarned; ) {
			SMNode node = (SMNode) it.next();

			Set rebound = new HashSet(node.collectableWeakRefs);
			rebound.retainAll(node.boundVars);

			if (rebound.isEmpty() && !node.isInitialNode() && !node.isFinalNode()) {
				hasWarned = true;
				String msg="Variable bindings may cause space leak";
		        abc.main.Main.v().getAbcExtension().reportError(ErrorInfo.WARNING, msg, pos);
			}
		}
	}

	/**
       * Minimizes by the well-known (?)
       * reverse/determinize/reverse/determinize method.
       * @return the minimized automaton 
       */
      protected TMStateMachine getMinimized() {
          //reverse
          reverse();
          //create determinized copy
          TMStateMachine det = determinise();
          //restore original
          reverse();
          //do second iteration on copy
          det.reverse();
          return det.determinise();
      }
      
    /**
     * Minimizes by the well-known (?)
     * reverse/determinize/reverse/determinize method.
     */
    protected void minimize() {
        TMStateMachine minimized = getMinimized();
        this.nodes = minimized.nodes;
        this.edges = minimized.edges;
    }
    
    /**
     * Transforms the FSA that was generated from the regular expression into an NFA for
     * matching suffixes interleaved with skips and ending in a declared symbol against
     * the regular expression. Should be called once.
     * @param tm tracematch contains the set of symbols, and the variables that
     *           those symbols bind
     * @param formals list of the names of variables used (no types)
     * @param notused names of formals that are not used in the tracematch body
     * @param pos position of tracematch
     */
    public void prepareForMatching(TraceMatch tm, 
                                   List formals, 
                                   Collection notused,
                                   Position pos) {
		TMStateMachine det = null;
		eliminateEpsilonTransitions();
		
		if(!abc.main.Debug.v().useNFA) {
		    reverse();
		    det = determinise();
		    reverse();
		    det.reverse();
		    det = det.determinise();
		    det.addSelfLoops(tm.getSymbols(),true);
		    det.removeSkipToFinal();
		    
		    det.compressStates();
		    det.renumberStates();
		}
		addSelfLoops(tm.getSymbols(),true);
	        removeSkipToFinal();
		
	        compressStates();
	        renumberStates();
	
		if(!abc.main.Debug.v().useNFA) {
		    if(this.nodes.size() >= det.nodes.size()) {
			this.edges = det.edges;
			this.nodes = det.nodes;
		    }
		}

        collectBindingInfo(formals, tm, notused, pos);
    }
    
    /**
	 * Removes the given edges from the state machine, also removing it from
	 * its connected nodes.
	 * @param toRemove collection of edges to remove
	 */
	public void removeEdges(Collection/*<SMEdge>*/ toRemove) {
		Iterator edgeIterator = toRemove.iterator();
		while(edgeIterator.hasNext()) {
			SMEdge edge = (SMEdge) edgeIterator.next();
			assert edges.contains(edge);
			
			//remove the edge p-->q from the list
			edges.remove(edge);
			
			//remove it as outedge from p 
			edge.getSource().removeOutEdge(edge);
			
			//remove it as inedge from q
			edge.getTarget().removeInEdge(edge);
		}
	}

    public String toString() {
        String result = "State machine:\n==============\n";
        SMNode cur; SMEdge edge;
        Iterator it = nodes.iterator();
        it = nodes.iterator();
        while(it.hasNext()) {
            cur = (SMNode)it.next();
            if(cur.isInitialNode()) result += "Initial ";
            if(cur.isFinalNode()) result += "Final ";
            result += "State " + cur.getNumber() + " (";
            result += "needStrongRefs" + cur.needStrongRefs + ", ";
            result += "collectableWeakRefs" + cur.collectableWeakRefs + ", ";
			result += "weakRefs" + cur.weakRefs + ", ";
			result += "collectSets" + cur.collectSets + ", ";
			result += "boundVars" + cur.boundVars + ")\n";
            Iterator edgeIt = cur.getOutEdgeIterator();
            while(edgeIt.hasNext()) {
                edge = (SMEdge)edgeIt.next();
                result += "  -->[" + edge 
                        + "] to State " + edge.getTarget().getNumber() + "\n";
            }
        }
        return result;
    }
    
    /**
     * Computes all determining symbols (if any).
     * A determining symbol is a symbol that, after read on one single consistent variable binding, leads
     * for sure to a certain set of states, <i>regardless</i> on what the previous state under this binding was.
     * @param tm owning {@link TraceMatch}
     * @return a mapping from each determining symbol to the set of states that the automaton is in after reading the symbol
     */
    public Map<String,Set<SMNode>> getDeterminingSymbols(TraceMatch tm) {
    	Map<String,Set<SMNode>> res = new HashMap<String,Set<SMNode>>();

    	//for each symbol
		for (String symbolName : (Set<String>)tm.getSymbols()) {
		
			//get all non-initial states
			Set<SMNode> allStates = new HashSet<SMNode>();
			for (Iterator<SMNode> iterator = getStateIterator(); iterator.hasNext();) {
				SMNode s = (SMNode) iterator.next();
				allStates.add(s);
			}
			Set<SMNode> nonInitialStates = new HashSet<SMNode>(allStates);
			nonInitialStates.removeAll(getInitialStates());
			
			//first, compute all sets of successor nodes reachable under the given symbol from all initial states;
			//those states are always reached, in each configuration, due to the suffix semantics of tracematches
			Set<Set<SMNode>> successorSetsFromInitialStates = getSuccessorsUnderSymbolFromStates(getInitialStates(), symbolName);

			//now, compute the same for all remaining non-initial states
			Set<Set<SMNode>> successorSetsFromNonInitialStates = getSuccessorsUnderSymbolFromStates(nonInitialStates, symbolName);

			//we now have to add the successor states of all initial states to each set reachable from non-initial states (again, due to suffix semantics)
			Set<Set<SMNode>> successorSets = new HashSet<Set<SMNode>>();
			for (Set<SMNode> nonInitSet : successorSetsFromNonInitialStates) {
				for (Set<SMNode> initSet : successorSetsFromInitialStates) {
					Set<SMNode> join = new HashSet<SMNode>(nonInitSet);
					join.addAll(initSet);
					successorSets.add(join);
				}
			}
			
			//there really should at least be one set of states reachable under this symbol
			assert !successorSets.isEmpty();
			
			//if the set of possible successor states for this symbol is unique, we have good news to report 
	    	if(successorSets.size()==1) {
	    		Set<SMNode> onlyElement = successorSets.iterator().next();
				res.put(symbolName,onlyElement);
	    	}
		}
		
		return res;
    }

	/**
	 * Computes the set of possible sets of successor nodes that can be reached from the given sourceStates under the symbol with the
	 * given symbolName.
	 * @param sourceStates a set of tracematch automaton states
	 * @param symbolName a symbol name of the tracematch
	 * @return a set s containing a set t if and only if the set of successor states t can be reached via transitions labeled with symbolName
	 * from a state in sourceStates
	 */
	protected Set<Set<SMNode>> getSuccessorsUnderSymbolFromStates(Set<SMNode> sourceStates, String symbolName) {
		Set<Set<SMNode>> allSuccessorSetsUnderAllSymbols;
		allSuccessorSetsUnderAllSymbols = new HashSet<Set<SMNode>>(); 
		
		//for all possible incoming states
		for (Iterator<SMNode> iterator = sourceStates.iterator(); iterator.hasNext();) {
			SMNode s = iterator.next();
			
			//final nodes have no successors
			if(s.isFinalNode()) continue;
			
			//compute successor states under that symbols
			
			Set<SMNode> successorsUnderSymbol = new HashSet<SMNode>();
			//by default, we stay in the current node
			successorsUnderSymbol.add(s);
			for (Iterator<SMEdge> edgeIter = (Iterator<SMEdge>)s.getOutEdgeIterator(); edgeIter.hasNext();) {
				SMEdge outEdge = edgeIter.next();
				if(outEdge.getLabel().equals(symbolName)) {
					if(outEdge.isSkipEdge()) {
						//we do not stay in the current state if we have a skip-loop with that symbol
						successorsUnderSymbol.remove(s);
					} else {
						//add successor state
						successorsUnderSymbol.add(outEdge.getTarget());
					}
				}
			}
			//we also are always in all initial states
			successorsUnderSymbol.addAll(getInitialStates());
			
			allSuccessorSetsUnderAllSymbols.add(successorsUnderSymbol);
		}
		return allSuccessorSetsUnderAllSymbols;
	}
	
	@Override
	public boolean alwaysInInitialState() {
		return true;
	}
    
}
