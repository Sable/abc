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

import java.util.Collection;
import java.util.Map;

import polyglot.ast.Node;
import polyglot.types.SemanticException;
import abc.tm.weaving.matching.State;
import abc.tm.weaving.matching.StateMachine;

/**
 * @author Julian Tibble
 */
public interface Regex extends Node
{
    /**
     * Return the set of pointcut variables which must be
     * bound by any string which matches this regular
     * expression.
     */
    Collection mustBind(Map sym_to_vars) throws SemanticException;

    /**
     * Return the set containing each symbol which occurs
     * as the final symbol of a string which matches this
     * regular expresion.
     */
    Collection finalSymbols();

    /**
     * Return the set containing each symbol which occurs
     * as a non-final symbol in a string which matches this
     * regular expression.
     *
     * n.b. this set will not, in general, be disjoint from
     *      the set returned by finalSymbols()
     */
    Collection nonFinalSymbols();

    /**
     * Returns true or false if the regular expression
     * matches the empty string, or not, respectively.
     */
    boolean matchesEmptyString();

    /**
     * Creates a finite state machine, equivalent to the
     * regular expression, which is used by the weaver.
     */
    public StateMachine makeSM();

    /**
     * Create a finite state machine for this regular
     * expression, given a start and finish node.
     *
     * @see #makeSM()
     * @parm sm         the state machine under construction
     * @param start     the start node
     * @param finish    the finish node
     * @param own_start true if, and only if, the current
     *                  regular expression is the only one
     *                  to create outgoing transitions from
     *                  the start node
     */
    void makeSM(StateMachine sm, State start, State finish, boolean own_start);

}
