package abc.aspectj.ast;

import polyglot.ast.Node;

import java.util.List;

/**
 * 
 * @author Oege de Moor
 * @author Aske Simon Christensen
 */
public interface MethodPattern extends MethodConstructorPattern
{
    public abc.weaving.aspectinfo.MethodPattern makeAIMethodPattern();

    /* probably should delegate matching to this instead, but this is easier for now */
    public List/*<ModifierPattern>*/ getModifiers();
    public TypePatternExpr getType();
    public ClassTypeDotId getName();
    public List/*<FormalPattern>*/ getFormals();
    public List/*<ThrowsPattern>*/ getThrowspats();

    public boolean equivalent(MethodPattern e);
}
