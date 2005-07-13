package abc.tm.weaving.matching;

/** 
 * High-level representation of a state machine state.
 *
 *  @author Pavel Avgustinov
 */

public interface State {
    /**
     * Specifies whether this is an initial state.
     * @param b The new value of the 'initial' flag.
     */
    public void setInitial(boolean b);
    
    /**
     * Specifies whether this is an final state or not.
     * @param b The new value of the 'final' flag.
     */
    public void setFinal(boolean b);
}
