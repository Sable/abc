package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PCBinary_c extends Pointcut_c implements PCBinary
{
    protected Pointcut left;
    protected Operator op;
    protected Pointcut right;
    protected Precedence precedence;

    public PCBinary_c(Position pos, Pointcut left, Operator op, Pointcut right)    {
	super(pos);
        this.left = left;
	this.op = op;
	this.right = right;
	this.precedence = op.precedence();
    }

    public Precedence precedence() {
        return precedence;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	printSubExpr(left, true, w, tr);
	w.write(" ");
	w.write(op.toString());
	w.allowBreak(2, " ");
	printSubExpr(right, false, w, tr);
    }

}
