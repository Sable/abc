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

import polyglot.ext.jl.ast.Node_c;
import polyglot.types.SemanticException;
import polyglot.util.Position;

import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.StateMachine;

import java.util.*;

/**
 * @author Julian Tibble
 */
public class RegexSymbol_c extends Regex_c
{
    protected String name;

    public RegexSymbol_c(Position pos, String name)
    {
        super(pos);
        this.name = name;
    }

    public Collection mustBind(Map sym_to_vars) throws SemanticException
    {
        if (!sym_to_vars.containsKey(name))
            throw new SemanticException("Symbol \"" + name + "\" not found.", position());

        return new HashSet((Collection) sym_to_vars.get(name));
    }

    public Collection finalSymbols()
    {
        Collection a = new HashSet();
        a.add(name);
        return a;
    }

    public Collection nonFinalSymbols()
    {
        return new HashSet();
    }

    public boolean matchesEmptyString()
    {
        return false;
    }

    public void makeSM(StateMachine sm, State start, State finish,
                       boolean own_start)
    {
        sm.newTransition(start, finish, name);
    }
}
