package abc.aspectj.ast;

import polyglot.ast.Node;

public interface MethodPattern extends MethodConstructorPattern
{
    /* new stuff to be added */

    public abc.weaving.aspectinfo.MethodPattern makeAIMethodPattern();
}
