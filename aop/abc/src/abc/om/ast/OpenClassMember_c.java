/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Neil Ongkingco
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

package abc.om.ast;

import polyglot.ext.jl.ast.Node_c;
import polyglot.util.Position;
import abc.aspectj.ast.*;

public class OpenClassMember_c extends Node_c implements OpenClassMember {
	
	OpenClassFlags flags;
	ClassnamePatternExpr cpe;
	
	public OpenClassMember_c(OpenClassFlags flags, 
			ClassnamePatternExpr cpe, 
			Position pos) {
		super(pos);
		this.flags = flags;
		this.cpe = cpe;
	}
	
}
