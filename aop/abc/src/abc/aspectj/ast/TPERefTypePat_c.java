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

    public boolean matchesClass(PCNode context, PCNode cl) {
	return pat.matchesClass(context, cl);
    }

    public boolean matchesClassArray(PCNode context, PCNode cl, int dim) {
	return pat.matchesClassArray(context, cl, dim);
    }

    public boolean matchesPrimitive(String prim) {
	return false;
    }

    public boolean matchesPrimitiveArray(String prim, int dim) {
	return false;
    }

}
