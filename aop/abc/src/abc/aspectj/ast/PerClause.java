package abc.aspectj.ast;

import polyglot.ast.Node;

import abc.weaving.aspectinfo.*;

/**
 * A <code>PerClause</code> represents the per-clause of an aspect
 * declaration.
 */
public interface PerClause extends Node
{
    /* new stuff to be added */

    public Per makeAIPer();
}
