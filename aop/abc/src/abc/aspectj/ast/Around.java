package abc.aspectj.ast;

import polyglot.ast.MethodDecl;

public interface Around extends AdviceSpec
{
    void setProceed(MethodDecl proceed);
}
