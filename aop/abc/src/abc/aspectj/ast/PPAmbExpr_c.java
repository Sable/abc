package polyglot.ext.aspectj.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;
import java.util.*;

import polyglot.ext.jl.ast.AmbExpr_c;

public class PPAmbExpr_c extends AmbExpr_c implements AmbExpr
{

    public PPAmbExpr_c(Position pos, String name) {
	super(pos,name);
    }

    public Precedence precedence() {   
	return Precedence.LITERAL;   
    } 

}                               
