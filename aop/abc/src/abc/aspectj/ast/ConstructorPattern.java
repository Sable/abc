package abc.aspectj.ast;

import polyglot.ast.Node;

public interface ConstructorPattern extends MethodConstructorPattern
{
    /* new stuff to be added */

    public abc.weaving.aspectinfo.ConstructorPattern makeAIConstructorPattern();
}
