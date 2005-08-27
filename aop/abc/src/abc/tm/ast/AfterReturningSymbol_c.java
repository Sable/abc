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
public class AfterReturningSymbol_c extends Node_c
                                    implements SymbolKind
{
    private Local return_var = null;

    public AfterReturningSymbol_c(Position pos)
    {
        super(pos);
    }

    public AfterReturningSymbol_c(Position pos, Local return_var)
    {
        super(pos);
        this.return_var = return_var;
    }

    public String kind()
    {
        return AFTER;
    }

    public Collection binds()
    {
        Collection binds = new HashSet();
        if (return_var != null)
            binds.add(return_var.name());
        return binds;
    }

    public AdviceSpec generateAdviceSpec(TMNodeFactory nf, List formals,
                                            TypeNode voidn)
    {
        AdviceFormal return_val_formal = null;
        TypeNode return_type = getReturnType(formals);

        if (return_var != null && return_type != null) {
            return_val_formal = nf.AdviceFormal(return_var.position(),
                                                Flags.NONE,
                                                return_type,
                                                return_var.name());
        }
      
        
        // Generate the advice spec
        return nf.AfterReturning(position(), formals, return_val_formal, voidn);
    }

    private TypeNode getReturnType(List formals)
    {
        if (return_var == null)
            return null;

        // Find the type of the pointcut variable for the return value
        Iterator i = formals.iterator();
        TypeNode return_type = null;

        while (i.hasNext() && return_type == null) {
            Formal f = (Formal) i.next();
            if (f.name().equals(return_var.name()))
                return_type = f.type();
        }

        return return_type;
    }

    // node visiting code
    protected Node reconstruct(Local return_var)
    {
        if (this.return_var != return_var) {
            AfterReturningSymbol_c n = (AfterReturningSymbol_c) this.copy();
            n.return_var = return_var;
            return n;
        }
        return this;
    }

    public Node visitChildren(NodeVisitor v)
    {
        Local return_var = (Local) visitChild(this.return_var, v);
        return reconstruct(return_var);
    }

    public List aroundVars()
    {
        return null;
    }
}
