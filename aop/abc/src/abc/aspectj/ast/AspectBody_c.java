/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
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

package abc.aspectj.ast;

import java.util.List;

import abc.aspectj.ast.AspectBody;
import abc.aspectj.extension.AJClassBody_c;
import polyglot.ast.ClassBody;
import polyglot.ext.jl.ast.ClassBody_c;
import polyglot.util.Position;

/**
 * An <code>AspectBody</code> represents the body of an aspect
 * declaration 
 * 
 * @author Oege de Moor
 */
public class AspectBody_c extends AJClassBody_c implements AspectBody
{

    public AspectBody_c(Position pos, List members) {
        super(pos,members);
    }
     
}
