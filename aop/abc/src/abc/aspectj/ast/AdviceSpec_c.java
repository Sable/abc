package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.Node_c;

public class AdviceSpec_c extends Node_c implements AdviceSpec
{
    protected List formals;
    protected TypeNode returnType;
    protected Formal returnVal;

    public AdviceSpec_c(Position pos, List formals, TypeNode returnType, Formal returnVal) {
        super(pos);
        this.formals = formals;
	this.returnType = returnType;
	this.returnVal = returnVal;
    }

    protected AdviceSpec_c reconstruct(List formals, TypeNode returnType, Formal returnVal) {
	if (!CollectionUtil.equals(formals, this.formals) ||
	    returnType != this.returnType ||
	    returnVal != this.returnVal) {
	    AdviceSpec_c n = (AdviceSpec_c) copy();
	    n.formals = formals; //FIXME: Copy list?
	    n.returnType = returnType;
	    n.returnVal = returnVal;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	List formals = (List) visitList(this.formals, v);
	TypeNode returnType = (TypeNode) visitChild(this.returnType, v);
	Formal returnVal = (Formal) visitChild(this.returnVal, v);
	return reconstruct(formals, returnType, returnVal);
    }

    public List formals() {
	return formals;
    }

    public TypeNode returnType() {
	return returnType;
    }
    
    public Formal returnVal() {
    	return returnVal;
    }

   public String kind() {
   		return "advice";
   }
}
