package arc.aspectj.ast;

import arc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class TPEType_c extends TypePatternExpr_c implements TPEType
{
    protected TypeNode type;
    protected Integer dims;

    public TPEType_c(Position pos, TypeNode type, Integer dims)  {
	super(pos);
        this.type = type;
	this.dims = dims;
    }

    public Precedence precedence() {
	return Precedence.UNARY;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        print(type, w, tr);
    }

    public boolean matchesClass(PCNode context, PCNode cl) {
	return false;
    }

    public boolean matchesClassArray(PCNode context, PCNode cl, int dim) {
	return false;
    }

    public boolean matchesPrimitive(String prim) {
	return dims == null && type.toString().equals(prim);
    }

    public boolean matchesPrimitiveArray(String prim, int dim) {
	return dims != null && dims.intValue() == dim && type.toString().equals(prim);
    }
}
