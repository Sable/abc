package abc.aspectj.ast;

import abc.aspectj.visit.*;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

public class TPEType_c extends TypePatternExpr_c implements TPEType
{
    protected TypeNode type;

    public TPEType_c(Position pos, TypeNode type)  {
	super(pos);
        this.type = type;
    }

    public Precedence precedence() {
		return Precedence.UNARY;
    }

	/** Reconstruct the type pattern */
	protected TPEType_c reconstruct(TypeNode type) {
		if (this.type != type) {
			 TPEType_c n = (TPEType_c) copy();
			 n.type = type;
			 return n;
		}
		return this;
	}

	/** Visit the children of the type pattern. */
	public Node visitChildren(NodeVisitor v) {
		TypeNode type = (TypeNode) visitChild(this.type, v);
		return reconstruct(type);
	}
    
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        print(type, w, tr);
    }

    public String toString() {
	return type.toString();
    }

    public boolean matchesClass(PatternMatcher matcher, PCNode cl) {
	return false;
    }

    public boolean matchesClassArray(PatternMatcher matcher, PCNode cl, int dim) {
	return false;
    }

    public boolean matchesPrimitive(PatternMatcher matcher, String prim) {
	return type.toString().equals(prim);
    }

    public boolean matchesPrimitiveArray(PatternMatcher matcher, String prim, int dim) {
	return false;
    }

    public ClassnamePatternExpr transformToClassnamePattern(AspectJNodeFactory nf) throws SemanticException {
	throw new SemanticException("Primitive type in classname pattern");
    }

}
