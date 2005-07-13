package abc.tm.weaving.matching;

/** 
 * High-level representation of the state machine of the regex for tracematches -- interface
 * for creating the state machine
 *
 *  @author Pavel Avgustinov
 */

public interface StateMachine {
    /**
     * Create a new state and add it to the FSA. It will have no transitions.
     * @return the newly created State
     */
    public State newState();
    
    /**
     * Adds a new transition to the FSA
     * @param from Starting node for the transition
     * @param to Ending node for the transition
     * @param s The label -- name of the symbol, or null for epsilon transitions.
     */
    public void newTransition(State from, State to, String s);
}
