package arc.aspectj.ast;

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

}
