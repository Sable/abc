/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Pavel Avgustinov
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

import java.util.*;

import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import polyglot.types.SemanticException;
import polyglot.util.ErrorInfo;
import polyglot.util.Position;
import polyglot.util.ErrorQueue;
import abc.main.Debug;
import abc.polyglot.util.ErrorInfoFactory;

/**
 * Implementation of the StateMachine interface for tracematch matching
 * @author Pavel Avgustinov
 */

public class TMStateMachine implements StateMachine {

    protected LinkedHashSet edges = new LinkedHashSet(), nodes = new LinkedHashSet();
    
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
     */
    public void newTransition(State from, State to, String s) {
        SMNode f = (SMNode)from;
        SMNode t = (SMNode)to;
        SMEdge edge = new SMEdge(f, t, s);
        f.addOutgoingEdge(edge);
        t.addIncomingEdge(edge);
        edges.add(edge);
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
    protected void eliminateEpsilonTransitions() {
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
    private Set initReachable() {
    	Set result = new LinkedHashSet();
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
	private Set finalReachable() {
		Set result = new LinkedHashSet();
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
    protected void compressStates() {
        // TODO: This might be better done with flags on the nodes...
        Set initReachable = initReachable();
        Set finalReachable = finalReachable();
       
        // The set of nodes we need to keep is (initReachable intersect finalReachable), 
        LinkedHashSet nodesToRemove = new LinkedHashSet(nodes);
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
        String l;
        Iterator it = nodes.iterator();
        while(it.hasNext()) {
            cur = (SMNode)it.next();
	    // Initial states always have 'true' constraints anyway.
	    if(!cur.isInitialNode()) 
		newTransition(cur, cur, ""); // add skip loop
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
	LinkedHashSet finalNodes = new LinkedHashSet();
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
		if(!edge.getLabel().equals("") || (edge.getTarget() != cur)) {
		    suitable = false;
		    break;
		}
	    }
	    if(suitable) {
		newFinalNode = cur;
		edgeIt = cur.getOutEdgeIterator();
		while(edgeIt.hasNext()) {
		    edge = (SMEdge)edgeIt.next();
		    cur.removeOutEdge(edge);
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
                    if(!edge.getLabel().equals("") && !edge.getSource().hasEdgeTo(newFinalNode, edge.getLabel())) { 
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
        // do a backwards analysis from the final nodes
    	//  
    	// for an edge e, the flow function is
    	//  flowAlongEdge(e)(X) = X union e.boundVars
    	//
    	// we want to compute the meet-over-all-paths solution at each state
    	initCollectableWeakRefs(tm);
    	fixCollectableWeakRefs(tm);
        collectableWeakRefsToOtherRefs(formals,notused,tm);
        initBoundVars(formals);
        fixBoundVars(tm);
        if (!formals.isEmpty())
        	generateLeakWarnings(pos);
    }
    
   	
	/**
     * initialise the collectableWeakRefs fields for the meet-over-all-paths computation
     * 
	 * @param formals all variables declared in the tracematch
	 */
	private void initCollectableWeakRefs(TraceMatch tm) {
    	// we want a maximal fixpoint so for all final nodes the
    	// starting value is the empty set
		// and for all other nodes it is the set of all non-primitive formals
    	for (Iterator edgeIter = getStateIterator(); edgeIter.hasNext(); ) {
        	SMNode node = (SMNode) edgeIter.next();
        	if (node.isFinalNode())
        		node.collectableWeakRefs = new LinkedHashSet();
        	else
        		node.collectableWeakRefs = new LinkedHashSet(tm.getNonPrimitiveFormalNames()); 
        }
	}
	
	/**
		 * initialise the boundVars fields for the meet-over-all-paths computation
		 * 
		 * @param formals all variables declared in the tracematch
		 */
	private void initBoundVars(Collection formals) {
			// we want a maximal fixpoint so for all final nodes the
			// starting value is the empty set
			// and for all other nodes it is the set of all formals
			for (Iterator edgeIter = getStateIterator(); edgeIter.hasNext(); ) {
				SMNode node = (SMNode) edgeIter.next();
				if (node.isInitialNode())
					node.boundVars = new LinkedHashSet();
				else
					node.boundVars = new LinkedHashSet(formals); 
			}
		}

	/**
	 * do fixpoint iteration using a worklist of edges
	 * 
	 * @param tm tracematch, which provides a mapping from symbols
     *           to sets of bound variables
	 */
	private void fixCollectableWeakRefs(TraceMatch tm) {
		// the worklist contains edges whose target has changed value
        List worklist = new LinkedList(edges);
        while (!worklist.isEmpty()) {
        	SMEdge edge = (SMEdge) worklist.remove(0);
        	SMNode src = edge.getSource();
        	SMNode tgt = edge.getTarget();
        	// now compute the flow function along this edge
        	Set flowAlongEdge = new LinkedHashSet(tgt.collectableWeakRefs);
        	Collection c = tm.getVariableOrder(edge.getLabel());
        	if (c != null)
        	   flowAlongEdge.addAll(c);
        	// if src.collectableWeakRefs is already smaller, skip
        	if (!flowAlongEdge.containsAll(src.collectableWeakRefs)) {
               // otherwise compute intersection of 
        	   // src.collectableWeakRefs and flowAlongEdge
        	   src.collectableWeakRefs.retainAll(flowAlongEdge);
               // add any edges whose target has been affected to
        	   // the worklist
        	   for (Iterator edgeIter=edges.iterator(); edgeIter.hasNext(); ) {
        	   	   SMEdge anotherEdge = (SMEdge) edgeIter.next();
        	   	   if (anotherEdge.getTarget() == src && 
        	   	   		!worklist.contains(anotherEdge))
        	   	   	worklist.add(0,anotherEdge);	
        	   }
        	}
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
		        abc.main.Main.v().error_queue.enqueue
						(new ErrorInfo(ErrorInfo.WARNING,
									   msg,
									   pos));
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
     * PROTOTYPE METHOD
     *
     * Prints out which variables should be used to index the disjuncts
     * on each state.
     *
     * @param tm represents the tracematch for this automaton and has
     *           lists of the symbols used and the variables that they
     *           bind
     */
    private void chooseIndices(TraceMatch tm)
    {
        if (abc.main.Debug.v().printIndices)
        	System.out.println(this);

        Collection frequentSymbols = tm.getFrequentSymbols();

        Iterator nodeIt = nodes.iterator();
        while(nodeIt.hasNext()) {
            SMNode cur = (SMNode) nodeIt.next();

            // we do not index on the initial or final node
            if (cur.isInitialNode() || cur.isFinalNode())
                continue;

            // calculate indices[i] = intersect[sym] (bound[i] /\ binds[sym])
            //   BUT only for the symbols where the inner
            //       intersection is not empty
            //
            // if some symbols have been annotated as frequent
            // then only consider them when making indexing decisions

            Iterator symIt =
                frequentSymbols == null ? tm.getSymbols().iterator()
                                        : frequentSymbols.iterator();

            HashSet indices = new HashSet(cur.boundVars);
            while (symIt.hasNext()) {
                String symbol = (String) symIt.next();

                if (frequentSymbols != null
                        && !frequentSymbols.contains(symbol))
                    continue;

                HashSet tmp = new HashSet(cur.boundVars);
                tmp.retainAll(tm.getVariableOrder(symbol));

                if (!tmp.isEmpty())
                    indices.retainAll(tmp);
            }

            HashSet collectable = new HashSet(indices);
            collectable.retainAll(cur.collectableWeakRefs);
            HashSet primitive = new HashSet(indices);
            primitive.removeAll(tm.getNonPrimitiveFormalNames());
            HashSet weak = new HashSet(indices);
            weak.retainAll(cur.weakRefs);

            indices.removeAll(collectable);
            indices.removeAll(primitive);
            indices.removeAll(weak);


            if (abc.main.Debug.v().printIndices) {
                System.out.println("State " + cur.getNumber());
                System.out.println(" - collectable indices: " + collectable);
                System.out.println(" -   primitive indices: " + primitive);
                System.out.println(" -        weak indices: " + weak);
                System.out.println(" -       other indices: " + indices);
            }

            // index by collectable weak-references first, so that whole
            // subtrees of the index will be automatically thrown away
            cur.indices.addAll(collectable);
            cur.indices.addAll(primitive);
            cur.indices.addAll(weak);
            cur.indices.addAll(indices);
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
        chooseIndices(tm);
    }
    
    public Iterator getStateIterator() {
        return nodes.iterator();
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
    
    public String toString() {
        String result = "State machine:\n==============\n";
        java.util.Map stateNumbers = new java.util.HashMap();
        SMNode cur; SMEdge edge;
        int cnt = 0;
        Iterator it = nodes.iterator();
        while(it.hasNext()) {
            stateNumbers.put(it.next(), new Integer(cnt++));
        }
        it = nodes.iterator();
        while(it.hasNext()) {
            cur = (SMNode)it.next();
            if(cur.isInitialNode()) result += "Initial ";
            if(cur.isFinalNode()) result += "Final ";
            result += "State " + stateNumbers.get(cur) + " (";
            result += "needStrongRefs" + cur.needStrongRefs + ", ";
            result += "collectableWeakRefs" + cur.collectableWeakRefs + ", ";
			result += "weakRefs" + cur.weakRefs + ", ";
			result += "boundVars" + cur.boundVars + ")\n";
            Iterator edgeIt = cur.getOutEdgeIterator();
            while(edgeIt.hasNext()) {
                edge = (SMEdge)edgeIt.next();
                result += "  -->[" + (edge.getLabel() == "" ? "SKIP" : edge.getLabel()) 
                        + "] to State " + stateNumbers.get(edge.getTarget()) + "\n";
            }
        }
        return result;
    }
}
