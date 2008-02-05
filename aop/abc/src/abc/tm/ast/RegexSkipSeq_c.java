/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Julian Tibble
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

import polyglot.ast.Node;
import polyglot.ext.jl.ast.Node_c;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;

import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.StateMachine;

import java.util.*;

/**
 * @author Julian Tibble
 * @author Eric Bodden
 */
public class RegexSkipSeq_c extends Regex_c
{
    protected Regex before;
    protected List prohibited; // symbols not allowed to occur in between
    protected Set allowed;
    protected Regex after;

    public RegexSkipSeq_c(Position pos, Regex before,
                          List prohibited, Regex after)
    {
        super(pos);
        this.before = before;
        this.prohibited = prohibited;
        this.after = after;
    }

    public Collection mustBind(Map sym_to_vars) throws SemanticException
    {
        Iterator i = prohibited.iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            if (!sym_to_vars.containsKey(name))
                throw new SemanticException("Symbol \"" + name +
                                            "\" not found.", position());
        }

        allowed = new HashSet(sym_to_vars.keySet());
        allowed.removeAll(prohibited);

        Collection result = before.mustBind(sym_to_vars);
        result.addAll(after.mustBind(sym_to_vars));
        return result;
    }

    public Collection finalSymbols()
    {
        return after.finalSymbols();
    }

    public Collection nonFinalSymbols()
    {
        Collection result = before.nonFinalSymbols();
        result.addAll(before.finalSymbols());
        result.addAll(after.nonFinalSymbols());
        return result;
    }

    public boolean matchesEmptyString()
    {
        return false;
    }

    /** 
     * {@inheritDoc}
     */
    public void makeSM(StateMachine sm, State start, State finish,
                       boolean own_start)
    {
        State middle = sm.newState();
        before.makeSM(sm, start, middle, own_start);
        after.makeSM(sm, middle, finish, false);

        Iterator i = allowed.iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            sm.newTransition(middle, middle, name);
        }
    }
    
    protected Node reconstruct(Node n, Regex before, Regex after)
    {
        if (before != this.before || after != this.after)
        {
            RegexSkipSeq_c new_n = (RegexSkipSeq_c) n.copy();
            new_n.before = before;
            new_n.after = after;
            return new_n;
        }
        return n;
    }

    public Node visitChildren(NodeVisitor v)
    {
        Node n = super.visitChildren(v);

        Regex before = (Regex) visitChild(this.before, v);
        Regex after = (Regex) visitChild(this.before, v);
        
        return reconstruct(n, before, after);
    }
}
