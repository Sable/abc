package arc.aspectj.ast;

import arc.aspectj.visit.*;

import polyglot.ast.Node;

import java.util.*;

public interface NamePattern extends Node
{
    public Set/*<PCNode>*/ match(PCNode context, Set/*<PCNode>*/ classes, Set/*<PCNode>*/ packages);

    boolean universal();
}
