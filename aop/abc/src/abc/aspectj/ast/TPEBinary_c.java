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

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	printSubExpr(left, true, w, tr);
	w.write(" ");
	w.write(op.toString());
	w.allowBreak(2, " ");
	printSubExpr(right, false, w, tr);
    }

    public boolean matchesClass(PCNode context, PCNode cl) {
	if (op == COND_OR) {
	    return left.matchesClass(context, cl) || right.matchesClass(context, cl);
	}
	if (op == COND_AND) {
	    return left.matchesClass(context, cl) && right.matchesClass(context, cl);
	}
	throw new RuntimeException("Illegal CPE op");
    }

    public boolean matchesClassArray(PCNode context, PCNode cl, int dim) {
	if (op == COND_OR) {
	    return left.matchesClassArray(context, cl, dim) || right.matchesClassArray(context, cl, dim);
	}
	if (op == COND_AND) {
	    return left.matchesClassArray(context, cl, dim) && right.matchesClassArray(context, cl, dim);
	}
	throw new RuntimeException("Illegal CPE op");
    }

    public boolean matchesPrimitive(String prim) {
	if (op == COND_OR) {
	    return left.matchesPrimitive(prim) || right.matchesPrimitive(prim);
	}
	if (op == COND_AND) {
	    return left.matchesPrimitive(prim) && right.matchesPrimitive(prim);
	}
	throw new RuntimeException("Illegal CPE op");
    }

    public boolean matchesPrimitiveArray(String prim, int dim) {
	if (op == COND_OR) {
	    return left.matchesPrimitiveArray(prim, dim) || right.matchesPrimitiveArray(prim, dim);
	}
	if (op == COND_AND) {
	    return left.matchesPrimitiveArray(prim, dim) && right.matchesPrimitiveArray(prim, dim);
	}
	throw new RuntimeException("Illegal CPE op");
    }

}
