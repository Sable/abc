/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Eric Bodden
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
 * @author Eric Bodden
 */
public class SkipLoop extends SMEdge {


	/**
	 * Creates a new skip loop which skips label <code>labelToSkip</code>
	 * at state <code>state</code>.
	 * @param state the state to be set as source and target of the edge
	 * @param labelToSkip the label for this skip loop
	 */
	public SkipLoop(SMNode state, String labelToSkip) {
		super(state, state, labelToSkip);
	}
	
	/**
     * Tells whether this edge is a skip edge.
     * @return <code>true</code>
     */
	public boolean isSkipEdge() {
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "SKIP("+super.toString()+")";
	}

}
