package abc.aspectj.ast;

import abc.aspectj.visit.*;

import polyglot.ast.Node;
import polyglot.types.SemanticException;

public interface RefTypePattern extends Node
{
    /* new stuff to be added */

    public boolean matchesClass(PatternMatcher matcher, PCNode cl);

    public boolean matchesArray(PatternMatcher matcher);

    public ClassnamePatternExpr transformToClassnamePattern(AspectJNodeFactory nf) throws SemanticException;

    public boolean equivalent(RefTypePattern p);

}

