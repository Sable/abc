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
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.visit.NodeVisitor;
import soot.CompilationDeathException;
import abc.ra.ast.RANodeFactory;
import abc.ra.ast.RelAdviceDecl;
import abc.ra.ast.RelAspectDecl;
import abc.ra.ast.RelTMDecl;

/**
 * Generates a relational tracematch ({@link RelTMDecl}) from a relational advice declaration ({@link RelAdviceDecl}).
 *
 * @author Eric Bodden
 */
public class GenerateTracematch extends NodeVisitor {
	
	protected final Stack<Node> declaringAspect;
	private final RANodeFactory nf;
	private final TypeSystem ts;
	
	public GenerateTracematch(RANodeFactory nf, TypeSystem ts) {
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
		if(n instanceof RelAdviceDecl) {
			//do the rewrite
			RelAdviceDecl relAdviceDecl = (RelAdviceDecl) n;
			if(!declaringAspect.isEmpty()) {
				RelAspectDecl container = (RelAspectDecl) declaringAspect.peek();
				n = relAdviceDecl.genTraceMatch(container, nf, ts);
			}
		} else if(n instanceof RelAspectDecl) {
			//pop the relational aspect again
			declaringAspect.pop();
		}		
		return super.leave(old, n, v);
	}
	
	

}
