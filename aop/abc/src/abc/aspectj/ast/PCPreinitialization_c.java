package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PCPreinitialization_c extends Pointcut_c 
                                   implements PCPreinitialization
{
    protected ConstructorPattern pat;

    public PCPreinitialization_c(Position pos, ConstructorPattern pat)  {
	super(pos);
        this.pat = pat;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("preinitialization(");
        print(pat, w, tr);
        w.write(")");
    }

}
