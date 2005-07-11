/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
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

import polyglot.ext.jl.ast.Node_c;
import polyglot.util.Position;

import java.util.*;

/**
 * @author Julian Tibble
 */
public class RegexAlternation_c extends Node_c
                                implements Regex
{
    protected Regex a;
    protected Regex b;

    public RegexAlternation_c(Position pos, Regex a, Regex b)
    {
        super(pos);
        this.a = a;
        this.b = b;
    }

    public Collection mustContain(Map sym_to_vars)
    {
        Collection c = a.mustContain(sym_to_vars);
        c.retainAll(b.mustContain(sym_to_vars));
        return c;
    }

    public Collection finalSymbols()
    {
        Collection c = a.finalSymbols();
        c.addAll(b.finalSymbols());
        return c;
    }

    public boolean matchesEmptyString()
    {
        return a.matchesEmptyString() || b.matchesEmptyString();
    }
}
