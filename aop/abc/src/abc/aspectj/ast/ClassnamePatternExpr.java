package polyglot.ext.aspectj.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;


public interface ClassnamePatternExpr extends Node
{

    Precedence precedence();

    void printSubExpr(ClassnamePatternExpr expr, boolean associative,
                      CodeWriter w, PrettyPrinter pp);
}

