package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PCWithinCode_c extends Pointcut_c implements PCWithinCode
{
    protected MethodConstructorPattern pat;

    public PCWithinCode_c(Position pos, MethodConstructorPattern pat)  {
	super(pos);
        this.pat = pat;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("withincode(");
        print(pat, w, tr);
        w.write(")");
    }

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	if (pat instanceof MethodPattern) {
	    return new abc.weaving.aspectinfo.ConditionPointcut
		(new abc.weaving.aspectinfo.WithinMethod(((MethodPattern)pat).makeAIMethodPattern()),
		 position());
	} else if (pat instanceof ConstructorPattern) {
	    return new abc.weaving.aspectinfo.ConditionPointcut
		(new abc.weaving.aspectinfo.WithinConstructor(((ConstructorPattern)pat).makeAIConstructorPattern()),
		 position());
	} else {
	    throw new RuntimeException("Unexpected MethodConstructorPattern type in withincode pointcut: "+pat);
	}
    }
}
