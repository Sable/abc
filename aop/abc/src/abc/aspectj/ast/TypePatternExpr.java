package polyglot.ext.aspectj.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;


public interface TypePatternExpr extends Node
{

    Precedence precedence();

    void printSubExpr(TypePatternExpr expr, boolean associative,
                      CodeWriter w, PrettyPrinter pp);
}

