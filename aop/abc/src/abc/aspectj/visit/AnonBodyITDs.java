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
 * Created on 16-Aug-2004
 */
package abc.aspectj.visit;

import polyglot.ast.NodeFactory;
import polyglot.ast.Node;
import polyglot.frontend.Job;
import polyglot.types.TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.ClassType;
import polyglot.visit.NodeVisitor;
import polyglot.visit.ContextVisitor;

import abc.aspectj.types.AJContext;

/**
 * @author Oege de Moor
 */
public class AnonBodyITDs extends ContextVisitor {


	public AnonBodyITDs(Job job, TypeSystem ts, NodeFactory nf) {
		super(job, ts, nf);
		// TODO Auto-generated constructor stub
	}

	protected NodeVisitor enterCall(Node n) throws SemanticException {
		// we only inspect the topmost node
		return this.bypassChildren(n);
	}
	   
	protected Node leaveCall(Node n) throws SemanticException {
		// if that node is the body of an anonymous class, we 
		// add interface itds as needed
		AJContext ajc = (AJContext) context();
		ClassType anonType = ajc.currentClass();
	    if (anonType !=null && anonType.isAnonymous()) {
	   		 InterfaceITDs.process(anonType);
	   }
	   return n;
	   }
}
