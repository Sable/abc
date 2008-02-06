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
package abc.da.types;

import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.ast.Formal;
import polyglot.types.SemanticException;
import abc.aspectj.types.AspectType;
import abc.da.ast.AdviceDependency;
import abc.da.ast.AdviceName;

/**
 * A type node for an aspect that can hold dependent advice.
 * @author Eric Bodden
 */
public interface DAAspectType extends AspectType {

	/**
	 * Checks that no two advice are given the same name in the aspect.
	 * @throws SemanticException if two advice with the same name exist
	 */
	public void checkDuplicateAdviceNames() throws SemanticException;

	/**
	 * Associates a mapping from advice names to their {@link Formal}s
	 * with this aspect type.
	 * @param adviceNameToFormals a mapping from advice names to {@link Formal}s of
	 * the respective advice; includes returning/throwing formal at the last position
	 */
	public void setAdviceNameToFormals(Map<AdviceName,List<Formal>> adviceNameToFormals);

	/**
	 * Returns the mapping from advice names to {@link Formal}s.
	 */
	public Map<AdviceName,List<Formal>> getAdviceNameToFormals();

	/**
	 * Adds a set of advice names that are references in a {@link AdviceDependency}.
	 * @param allAdviceNames
	 */
	public void addReferencedAdviceNames(Set<String> allAdviceNames);

	/**
	 * Returns the set of advice names that are references in a {@link AdviceDependency}.
	 */
	public Set<String> getAllReferencedAdviceNames();

}