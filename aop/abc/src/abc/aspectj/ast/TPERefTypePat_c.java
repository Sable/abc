package arc.aspectj.ast;

import arc.aspectj.visit.*;

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

    public boolean matchesClass(PatternMatcher matcher, PCNode cl) {
	return pat.matchesClass(matcher, cl);
    }

    public boolean matchesClassArray(PatternMatcher matcher, PCNode cl, int dim) {
	return pat.matchesArray(matcher);
    }

    public boolean matchesPrimitive(PatternMatcher matcher, String prim) {
	return false;
    }

    public boolean matchesPrimitiveArray(PatternMatcher matcher, String prim, int dim) {
	return pat.matchesArray(matcher);
    }

    public ClassnamePatternExpr transformToClassnamePattern(AspectJNodeFactory nf) throws SemanticException {
	return pat.transformToClassnamePattern(nf);
    }

}
