package abc.eaj.extension;

import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.ast.Pointcut;

import abc.eaj.visit.GlobalPointcuts;

/**
 * @author Julian Tibble
 */
public interface EAJAdviceDecl extends AdviceDecl
{
    EAJAdviceDecl conjoinPointcutWith(GlobalPointcuts visitor, Pointcut global);
}
