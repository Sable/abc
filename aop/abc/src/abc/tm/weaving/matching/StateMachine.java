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
import java.util.Set;

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
     * @return the new transition 
     */
    public SMEdge newTransition(State from, State to, String s);

    public int getNumberOfStates();
    public Iterator<State> getStateIterator();
    public Iterator<SMEdge> getEdgeIterator();
    public SMNode getStateByNumber(int n);

    public Set<SMNode> getInitialStates();
}
