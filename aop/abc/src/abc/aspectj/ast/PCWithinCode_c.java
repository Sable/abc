package arc.aspectj.ast;

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

}
