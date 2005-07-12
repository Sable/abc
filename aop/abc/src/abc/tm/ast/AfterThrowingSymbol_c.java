/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
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

import polyglot.ext.jl.ast.*;

import abc.aspectj.ast.*;

import java.util.*;

/**
 * @author Julian Tibble
 */
public class AfterThrowingSymbol_c extends Node_c
                                   implements SymbolKind
{
    private Local exception_var;

    public AfterThrowingSymbol_c(Position pos, Local exception_var)
    {
        super(pos);
        this.exception_var = exception_var;
    }

    public String kind()
    {
        return AFTER;
    }

    public AdviceSpec generateAdviceSpec(TMNodeFactory nf, List formals,
                                            TypeNode voidn)
    {
        // Find the type of the pointcut variable for the thrown exception
        AdviceFormal new_formal = null;

        Iterator i = formals.iterator();

        while (i.hasNext() && new_formal == null) {
            Formal f = (Formal) i.next();
            if (f.name().equals(exception_var.name()))
            new_formal = nf.AdviceFormal(exception_var.position(),
                                Flags.NONE, f.type(), f.name());
        }

        // Generate the advice spec
        return nf.AfterThrowing(position(), formals, new_formal, voidn);
    }

    public AdviceSpec generateSomeAdviceSpec(TMNodeFactory nf, TypeNode voidn,
                                                TypeNode ret_type)
    {
        return nf.After(position(), new LinkedList(), voidn);
    }
}
