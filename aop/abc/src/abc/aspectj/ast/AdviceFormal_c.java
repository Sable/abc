/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
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

package abc.aspectj.ast;

import polyglot.util.Position;
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.types.Context;

import polyglot.ext.jl.ast.Formal_c;

/** A class for representing special advice formals like the return value 
 * of <code> afterreturning</code> or <code>afterthrowing</code>.
 * @author Oege de Moor */
public class AdviceFormal_c extends Formal_c implements AdviceFormal {

   public AdviceFormal_c(Position pos, Flags flags, TypeNode tn, String name) {
   	   super(pos, flags, tn, name);
   }

	/** advice formals are not automatically added to the context,
     * unlike ordinary formals: they are not in scope in the pointcut,
     * but they are visible in the advice body.
     * @see{Context AdviceDecl_c.enterScope(Node child, Context c)}
	 */
   public void addDecls(Context c) {
	 // do nothing
   }

}
