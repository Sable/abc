package arc.aspectj.ast;

import arc.aspectj.visit.*;

import polyglot.ast.Node;

public interface RefTypePattern extends Node
{
    /* new stuff to be added */

    public boolean matchesClass(PCNode context, PCNode cl);

    public boolean matchesClassArray(PCNode context, PCNode cl, int dim);

}
