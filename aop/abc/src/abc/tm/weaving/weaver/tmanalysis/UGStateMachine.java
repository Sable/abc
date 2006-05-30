package abc.tm.weaving.weaver.tmanalysis;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import soot.MethodOrMethodContext;
import soot.Unit;
import soot.jimple.InvokeStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.UnitGraph;
import abc.main.Debug;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.TMStateMachine;
import abc.tm.weaving.weaver.tmanalysis.util.IdentityHashSet;

/**
 * A state machine abstracting a unit graph.
 * The state machine reflects the transition structure of a unit graph
 * with respect to tracematch symbols that match its units.
 * More specificly, this state machine has a unique starting state reflecting
 * the program state when entering the unit graph and a unique end state
 * reflecting the program state when leaving it.
 * Then, there exist transitions <i>(q,l,p)</i> from <i>q</i> to <i>p</i> with label <i>l</i>
 * and <i>l = {l1,...,ln)</i> when from q the state p is directly reachable (i.e. without passing
 * through another state first) by issuing the symbols <i>{l1,...,ln)</i>. 
 *
 * @author Eric Bodden
 */
public class UGStateMachine extends TMStateMachine implements Cloneable {
	
    /**
     * Epsilon is represented by <code>null</code>.
     */
    protected final static String EPSILON = null; 
	
    /**
     * A mapping from a unit to a state representing the program state
     * immediately before executing the unit. 
     */
    protected /*final*/ IdentityHashMap unitToState; //cannot be final cause we set it within clone()
			
	/**
	 * The associated unit graph. 
	 */
	protected final UnitGraph ug;
	
	/**
	 * The unique stating state / end state.
	 */
	protected State uniqueInitialState, uniqueEndState;
	
    /**
     * Temporary set, only used internally.
     */
    protected transient IdentityHashSet processedUnits;

    /**
     * Creates a new unit graph state machine
	 * @param unitGraph
	 */
	public UGStateMachine(UnitGraph unitGraph) {
		assert unitGraph!=null;
		
		ug = unitGraph;
		
		processedUnits = new IdentityHashSet(); 
		
		unitToState = new IdentityHashMap();
		
        //build initial state
		uniqueInitialState = newState();
		uniqueInitialState.setInitial(true);
		
        //add epsilon transitions from initial state to a successor state for each
        //entry unit; then adds states for their whole "tail"
		for (Iterator headIter = ug.getHeads().iterator(); headIter.hasNext();) {
			Unit head = (Unit) headIter.next();
			
            //initial transition
			newTransition(uniqueInitialState, stateFor(head), EPSILON);
            //all other states
			addStatesFor(head);
		}
			
        //construct unique end state
		uniqueEndState = newState();
		uniqueEndState.setFinal(true);

        //create epsilon transitions for all states representing a tail
        //unit to the unique end state
		for (Iterator tailIter = ug.getTails().iterator(); tailIter.hasNext();) {
			Unit tail = (Unit) tailIter.next();
			
			newTransition(stateFor(tail), uniqueEndState, EPSILON);
		}
				
        //do not need this any more
		processedUnits = null;
        
        //clean up
		cleanup();		
		
        if(Debug.v().useMinimizedDFA) {
            //build a minimized DFA
            minimize();
            //clean up
    		cleanup();		
        }
        
	}
	
    /**
	 * Eliminates epsilon transitions (except the ones we need to
	 * maintain a unique starting state and end state) and unreachable states,
	 * then renumbers the states.
	 */
	public void cleanup() {
		eliminateEpsilonTransitions();
		compressStates();
		uniqueStartEndStates();
		renumberStates();		
	}
	
    /** 
     * Generates unique start/end states. 
     */
    protected void uniqueStartEndStates() {

    	//add fresh start node
    	uniqueInitialState = newState();
    	uniqueInitialState.setInitial(true);
    	
    	//add fresh end node
    	uniqueEndState = newState();
    	uniqueEndState.setFinal(true);    	
    	
        //restore again the uniqueness of start/end nodes
        for (Iterator iter = nodes.iterator(); iter.hasNext();) {
			State state = (State) iter.next();
			
			if(state.isInitialNode() && state!=uniqueInitialState) {
				newTransition(uniqueInitialState,state,EPSILON);
				state.setInitial(false);
			}
			if(state.isFinalNode() && state!=uniqueEndState) {
				newTransition(state,uniqueEndState,EPSILON);
				state.setFinal(false);
			}
		}

    }
    
