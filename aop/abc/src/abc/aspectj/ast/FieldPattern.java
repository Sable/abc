package abc.aspectj.ast;

import polyglot.ast.Node;

import java.util.*;

public interface FieldPattern extends Node
{
    /* new stuff to be added */

    public abc.weaving.aspectinfo.FieldPattern makeAIFieldPattern();

    public List/*<ModifierPattern>*/ getModifiers();
    public TypePatternExpr getType();
    public ClassTypeDotId getName();
}
