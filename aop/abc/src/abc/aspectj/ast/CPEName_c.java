package polyglot.ext.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class CPEName_c extends ClassnamePatternExpr_c 
                       implements CPEName
{
    protected NamePattern pat;

    public CPEName_c(Position pos, NamePattern pat)  {
	super(pos);
        this.pat = pat;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	print(pat,w,tr);
    }

}
