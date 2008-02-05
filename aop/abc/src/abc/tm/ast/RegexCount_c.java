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
import java.util.HashSet;
import java.util.Map;

import polyglot.ast.Node;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.StateMachine;

/**
 * @author Julian Tibble
 * @author Eric Bodden
 */
public class RegexCount_c extends Regex_c
{
    protected Regex a;
    protected int min;
    protected int max;

    public RegexCount_c(Position pos, Regex a, int min, int max)
    {
        super(pos);
        this.a = a;
        this.min = min;
        this.max = max;
    }

    public Collection mustBind(Map sym_to_vars) throws SemanticException
    {
        if (min == 0)
            return new HashSet();
        else
            return a.mustBind(sym_to_vars);
    }

    public Collection finalSymbols()
    {
        return a.finalSymbols();
    }

    public Collection nonFinalSymbols()
    {
        Collection c = a.nonFinalSymbols();

        if (max > 1)
            c.addAll(a.finalSymbols());

        return c;
    }

    public boolean matchesEmptyString()
    {
        return min == 0 || a.matchesEmptyString();
    }
       
    /** 
     * {@inheritDoc}
     */
    public void makeSM(StateMachine sm, State start, State finish,
                       boolean own_start)
    {
        if (min == 0)
            sm.newTransition(start, finish, null);

        State middle = start;

        // max is always >= 1
        for (int i = 1; i < max; i++) {
            State s = sm.newState();
            a.makeSM(sm, middle, s, false);
            if (i >= min)
                sm.newTransition(s, finish, null);
            middle = s;
        }

        a.makeSM(sm, middle, finish, false);
    }
    
    protected Node reconstruct(Node n, Regex a)
    {
        if (a != this.a)
        {
            RegexCount_c new_n = (RegexCount_c) n.copy();
            new_n.a = a;
            return new_n;
        }
        return n;
    }

    public Node visitChildren(NodeVisitor v)
    {
        Node n = super.visitChildren(v);

        Regex a = (Regex) visitChild(this.a, v);
        
        return reconstruct(n, a);
    }
    
}
