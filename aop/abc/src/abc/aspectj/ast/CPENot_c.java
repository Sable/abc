package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class CPENot_c extends ClassnamePatternExpr_c implements CPENot
{
    protected ClassnamePatternExpr cpe;

    public CPENot_c(Position pos, ClassnamePatternExpr cpe)  {
	super(pos);
        this.cpe = cpe;
    }

    public Precedence precedence() {
	return Precedence.UNARY;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("!");
        printSubExpr(cpe, true, w, tr);
    }

}
