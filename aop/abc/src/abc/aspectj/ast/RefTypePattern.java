package abc.aspectj.ast;

import abc.aspectj.visit.*;

import polyglot.ast.Node;
import polyglot.types.SemanticException;

/**
 * A pattern for a reference type
 * 
 * @author Aske Simon Christensen
 * @author Oege de Moor
 *
 */
public interface RefTypePattern extends Node
{
    /* new stuff to be added */

    public boolean matchesClass(PatternMatcher matcher, PCNode cl);

    public boolean matchesArray(PatternMatcher matcher);

    public ClassnamePatternExpr transformToClassnamePattern(AJNodeFactory nf) throws SemanticException;

    public boolean equivalent(RefTypePattern p);

}

