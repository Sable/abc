package abc.eaj.ast;

import polyglot.ast.Node;
import polyglot.types.Context;

import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.ast.Pointcut;

import abc.eaj.visit.GlobalPointcuts;

public interface EAJAdviceDecl extends AdviceDecl
{
    EAJAdviceDecl conjoinPointcutWith(GlobalPointcuts visitor, Pointcut global);
}
