package polyglot.ext.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class TPERefTypePat_c extends TypePatternExpr_c 
    implements TPERefTypePat
{
    protected RefTypePattern pat;

    public TPERefTypePat_c(Position pos, RefTypePattern pat)  {
	super(pos);
        this.pat = pat;
    }

    public Precedence precedence() {
	return Precedence.UNARY;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	print(pat, w, tr);
    }

}
