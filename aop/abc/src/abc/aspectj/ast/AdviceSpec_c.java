package polyglot.ext.aspectj.ast;

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

    public AdviceSpec_c(Position pos) {
        super(pos);
        this.formals = null;
	    this.returnType = null;
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

}
