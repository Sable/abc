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
 * check dependencies between named pointcuts, and also abstract flags
 */
 
package abc.aspectj.visit;

import polyglot.ast.NodeFactory;
import polyglot.ast.Node;

import polyglot.frontend.Job;
import polyglot.types.TypeSystem;
import polyglot.types.SemanticException;
import polyglot.visit.ContextVisitor;

/**
 * check dependencies between named pointcuts, and also abstract flags.
 * local dependencies are set by typechecker; this phase builds the global graph.
 * @author Oege de Moor
 */
public class DependsChecker extends ContextVisitor {

	
	public DependsChecker(Job job, TypeSystem ts, NodeFactory nf) {
		super(job, ts, nf);
	}
	
	protected Node leaveCall(Node n) throws SemanticException {
		if (n instanceof DependsCheck) {
			return (((DependsCheck)n).checkDepends(this));
		}
		return n;
	}


}
