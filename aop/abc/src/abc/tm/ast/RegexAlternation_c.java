/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Julian Tibble
 * Copyright (C) 2007 Eric Bodden
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

import java.util.Collection;
import java.util.Map;

import polyglot.types.SemanticException;
import polyglot.util.Position;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.StateMachine;

/**
 * @author Julian Tibble
 * @author Eric Bodden
 */
public class RegexAlternation_c extends Regex_c
{
    protected Regex a;
    protected Regex b;

    public RegexAlternation_c(Position pos, Regex a, Regex b)
    {
        super(pos);
        this.a = a;
        this.b = b;
    }

    public Collection mustBind(Map sym_to_vars) throws SemanticException
    {
        Collection c = a.mustBind(sym_to_vars);
        c.retainAll(b.mustBind(sym_to_vars));
        return c;
    }

    public Collection finalSymbols()
    {
        Collection c = a.finalSymbols();
        c.addAll(b.finalSymbols());
        return c;
    }

    public Collection nonFinalSymbols()
    {
        Collection c = a.nonFinalSymbols();
        c.addAll(b.nonFinalSymbols());
        return c;
    }

    public boolean matchesEmptyString()
    {
        return a.matchesEmptyString() || b.matchesEmptyString();
    }

    /** 
     * {@inheritDoc}
     */
    public void makeSM(StateMachine sm, State start,
                       State finish, boolean own_start)
    {
        a.makeSM(sm, start, finish, false);
        b.makeSM(sm, start, finish, false);
    }

	/** 
	 * {@inheritDoc}
	 */
	public void makeNecessarySymbolsSM(StateMachine sm, State start,
			State finish, boolean own_start) {
        a.makeNecessarySymbolsSM(sm, start, finish, false);
        b.makeNecessarySymbolsSM(sm, start, finish, false);
	}
}
