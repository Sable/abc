package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class TPENot_c extends TypePatternExpr_c implements TPENot
{
    protected TypePatternExpr tpe;

    public TPENot_c(Position pos, TypePatternExpr tpe)  {
	super(pos);
        this.tpe = tpe;
    }

    public Precedence precedence() {
	return Precedence.UNARY;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("!");
        printSubExpr(tpe, true, w, tr);
    }

}
