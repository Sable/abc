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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.Ext;
import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.TypeSystem;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.ast.AdviceSpec;
import abc.da.ast.AdviceName;
import abc.da.ast.AdviceNameAndParams;
import abc.da.ast.DAAspectDecl;
import abc.da.ast.NameExtension;
import abc.da.ast.DAAdviceDecl;
import abc.da.types.DAAspectType;
import abc.da.types.DAContext;

/**
 * This visitor records the names and {@link Formal}s of all
 * named advice and then sets them on the surrounding aspect
 * type ({@link DAAspectType}).
 * 
 * Depending on when this visitor is executed, the {@link Formal}s either
 * do have disambiguated types or not.
 * 
 * @author Eric Bodden
 */
public class AdviceNames extends ContextVisitor {

	public AdviceNames(Job job, TypeSystem ts, NodeFactory nf) {
		super(job, ts, nf);
	}

	@Override
	public NodeVisitor enter(Node parent, Node n) {
		ContextVisitor enter = (ContextVisitor) super.enter(parent, n);
		if(n instanceof AdviceDecl) {
			AdviceDecl ad = (AdviceDecl) n;
			enter = context(((DAContext)enter.context()).pushAdviceDecl(ad));			
		}
		return enter;
	}
		
	@Override
	public Node leave(Node parent, Node old, Node n, NodeVisitor v) {
		if(n instanceof AdviceSpec) {
			AdviceSpec adviceSpec = (AdviceSpec) n;
			Ext ext = adviceSpec.ext();
			if(ext instanceof NameExtension) {
				NameExtension nameExt = (NameExtension) ext;
				DAContext context = (DAContext) context();
				AdviceName adviceName = nameExt.getName(); 
				if(adviceName!=null) {
					List<Formal> allFormals = new LinkedList();
					AdviceDecl currentAdviceDecl = context.currentAdviceDecl();
					if(currentAdviceDecl.flags().intersects(DAAdviceDecl.DEPENDENT)) {
						allFormals.addAll(currentAdviceDecl.formals());
						if(currentAdviceDecl.getReturnThrowsFormal()!=null) {
							allFormals.add(currentAdviceDecl.getReturnThrowsFormal());
						}
						context.addAdviceNameAndFormals(adviceName, allFormals);
					}
				}
			}
		}
		if(n instanceof DAAspectDecl) {
			DAAspectDecl aspectDecl = (DAAspectDecl) n;
			DAAspectType type = (DAAspectType) aspectDecl.type();
			DAContext context = (DAContext) context();
			type.setAdviceNameToFormals(context.currentAdviceNameToFormals());
		}
		if(n instanceof AdviceNameAndParams) {
			AdviceNameAndParams adviceNameAndParams = (AdviceNameAndParams) n;
			DAContext context = (DAContext) context();
			DAAspectType type = (DAAspectType) context.currentAspect();
			type.addReferencedAdviceNames(Collections.singleton(adviceNameAndParams.getName()));
		}
		return super.leave(parent, old, n, v);
	}
	
	

}
