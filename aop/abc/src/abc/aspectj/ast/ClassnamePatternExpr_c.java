package polyglot.ext.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.Node_c;

public class ClassnamePatternExpr_c extends Node_c 
                                    implements ClassnamePatternExpr
{

    public ClassnamePatternExpr_c(Position pos) {
        super(pos);
    }

    public Precedence precedence() {
	return Precedence.UNKNOWN;
    }

    public void printSubExpr(ClassnamePatternExpr expr, boolean associative,
                             CodeWriter w, PrettyPrinter pp) {
        if (! associative && precedence().equals(expr.precedence()) ||
	    precedence().isTighter(expr.precedence())) {
	    w.write("(");
            printBlock(expr, w, pp);
	    w.write( ")");
	}
        else {
            printBlock(expr, w, pp);
        }
    }

}
