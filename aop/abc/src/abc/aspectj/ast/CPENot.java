package abc.aspectj.ast;

/** negation of a ClassnamePatternExpr.
 * 
 * @author Oege de Moor
 * @ author Aske Simon Christensen
 */
public interface CPENot extends ClassnamePatternExpr
{

    public ClassnamePatternExpr getCpe();

}
