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

package abc.eaj.weaving.residues;

import abc.soot.util.*;
import abc.weaving.residues.*;
import abc.weaving.weaver.*;

import soot.*;
import soot.jimple.*;
import soot.util.*;

import java.util.*;

/**
 * The dynamic residue of a let(...) pointcut
 * @author Julian Tibble
 */
public class LetResidue extends Residue
{
    private WeavingVar bound_var;
    private SootMethod impl;
    private List/*<WeavingVar>*/ args;

    private LetResidue(WeavingVar bound_var, SootMethod impl, List args)
    {
        this.bound_var = bound_var;
        this.impl = impl;
        this.args = args;
    }

    public Residue optimize()
    {
        return this;
    }

    public Residue inline(ConstructorInliningMap cim)
    {
        WeavingVar inlined_bound_var = bound_var.inline(cim);
        List inlined_args = WeavingVar.inline(args, cim);

        return construct(inlined_bound_var, impl, inlined_args);
    }

    public Residue resetForReweaving()
    {
        bound_var.resetForReweaving();
        Iterator i = args.iterator();

        while (i.hasNext()) {
            WeavingVar arg = (WeavingVar) i.next();
            arg.resetForReweaving();
        }

        return this;
    }

    public static LetResidue construct(WeavingVar bound_var,
                                       SootMethod impl,
                                       List args)
    {
        // FIXME: not sure this does is correct for 
        //        bound_var --- I've ignored boxing
        //        stuff because I don't know what it
        //        should do.

        return new LetResidue(bound_var, impl, args);
    }

    public String toString()
    {
        return "bind(" + bound_var + ",...)";
    }

    public Stmt codeGen(SootMethod method, LocalGeneratorEx localgen,
                        Chain units, Stmt begin, Stmt fail,
                        boolean sense, WeavingContext wc)
    {
        List actuals = new Vector(args.size());
        Iterator i = args.iterator();

        // FIXME, I probably need it
        while (i.hasNext()) {
            WeavingVar wv = (WeavingVar) i.next();
            actuals.add(wv.get());
        }

        InvokeExpr letcall =
            Jimple.v().newStaticInvokeExpr(impl.makeRef(), actuals);

        return bound_var.set(localgen, units, begin, wc, letcall);
    }
}
