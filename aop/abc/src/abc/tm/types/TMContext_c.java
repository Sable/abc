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
import abc.aspectj.types.*;

import java.util.Collection;

/**
 * @author Julian Tibble
 */
public class TMContext_c extends AJContext_c
                         implements TMContext
{
    protected boolean inSymbol;
    protected Collection symbolMustBind;

    public TMContext_c(TypeSystem ts)
    {
        super(ts);
        inSymbol = false;
        symbolMustBind = null;
    }

    public TMContext pushSymbol(Collection mustBind)
    {
        TMContext_c c = (TMContext_c) push();

        c.inSymbol = true;
        c.symbolMustBind = mustBind;

        return c;
    }

    public boolean inSymbol()
    {
        return inSymbol;
    }
	
    public Collection getSymbolMustBind()
    {
        return symbolMustBind;
    }
}
