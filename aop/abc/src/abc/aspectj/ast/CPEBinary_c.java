package arc.aspectj.ast;

import arc.aspectj.visit.*;

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

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	printSubExpr(left, true, w, tr);
	w.write(" ");
	w.write(op.toString());
	w.allowBreak(2, " ");
	printSubExpr(right, false, w, tr);
    }

    public boolean matches(PCNode context, PCNode cl) {
	if (op == COND_OR) {
	    return left.matches(context, cl) || right.matches(context, cl);
	}
	if (op == COND_AND) {
	    return left.matches(context, cl) && right.matches(context, cl);
	}
	throw new RuntimeException("Illegal CPE op");
    }
}
