/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Pavel Avgustinov
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

package abc.aspectj.extension;

import polyglot.ast.*;
import polyglot.ext.jl.ast.*;

import polyglot.util.InternalCompilerError;

import abc.aspectj.ast.*;
import abc.aspectj.types.*;
import abc.aspectj.visit.*;
import polyglot.types.*;
import polyglot.visit.*;

/**
 * @author Pavel Avgustinov
 * @author Oege de Moor
 *
 */
public class AssignDel_c extends JL_c
{
	
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		AJContext ajc = (AJContext) tc.context();
		Assign a = (Assign) node();
		if (ajc.inIf() && (a.left() instanceof Local))
			throw new SemanticException("Cannot assign to a local within a pointcut.", node().position());
		return node().typeCheck(tc);
	}
}
