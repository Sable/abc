package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PCWithin_c extends Pointcut_c implements PCWithin
{
    protected ClassnamePatternExpr pat;

    public PCWithin_c(Position pos, ClassnamePatternExpr pat)  {
	super(pos);
        this.pat = pat;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("within(");
        print(pat, w, tr);
        w.write(")");
    }

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	return new abc.weaving.aspectinfo.ConditionPointcut
	    (new abc.weaving.aspectinfo.Within(pat.makeAIClassnamePattern()),
	     position());
    }
}
