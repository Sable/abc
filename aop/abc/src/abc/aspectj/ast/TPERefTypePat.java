package abc.aspectj.ast;

/**
 * A type pattern expression that is a reference type pattern.
 * @author Oege de Moor
 * @author Aske Simon Christensen
 */
public interface TPERefTypePat extends TypePatternExpr
{

    public RefTypePattern getPattern();

}
