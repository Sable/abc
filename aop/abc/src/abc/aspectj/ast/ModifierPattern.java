package abc.aspectj.ast;

import polyglot.ast.Node;

import polyglot.types.*;

/**
 * 
 * @author Oege de Moor
 *
 */
public interface ModifierPattern extends Node
{

    public Flags modifier();
    public boolean positive();

    public boolean equivalent(ModifierPattern p);

    /* new stuff to be added */
}
