package abc.aspectj.ast;

import polyglot.ast.TypeNode;

public interface DeclareParentsExt extends DeclareDecl
{
    /* new stuff to be added */

    public ClassnamePatternExpr pat();
    public TypeNode type();

}
