package abc.tm.weaving.weaver.tmanalysis;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;

import soot.Unit;
import soot.toolkits.graph.UnitGraph;
import abc.main.Debug;
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
public class UGStateMachine extends TMStateMachine {
	
    /**
     * Epsilon is represented by <code>null</code>.
     */
    protected final static String EPSILON = null; 
	
    /**
     * A mapping from a unit to a state representing the program state
     * immediately before executing the unit. 
     */
    protected final IdentityHashMap unitToState;
			
	/**
	 * The associated unit graph. 
	 */
	protected final UnitGraph ug;
	
	/**
	 * The unique stating state / end state.
	 */
	protected final State uniqueInitialState, uniqueEndState;
	
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

        //create epsilon transitions for all states representing a tail
        //unit to the unique end state
		for (Iterator tailIter = ug.getTails().iterator(); tailIter.hasNext();) {
			Unit tail = (Unit) tailIter.next();
			
			newTransition(stateFor(tail), uniqueEndState, EPSILON);
		}
				
        //do not need this any more
		processedUnits = null;
        
        ////clean up - those must be all equivalent transformations
        //fold epsilon transitions
        eliminateEpsilonTransitions();
        //delete unreachable states
        compressStates();
        if(Debug.v().useMinimizedDFA) {
            //build a minimized DFA
            minimize();
        }
        //number states in increasing order 
        renumberStates();
	}
	
    /** 
     * {@inheritDoc}
     */
    protected void eliminateEpsilonTransitions() {
        //temporarily mark initial state as initial and final state as final
        uniqueInitialState.setInitial(true);
        uniqueEndState.setFinal(true);
        //do the elimination
        super.eliminateEpsilonTransitions();
        //reset; cause in the big picture things look different
        uniqueInitialState.setInitial(false);
        uniqueEndState.setFinal(false);
    }
    
    /** 
     * {@inheritDoc}
     */
    protected void compressStates() {
        //temporarily mark initial state as initial and final state as final
        uniqueInitialState.setInitial(true);
        uniqueEndState.setFinal(true);
        //do the compression
        super.compressStates();
        //reset; cause in the big picture things look different
        uniqueInitialState.setInitial(false);
        uniqueEndState.setFinal(false);
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
				
				newTransition(stateFor(u), stateFor(succ), labelFor(u));
				
				addStatesFor(succ);			
			}
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

//	/**
//	 * @param cg 
//	 */
//	public void join(CallGraph cg) {
//		for (Iterator iter = ug.getBody().getUnits().iterator(); iter.hasNext();) {
//			Unit u = (Unit) iter.next();			
//			
//			if(u instanceof InvokeStmt) {
//				InvokeStmt stmt = (InvokeStmt) u;
//				
//				for (Iterator edgeIter = cg.edgesOutOf(stmt); edgeIter.hasNext();) {
//					Edge edge = (Edge) edgeIter.next();
//					SootMethod callee = edge.getTgt().method();
//					assert callee!=null;
//					
//					UGStateMachineTag tag = (UGStateMachineTag) callee.getTag(UGStateMachineTag.NAME);
//					if(tag!=null) {
//					
//						UGStateMachine calleeSM = tag.getStateMachine();
//						State calleesInitialState = calleeSM.getInitialState();
//						
//						edges.addAll(calleeSM.edges);
//						nodes.addAll(calleeSM.nodes);	
//						newTransition(stateFor(stmt), calleesInitialState, labelFor(stmt));
//						
//						//add back edges from callee return statement
//						State retState = calleeSM.getEndState();
//						
//						for (Iterator iterator = ug.getSuccsOf(stmt).iterator(); iterator
//								.hasNext();) {
//							Unit succ = (Unit) iterator.next();
//							
//							//TODO is that always gonna by epsilon? Probably...
//							newTransition(retState, stateFor(succ), EPSILON);
//						}
//					}
//				}
//			}
//		}	
//		
//		uniqueInitialState.setInitial(true);
//		uniqueEndState.setFinal(true);
//		eliminateEpsilonTransitions();
//		compressStates();
//        //TODO reenable
//		//minimize();        
//		renumberStates();		
//
//		for (Iterator iter = getStateIterator(); iter.hasNext();) {
//			State s = (State) iter.next();
//			s.setInitial(false);
//			s.setFinal(false);
//		}
//	}
	
//	/**
//	 * Minimizes by the well-known (?)
//	 * reverse/determinize/reverse/determinize/ method.
//	 * 
//	 * May be expensive for larger automata.
//	 * Assumes that 
//	 */
//	protected void minimize() {
//		//reverse
//		reverse();
//		//create determinized copy
//		TMStateMachine det = determinise();
//		//restore original
//	    reverse();
//	    //do second iteration on copy
//	    det.reverse();
//	    det = det.determinise();
//	    this.edges = det.edges;
//	    this.nodes = det.nodes;
//	}


}
