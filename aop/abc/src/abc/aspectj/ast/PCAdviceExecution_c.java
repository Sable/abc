package polyglot.ext.aspectj.ast;

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

}