	/**
	 * @return the unique starting state
	 */
	public State getInitialState() {
		return uniqueInitialState;
	}
	
	/**
	 * @return the unique end state
	 */
	public State getEndState() {
		return uniqueEndState;
	}

	/**
     * Adds states and transition for all (transitive) successor units for u.
	 * @param u a unit
	 */
	protected void addStatesFor(Unit u) {
		if(!processedUnits.contains(u)) {
			processedUnits.add(u);
			for (Iterator iter = ug.getSuccsOf(u).iterator(); iter.hasNext();) {
				Unit succ = (Unit) iter.next();
				
				newTransition(stateFor(u), stateFor(succ), u);
				
				addStatesFor(succ);			
			}
		}
	}

    /** 
     * Generates a new transition. The label depends on the kind of unit.
     * @param from the starting state
     * @param to the end state
     * @param u the unit whose execution triggers the transition
     */
    public void newTransition(State from, State to, Unit u) {
        if(u instanceof InvokeStmt) {
        	//for invoke statements we generate a special edge,
        	//cause we need to treat them in a special way during interprocedural
        	//analysis
            InvokeStmt invokeStmt = (InvokeStmt) u;
			SMNode f = (SMNode)from;
            SMNode t = (SMNode)to;
            //in particular, the invoke edge holds a reference to the invoke statement
            SMEdge edge = new InvokeEdge(f, t, invokeStmt);
            f.addOutgoingEdge(edge);
            t.addIncomingEdge(edge);
            edges.add(edge);
        } else {
            //else, just add a transition labelled
            //with the symbols matching u
            super.newTransition(from, to, labelFor(u));
        }
    }
    
	/**
     * Returns the unique state for u.
	 * @param u a unit
	 * @return the state representing the program state immediately
     * before executing u
	 */
	protected State stateFor(Unit u) {
		State s = (State) unitToState.get(u);
		
		if(s==null) {
			s = newState();
			unitToState.put(u, s);
		}
		
		return s;
	}
	
	
	/**
     * Creates a label based on the symbols that match u.
     * This label must be unique for each set of symbols. 
	 * @param u a unit
	 * @return a label representing the symbols that match u
	 */
	protected String labelFor(Unit u) {
		List matchingSymbols = matchingSymbols(u);
        //sort list so that we get a unique string label
        //for each set of matching symbols
        Collections.sort(matchingSymbols);
		if(matchingSymbols.size()>0) {
            //intern the string for efficiency
			return matchingSymbols.toString().intern();
		} else {
			return EPSILON;
		}
	}
	
