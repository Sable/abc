package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PCAdviceExecution_c extends Pointcut_c 
    implements PCAdviceExecution
{

    public PCAdviceExecution_c(Position pos)  {
	super(pos);
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("adviceexecution()");
    }

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	return new abc.weaving.aspectinfo.AndPointcut
	    (new abc.weaving.aspectinfo.WithinAdvice(position()),
	     new abc.weaving.aspectinfo.Execution(position()),
	     position());
    }
}
