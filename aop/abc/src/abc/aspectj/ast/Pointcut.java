package arc.aspectj.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

public interface Pointcut extends Node
{
     /** Get the precedence of the expression. */
    Precedence precedence();

    void printSubExpr(Pointcut pc, boolean associative,
                      CodeWriter w, PrettyPrinter pp);
}