	/**
     * Returns a list of symbols matching u.
	 * @param u a unit
	 * @return the list of tracematch symbols matching u
	 */
	protected List matchingSymbols(Unit u) {
        //look for a matching symbols tag
		if(u.hasTag(MatchingTMSymbolTag.NAME)) {
		    MatchingTMSymbolTag tag = (MatchingTMSymbolTag) u.getTag(MatchingTMSymbolTag.NAME);
            //get the appropriate symbol IDs
            List matchingSymbolIDs = tag.getMatchingSymbolIDs();
			assert matchingSymbolIDs.size()>0;
            return matchingSymbolIDs;
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * Creates a "folded" copy of this state machine, where invoke edges
	 * have been inlined to edged to each corresponding callee and back from the
	 * callee to the target node of the invoke edge.
	 * <b>Note that the copy does not hold deep copies of nodes and edges!</b>
	 * @param cg a (possibly abstracted) call graph
	 * @return a folded shallow copy of this state machine 
	 */
	public UGStateMachine fold(CallGraph cg) {
		UGStateMachine clone;
		try {
			//create a copy
			clone = (UGStateMachine) clone();
			//fold this copy
			clone.foldThis(cg);
			//and return
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("A problem occurred when trying to clone" +
					" a UGStateMachine.",e);
		}
	}
		
	/**
	 * Folds this state machine, i.e. inlines invoke edges.
	 * @param cg a (possibly abstracted) call graph
	 */
	protected void foldThis(CallGraph cg) {
		//we have to use copies here to avoid concurrent modification exceptions
		//TODO could be easiert to actually use a fresh automaton instance here
		LinkedHashSet newNodes = new LinkedHashSet();
		LinkedHashSet newEdges = new LinkedHashSet();
		
		//over all transitions
		for (Iterator transIter = edges.iterator(); transIter.hasNext();) {
			SMEdge outTrans = (SMEdge) transIter.next();
			
			//if we have an invoke transition, "inline" it
			if(outTrans instanceof InvokeEdge) {
				InvokeEdge invokeEdge = (InvokeEdge) outTrans;

				//get all outgoing call edges
				Iterator callEdgeIter = cg.edgesOutOf(invokeEdge.getInvokeStmt());
				
				if(callEdgeIter.hasNext()) {
					//if we have any outgoing call edges, add for each such edge
					//epsilon transitions to the state machine of the call target
				
					//for each call graph edge going out from the invoke statement
					while(callEdgeIter.hasNext()) {
						Edge callEdge = (Edge) callEdgeIter.next();						
						MethodOrMethodContext target = callEdge.getTgt();
						
						//each method should by now have an associated state machine
						assert target.method().hasTag(UGStateMachineTag.NAME);
	
						//get the state machine of the target method
						UGStateMachineTag smTag =
							(UGStateMachineTag) target.method().getTag(UGStateMachineTag.NAME);
						UGStateMachine targetStateMachine = smTag.getStateMachine();
	
						//recurse for this state machine
						targetStateMachine = targetStateMachine.fold(cg);
						
						//add an epsilon transition from the start node of the
						//invoke transition to the start node of the target state machine
						SMNode targetState = (SMNode) targetStateMachine.getInitialState();
						SMEdge newOutEdge = new SMEdge(invokeEdge.getSource(),targetState,EPSILON);
						newEdges.add(newOutEdge);
						invokeEdge.getSource().addOutgoingEdge(newOutEdge);
						targetState.addIncomingEdge(newOutEdge);
						
						
						//add an epsilon transition from the end node of the
						//target state machine to the end node of the invoke statement
						SMNode sourceState = (SMNode) targetStateMachine.getEndState();
						SMEdge newInEdge = new SMEdge(sourceState,invokeEdge.getTarget(),EPSILON);
						newEdges.add(newInEdge);
						sourceState.addOutgoingEdge(newInEdge);
						invokeEdge.getTarget().addIncomingEdge(newInEdge);
						
						//copy over all edges from the target state machine;
						//cannot directly copy into nodes/edges lists cause we are
						//iterating over those (would give ConcurrentModificationException)
						newNodes.addAll(targetStateMachine.nodes);
						newEdges.addAll(targetStateMachine.edges);

					}
					
					//remove the original invoke edge
					invokeEdge.getSource().removeOutEdge(invokeEdge);
					invokeEdge.getTarget().removeInEdge(invokeEdge);

				} else {
					
					//if we have no outgoing call edges then retain the original edge
					//but make it an epsilon transition
					invokeEdge.setLabel(EPSILON);
					newEdges.add(invokeEdge);
					
				}

			} else {
				//simply copy the edge
				newEdges.add(outTrans);
			}
			//and copy the nodes
			newNodes.add(outTrans.getSource());
			newNodes.add(outTrans.getTarget());
		}
		
		//copy over all nodes and edges
		nodes = newNodes;
		edges = newEdges;
		//clean up after us
		cleanup();		
	}
	
	/**
	 * Special edge in the automaton which reflects an invoke expression.
	 * We have to retain those for the interprocedural analysis.
	 * @author Eric Bodden
	 */
	protected class InvokeEdge extends SMEdge {

        protected InvokeStmt invokeStmt; 
        
        /**
         * @param from
         * @param to
         * @param stmt
         */
        public InvokeEdge(SMNode from, SMNode to, InvokeStmt stmt) {            
            super(from, to, stmt.toString());
            assert stmt.toString()!=null; //must not be null cause null here means epsilon
            invokeStmt = stmt;
        }

		/**
		 * @return the associated invoke statement
		 */
		public InvokeStmt getInvokeStmt() {
			return invokeStmt;
		}
        
    }
	
	/** 
	 * {@inheritDoc}
	 */
	protected Object clone() throws CloneNotSupportedException {		
		UGStateMachine clone = (UGStateMachine) super.clone();
		
		//make deep copies of the most important
		//structures
		clone.edges = (LinkedHashSet) edges.clone();
		clone.nodes = (LinkedHashSet) nodes.clone();
		clone.unitToState = (IdentityHashMap) unitToState.clone();
	
		return clone;
	}
    
}
