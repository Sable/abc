package arc.aspectj.ast;

import arc.aspectj.visit.*;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;


public interface TypePatternExpr extends Node
{

    Precedence precedence();

    void printSubExpr(TypePatternExpr expr, boolean associative,
                      CodeWriter w, PrettyPrinter pp);

    public boolean matchesClass(PCNode context, PCNode cl);

    public boolean matchesClassArray(PCNode context, PCNode cl, int dim);

    public boolean matchesPrimitive(String prim);

    public boolean matchesPrimitiveArray(String prim, int dim);
}

