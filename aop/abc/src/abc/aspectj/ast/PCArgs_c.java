package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PCArgs_c extends Pointcut_c implements PCArgs
{
    protected TypedList pats;

    public PCArgs_c(Position pos, List pats)  {
	super(pos);
        this.pats = TypedList.copyAndCheck(pats,FormalPattern.class,true);
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("args(");
        for (Iterator i = pats.iterator(); i.hasNext(); ) {
	        FormalPattern fp = (FormalPattern) i.next();
		print(fp, w, tr);

		if (i.hasNext()) {
		    w.write(",");
		    w.allowBreak(4, " ");
		}
	    }
        w.write(")");
    }

}
