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

package abc.tm.types;

import polyglot.types.*;
import polyglot.ext.jl.types.*;
import abc.aspectj.types.*;

import java.util.*;

/**
 * @author Julian Tibble
 */
public class TMContext_c extends AJContext_c
                         implements TMContext
{
    // the set of all locals that can be used, in this
    // scope (if we are in a symbol), either because the
    // current symbol binds them, or because they were
    // added to the scope inside the symbol
    protected Collection canUse;

    public TMContext_c(TypeSystem ts)
    {
        super(ts);
        canUse = null;
    }

    protected Context_c push()
    {
        TMContext_c tmc = (TMContext_c) super.push();
        if (canUse != null)
            tmc.canUse = new HashSet(canUse);
        return tmc;
    }

    public TMContext pushSymbol(Collection mustBind)
    {
        TMContext_c c = (TMContext_c) push();

        c.canUse = mustBind;

        return c;
    }

    public boolean isUnboundTMFormal(String name)
    {
        return (canUse != null) && !canUse.contains(name);
    }

    public void addVariableToThisScope(VarInstance var)
    {
        super.addVariableToThisScope(var);
        if (canUse != null)
            canUse.add(var.name());
    }
}
