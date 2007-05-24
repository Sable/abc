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
 * An interface for an edge factory for edges in
 * tracematch state machines.
 * @author Eric Bodden
 */
public interface SMEdgeFactory {
	
	/**
	 * Creates a new Transition.
	 * @param from the source state
	 * @param to the target state
	 * @param label the transition label
	 */
	public SMEdge createTransition(State from, State to, String label); 

	/**
	 * Creates a skip loop for the given label.
	 * @param node the state which is going be the start and end state of the transition
	 * @param label the label of this skip loop
	 * @return a new edge <i>(node,SKIP(label),node)</i>
	 */
	public SMEdge createSkipTransition(SMNode node, String label);

	/**
	 * The default edge factory for the tracematch implementation. 
	 * @author Eric Bodden
	 */
	public class DefaultSMEdgeFactory implements SMEdgeFactory {

		private static SMEdgeFactory instance;
		
		private DefaultSMEdgeFactory() {
		}
		
		/** 
		 * {@inheritDoc}
		 */
		public SMEdge createTransition(State from, State to, String label) {
			return new SMEdge((SMNode)from, (SMNode)to, label);
		}
		
		/** 
		 * {@inheritDoc}
		 */
		public SMEdge createSkipTransition(SMNode node, String label) {
			return new SkipLoop(node, label);
		}
		
		/**
		 * Returns the singleton instance of this factory.
		 * @return a {@link DefaultSMEdgeFactory}
		 */
		public static SMEdgeFactory v() {
			if(instance == null) {
				instance = new DefaultSMEdgeFactory();
			}
			return instance;
		}

	}

}
