package abc.aspectj.ast;

import polyglot.ast.Node;
import polyglot.types.SemanticException;

import abc.aspectj.visit.DeclareParentsAmbiguityRemover;

import abc.weaving.aspectinfo.AbcClass;

import java.util.*;

public interface DeclareParentsImpl extends DeclareDecl
{
    /* new stuff to be added */

    public ClassnamePatternExpr pat();
    public List/*<TypeNode>*/ interfaces();

    public Node disambiguate(DeclareParentsAmbiguityRemover ar) throws SemanticException;

    public void addTarget(AbcClass cl);
}
