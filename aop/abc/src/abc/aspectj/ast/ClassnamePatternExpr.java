package abc.aspectj.ast;

import abc.aspectj.visit.*;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;


public interface ClassnamePatternExpr extends Node
{

    Precedence precedence();

    void printSubExpr(ClassnamePatternExpr expr, boolean associative,
                      CodeWriter w, PrettyPrinter pp);

    public boolean matches(PatternMatcher matcher, PCNode cl);

}

