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

package abc.eaj.extension;

import java.util.*;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.Position;

import abc.aspectj.ast.*;

import abc.eaj.visit.*;

/**
 * @author Julian Tibble
 */
public class EAJAdviceDecl_c extends AdviceDecl_c implements EAJAdviceDecl
{
    public EAJAdviceDecl_c(Position pos, Flags flags,
                           AdviceSpec spec, List throwTypes,
                           Pointcut pc, Block body)
    {
        super(pos, flags, spec, throwTypes, pc, body);
    }

    public EAJAdviceDecl conjoinPointcutWith(GlobalPointcuts visitor, Pointcut global)
    {
        EAJAdviceDecl_c n = (EAJAdviceDecl_c) this.copy();
        n.pc = visitor.conjoinPointcuts(pc, global);
        return n;
    }
}
