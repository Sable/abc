package abc.aspectj.ast;

import polyglot.util.Enum;
import polyglot.ast.Node;
import polyglot.types.SemanticException;

import abc.aspectj.visit.DeclareParentsAmbiguityRemover;

import abc.weaving.aspectinfo.AbcClass;

import java.util.*;

public interface DeclareParents extends DeclareDecl
{
    /* new stuff to be added */

    public static class Kind extends Enum {
	public Kind(String name) {
	    super(name);
	}
    }

    public static final Kind EXTENDS = new Kind("extends");
    public static final Kind IMPLEMENTS = new Kind("implements");


    public ClassnamePatternExpr pat();
    public List/*<TypeNode>*/ parents();
    public Kind kind();

    public Node disambiguate(DeclareParentsAmbiguityRemover ar) throws SemanticException;

    public void setKind(Kind kind);
    public void addTarget(AbcClass cl);
}
