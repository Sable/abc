package abc.eaj.ast;

import java.util.*;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

import abc.aspectj.ast.*;
import abc.aspectj.visit.*;

import abc.eaj.ast.EAJNodeFactory;
import abc.eaj.util.ToReceiver;
import abc.eaj.visit.GlobalPointcuts;


public class GlobalPointcutDecl_c extends PointcutDecl_c
                                  implements GlobalPointcutDecl
{
    ClassnamePatternExpr aspect_pattern; // aspects that match this pattern
    Pointcut pointcut;                   // should conjoin this pointcut
                                         // with the pointcut for each piece of
                                         // advice in the matching aspect
    String name;

    public GlobalPointcutDecl_c(Position pos,
                                ClassnamePatternExpr aspect_pattern,
                                Pointcut pointcut,
                                String name,
                                TypeNode voidn)
    {
        super(pos, Flags.PUBLIC, name, new LinkedList(), pointcut, voidn);
        this.aspect_pattern = aspect_pattern;
        this.pointcut = pointcut;
        this.name = name;
    }

    protected Node reconstruct(Node n, ClassnamePatternExpr aspect_pattern,
                                       Pointcut pointcut)
    {
        if (   aspect_pattern != this.aspect_pattern
            ||       pointcut != this.pointcut)
        {
            GlobalPointcutDecl_c new_n = (GlobalPointcutDecl_c) n.copy();
            new_n.aspect_pattern = aspect_pattern;
            new_n.pointcut = pointcut;

            return new_n;
        }
        return n;
    }

    public Node visitChildren(NodeVisitor v)
    {
        Node n = super.visitChildren(v);

        ClassnamePatternExpr aspect_pattern =
                (ClassnamePatternExpr) visitChild(this.aspect_pattern, v);

        Pointcut pointcut = (Pointcut) visitChild(this.pointcut, v);

        return reconstruct(n, aspect_pattern, pointcut);
    }

    public void registerGlobalPointcut(GlobalPointcuts visitor,
                                       Context context,
                                       EAJNodeFactory nf)
    {
        // construct PCName reference to this global pointcut
        Receiver r = ToReceiver.fromString(nf, position(),
                                           context.currentClass().fullName());
        Pointcut name_ref = nf.PCName(position(), r, name, new LinkedList());

        visitor.addGlobalPointcut(aspect_pattern, name_ref);
    }
}
