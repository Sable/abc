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

    public boolean matchesClass(PCNode context, PCNode cl) {
	return !tpe.matchesClass(context, cl);
    }

    public boolean matchesClassArray(PCNode context, PCNode cl, int dim) {
	return !tpe.matchesClassArray(context, cl, dim);
    }

    public boolean matchesPrimitive(String prim) {
	return !tpe.matchesPrimitive(prim);
    }

    public boolean matchesPrimitiveArray(String prim, int dim) {
	return !tpe.matchesPrimitiveArray(prim, dim);
    };
}
