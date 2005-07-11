/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
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

import abc.aspectj.ast.*;

import java.util.*;

/**
 * @author Julian Tibble
 *
 * This class is for compiler-generated advice for implementing
 * TraceMatches. This advice requires different type-checking and
 * precedence rules to normal advice.
 */
public class TMAdviceDecl_c extends AdviceDecl_c
                            implements TMAdviceDecl
{
    public TMAdviceDecl_c(Position pos, Flags flags, AdviceSpec spec,
                            List throwTypes, Pointcut pc, Block body)
    {
        super(pos, flags, spec, throwTypes, pc, body);
    }

    public Node typeCheck(TypeChecker tc) throws SemanticException
    {
        // Remove all tracematch formals that are not used in
        // the pointcut for this piece of advice.
 
        Collection pc_locals = pc.mayBind();
        Iterator i = formals.iterator();
        List new_formals = new LinkedList();

        while (i.hasNext()) {
            Formal f = (Formal) i.next();
            if (pc_locals.contains(f.name()))
                new_formals.add(f);
        }
        formals = new_formals;

        return super.typeCheck(tc);
    }

    // for debugging;
    public NodeVisitor disambiguateEnter(AmbiguityRemover ar)
                                            throws SemanticException
    {
        return super.disambiguateEnter(ar);
    }
}
