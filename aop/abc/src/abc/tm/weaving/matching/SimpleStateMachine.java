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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An abstraction of a {@link TMStateMachine}. Supports basic operations on state machines but
 * has no dependencies on the tracematch semantics. This is used by the extension abc.da, but may also be used
 * by other extensions in the future.
 * 
 * @author Eric Bodden
 */
@SuppressWarnings("unchecked")
public class SimpleStateMachine implements StateMachine {

	protected LinkedHashSet<SMEdge> edges = new LinkedHashSet<SMEdge>();
	/** List of nodes. The code generation relies on iteration order here. Hence, it has to be a *linked* hash set. */
	protected LinkedHashSet<SMNode> nodes = new LinkedHashSet<SMNode>();

	public State newState() {
	    SMNode n = new SMNode(false, false);
	    nodes.add(n);
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
	
	public void removeTransition(SMEdge edge) {
	    SMNode f = (SMNode)edge.getSource();
	    SMNode t = (SMNode)edge.getTarget();	    
	    f.removeOutEdge(edge);
	    t.removeInEdge(edge);
	    edges.remove(edge);
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
	
	public Set<SMNode> getFinalStates() {
		// In principle, we could memoize this.
		Set<SMNode> initialStates = new HashSet();
	
		for (Iterator iterator = getStateIterator(); iterator.hasNext();) {
			SMNode state = (SMNode) iterator.next();
			if(state.isFinalNode()) {
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

	public int getNumberOfStates() {
	    return nodes.size();
	}

	/**
	 * Adds a new skip loop to <code>state</code> with label <code>label</code>.
	 * @param state the state to attach the skip loop to
	 * @param label the label for the skip loop
	 */
	public void newSkipLoop(State state, String label) {
	    SMNode s = (SMNode)state;
	    SMEdge edge = new SkipLoop(s,label);
	    s.addOutgoingEdge(edge);
	    s.addIncomingEdge(edge);
	    edges.add(edge);
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
	protected void addSelfLoops(Collection/*<String>*/ declaredSymbols,boolean notToIntialState) {
	    SMNode cur;
	    Iterator it = nodes.iterator();
	    while(it.hasNext()) {
	        cur = (SMNode)it.next();
	        // Initial states always have 'true' constraints anyway.
	        if(!(notToIntialState && cur.isInitialNode())) { 
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
	 * Uses the standard powerset construction to determinise the current automaton.
	 * Assumes there are no epsilon transitions (eliminate those first).
	 */
	public TMStateMachine determinise() {
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
						if(edge.isSkipEdge()) continue;
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
	
    public void prepare(Collection<String> formals, Map<String, ? extends Collection<String>> symToVars) {
		TMStateMachine det = null;
		eliminateEpsilonTransitions();

		reverse();
		det = determinise();
		reverse();
		det.reverse();
		det = det.determinise();
		this.edges = det.edges;
		this.nodes = det.nodes;
		addSelfLoops(symToVars.keySet(),false/*also to initial state*/);

		compressStates();
		renumberStates();
		
		initBoundVars(formals);
		fixBoundVars(symToVars);

	}

	public String toString() {
	    String result = "State machine:\n==============\n";
	    SMNode cur; SMEdge edge;
	    Iterator<SMNode> it = nodes.iterator();
	    it = nodes.iterator();
	    while(it.hasNext()) {
	        cur = it.next();
	        if(cur.isInitialNode()) result += "Initial ";
	        if(cur.isFinalNode()) result += "Final ";
	        result += "State " + cur.getNumber() + "\n";
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
	 * @return returns <code>true</code> if the state machine will always remain (also) in its initial state 
	 */
	public boolean alwaysInInitialState() {
		return false;
	}
	
	/**
	 * Returns for the given symbol the numbers of the states on which this symbol
	 * has an effect, i.e. at which the symbol does not simply loop.
	 */
	public Set<Integer> getNumbersOfStatesAffectedBySymbol(String symbol) {
		Set<Integer> sourceStateNumbers = new HashSet<Integer>();
		for(Iterator<SMEdge> edgeIter = getEdgeIterator();edgeIter.hasNext();) {
			SMEdge edge = edgeIter.next();
			if(edge.getLabel().equals(symbol) && (edge.isSkipEdge() || edge.getSource()!=edge.getTarget())) {
				sourceStateNumbers.add(edge.getSource().getNumber());
			}
		}
		return sourceStateNumbers;
	}	
	
	/**
	 * initialise the boundVars fields for the meet-over-all-paths computation
	 * 
	 * @param formals all variables declared in the tracematch
	 */
	public void initBoundVars(Collection<String> formals) {
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
	 * @param symToVars tracematch, which provides a mapping from symbols
     *           to sets of bound variables
	 */
	public void fixBoundVars(Map<String, ? extends Collection<String>> symToVars) {
		// the worklist contains edges whose target has changed value
		List worklist = new LinkedList(edges);
		while (!worklist.isEmpty()) {
			SMEdge edge = (SMEdge) worklist.remove(0);
			SMNode src = edge.getSource();
			SMNode tgt = edge.getTarget();
			// now compute the flow function along this edge
			Set flowAlongEdge = new LinkedHashSet(src.boundVars);
			Collection c = symToVars.get(edge.getLabel());
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
	
	public Set<SMNode> getStates() {
		return Collections.unmodifiableSet(nodes);
	}

}
