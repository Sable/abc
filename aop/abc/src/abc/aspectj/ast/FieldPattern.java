package abc.aspectj.ast;

import polyglot.ast.Node;

public interface FieldPattern extends Node
{
    /* new stuff to be added */

    public abc.weaving.aspectinfo.FieldPattern makeAIFieldPattern();
}
