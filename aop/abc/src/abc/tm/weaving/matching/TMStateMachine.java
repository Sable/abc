package abc.tm.weaving.matching;

import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map;
import java.util.Collection;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;

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
     * In a first pass, for each node N, add edges to every node that can be reached via one
     * (non-epsilon) edge from some node in closure(N). Make all nodes in the closure of any
     * initial node initial, and make a node N final if closure(N) contains a final node.
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
            boolean shouldBeFinal = false;
            // .. for every node in the closure
            while(closureIt.hasNext()) {
                next = (SMNode)closureIt.next();
                // .. add all outgoing non-epsilon transitions to the current node.
                cur.copySymbolTransitions(next);
                // Any node in the closure of an initial node is initial.
                if(isInitial) next.setInitial(true);
                // If any node in the closure is final, so is the current node.
                shouldBeFinal |= next.isFinalNode();
            }
            // Set the final flag if necessary -- note that we don't clear it even
            // if shouldBeFinal is false
            if(shouldBeFinal) cur.setFinal(true);
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
     * Removes 'unneeded' states -- i.e. states that cannot possibly lie on a path from
     * an initial state to a finnal state. Assumes there are no epsilon transitions (not
     * sure if this is necessary, though).
     */
    protected void compressStates() {
        // TODO: This might be better done with flags on the nodes...
        LinkedHashSet initReachable = new LinkedHashSet(), finalReachable = new LinkedHashSet();
        SMNode cur;
        SMEdge edge;
        Iterator edgeIt, it = nodes.iterator();
        while(it.hasNext()) {
            cur = (SMNode)it.next();
            if(cur.isInitialNode()) cur.fillInClosure(initReachable, false, true);
            if(cur.isFinalNode()) cur.fillInClosure(finalReachable, false, false);
        }
        // The set of nodes we need to keep is (initReachable intersect finalReachable), 
        LinkedHashSet nodesToRemove = new LinkedHashSet(nodes);
        initReachable.retainAll(finalReachable); // nodes that are both init- and final-reachable
        nodesToRemove.removeAll(initReachable);  // -- we want to keep them
        
        // iterate over all nodes we want to remove and remove them, i.e. destroy their edges
        it = nodesToRemove.iterator();
        while(it.hasNext()) {
            cur = (SMNode)it.next();
            edgeIt = cur.getOutEdgeIterator();
            while(edgeIt.hasNext()) {
                edge = (SMEdge)edgeIt.next();
                edge.getTarget().removeInEdge(edge);
                edges.remove(edge);
                edgeIt.remove(); // call this rather than removeOutEdge, as we mustn't
                                 // alter the collection while iterating over it
            }
            edgeIt = cur.getInEdgeIterator();
            while(edgeIt.hasNext()) {
                edge = (SMEdge)edgeIt.next();
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
     * This assumes that no state already has a skip loop. Skips are empty labels (as opposed
     * to null labels, which represent epsilon transitions -- those should have been eliminated).
     */
    protected void addSelfLoops(Collection/*<String>*/ declaredSymbols) {
        SMNode cur;
        String l;
        Iterator it = nodes.iterator();
        while(it.hasNext()) {
            cur = (SMNode)it.next();
            newTransition(cur, cur, ""); // add skip loop
            if(cur.isInitialNode()) {
                // need to add self-loops for every declared symbol
                Iterator symIt = declaredSymbols.iterator();
                while(symIt.hasNext()) {
                    l = (String)symIt.next();
                    if(!cur.hasEdgeTo(cur, l)) {
                        newTransition(cur, cur, l);
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
     */
    protected void removeSkipToFinal() {
        SMNode cur;
        State newNode;
        SMEdge edge;
        // need this as we can't modify the collection while iterating over it
        LinkedHashSet nodesToAdd = new LinkedHashSet();
        // TODO: Could keep a separate record of all final states
        Iterator it = nodes.iterator();
        while(it.hasNext()) {
            cur = (SMNode)it.next();
            if(cur.isFinalNode()) {
                cur.setFinal(false);
                newNode = newStateDontAdd();
                newNode.setFinal(true);
                nodesToAdd.add(newNode);
                Iterator edgeIt = cur.getInEdgeIterator();
                while(edgeIt.hasNext()) {
                    edge = (SMEdge)edgeIt.next();
                    newTransition(edge.getSource(), newNode, edge.getLabel());
                }
            }
        }
        nodes.addAll(nodesToAdd);
    }
    
    /**
     * Accumulates, for each state, information about which tracematch vars must be
     * stored using a strong reference. We want to use reference for a variable X in
     * state S if and only if every path from the S to a final state F binds X.
     * 
     * Conversely, we must keep a strong reference if and only if there is some path
     * from S to a final state that does not bind X. 
     */
    protected void collectBindingInfo(Collection declaredSymbols,Map symtovar) {
        List ws = new LinkedList(edges);
        for (Iterator it = getStateIterator(); it.hasNext(); ) {
        	SMNode smn = (SMNode) it.next();
        	smn.needWeakRefs = new LinkedHashSet(declaredSymbols); 
        }
        while (!ws.isEmpty()) {
        	SMEdge e = (SMEdge) ws.remove(0);
        	SMNode v = e.getSource();
        	SMNode w = e.getTarget();
        	Set belw = new LinkedHashSet(w.needWeakRefs);
        	belw.addAll((Collection)symtovar.get(e.getLabel()));
        	Set lv = (Set) symtovar.get(e.getLabel());
        	if (!belw.equals(lv)) {
        	   v.needWeakRefs.retainAll(belw);
        	   for (Iterator ite=edges.iterator(); ite.hasNext(); ) {
        	   	   SMEdge e2 = (SMEdge) ite.next();
        	   	   if (e2.getTarget() == v && !ws.contains(e2))
        	   	   	ws.add(0,e2);	
        	   }
        	}
        }
		for (Iterator it = getStateIterator(); it.hasNext(); ) {
			SMNode smn = (SMNode) it.next();
			smn.needStrongRefs = new LinkedHashSet(declaredSymbols);
			for (Iterator it2 = smn.needStrongRefs.iterator(); it2.hasNext(); ) {
				String s = (String) it2.next();
				if (smn.needWeakRefs.contains(s))
					it2.remove(); 
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
     * Transforms the FSA that was generated from the regular expression into an NFA for
     * matching suffixes interleaved with skips and ending in a declared symbol against
     * the regular expression. Should be called once.
     * @param declaredSymbols list of the names of all declared symbols.
     */
    public void prepareForMatching(Collection declaredSymbols, Map symToVar) {
        eliminateEpsilonTransitions();
        compressStates();
        addSelfLoops(declaredSymbols);
        removeSkipToFinal();
        collectBindingInfo(declaredSymbols,symToVar);
        renumberStates();
    }
    
    public Iterator getStateIterator() {
        return nodes.iterator();
    }
    
    public String toString() {
        String result = "State machine:\n--------------\n";
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
            result += "State " + stateNumbers.get(cur) + "\n";
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
