package polyglot.ext.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.Node_c;

public class ClassTypeDotNew_c extends Node_c implements ClassTypeDotNew
{
    protected TypePatternExpr base;
   
    public ClassTypeDotNew_c(Position pos, 
			      TypePatternExpr base)  {
	super(pos);
        this.base = base;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	if (base != null) {
	    w.write("(");
	    print(base,w,tr);
	    w.write(").");
	}
	w.write("new");
    }

}
