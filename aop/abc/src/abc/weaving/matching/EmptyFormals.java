/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
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

package abc.weaving.matching;

import polyglot.util.InternalCompilerError;
import abc.weaving.aspectinfo.*;
import abc.weaving.residues.*;

/** An empty weaving environment, for use in contexts where named pointcut
 *  variables aren't supported.
 *  @author Ganesh Sittampalam
 */

public class EmptyFormals implements WeavingEnv {
    public WeavingVar getWeavingVar(Var v) {
	throw new InternalCompilerError
	    ("Undefined variable "+v.getName()+" escaped frontend",v.getPosition());
    }

    public AbcType getAbcType(Var v) {
	throw new InternalCompilerError
	    ("Undefined variable "+v.getName()+" escaped frontend",v.getPosition());
    }
}
