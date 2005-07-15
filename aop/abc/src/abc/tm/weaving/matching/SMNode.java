package abc.tm.weaving.matching;

import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Set;

/** 
 * A node in the state machine. Contains information about successors, whether it's
 * an initial/final node, and (eventually) variable binding tracking information.
 *
 *  @author Pavel Avgustinov
 */

public class SMNode implements State {

    protected int number = -1;
    protected boolean initialNode = false;
    protected boolean finalNode = false;
    protected LinkedHashSet/*<SMEdge>*/ incoming = new LinkedHashSet();
    protected LinkedHashSet/*<SMEdge>*/ outgoing = new LinkedHashSet();
    
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
    public void fillInClosure(Set result, boolean epsilonOnly) {
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
    public void fillInClosure(Set result, boolean epsilonOnly, boolean forward) {
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
                    next.fillInClosure(result, epsilonOnly);
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
        Iterator it = outgoing.iterator();
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
     * Determines whether there exists an edge to a node with a given label
     * @param to The target node
     * @param out true if outgoing edges are to be considered
     * @return
     */
    public boolean hasEdgeTo(SMNode to, String label) {
        SMEdge edge;
        Iterator it = outgoing.iterator();
        while(it.hasNext()) {
            edge = (SMEdge)it.next();
            if(edge.getTarget() == to && edge.getLabel() == label) return true;
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
