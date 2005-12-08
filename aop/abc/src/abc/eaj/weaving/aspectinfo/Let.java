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

package abc.eaj.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import soot.*;

import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;

import abc.eaj.weaving.residues.*;

/** Handler for <code>let</code> condition pointcut.
 *  @author Julian Tibble
 */
public class Let extends If
{
    private Var bound_var;

    public Let(Var bound_var, List vars, MethodSig impl,
              int jp, int jpsp, int ejp, Position pos)
    {
        super(vars, impl, jp, jpsp, ejp, pos);
        this.bound_var = bound_var;
    }

    public Var getBoundVar()
    {
        return bound_var;
    }

    public String toString() {
        return "let(...)";
    }

    public void getFreeVars(Set/*<String>*/ result)
    {
        result.add(bound_var.getName());
    }

    public Pointcut inline(Hashtable renameEnv,
                           Hashtable typeEnv,
                           Aspect context,
                           int cflowdepth)
    {
        Var new_bound_var = bound_var.rename(renameEnv);
        Iterator i = getVars().iterator();
        List new_vars = new LinkedList();

        while (i.hasNext())
            new_vars.add(((Var) i.next()).rename(renameEnv));

        return new Let(new_bound_var, new_vars, getImpl(),
                       joinPointPos(), joinPointStaticPartPos(),
                       enclosingJoinPointPos(), getPosition());
    }

    public Residue matchesAt(MatchingContext mc)
    {
        WeavingVar bound_wvar = mc.getWeavingEnv().getWeavingVar(bound_var);
        List/*<WeavingVar>*/ args = new LinkedList();

        Residue getvars = getWeavingVars(getVars(), args, mc);
        Residue let =
            LetResidue.construct(bound_wvar, getImpl().getSootMethod(), args);

        return AndResidue.construct(getvars, let);
    }

    public boolean unify(Pointcut otherpc, Unification unification)
    {
        return false;
    }
}
