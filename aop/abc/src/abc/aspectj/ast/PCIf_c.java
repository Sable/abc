package polyglot.ext.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PCIf_c extends Pointcut_c implements PCIf
{
    protected Expr expr;

    public PCIf_c(Position pos, Expr expr)  {
	super(pos);
        this.expr = expr;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("if(");
        print(expr, w, tr);
        w.write(")");
    }

}
