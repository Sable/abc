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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import abc.tm.weaving.aspectinfo.CollectSetSet;

/** 
 * A node in the state machine. Contains information about successors, whether it's
 * an initial/final node, and (eventually) variable binding tracking information.
 *
 *  @author Pavel Avgustinov
 */

public class SMNode implements State {

	protected static final String ANY_LABEL = new String("ANY LABEL");
    
    protected int number = -1;
    protected boolean initialNode = false;
    protected boolean finalNode = false;
    protected LinkedHashSet<SMEdge> incoming = new LinkedHashSet<SMEdge>();
    protected LinkedHashSet<SMEdge> outgoing = new LinkedHashSet<SMEdge>();
    
    /** set of variables for which this state needs to keep strong refs */
    public LinkedHashSet<String> needStrongRefs = new LinkedHashSet<String>();
    
    /** set of variables for which we may use weak references, 
         and any disjunct containing these variables may be garbage-collected
         when the weak reference is garbage-collected */
    public LinkedHashSet<String> collectableWeakRefs = new LinkedHashSet<String>();
    
    /** set of variables for which we may use weak references,
         but disjuncts containing these cannot necessarily be garbage-collected
         when the weak reference is garbage-collected */
	public LinkedHashSet<String> weakRefs = new LinkedHashSet<String>();
	
	/** set of variables that is guaranteed to be bound at this state.
	 *   when adding a set of >1 negative bindings, we can decide based on this
	 *   information whether it is necessary to create a set of disjuncts as the return value
	 */
	public LinkedHashSet<String> boundVars = new LinkedHashSet<String>();
 
	/**
	 * Set of sets the complete garbage-collection of which implies the match is invalidated.
	 */
	public CollectSetSet/*<String>*/ collectSets;
	
    private TMStateMachine hostFSA;
    
    public SMNode(TMStateMachine fsa, boolean isInitial, boolean isFinal) {
        hostFSA = fsa;
        initialNode = isInitial;
        finalNode = isFinal;
    }
    
    public void addIncomingEdge(SMEdge edge) {
        incoming.add(edge);
    }
    
    public void addOutgoingEdge(SMEdge edge) {
        outgoing.add(edge);
    }

    /**
     * @return Returns the finalNode.
     */
    public boolean isFinalNode() {
        return finalNode;
    }

    /**
     * @param finalNode The finalNode to set.
     */
    public void setFinal(boolean finalNode) {
        this.finalNode = finalNode;
    }

    /**
     * @return Returns the initialNode.
     */
    public boolean isInitialNode() {
        return initialNode;
    }

    /**
     * @param initialNode The initialNode to set.
     */
    public void setInitial(boolean initialNode) {
        this.initialNode = initialNode;
    }

    /**
     * Computes the closure of the node -- i.e. the set of all nodes reachable from
     * this node by a sequence of zero or more transitions. Result is stored in the
     * accumulating parameter, which should be empty at first -- if it isn't, nodes
     * that are already in it aren't expanded.
     * 
     * TODO: Could employ a cunning caching scheme, as this repeatedly calculates closures
     * for sub-nodes..
     * 
     * This version follows transitions forwards.
     * 
     * @param result Set which is populated with the elements of the closure.
     * @param epsilonOnly specify whether only epsilon transitions should be considered.
     */
    public void fillInClosure(Set<SMNode> result, boolean epsilonOnly) {
        fillInClosure(result, epsilonOnly, true);
    }
    /**
     * Computes the closure of the node -- i.e. the set of all nodes reachable from
     * this node by a sequence of zero or more transitions. Result is stored in the
     * accumulating parameter, which should be empty at first -- if it isn't, nodes
     * that are already in it aren't expanded.
     * 
     * TODO: Could employ a cunning caching scheme, as this repeatedly calculates closures
     * for sub-nodes..
     * @param result Set which is populated with the elements of the closure.
     * @param epsilonOnly specify whether only epsilon transitions should be considered.
     * @param forward Specify whether we follow transitions forward or backward.
     */
    public void fillInClosure(Set<SMNode> result, boolean epsilonOnly, boolean forward) {
        SMEdge cur;
        SMNode next;
        result.add(this);
        Iterator it;
        if(forward) it = outgoing.iterator(); else it = incoming.iterator();
        while(it.hasNext()) {
            cur = (SMEdge)it.next();
            if(!epsilonOnly || cur.getLabel() == null) { // Epsilon transition
                if(forward) next = cur.getTarget(); else next = cur.getSource(); // handle direction
                if(!result.contains(next)) { // we haven't visited that node
                    next.fillInClosure(result, epsilonOnly,forward);
                }
            }
        }
    }
    
