package abc.aspectj.ast;

import polyglot.ast.Node;


/** 
 * represent  ClassnamePatternExpr.SimpleNamePattern in pointcuts.
 * 
 * @author Oege de Moor
 */
public interface ClassTypeDotId extends Node
{
    public ClassnamePatternExpr base();
    public SimpleNamePattern name();

    public boolean equivalent(ClassTypeDotId c);
}
