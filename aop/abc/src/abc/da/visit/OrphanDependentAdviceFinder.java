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

import java.util.Set;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import abc.da.ast.AdviceName;
import abc.da.ast.DAAspectDecl;
import abc.da.types.DAAspectType;

public class OrphanDependentAdviceFinder extends ContextVisitor {

	public OrphanDependentAdviceFinder(Job job, TypeSystem ts, NodeFactory nf) {
		super(job, ts, nf);
	}
	
	@Override
	protected NodeVisitor enterCall(Node n) throws SemanticException {
		if(n instanceof DAAspectDecl) {
			DAAspectDecl aspectDecl = (DAAspectDecl) n;
			DAAspectType aspect = (DAAspectType) aspectDecl.type();
			Set<String> referencedNames = aspect.getAllReferencedAdviceNames();
			Set<AdviceName> allAdviceNames = aspect.getAdviceNameToFormals().keySet();
			for (AdviceName adviceName : allAdviceNames) {
				if(!referencedNames.contains(adviceName.getName())) {
					throw new SemanticException("Advice "+aspect.fullName()+"."+adviceName.getName()+" is " +
							"never referenced in any dependency declaration.",adviceName.position());
				}
			}			
		}	
		return super.enterCall(n);
	}

}
