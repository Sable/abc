package abc.aspectj.ast;

import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.types.SemanticException;

import abc.aspectj.visit.DeclareParentsAmbiguityRemover;

public interface DeclareParentsExt extends DeclareDecl
{
    /* new stuff to be added */

    public ClassnamePatternExpr pat();
    public TypeNode type();

    public Node disambiguate(DeclareParentsAmbiguityRemover ar) throws SemanticException;
}
