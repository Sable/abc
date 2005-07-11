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
public class AfterReturningSymbol_c extends Node_c
                                    implements SymbolKind
{
    boolean bindsReturnVal = false;
    private Local return_var = null;

    public AfterReturningSymbol_c(Position pos)
    {
        super(pos);
    }

    public AfterReturningSymbol_c(Position pos, Local return_var)
    {
        super(pos);
        this.bindsReturnVal = true;
        this.return_var = return_var;
    }

    public AdviceSpec generateAdviceSpec(TMNodeFactory nf, List formals,
                                            TypeNode voidn)
    {
        // Find the type of the pointcut variable for the return value
        AdviceFormal new_formal = null;
        Iterator i = formals.iterator();

        while (i.hasNext() && new_formal == null) {
            Formal f = (Formal) i.next();
            if (f.name().equals(return_var.name()))
                new_formal = nf.AdviceFormal(return_var.position(),
                                    Flags.NONE, f.type(), f.name());
        }

        // Generate the advice spec
        return nf.AfterReturning(position(), formals, new_formal, voidn);
    }
}
