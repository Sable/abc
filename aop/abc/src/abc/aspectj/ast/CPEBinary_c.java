package abc.aspectj.ast;

import abc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class CPEBinary_c extends ClassnamePatternExpr_c 
                         implements CPEBinary
{
    protected ClassnamePatternExpr left;
    protected Operator op;
    protected ClassnamePatternExpr right;
    protected Precedence precedence;

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
	    return left.matches(matcher, cl) || right.matches(matcher, cl);
	}
	if (op == COND_AND) {
	    return left.matches(matcher, cl) && right.matches(matcher, cl);
	}
	throw new RuntimeException("Illegal CPE op");
    }
}
