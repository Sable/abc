package arc.aspectj.ast;

import arc.aspectj.visit.*;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;


public interface ClassnamePatternExpr extends Node
{

    Precedence precedence();

    void printSubExpr(ClassnamePatternExpr expr, boolean associative,
                      CodeWriter w, PrettyPrinter pp);

    public boolean matches(PCNode context, PCNode cl);

}

