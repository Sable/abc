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

	/**
	 * @return <code>true</code> is this is a final node
	 */
	public boolean isFinalNode();

	/**
	 * @return <code>true</code> is this is an initial node
	 */
	public boolean isInitialNode();

}
