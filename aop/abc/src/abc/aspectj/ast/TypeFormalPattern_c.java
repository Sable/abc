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

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.Node_c;

/**
 *  A formal pattern that is a type pattern expression.
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 *
 */
public class TypeFormalPattern_c extends Node_c 
                                 implements TypeFormalPattern
{

    TypePatternExpr expr;

    public TypeFormalPattern_c(Position pos,
			       TypePatternExpr expr) {
        super(pos);
        this.expr = expr;
    }
    
	/** Reconstruct the type pattern. */
    protected TypeFormalPattern_c reconstruct(TypePatternExpr expr) {
		if (expr != this.expr) {
			 TypeFormalPattern_c n = (TypeFormalPattern_c) copy();
			 n.expr = expr;
			 return n;
		}
		return this;
	}

	/** Visit the children of the type pattern. */
	public Node visitChildren(NodeVisitor v) {
		 TypePatternExpr expr = (TypePatternExpr) visitChild(this.expr, v);
		 return reconstruct(expr);
	}

    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
	print(expr,w,pp);
    }

    public TypePatternExpr expr() {
	return expr;
    }

    public String toString() {
	return expr.toString();
    }

    public boolean equivalent(FormalPattern p) {
	if (p.getClass() == this.getClass()) {
	    return expr.equivalent(((TypeFormalPattern)p).expr());
	} else return false;
    }

}
