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

/*
 * Created on Oct 7, 2004
 *
 */
package abc.aspectj.extension;

import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.TypeNode;
import polyglot.ext.jl.ast.MethodDecl_c;
import polyglot.types.Flags;
import polyglot.util.Position;
import polyglot.visit.*;
import polyglot.types.*;

import abc.aspectj.visit.AJAmbiguityRemover;

/**
 * @author Oege de Moor
 *
 */
public class AJMethodDecl_c extends MethodDecl_c {

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @param arg6
	 */
	public AJMethodDecl_c(
		Position arg0,
		Flags arg1,
		TypeNode arg2,
		String arg3,
		List arg4,
		List arg5,
		Block arg6) {
		super(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
		// TODO Auto-generated constructor stub
	}
	
	 public NodeVisitor disambiguateEnter(AmbiguityRemover ar) throws SemanticException {
			if (ar.kind() == AmbiguityRemover.SUPER || ar instanceof AJAmbiguityRemover) {
				return ar.bypassChildren(this);
			}
			else if (ar.kind() == AmbiguityRemover.SIGNATURES) {
				if (body != null) {
					return ar.bypass(body);
				}
			}

			return ar;
		} 
}
