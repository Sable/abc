package polyglot.ext.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PCNot_c extends Pointcut_c implements PCNot
{
    protected Pointcut pc;

    public PCNot_c(Position pos, Pointcut pc)  {
	super(pos);
        this.pc = pc;
    }

    public Precedence precedence() {
	return Precedence.UNARY;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("!");
        printSubExpr(pc, true, w, tr);
    }

}
