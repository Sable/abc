package arc.aspectj.ast;

import arc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

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

    public ClassnamePatternExpr transformToClassnamePattern(AspectJNodeFactory nf) throws SemanticException {
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

}
