package abc.aspectj.ast;

import polyglot.ast.*;
import polyglot.ext.jl.ast.Expr_c;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

import java.util.List;

public class Comma_c extends Expr_c
                     implements Expr
{
    List /*Local*/ locals;
    List /*Expr*/ exprs;

    public Comma_c(Position pos, List locals, List exprs)
    {
        super(pos);
        this.locals = locals;
        this.exprs = exprs;
    }

    // reachability 

    public Term entry()
    {
        throw new InternalCompilerError("This node should be introduced " +
                                    "too late for that.");
    }

    public List acceptCFG(CFGBuilder v, List succs)
    {
        throw new InternalCompilerError("This node should be introduced " +
                                    "too late for that.");
    }

    public boolean reachable()
    {
        throw new InternalCompilerError("This node should be introduced " +
                                    "too late for that.");
    }

    public Term reachable(boolean reachability)
    {
        throw new InternalCompilerError("This node should be introduced " +
                                    "too late for that.");
    }


    // handle visitors

    protected Node reconstruct(List locals, List exprs)
    {
        if (   this.locals != locals
            || this.exprs != exprs  )
        {
            Comma_c comma = (Comma_c) copy();
            comma.locals = locals;
            comma.exprs = exprs;
            return comma;
        }
        return this;
    }

    public Node visitChildren(NodeVisitor v)
    {
        List locals = visitList(this.locals, v);
        List exprs = visitList(this.exprs, v);
        return reconstruct(locals, exprs);
    }
    
    public String toString() {
        return "Comma_c(" + locals + ", " + exprs + ")";
    }
}
