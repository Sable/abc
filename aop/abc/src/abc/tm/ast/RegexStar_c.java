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

import polyglot.ast.Node;
import polyglot.ext.jl.ast.Node_c;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;

import abc.aspectj.ast.Pointcut;
import abc.aspectj.visit.AspectMethods;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.StateMachine;

import java.util.*;

/**
 * @author Julian Tibble
 * @author Eric Bodden
 */
public class RegexStar_c extends Regex_c
{
    protected Regex a;

    public RegexStar_c(Position pos, Regex a)
    {
        super(pos);
        this.a = a;
    }

    public Collection mustBind(Map sym_to_vars) throws SemanticException
    {
        return new HashSet();
    }

    public Collection finalSymbols()
    {
        return a.finalSymbols();
    }

    public Collection nonFinalSymbols()
    {
        Collection c = a.nonFinalSymbols();
        c.addAll(a.finalSymbols());
        return c;
    }

    public boolean matchesEmptyString()
    {
        return true;
    }

    /** 
     * {@inheritDoc}
     */
    public void makeSM(StateMachine sm, State start, State finish,
                       boolean own_start)
    {
        State loop_node;
        
        if (own_start) {
            loop_node = start;
        } else {
            loop_node = sm.newState();
            sm.newTransition(start, loop_node, null);
        }

        a.makeSM(sm, loop_node, loop_node, false);
        sm.newTransition(loop_node, finish, null);
    }
    
    /** 
     * {@inheritDoc}
     */
    public void makeNecessarySymbolsSM(StateMachine sm, State start,
    		State finish, boolean own_start) {
    	//simply generate epsilon-transitions, as anything within a
    	//Kleene-star is not necessary to reach a final state 
        sm.newTransition(start, finish, null);
    }
    
    protected Node reconstruct(Node n, Regex a)
    {
        if (a != this.a)
        {
            RegexStar_c new_n = (RegexStar_c) n.copy();
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
