package abc.aspectj.ast;

import polyglot.ast.Node;

public interface ClassTypeDotNew extends Node
{
    public ClassnamePatternExpr base();

    public boolean equivalent(ClassTypeDotNew c);

    /* new stuff to be added */
}
