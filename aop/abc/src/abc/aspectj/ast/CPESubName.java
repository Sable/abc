package abc.aspectj.ast;

/** a (class+) ClassnamePatternExpr that matches all subclasses.
 * 
 * @author Oege de Moor
 * @author Aske Simon Christensen
 */
public interface CPESubName extends ClassnamePatternExpr
{

    public NamePattern getNamePattern();

}
