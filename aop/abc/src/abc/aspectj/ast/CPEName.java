package abc.aspectj.ast;

/** ClassnamePatternExpr that is just a name pattern.
 * 
 * @author Oege de Moor
 */
public interface CPEName extends ClassnamePatternExpr
{

    public NamePattern getNamePattern();

}
