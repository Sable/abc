package abc.aspectj.ast;

import polyglot.ast.Node;

public interface ClassTypeDotId extends Node
{
    public ClassnamePatternExpr base();
    public SimpleNamePattern name();

    /* new stuff to be added */
}
