package arc.aspectj.ast;

import arc.aspectj.visit.*;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;


public interface TypePatternExpr extends Node
{

    Precedence precedence();

    void printSubExpr(TypePatternExpr expr, boolean associative,
                      CodeWriter w, PrettyPrinter pp);

    public boolean matchesClass(PatternMatcher matcher, PCNode cl);

    public boolean matchesClassArray(PatternMatcher matcher, PCNode cl, int dim);

    public boolean matchesPrimitive(PatternMatcher matcher, String prim);

    public boolean matchesPrimitiveArray(PatternMatcher matcher, String prim, int dim);

    public ClassnamePatternExpr transformToClassnamePattern(AspectJNodeFactory nf) throws SemanticException;
}

