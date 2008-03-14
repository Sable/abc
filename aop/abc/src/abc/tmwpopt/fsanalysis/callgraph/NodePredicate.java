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
package abc.tmwpopt.fsanalysis.callgraph;

import soot.MethodOrMethodContext;

/**
 * A predicate over a call graph node.
 * @author Eric Bodden
 */
public interface NodePredicate {

	/**
     * Returns <code>true</code> if the predicate holds for the node.
	 * @param node some method or method context
	 * @return <code>true</code> if the predicate matches the node
	 */
	public boolean want(MethodOrMethodContext node);
	
	/**
     * Returns <code>true</code> if children of this node should be visited.
	 * @param node some method or method context
	 * @return <code>true</code> if the predicate matches the node
	 */
	public boolean visitChildren(MethodOrMethodContext node);
}
