package abc.aspectj.ast;

import polyglot.ast.Node;

public interface FormalPattern extends Node
{
    /* new stuff to be added */
    public boolean equivalent(FormalPattern p);
}
