/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
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
package abc.da.ast;

import java.util.List;
import java.util.Map;

import polyglot.ast.Formal;
import polyglot.ast.Node;

/**
 * A combination of an advice name and a vector parameters (variable names).
 * @author Eric Bodden
 */
public interface AdviceNameAndParams extends Node {

	/**
	 * Returns the advice name.
	 */
	public String getName();
	
	/**
	 * Returns the list of variable names. Variables that start with {@link AdviceDependency#WILDCARD}
	 * are wildcards.
	 */
	public List<String> getParams();

	/**
	 * If no parameters were given by the user (i.e. {@link #getParams()} returns an empty list),
	 * this method sets default parameters from the given map. The new AST node is returned. This
	 * node remains untouched.
	 */
	public AdviceNameAndParams defaultParams(Map<AdviceName, List<Formal>> adviceNameToFormals);

	/**
	 * Extracts the formals for this advice name from the map.
	 */
	public List<Formal> findFormalsForAdviceName(Map<AdviceName, List<Formal>> adviceNameToFormals);

	/**
	 * Returns <code>true</code>, if no parameters were given by the user and hence they are inferred
	 * from advice declarations.
	 */
	public boolean hasInferredParams();

}
