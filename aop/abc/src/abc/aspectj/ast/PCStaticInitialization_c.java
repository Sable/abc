package polyglot.ext.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class PCStaticInitialization_c extends Pointcut_c 
    implements PCStaticInitialization
{
    protected ClassnamePatternExpr pat;

    public PCStaticInitialization_c(Position pos, 
                                    ClassnamePatternExpr pat)  {
	super(pos);
        this.pat = pat;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("staticinitialization(");
        print(pat, w, tr);
        w.write(")");
    }

}
