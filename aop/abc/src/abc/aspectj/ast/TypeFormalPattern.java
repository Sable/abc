package abc.aspectj.ast;

import polyglot.ast.Node;

/**
 * A formal pattern that is a type pattern expr.
 * @author Oege de Moor
 */
public interface TypeFormalPattern extends FormalPattern
{
    /* new stuff to be added */

    public TypePatternExpr expr();
}
