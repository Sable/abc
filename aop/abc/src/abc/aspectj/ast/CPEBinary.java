/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 * Copyright (C) 2004 Aske Simon Christensen
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

import polyglot.util.Enum;
import polyglot.ast.Precedence;


/** binary operators on classname pattern expressions.
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 */
public interface CPEBinary extends ClassnamePatternExpr
{

    public Operator getOperator();
    public ClassnamePatternExpr getLeft();
    public ClassnamePatternExpr getRight();

    public static class Operator extends Enum {
	Precedence prec;

        public Operator(String name, Precedence prec) {
	    super(name);
	    this.prec = prec;
	}

	/** Returns the precedence of the operator. */
	public Precedence precedence() { return prec; }
    }

    public static final Operator COND_OR  = new Operator("||", Precedence.COND_OR);
    public static final Operator COND_AND = new Operator("&&", Precedence.COND_AND);
}
