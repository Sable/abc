/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
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
package abc.da.visit;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import abc.da.ast.AdviceDependency;

/**
 * This checks that the parameters in {@link AdviceDependency}s are used
 * consistently, i.e. that parameters with the same variable name actually
 * have a common ancestor type.
 * 
 * @author Eric Bodden
 */
public class TypeCheckAdviceParams extends ContextVisitor {

	public TypeCheckAdviceParams(Job job, TypeSystem ts, NodeFactory nf) {
		super(job, ts, nf);
	}

	@Override
	protected NodeVisitor enterCall(Node n) throws SemanticException {
		if(n instanceof AdviceDependency) {
			AdviceDependency adviceDependency = (AdviceDependency) n;
			n = adviceDependency.typeCheckAdviceParams(this);
		}
		return super.enterCall(n);
	}

}
