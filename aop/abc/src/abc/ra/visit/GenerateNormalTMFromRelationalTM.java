/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Reehan Shaikh
 * Copyright (C) 2007 Eric Bodden
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
package abc.ra.visit;

import java.util.Stack;

import polyglot.ast.Node;
import polyglot.types.TypeSystem;
import polyglot.visit.NodeVisitor;
import abc.ra.ast.RANodeFactory;
import abc.ra.ast.RelAspectDecl;
import abc.ra.ast.RelTMDecl;
import abc.ra.ast.TMFromRelTMDecl_c;
import abc.tm.ast.TMDecl;

/**
 * Visitor which rewrites a relational tracematch to a normal tracematch (of type {@link TMFromRelTMDecl_c}).
 *
 * @author Eric Bodden
 */
public class GenerateNormalTMFromRelationalTM extends NodeVisitor {
	
	protected final Stack<Node> declaringAspect;
	protected final RANodeFactory nf;
	protected final TypeSystem ts;
	
	public GenerateNormalTMFromRelationalTM(RANodeFactory nf, TypeSystem ts) {
		this.nf = nf;
		this.ts = ts;
		this.declaringAspect = new Stack<Node>();
	}

	public NodeVisitor enter(Node parent, Node n) {
		if(n instanceof RelAspectDecl) {
			//need handle to surrounding relational aspetct below, so push it on the stack
			declaringAspect.push(n);
		}
		return super.enter(parent, n);
	}
	
	public Node leave(Node old, Node n, NodeVisitor v) {
		if(n instanceof RelTMDecl) {
			//do the rewrite
			RelTMDecl relTMDecl = (RelTMDecl) n;
			RelAspectDecl container = (RelAspectDecl) declaringAspect.peek();
			TMDecl normalTM = relTMDecl.genNormalTraceMatch(container, nf, ts);
			String tmBodyMethodName = normalTM.name();
			//register tm body method for rewrite in the backend
			container.addTmBodyMethodName(tmBodyMethodName);
			n = normalTM;
		} else if(n instanceof RelAspectDecl) {
			//pop the relational aspect again
			declaringAspect.pop();
		}		
		return super.leave(old, n, v);
	}
	
	

}
