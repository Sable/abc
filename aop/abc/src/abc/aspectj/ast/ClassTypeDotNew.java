package abc.aspectj.ast;

import polyglot.ast.Node;

/** represent (ClassNamePatternExpr.new) in pointcuts
 * 
 * @author Oege de Moor
 */
public interface ClassTypeDotNew extends Node
{
    public ClassnamePatternExpr base();

    public boolean equivalent(ClassTypeDotNew c);

    /* new stuff to be added */
}
