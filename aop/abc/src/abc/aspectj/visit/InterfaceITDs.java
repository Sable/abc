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

package abc.aspectj.visit;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Set;
import java.util.HashSet;

import polyglot.ast.NodeFactory;
import polyglot.ast.Node;
import polyglot.ast.ClassDecl;
import polyglot.ast.TypeNode;

import polyglot.frontend.Pass;

import polyglot.types.TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.ClassType;
import polyglot.types.MethodInstance;
import polyglot.types.FieldInstance;
import polyglot.types.ConstructorInstance;

import polyglot.visit.NodeVisitor;
import polyglot.visit.ContextVisitor;

import abc.aspectj.ExtensionInfo;
import abc.aspectj.types.InterTypeMemberInstance;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.AbcClass;

/**
 * 
 * @author Oege de Moor
 *
 */
public class InterfaceITDs extends OncePass {


	public InterfaceITDs(Pass.ID id) {
	super(id);
	}
	
	public void once() {
		for (Iterator weavableClasses = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator();
		 	weavableClasses.hasNext(); ) {
		 		ClassType ctype = ((AbcClass) weavableClasses.next()).getPolyglotType();
				if (ctype.flags().isInterface())
					continue;
				// System.out.println("processing "+ctype);
				process(ctype);
		 	}
	}

	public static void process(ClassType ctype) {
		Stack interfaces = new Stack();
		Set visited = new HashSet();
		interfaces.addAll(ctype.interfaces());
		while(!(interfaces.isEmpty())) {
			ClassType interf = ((ClassType) interfaces.pop());
			if (visited.contains(interf))
						continue;
			visited.add(interf);
			// System.out.println("ctype="+ctype+" intrf="+interf);
					
			// does the super type of ctype also implement this interface?
			// if so, we'll add it to the super type instead
			if (ctype.superType() != null)
				if (ctype.superType().descendsFrom(interf)) 
					continue; 
			//	also add ITDS in the interfaces that interf extends
			interfaces.addAll(interf.interfaces()); 
			for (Iterator mit = interf.methods().iterator(); mit.hasNext(); ) {
				MethodInstance mi = (MethodInstance) mit.next();
				if (mi instanceof InterTypeMemberInstance) {
					abc.aspectj.ast.IntertypeMethodDecl_c.overrideITDmethod(ctype,
									   mi.container(ctype).flags(((InterTypeMemberInstance)mi).origFlags()));
		 	
				}
			}
			for (Iterator fit = interf.fields().iterator(); fit.hasNext(); ) {
				FieldInstance fi = (FieldInstance) fit.next();
				if (fi instanceof InterTypeMemberInstance) {
					abc.aspectj.ast.IntertypeFieldDecl_c.overrideITDField(ctype,fi);
				}	
			}
			for (Iterator cit = interf.constructors().iterator(); cit.hasNext(); ) {
				ConstructorInstance ci = (ConstructorInstance) cit.next();
				if (ci instanceof InterTypeMemberInstance) {
					abc.aspectj.ast.IntertypeConstructorDecl_c.overrideITDconstructor(ctype,
									 ci.container(ctype).flags(((InterTypeMemberInstance)ci).origFlags()));
				}
			}
		}
	}



}
