package abc.aspectj.ast;

import abc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class TPEArray_c extends TypePatternExpr_c 
    implements TPEArray
{
    protected TypePatternExpr base;
    protected int dims;

    public TPEArray_c(Position pos, TypePatternExpr base, int dims)  {
	super(pos);
        this.base = base;
	this.dims = dims;
    }

    protected TPEArray_c reconstruct(TypePatternExpr base) {
	if (base != this.base) {
	    TPEArray_c n = (TPEArray_c) copy();
	    n.base = base;
	    return n;
	}
	return this;
    }

    public Node vistChildren(NodeVisitor v) {
	TypePatternExpr base = (TypePatternExpr) visitChild(this.base, v);
	return reconstruct(base);
    }

    public Precedence precedence() {
	return Precedence.UNARY;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	print(base, w, tr);
	for (int i = 0; i < dims; i++) 
	    w.write("[]");
    }

    public boolean matchesClass(PatternMatcher matcher, PCNode cl) {
	return false;
    }

    public boolean matchesClassArray(PatternMatcher matcher, PCNode cl, int dim) {
	if (dim == dims) {
	    return base.matchesClass(matcher, cl);
	}
	if (dim > dims) {
	    return base.matchesClassArray(matcher, cl, dim-dims);
	}
	return false;
    }

    public boolean matchesPrimitive(PatternMatcher matcher, String prim) {
	return false;
    }

    public boolean matchesPrimitiveArray(PatternMatcher matcher, String prim, int dim) {
	if (dim == dims) {
	    return base.matchesPrimitive(matcher, prim);
	}
	if (dim > dims) {
	    return base.matchesPrimitiveArray(matcher, prim, dim-dims);
	}
	return false;
    }

    public ClassnamePatternExpr transformToClassnamePattern(AspectJNodeFactory nf) throws SemanticException {
	throw new SemanticException("Array in classname attern");
    }


}
