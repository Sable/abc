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

import abc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

/** binary operation (&&,||) on type pattern exprs.
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 */
public class TPEBinary_c extends TypePatternExpr_c 
                         implements TPEBinary
{
    protected TypePatternExpr left;
    protected Operator op;
    protected TypePatternExpr right;
    protected Precedence precedence;

    public TPEBinary_c(Position pos, 
                       TypePatternExpr left, 
                       Operator op, 
                       TypePatternExpr right) {
	super(pos);
        this.left = left;
	this.op = op;
	this.right = right;
	this.precedence = op.precedence();
    }

    public TypePatternExpr left() {
	return left;
    }
    public TypePatternExpr right() {
	return right;
    }
    public Operator op() {
	return op;
    }

    protected TPEBinary_c reconstruct(TypePatternExpr left, TypePatternExpr right) {
	if (left != this.left || right != this.right) {
	    TPEBinary_c n = (TPEBinary_c) copy();
	    n.left = left;
	    n.right = right;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	TypePatternExpr left = (TypePatternExpr) visitChild(this.left, v);
	TypePatternExpr right = (TypePatternExpr) visitChild(this.right, v);
	return reconstruct(left, right);
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	printSubExpr(left, true, w, tr);
	w.write(" ");
	w.write(op.toString());
	w.allowBreak(2, " ");
	printSubExpr(right, false, w, tr);
    }

    public String toString() {
	return "("+left+" "+op+" "+right+")";
    }

    public boolean matchesClass(PatternMatcher matcher, PCNode cl) {
	if (op == COND_OR) {
	    return left.matchesClass(matcher, cl) || right.matchesClass(matcher, cl);
	}
	if (op == COND_AND) {
	    return left.matchesClass(matcher, cl) && right.matchesClass(matcher, cl);
	}
	throw new RuntimeException("Illegal TPE op");
    }

    public boolean matchesClassArray(PatternMatcher matcher, PCNode cl, int dim) {
	if (op == COND_OR) {
	    return left.matchesClassArray(matcher, cl, dim) || right.matchesClassArray(matcher, cl, dim);
	}
	if (op == COND_AND) {
	    return left.matchesClassArray(matcher, cl, dim) && right.matchesClassArray(matcher, cl, dim);
	}
	throw new RuntimeException("Illegal TPE op");
    }

    public boolean matchesPrimitive(PatternMatcher matcher, String prim) {
	if (op == COND_OR) {
	    return left.matchesPrimitive(matcher, prim) || right.matchesPrimitive(matcher, prim);
	}
	if (op == COND_AND) {
	    return left.matchesPrimitive(matcher, prim) && right.matchesPrimitive(matcher, prim);
	}
	throw new RuntimeException("Illegal TPE op");
    }

    public boolean matchesPrimitiveArray(PatternMatcher matcher, String prim, int dim) {
	if (op == COND_OR) {
	    return left.matchesPrimitiveArray(matcher, prim, dim) || right.matchesPrimitiveArray(matcher, prim, dim);
	}
	if (op == COND_AND) {
	    return left.matchesPrimitiveArray(matcher, prim, dim) && right.matchesPrimitiveArray(matcher, prim, dim);
	}
	throw new RuntimeException("Illegal TPE op");
    }

    public ClassnamePatternExpr transformToClassnamePattern(AJNodeFactory nf) throws SemanticException {
	ClassnamePatternExpr cpe1 = left.transformToClassnamePattern(nf);
	ClassnamePatternExpr cpe2 = right.transformToClassnamePattern(nf);
	if (op == COND_OR) {
	    return nf.CPEBinary(position, cpe1, CPEBinary.COND_OR, cpe2);
	}
	if (op == COND_AND) {
	    return nf.CPEBinary(position, cpe1, CPEBinary.COND_AND, cpe2);
	}
	throw new RuntimeException("Illegal TPE op");
    }

    public boolean equivalent(TypePatternExpr t) {
	if (t.getClass() == this.getClass()) {
	    TPEBinary tb = (TPEBinary) t;
	    return (left.equivalent(tb.left())
		    && right.equivalent(tb.right()) 
		    && (op == tb.op()));
	} else return false;
    }

}
    
