package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.Node_c;

public class TypeFormalPattern_c extends Node_c 
                                 implements TypeFormalPattern
{

    TypePatternExpr expr;

    public TypeFormalPattern_c(Position pos,
			       TypePatternExpr expr) {
        super(pos);
        this.expr = expr;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
	print(expr,w,pp);
    }

}
