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

import polyglot.ast.Formal;
import polyglot.types.SemanticException;
import polyglot.util.Position;

import abc.aspectj.ast.Pointcut;

import abc.eaj.ast.PCLocalVars_c;

import java.util.*;

/**
 * @author Julian Tibble
 *
 * Encapsulate all the free variables in a symbol pointcut.
 */
public class ClosedPointcut_c extends PCLocalVars_c
                              implements ClosedPointcut
{
    public ClosedPointcut_c(Position pos, List formals, Pointcut pc)
    {
        super(pos, formals, pc);
    }

    /**
     * we are encapsulating all the variables so mayBind() returns
     * an empty set (and we skip the binding checks for the child
     * pointcut, because they have already been done)
     */
    public Collection mayBind() throws SemanticException
    {
        // Remove all tracematch formals that are not used in
        // the pointcut
 
        Collection pc_locals = pc.mustBind();
        Iterator i = formals.iterator();
        List new_formals = new LinkedList();

        while (i.hasNext()) {
            Formal f = (Formal) i.next();
            if (pc_locals.contains(f.name()))
                new_formals.add(f);
        }
        formals = new_formals;

        return new HashSet();
    }
}
