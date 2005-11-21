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

package abc.tm.extension;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;

import abc.tm.types.*;

/**
 * @author Julian Tibble
 */
public class LocalDel_c extends abc.aspectj.extension.LocalDel_c
{
    public Node typeCheck(TypeChecker tc) throws SemanticException
    {
        TMContext tmc = (TMContext) tc.context();

        if (tmc.inIf())
        {
            Local m = (Local) node();

            if (tmc.isUnboundTMFormal(m.name()))
                throw new SemanticException("Local " + m.name() +
                    " is not bound by this symbol so it cannot be" +
                    " used within if(..)", node().position());
        }

        return super.typeCheck(tc);
    }
}
