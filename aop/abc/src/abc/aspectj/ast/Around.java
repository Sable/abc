package abc.aspectj.ast;

import polyglot.ast.MethodDecl;

/** specification part of around advice.
 * 
 * @author Oege de Moor
 */
public interface Around extends AdviceSpec
{
    void setProceed(MethodDecl proceed);

    MethodDecl proceed();
}
