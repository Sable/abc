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

import polyglot.ast.ClassMember;
import polyglot.ast.Node;
import polyglot.types.SemanticException;
import polyglot.visit.ContextVisitor;

/**
 * The declaration of an advice dependency in source code.
 * An advice dependency declaration consists of a non-empty set
 * of strong advice names and a set of weak advice names.
 * Each advice name may have a vector of variables associated with it.
 * This vector must have the same length as the unique advice with that name
 * has parameters. (The returning/throwing parameter is taken as last parameter.)
 * Any variable can be {@link #WILDCARD}, which has the same meaning as a
 * fresh variable name.
 *  
 * @author Eric Bodden
 */
public interface AdviceDependency extends ClassMember {

	public static final String WILDCARD = "*";

	/**
	 * Checks that if two positions refer to the same variable, the advice formals
	 * at those positions are cast-convertible. 
	 * For primitive types we give a warning, that their variables will have no effect
	 * (if the variable at that position is not the {@link #WILDCARD}).
	 */
	public Node typeCheckAdviceParams(ContextVisitor visitor) throws SemanticException;

}
