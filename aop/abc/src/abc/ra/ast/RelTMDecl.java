/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
 * Copyright (C) 2007 Reehan Shaikh
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
package abc.ra.ast;

import polyglot.types.TypeSystem;
import abc.tm.ast.TMDecl;

/**
 * Declaration of a relational tracematch.
 * Such a tracematch has access to relational aspect formals.
 *
 * @author Eric Bodden
 */
public interface RelTMDecl extends TMDecl {

	
	/**
	 * Generates a {@link TMFromRelTMDecl_c} tracematch from this relational tracematch.
	 */
	public TMDecl genNormalTraceMatch(RelAspectDecl container, RANodeFactory nf, TypeSystem ts);

}
