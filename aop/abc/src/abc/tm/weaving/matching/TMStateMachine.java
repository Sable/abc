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

public class TMStateMachine implements StateMachine {

    protected LinkedHashSet<SMEdge> edges = new LinkedHashSet<SMEdge>();

	/** List of nodes. The code generation relies on iteration order here. Hence, it has to be a *linked* hash set. */
    protected LinkedHashSet<SMNode> nodes = new LinkedHashSet<SMNode>();
    
    public State newState() {
        SMNode n = new SMNode(this, false, false);
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
     * Assumes the from and to variables are actually the relevant implementations from
     * the abc.tm.weaving.matching package -- will throw ClassCastException otherwise
     * @return 
     */
    public SMEdge newTransition(State from, State to, String s) {
        SMNode f = (SMNode)from;
        SMNode t = (SMNode)to;
        SMEdge edge = new SMEdge(f, t, s);
        f.addOutgoingEdge(edge);
        t.addIncomingEdge(edge);
        edges.add(edge);
        return edge;
    }

    /**
     * Adds a new skip loop to <code>state</code> with label <code>label</code>.
     * @param state the state to attach the skip loop to
     * @param label the label for the skip loop
     */
    protected void newSkipLoop(State state, String label) {
        SMNode s = (SMNode)state;
        SMEdge edge = new SkipLoop(s,label);
        s.addOutgoingEdge(edge);
        s.addIncomingEdge(edge);
        edges.add(edge);
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
     * Transforms the NFA into an NFA without epsilon transitions. Here the algorithm:
     * 
     * Define the 'closure' of a node N, closure(N), to be the set of all nodes which can
     * be reached by zero or more epsilon transitions from N. Thus N is always in closure(N).
     * 
     * In a first pass, for each node N, for each node N' in closure(N) such that N != N', 
     * copy each non-epsilon incoming transition to N onto N'.
     * 
     * In a second pass, delete each epsilon transition.
     * 
     * This will leave some nodes inaccessible from the initial nodes and some from which
     * one cannot reach a final node -- those should be deleted in a clean-up pass which
     * should be done anyway.
     */
    public void eliminateEpsilonTransitions() {
        LinkedHashSet closure = new LinkedHashSet();
        SMNode cur, next;
        SMEdge edge;
        Iterator closureIt, edgeIt;
        // For each node...
        Iterator stateIt = nodes.iterator();
        while(stateIt.hasNext()) {
            closure.clear();
            cur = (SMNode)stateIt.next();
            // .. construct the epsilon-closure
            cur.fillInClosure(closure, true);
            // .. and, ignoring the node itself
            closure.remove(cur);
            closureIt = closure.iterator();
            boolean isInitial = cur.isInitialNode();

            // .. for every node in the closure
            while(closureIt.hasNext()) {
            		next = (SMNode)closureIt.next();
            		// .. for every edge coming into the original node
            		edgeIt = cur.getInEdgeIterator();
            		while(edgeIt.hasNext()) {
            			edge = (SMEdge)edgeIt.next();
            			// .. copy that edge onto the node from the closure if it isn't an epsilon transition
            			if(edge.getLabel() != null && !edge.getSource().hasEdgeTo(next, edge.getLabel())) {
            				newTransition(edge.getSource(), next, edge.getLabel());
            			}
            		}
            		// Any node in the closure of an initial node is initial.
            		if(isInitial) next.setInitial(true);
            }
        }
        // For each edge...
        edgeIt = edges.iterator();
        while(edgeIt.hasNext()) {
            edge = (SMEdge)edgeIt.next();
            if(edge.getLabel() == null) {
                edge.getTarget().removeInEdge(edge);
                edge.getSource().removeOutEdge(edge);
                edgeIt.remove();
            }
        }
    }
    
    /**
     * compute all states that are forwards-reachable from an initial state
     * @return reachable states
     */
    private Set<SMNode> initReachable() {
    	Set<SMNode> result = new LinkedHashSet<SMNode>();
		for(Iterator it=getStateIterator(); it.hasNext(); ) {
				   SMNode node = (SMNode) it.next();
				   if (node.isInitialNode())
					   node.fillInClosure(result,false,true);
	   }
        return result;
    }
    
    /**
     * compute all the states that are backwards reachable from a final state
     * @return set of reachable states
     */
	private Set<SMNode> finalReachable() {
		Set<SMNode> result = new LinkedHashSet<SMNode>();
		for(Iterator it=getStateIterator(); it.hasNext(); ) {
				SMNode node = (SMNode) it.next();
				if (node.isFinalNode())
					node.fillInClosure(result,false,false);
		}
		return result;
	}
    
    /**
     * Removes 'unneeded' states -- i.e. states that cannot possibly lie on a path from
     * an initial state to a finnal state. Assumes there are no epsilon transitions (not
     * sure if this is necessary, though).
     */
    public void compressStates() {
        // TODO: This might be better done with flags on the nodes...
        Set<SMNode> initReachable = initReachable();
        Set<SMNode> finalReachable = finalReachable();
       
        // The set of nodes we need to keep is (initReachable intersect finalReachable), 
        LinkedHashSet<SMNode> nodesToRemove = new LinkedHashSet<SMNode>(nodes);
        initReachable.retainAll(finalReachable); // nodes that are both init- and final-reachable
        nodesToRemove.removeAll(initReachable);  // -- we want to keep them
        
        // iterate over all nodes we want to remove and remove them, i.e. destroy their edges
        Iterator it = nodesToRemove.iterator();
        while(it.hasNext()) {
            SMNode cur = (SMNode)it.next();
            Iterator edgeIt = cur.getOutEdgeIterator();
            while(edgeIt.hasNext()) {
                SMEdge edge = (SMEdge)edgeIt.next();
                edge.getTarget().removeInEdge(edge);
                edges.remove(edge);
                edgeIt.remove(); // call this rather than removeOutEdge, as we mustn't
                                 // alter the collection while iterating over it
            }
            edgeIt = cur.getInEdgeIterator();
            while(edgeIt.hasNext()) {
                SMEdge edge = (SMEdge)edgeIt.next();
                edge.getSource().removeOutEdge(edge);
                edges.remove(edge);
                edgeIt.remove(); // call this rather than removeInEdge, as we mustn't
                                 // alter the collection while iterating over it
            }
            nodes.remove(cur);
        }
    }
    
    /**
     * Part of the automaton construction -- we add a skip self-loop to every state, and
     * a self-loop for each declared symbol for initial states. Compare email from Oege
     * from 13:22 04/07/05.
     * 
     * Ammendment: We don't actually include the self loops on initial states, since their
     * constraints are always considered true (we match all suffixes, i.e. we can always be
     * in an initial state).
     * 
     * This assumes that no state already has a skip loop. Skips are empty labels (as opposed
     * to null labels, which represent epsilon transitions -- those should have been eliminated).
     */
    protected void addSelfLoops(Collection/*<String>*/ declaredSymbols) {
        SMNode cur;
        Iterator it = nodes.iterator();
        while(it.hasNext()) {
            cur = (SMNode)it.next();
            // Initial states always have 'true' constraints anyway.
	        if(!cur.isInitialNode()) { 
	        	//for each symbol...
	        	for (Iterator symIter = declaredSymbols.iterator(); symIter.hasNext();) {
					String symbolName = (String) symIter.next();
					//... for which there does not exist a loop already at the state...					
					if(!cur.hasEdgeTo(cur, symbolName)) {
						//... add a skip loop for that symbol
						newSkipLoop(cur, symbolName);
					}
	            }
	        }
	    }
    }
    
    /**
     * To avoid having to update automaton states after *every* joinpoint, our implementation
     * crucially depends on the fact that the only transitions labelled with 'skip' are self-
     * loops.
     * 
     * The natural construction for that is to add self loops to all states (this is done by
     * addSelfLoops(), which should be called immediately before this method), then forming
     * another automaton Q with two states, self-loops for all symbols and skip on the first state
     * and transitions for every symbol to the second and final state, and taking the automaton
     * product of the two.
     * 
     * However, note that, since the first state of Q has a self-loop for every symbol and for
     * skip, every state that is paired with this state retains all transitions. Also, any non-
     * final state that is paired with the second state of Q is not final and has no outgoing 
     * edges, hence can be ignored. Thus the only interesting additions are the final states
     * that are paired with Q's second state.
     * 
     * We can obtain an equivalent automaton by doing the following for each final state S: Mark
     * it as non-final, and create a new node N (which will represent S paired with Q's second 
     * state). For each incoming edge of S that is not a skip, create an incoming edge on N from 
     * the same node and with the same label. Mark N as final. 
     * 
     * Finally, observe that no final node has outgoing edges, so we can obtain an automaton 
     * that's equivalent by collapsing all the resulting final states into a single state whose
     * set of incoming edges is the union of the incoming edges for the nodes we're collapsing
     * into it.
     */
    protected void removeSkipToFinal() {
        SMNode cur;
        SMNode newFinalNode = null;
        SMEdge edge;

	// Try to find a final state in the current automaton that can be used, i.e. that only
	// has an outgoing transition labelled with 'skip'.
	LinkedHashSet<SMNode> finalNodes = new LinkedHashSet<SMNode>();
        Iterator it = nodes.iterator();
	while(it.hasNext()) {
	    cur = (SMNode)it.next();
	    if(cur.isFinalNode()) finalNodes.add(cur);
	}
	it = finalNodes.iterator();
	while(it.hasNext()) {
	    cur = (SMNode)it.next();
	    boolean suitable = true;
	    Iterator edgeIt = cur.getOutEdgeIterator();
	    while(edgeIt.hasNext()) {
		edge = (SMEdge)edgeIt.next();
		if(!edge.isSkipEdge() || (edge.getTarget() != cur)) {
		    suitable = false;
		    break;
		}
	    }
	    if(suitable) {
		newFinalNode = cur;
		edgeIt = cur.getOutEdgeIterator();
		while(edgeIt.hasNext()) {
		    edge = (SMEdge)edgeIt.next();
		    edgeIt.remove();
		    edge.getTarget().removeInEdge(edge);
		    edges.remove(edge);
		}
		break;
	    }
	}
	if(newFinalNode == null) newFinalNode = (SMNode)newState();

	it = nodes.iterator();
        while(it.hasNext()) {
            cur = (SMNode)it.next();
            if(cur.isFinalNode()) {
                cur.setFinal(false);
                Iterator edgeIt = cur.getInEdgeIterator();
                while(edgeIt.hasNext()) {
                    edge = (SMEdge)edgeIt.next();
                    if(!edge.isSkipEdge() && !edge.getSource().hasEdgeTo(newFinalNode, edge.getLabel())) { 
                    	// i.e. if not a skip-edge and not a duplicate
                        newTransition(edge.getSource(), newFinalNode, edge.getLabel());
                    }
                }
            }
        }
        newFinalNode.setFinal(true);
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
        fixBoundVars(tm);
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
		 * initialise the boundVars fields for the meet-over-all-paths computation
		 * 
		 * @param formals all variables declared in the tracematch
		 */
	private void initBoundVars(Collection<String> formals) {
			// we want a maximal fixpoint so for all final nodes the
			// starting value is the empty set
			// and for all other nodes it is the set of all formals
			for (Iterator edgeIter = getStateIterator(); edgeIter.hasNext(); ) {
				SMNode node = (SMNode) edgeIter.next();
				if (node.isInitialNode())
					node.boundVars = new LinkedHashSet<String>();
				else
					node.boundVars = new LinkedHashSet<String>(formals); 
			}
		}

	/**
		 * do fixpoint iteration using a worklist of edges
		 * 
		 * @param tm tracematch, which provides a mapping from symbols
         *           to sets of bound variables
		 */
		private void fixBoundVars(TraceMatch tm) {
			// the worklist contains edges whose target has changed value
			List worklist = new LinkedList(edges);
			while (!worklist.isEmpty()) {
				SMEdge edge = (SMEdge) worklist.remove(0);
				SMNode src = edge.getSource();
				SMNode tgt = edge.getTarget();
				// now compute the flow function along this edge
				Set flowAlongEdge = new LinkedHashSet(src.boundVars);
				Collection c = tm.getVariableOrder(edge.getLabel());
				if (c != null)
				   flowAlongEdge.addAll(c);
				// if tgt.boundVars is already smaller, skip
				if (!flowAlongEdge.containsAll(tgt.boundVars)) {
				   // otherwise compute intersection of 
				   // tgt.boundVars and flowAlongEdge
				   tgt.boundVars.retainAll(flowAlongEdge);
				   // add any edges whose target has been affected to
				   // the worklist
				   for (Iterator edgeIter=edges.iterator(); edgeIter.hasNext(); ) {
					   SMEdge anotherEdge = (SMEdge) edgeIter.next();
					   if (anotherEdge.getSource() == tgt && 
							!worklist.contains(anotherEdge))
						worklist.add(0,anotherEdge);	
				   }
				}
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
     * Renumbers the states, starting from 0 and going in the iteration order of the
     * nodes set. Can break state-constraint class associations, so only call this once
     * after the FSA is fully transformed. Node numbers are -1 prior to this method
     * being called.
     */
    public void renumberStates() {
        int cnt = 0;
        Iterator it = nodes.iterator();
        while(it.hasNext()) {
            ((SMNode)it.next()).setNumber(cnt++);
        }
    }

    /**
     * Reverses the automaton (i.e. flip the direction of every edge, make final states initial
     * and initial states final).
     */
    protected void reverse() {
    		for(Iterator edgeIt = this.edges.iterator(); edgeIt.hasNext(); ) {
    			((SMEdge)edgeIt.next()).flip();
    		}
    		for(Iterator nodeIt = this.nodes.iterator(); nodeIt.hasNext(); ) {
    			SMNode node = (SMNode)nodeIt.next();
    			boolean init = node.isFinalNode();
    			boolean fin = node.isInitialNode();
    			node.setInitial(init);
    			node.setFinal(fin);
    		}
    }
    
    /**
     * Uses the standard powerset construction to determinise the current automaton.
     * Assumes there are no epsilon transitions (eliminate those first).
     */
    protected TMStateMachine determinise() {
    		TMStateMachine result = new TMStateMachine();
    		HashMap nodeMap = new HashMap();
    		
    		// Create the initial state of the new automaton
    		LinkedHashSet initialNodeSet = new LinkedHashSet();
    		for(Iterator nodeIt = nodes.iterator(); nodeIt.hasNext(); ) {
    			SMNode node = (SMNode)nodeIt.next();
    			if(node.isInitialNode()) initialNodeSet.add(node);
    		}
    		nodeMap.put(initialNodeSet, result.newState());
    		
    		// add the initial state to the worklist
    		LinkedList worklist = new LinkedList();
    		worklist.add(initialNodeSet);
    		
    		// While we have things in the worklist...
    		while(!worklist.isEmpty()) {
    			LinkedHashSet curSet = (LinkedHashSet)worklist.remove(0);
    			HashMap/*<String,LinkedHashSet>*/ succForSym = new HashMap();
    			// ... for each of the nodes in the next worklist item...
    			for(Iterator nodeIt = curSet.iterator(); nodeIt.hasNext(); ) {
    				SMNode node = (SMNode)nodeIt.next();
    				// ... for each outgoing edge of that node...
    				for(Iterator edgeIt = node.getOutEdgeIterator(); edgeIt.hasNext(); ) {
    					SMEdge edge = (SMEdge)edgeIt.next();
    					if(succForSym.get(edge.getLabel()) == null)
    						succForSym.put(edge.getLabel(), new LinkedHashSet());
    					// record that the target of the edge is reachable via a transition with the label.
    					((LinkedHashSet)succForSym.get(edge.getLabel())).add(edge.getTarget());
    				}
    			}
    			// Then, for each of the sets reachable with transitions of a given label, ...
    			for(Iterator symIt = succForSym.keySet().iterator(); symIt.hasNext(); ) {
    				String sym = (String)symIt.next();
    				if(nodeMap.get(succForSym.get(sym)) == null) { 
    					nodeMap.put(succForSym.get(sym), result.newState());
    					worklist.addLast(succForSym.get(sym));
    				}
    				// if the DFA doesn't have a corresponding transition, add it.
    				SMNode from = (SMNode)nodeMap.get(curSet);
    				SMNode to = (SMNode)nodeMap.get(succForSym.get(sym));
    				if(!from.hasEdgeTo(to, sym))
    					result.newTransition(from, to, sym);
    			}
    		}
    		// Finally, determine initial and final states of the new automaton.
    		// The only initial node is the one we started off with.
    		((SMNode)nodeMap.get(initialNodeSet)).setInitial(true);
    		
    		// A node is final if its nodeset contains a node that was final in the NFA.
    		for(Iterator setIt = nodeMap.keySet().iterator(); setIt.hasNext(); ) {
    			LinkedHashSet curSet = (LinkedHashSet)setIt.next();
    			boolean isFinal = false;
    			for(Iterator nodeIt = curSet.iterator(); nodeIt.hasNext() && !isFinal; ) {
    				isFinal |= ((SMNode)nodeIt.next()).isFinalNode();
    			}
    			((SMNode)nodeMap.get(curSet)).setFinal(isFinal);
    		}
		return result;
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
	    det.addSelfLoops(tm.getSymbols());
	    det.removeSkipToFinal();
	    
	    det.compressStates();
	    det.renumberStates();
	}
	addSelfLoops(tm.getSymbols());
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
    
    public Iterator getStateIterator() {
        return nodes.iterator();
    }

    public Iterator getEdgeIterator() {
        return edges.iterator();
    }

	public Set<SMNode> getInitialStates() {
		// In principle, we could memoize this.
		Set<SMNode> initialStates = new HashSet();

		for (Iterator iterator = getStateIterator(); iterator.hasNext();) {
			SMNode state = (SMNode) iterator.next();
			if(state.isInitialNode()) {
				initialStates.add(state);
			}
		}
		return initialStates;
	}

    public SMNode getStateByNumber(int n) {
        Iterator i = getStateIterator();
        while (i.hasNext()) {
            SMNode node = (SMNode) i.next();
            if (node.getNumber() == n)
                return node;
        }
        throw new RuntimeException("Looking up state number " + n +
                        ", but it does not exist.\n" + this);
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

    public int getNumberOfStates() {
        return nodes.size();
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
    
}
