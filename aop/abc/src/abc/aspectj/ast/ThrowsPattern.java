package abc.aspectj.ast;

import polyglot.ast.Node;

import polyglot.types.*;

public interface ThrowsPattern extends Node
{

    public ClassnamePatternExpr type();
    public boolean positive();

    /* new stuff to be added */
}
