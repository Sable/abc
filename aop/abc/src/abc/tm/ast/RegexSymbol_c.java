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
public class RegexSymbol_c extends Node_c
                           implements Regex
{
    protected String name;

    public RegexSymbol_c(Position pos, String name)
    {
        super(pos);
        this.name = name;
    }

    public Collection mustBind(Map sym_to_vars)
    {
        return new HashSet((Collection) sym_to_vars.get(name));
    }

    public Collection finalSymbols()
    {
        Collection a = new HashSet();
        a.add(name);
        return a;
    }

    public boolean matchesEmptyString()
    {
        return false;
    }
}
