package arc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

import polyglot.ext.jl.ast.Node_c;

public class Pointcut_c extends Node_c implements Pointcut
{

    public Pointcut_c(Position pos) {
        super(pos);
    }

    public Precedence precedence() {
	return Precedence.UNKNOWN;
    }

    public void printSubExpr(Pointcut pc, boolean associative,
                             CodeWriter w, PrettyPrinter pp) {
        if (! associative && precedence().equals(pc.precedence()) ||
	    precedence().isTighter(pc.precedence())) {
	    w.write("(");
            printBlock(pc, w, pp);
	    w.write( ")");
	}
        else {
            printBlock(pc, w, pp);
        }
    }
}
