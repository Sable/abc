package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.Node_c;

public class ClassTypeDotId_c extends Node_c implements ClassTypeDotId
{
    protected ClassnamePatternExpr base;
    protected SimpleNamePattern name;
   
    public ClassTypeDotId_c(Position pos, 
			    ClassnamePatternExpr base,
			    SimpleNamePattern name)  {
	super(pos);
        this.base = base;
        this.name = name;
    }

    public ClassnamePatternExpr base() {
	return base;
    }

    public SimpleNamePattern name() {
	return name;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	if (name != null) 
	    w.write("(");
        print(base,w,tr);
        if (name != null) {
	    w.write(").");
	    print(name,w,tr);
        }
    }

}
