/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Julian Tibble
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.tm.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;

import polyglot.ext.jl.ast.*;

import abc.aspectj.ast.*;

import java.util.*;

/**
 * @author Julian Tibble
 */
public class AfterThrowingSymbol_c extends Node_c
                                   implements SymbolKind
{
    private Local exception_var = null;

    public AfterThrowingSymbol_c(Position pos)
    {
        super(pos);
    }

    public AfterThrowingSymbol_c(Position pos, Local exception_var)
    {
        super(pos);
        this.exception_var = exception_var;
    }

    public String kind()
    {
        return AFTER;
    }

    public Collection binds()
    {
        Collection binds = new HashSet();
        if (exception_var != null)
            binds.add(exception_var.name());
        return binds;
    }

    public AdviceSpec generateAdviceSpec(TMNodeFactory nf, List formals,
                                            TypeNode voidn)
    {
        AdviceFormal exception_formal = null;
        TypeNode exception_type = getExceptionType(formals);

        if (exception_var != null && exception_type != null) {
            exception_formal = nf.AdviceFormal(exception_var.position(),
                                               Flags.NONE,
                                               exception_type,
                                               exception_var.name());
        }

        // Generate the advice spec
        return nf.AfterThrowing(position(), formals, exception_formal, voidn);
    }

    private TypeNode getExceptionType(List formals)
    {
        if (exception_var == null)
            return null;

        // Find the type of the pointcut variable for the exception thrown
        Iterator i = formals.iterator();
        TypeNode exception_type = null;

        while (i.hasNext() && exception_type == null) {
            Formal f = (Formal) i.next();
            if (f.name().equals(exception_var.name()))
                exception_type = f.type();
        }

        return exception_type;
    }

    // node visiting code
    protected Node reconstruct(Local exception_var)
    {
        if (this.exception_var != exception_var) {
            AfterThrowingSymbol_c n = (AfterThrowingSymbol_c) this.copy();
            n.exception_var = exception_var;
            return n;
        }
        return this;
    }

    public Node visitChildren(NodeVisitor v)
    {
        Local exception_var = (Local) visitChild(this.exception_var, v);
        return reconstruct(exception_var);
    }

    public List aroundVars()
    {
        return null;
    }
}
