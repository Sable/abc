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

package abc.ra.ast;

import java.util.List;

import polyglot.ast.Formal;
import polyglot.ast.NodeFactory;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import abc.aspectj.ast.AspectDecl;

public interface RelAspectDecl extends AspectDecl {

	/**
	 * Generates associate and release methods.
	 */
	public RelAspectDecl declareMethods(NodeFactory nf, TypeSystem ts);

	/**
	 * @return the relational aspect formals
	 */
	public List<Formal> formals();

	/** 
	 * Registers the name of a generated tracematch body method.
	 * Those bodies must be post-processed in the backend, replacing <code>this</code>
	 * by the state variable. 
	 */
	public void addTmBodyMethodName(String tmBodyMethodName);

}
