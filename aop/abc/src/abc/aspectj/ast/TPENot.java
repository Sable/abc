package abc.aspectj.ast;

/**
 * negation of a type pattern expression.
 * @author Oege de Moor
 * @author Aske Simon Christensen
 *
 */
public interface TPENot extends TypePatternExpr
{

    public TypePatternExpr getTpe();

}
