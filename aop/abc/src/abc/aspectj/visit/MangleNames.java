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
 * Created on Jun 9, 2004
 */
package abc.aspectj.visit;

import java.util.Iterator;

import polyglot.ast.ClassDecl;
import polyglot.ast.Node;

import polyglot.types.MethodInstance;
import polyglot.types.TypeSystem;

import polyglot.visit.NodeVisitor;

import abc.aspectj.types.InterTypeMemberInstance;
import abc.aspectj.types.AJTypeSystem;

/**
 * @author Oege de Moor
 */
public class MangleNames extends NodeVisitor {

	/**
	 * 
	 */
	public MangleNames() {
		super();
	}

	public NodeVisitor enter(Node n) {
		if (n instanceof ClassDecl) {
			ClassDecl cd = (ClassDecl) n;
			for (Iterator miIt = cd.type().methods().iterator(); miIt.hasNext(); ) {
				MethodInstance mi = (MethodInstance) miIt.next();
				if (mi instanceof InterTypeMemberInstance)
					((InterTypeMemberInstance) mi).setMangle();
			}
		}
		return this;
	}
}
