package abc.aspectj.ast;

/** A type pattern expression for array types.
 * 
 * @author Oege de Moor
 * @author Aske Simon Christensen
 */
public interface TPEArray extends TypePatternExpr
{

    public TypePatternExpr base();
    public int dims();

}
