package abc.aspectj.ast;

import polyglot.ast.Node;

import polyglot.types.*;

/**
 * 
 * @author Oege de Moor
 * @ author Aske Simon Christensen
 */
public interface ThrowsPattern extends Node
{

    public ClassnamePatternExpr type();
    public boolean positive();

    /* new stuff to be added */

    public boolean equivalent(ThrowsPattern p);
}
