package polyglot.ext.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PCSet_c extends Pointcut_c implements PCSet
{
    protected FieldPattern pat;

    public PCSet_c(Position pos, FieldPattern pat)  {
	super(pos);
        this.pat = pat;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("set(");
        print(pat, w, tr);
        w.write(")");
    }

}
