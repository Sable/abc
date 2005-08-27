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
public class AroundSymbol_c extends Node_c
                            implements SymbolKind
{
    // n.b. this list is not visited because it is retrieved
    //      by TMDecl_c
    private List proceed_vars;

    public AroundSymbol_c(Position pos, List proceed_vars)
    {
        super(pos);
        this.proceed_vars = proceed_vars;
    }

    public String kind()
    {
        return AROUND;
    }

    public Collection binds()
    {
        return new HashSet();
    }

    public AdviceSpec generateAdviceSpec(TMNodeFactory nf, List formals,
                                            TypeNode voidn)
    {
        // return a piece of `before' advice, not `around' advice,
        // because it is just to update bindings/dfa etc.
        return nf.Before(position(), formals, voidn);
    }

    public List aroundVars()
    {
        return proceed_vars;
    }
}
