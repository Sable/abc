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
import polyglot.types.TypeSystem;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.ast.AdviceSpec;
import abc.da.HasDAInfo;
import abc.da.ast.AdviceName;
import abc.da.ast.DAAdviceDecl;
import abc.da.ast.NameExtension;
import abc.da.types.DAContext;
import abc.da.weaving.aspectinfo.DAInfo;
import abc.main.Main;

/**
 * This visitor registers an aspect info for every dependent advice declaration.
 * @author Eric Bodden
 */
public class DAAspectInfoHarvester extends ContextVisitor {

	public DAAspectInfoHarvester(Job job, TypeSystem ts, NodeFactory nf) {
		super(job, ts, nf);
	}
	
	@Override
	public NodeVisitor enter(Node parent, Node n) {
		ContextVisitor enter = (ContextVisitor) super.enter(parent, n);
		if(n instanceof AdviceDecl) {
			AdviceDecl ad = (AdviceDecl) n;
			enter = context(((DAContext)enter.context()).pushAdviceDecl(ad));			
		}
		if(n instanceof AdviceSpec) {
			AdviceSpec adviceSpec = (AdviceSpec) n;			
			DAContext context = (DAContext) context();			
			AdviceDecl adviceDecl = context.currentAdviceDecl();
			if(adviceDecl.flags().intersects(DAAdviceDecl.DEPENDENT)) {
				NameExtension ext = (NameExtension) adviceSpec.ext();
				AdviceName adviceName = ext.getName();

				DAInfo dai = ((HasDAInfo) Main.v().getAbcExtension()).getDependentAdviceInfo();
				
				String qualifiedLowLevelAdviceName = context.currentAspect().fullName() + "." + adviceDecl.name();
				String qualifiedUserGivenAdviceName = context.currentAspect().fullName() + "." + adviceName.getName();
				
				dai.registerDependentAdvice(qualifiedLowLevelAdviceName,qualifiedUserGivenAdviceName);				
			}
		}
		return enter;
	}

	
}
