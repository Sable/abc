/* Abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL; 
 * if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.eaj.ast;

import java.util.*;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

import abc.aspectj.ast.*;

import abc.eaj.ast.EAJNodeFactory;
import abc.eaj.util.ToReceiver;
import abc.eaj.visit.GlobalPointcuts;

/**
 * @author Julian Tibble
 */
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
