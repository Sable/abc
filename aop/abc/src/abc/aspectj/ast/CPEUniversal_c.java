package arc.aspectj.ast;

import arc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class CPEUniversal_c extends ClassnamePatternExpr_c implements CPEUniversal
{
    public CPEUniversal_c(Position pos)  {
	super(pos);
    }

    public Precedence precedence() {
	return Precedence.UNARY;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("*");
    }

    public boolean matches(PatternMatcher matcher, PCNode cl) {
	return true;
    }
}
