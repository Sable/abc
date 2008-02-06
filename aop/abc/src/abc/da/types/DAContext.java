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

import polyglot.ast.Formal;

import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.types.AJContext;
import abc.da.ast.AdviceName;

/**
 * An extended context used for type checking of dependent advice.
 * @author Eric Bodden
 */
public interface DAContext extends AJContext {

	/**
	 * Adds an advice name and its {@link Formal}s to the context.
	 * returning/throwing formal has to be included (if given) at the last position
	 */
	public void addAdviceNameAndFormals(AdviceName adviceName, List<Formal> formals);

	/**
	 * Returns a mapping from an {@link AdviceName} to its {@link Formal}s in the current context. 
	 */
	public Map<AdviceName,List<Formal>> currentAdviceNameToFormals();

	/**
	 * Pushes the current advice declaration on the stack.
	 * Returns the resulting context.
	 */
	public AJContext pushAdviceDecl(AdviceDecl ad);

	/**
	 * Returns the current surrounding advice declaration.
	 */
	public AdviceDecl currentAdviceDecl();

}
