package arc.aspectj.ast;

import arc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class TPENot_c extends TypePatternExpr_c implements TPENot
{
    protected TypePatternExpr tpe;

    public TPENot_c(Position pos, TypePatternExpr tpe)  {
	super(pos);
        this.tpe = tpe;
    }

    public Precedence precedence() {
	return Precedence.UNARY;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("!");
        printSubExpr(tpe, true, w, tr);
    }

    public boolean matchesClass(PatternMatcher matcher, PCNode cl) {
	return !tpe.matchesClass(matcher, cl);
    }

    public boolean matchesClassArray(PatternMatcher matcher, PCNode cl, int dim) {
	return !tpe.matchesClassArray(matcher, cl, dim);
    }

    public boolean matchesPrimitive(PatternMatcher matcher, String prim) {
	return !tpe.matchesPrimitive(matcher, prim);
    }

    public boolean matchesPrimitiveArray(PatternMatcher matcher, String prim, int dim) {
	return !tpe.matchesPrimitiveArray(matcher, prim, dim);
    };

    public ClassnamePatternExpr transformToClassnamePattern(AspectJNodeFactory nf) throws SemanticException {
	ClassnamePatternExpr cpe = tpe.transformToClassnamePattern(nf);
	return nf.CPENot(position, cpe);
    }

}
