package abc.aspectj.ast;

import polyglot.ast.Node;

/**
 * 
 * @author Oege de Moor
 * @author Aske Simon Christensen
 */

public interface FormalPattern extends Node
{
    /* new stuff to be added */
    public boolean equivalent(FormalPattern p);
}
