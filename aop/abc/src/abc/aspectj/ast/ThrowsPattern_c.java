package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.Node_c;

public class ThrowsPattern_c extends Node_c implements ThrowsPattern
{
    protected ClassnamePatternExpr type;
    protected boolean positive;

    public ThrowsPattern_c(Position pos, 
			     ClassnamePatternExpr type, 
			     boolean positive)  {
	super(pos);
        this.type = type;
	this.positive = positive;
    }

    protected ThrowsPattern_c reconstruct(ClassnamePatternExpr type) {
	if (type != this.type) {
	    ThrowsPattern_c n = (ThrowsPattern_c) copy();
	    n.type = type;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	ClassnamePatternExpr type = (ClassnamePatternExpr) visitChild(this.type, v);
	return reconstruct(type);
    }

    public ClassnamePatternExpr type() {
	return type;
    }

    public boolean positive() {
	return positive;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	if (!positive)
	    w.write("!");
	print(type,w,tr);
    }

    public String toString() {
	if(positive) return type.toString();
	else return "!"+type.toString();
    }

}
