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
 * Created on Jul 13, 2004
 *
 */
package abc.aspectj.visit;

import polyglot.visit.NodeVisitor;
import polyglot.ast.Node;
import polyglot.ast.ClassDecl;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.AbcFactory;

/**
 * @author Oege de Moor
 *
 */
public class SourceClasses extends NodeVisitor {

	/**
	 * 
	 */
	public SourceClasses() {
		super();
		// TODO Auto-generated constructor stub
	}
	public NodeVisitor enter(Node n) {
		if (n instanceof ClassDecl) {
			ClassDecl cd = (ClassDecl) n;
			abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().registerSourceClass(AbcFactory.AbcClass(cd.type()));
		}
		return this;
	}

}
