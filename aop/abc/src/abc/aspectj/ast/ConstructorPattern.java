package abc.aspectj.ast;

import polyglot.ast.Node;

import java.util.*;

public interface ConstructorPattern extends MethodConstructorPattern
{
    /* new stuff to be added */

    public List/*<ModifierPattern>*/ getModifiers();
    public ClassTypeDotNew getName();
    public List/*<FormalPattern>*/ getFormals();
    public List/*<ThrowsPattern>*/ getThrowspats();

    public abc.weaving.aspectinfo.ConstructorPattern makeAIConstructorPattern();
}