    /**
     * Duplicates all non-epsilon transitions from a node onto this node. Used for 
     * epsilon-elimination.
     * @param from the node whose transitions are to be duplicated.
     */
    public void copySymbolTransitions(SMNode from) {
        SMEdge edge;
        SMNode to;
        String label;
        Iterator it = from.outgoing.iterator();
        while(it.hasNext()) {
            edge = (SMEdge)it.next();
            label = edge.getLabel();
            to = edge.getTarget(); 
            if(label != null && !hasEdgeTo(to, label)) { // only copy non-epsilon edges
                hostFSA.newTransition(this, to, label);
            }
        }
    }
    
    /**
     * Determines whether this node has an incoming edge with a given label.
     * @param label the label to consider
     * @return
     */
    public boolean hasInEdgeWithLabel(String label) {
    	SMEdge edge;
    	Iterator it = incoming.iterator();
    	while(it.hasNext()) {
    		edge = (SMEdge) it.next();
			if(edge.isSkipEdge()) continue;
    		if(edge.getLabel() == label || (label != null && label.equals(edge.getLabel()))) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Determines whether there exists an edge to a node with a given label
     * @param to The target node
     * @param label the label to check for
     * @return
     */
    public boolean hasEdgeTo(SMNode to, String label) {
        SMEdge edge;
        Iterator it = outgoing.iterator();
        while(it.hasNext()) {
            edge = (SMEdge)it.next();
            if(edge.getTarget() == to && !edge.isSkipEdge() &&
            		(edge.getLabel() == label || (label != null && label.equals(edge.getLabel())))) {
            	return true;
            }
        }
        return false;
    }
    
        /**
     * Determines whether this node has any skip loop at all, regardless of the
     * label of this loop. This is equivalent to calling {@link #hasSkipLoop(String)}
     * with {@link #ANY_LABEL}.
     * @return <code>true</code> if this state has a skip loop
     */
    public boolean hasSkipLoop() {   	
    	return hasSkipLoop(ANY_LABEL);
    }
    
    
    /**
     * Determines whether there exists a skip loop (with the given label)
     * on this node. 
     * @param label the label to check for or {@link #ANY_LABEL} as <i>don't care</i>
     * @return <code>true</code> if <code>label</code> is {@link #ANY_LABEL} and the state has any skip loop or
     * <code>label==s</code> and the state has a skip loop with label <code>s</code>
     */
    public boolean hasSkipLoop(String label) {
        SMEdge edge;
        Iterator it = outgoing.iterator();
        while(it.hasNext()) {
            edge = (SMEdge)it.next();
            if(edge.isSkipEdge() && (label==ANY_LABEL || label.equals(edge.getLabel()))) {
            	return true;
            }
        }
        return false;
    }
    
    public void removeInEdge(SMEdge edge) {
        incoming.remove(edge);
    }
    
    public void removeOutEdge(SMEdge edge) {
        outgoing.remove(edge);
    }
    
    public Iterator getOutEdgeIterator() {
        return outgoing.iterator();
    }
    
    public Iterator getInEdgeIterator() {
        return incoming.iterator();
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
