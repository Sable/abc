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

/** binary operators on classname pattern expressions.
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 */
public class CPEBinary_c extends ClassnamePatternExpr_c 
                         implements CPEBinary
{
    protected ClassnamePatternExpr left;
    protected Operator op;
    protected ClassnamePatternExpr right;
    protected Precedence precedence;

    public Operator getOperator() {
	return op;
    }
    public ClassnamePatternExpr getLeft() {
	return left;
    }
    public ClassnamePatternExpr getRight() {
	return right;
    }

    public CPEBinary_c(Position pos, 
                       ClassnamePatternExpr left, 
                       Operator op, 
                       ClassnamePatternExpr right) {
	super(pos);
        this.left = left;
	this.op = op;
	this.right = right;
	this.precedence = op.precedence();
    }

    protected CPEBinary_c reconstruct(ClassnamePatternExpr left, ClassnamePatternExpr right) {
	if (left != this.left || right != this.right) {
	    CPEBinary_c n = (CPEBinary_c) copy();
	    n.left = left;
	    n.right = right;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	ClassnamePatternExpr left = (ClassnamePatternExpr) visitChild(this.left, v);
	ClassnamePatternExpr right = (ClassnamePatternExpr) visitChild(this.right, v);
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

    public boolean matches(PatternMatcher matcher, PCNode cl) {
	if (op == COND_OR) {
	    return left.matches(cl) || right.matches(cl);
	}
	if (op == COND_AND) {
	    return left.matches(cl) && right.matches(matcher, cl);
	}
	throw new RuntimeException("Illegal CPE op");
    }

    public boolean equivalent(ClassnamePatternExpr otherexp) {
	if (otherexp.getClass() == this.getClass()) {
	    CPEBinary o = (CPEBinary)otherexp;
	    return (   (o.getOperator() == op)
		    && (left.equivalent(o.getLeft()))
		    && (right.equivalent(o.getRight())));
	} else return false;
    }
}
