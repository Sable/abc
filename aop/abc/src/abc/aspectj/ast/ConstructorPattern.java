package abc.aspectj.ast;

import polyglot.ast.Node;

import java.util.*;

/** patterns to capture constructor joinpoints.
 * 
 * @author Oege de Moor
 */
public interface ConstructorPattern extends MethodConstructorPattern
{

    public List/*<ModifierPattern>*/ getModifiers();
    public ClassTypeDotNew getName();
    public List/*<FormalPattern>*/ getFormals();
    public List/*<ThrowsPattern>*/ getThrowspats();

    public abc.weaving.aspectinfo.ConstructorPattern makeAIConstructorPattern();

    public boolean equivalent(ConstructorPattern e);
    public boolean canMatchEmptyArgumentList();
}
