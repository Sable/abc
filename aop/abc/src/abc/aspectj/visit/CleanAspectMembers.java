/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
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

import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.types.ParsedClassType;
import polyglot.types.ReferenceType;
import polyglot.types.MethodInstance;
import polyglot.types.Type;
import polyglot.types.Flags;
import polyglot.types.TypeSystem;
import polyglot.types.LocalInstance;

import abc.aspectj.ast.*;

import abc.aspectj.types.InterTypeMethodInstance_c;


import java.util.*;

/**
 * This visitor cleans up the AST prior to Jimplification, turning it
 * into a Java tree. Advice declarations are rewritten to pure method
 * declarations. Intertype field delcarations, declare declarations and
 * pointcut declarations, as well as intertype constructors are completely 
 * stripped out of the tree.
 * For all intertype declarations, we remove the relevant types 
 * (which were earlier added to do type checking).
 * 
 * @author Aske Simon Christensen
 * @author Oege de Moor
 * 
 */

public class CleanAspectMembers extends NodeVisitor {
    private NodeFactory nf;
    private TypeSystem ts;

    public CleanAspectMembers(NodeFactory nf,TypeSystem ts) {
	this.nf = nf;
	this.ts = ts;
    }

    public Node leave(Node old, Node n, NodeVisitor v) {
	if (n instanceof AdviceDecl) {
	    AdviceDecl ad = (AdviceDecl)n;
	    // System.out.println("Cleaning out an advice declaration");
	    return nf.MethodDecl(ad.position(), ad.flags(), ad.returnType(), ad.name(),
				 ad.formals(), ad.throwTypes(), ad.body())
		.methodInstance(ad.methodInstance());
	}
	if (n instanceof ClassDecl) {
	    ClassDecl cd = (ClassDecl)n;
	    ParsedClassType pct = cd.type();
	    List members = cd.body().members();
	    List newmembers = new ArrayList();
	    Iterator mi = members.iterator();
	    while (mi.hasNext()) {
		ClassMember m = (ClassMember)mi.next();
		if (m instanceof AdviceDecl) {
		    throw new RuntimeException("Advice declaration not cleaned up");
		}
		if (m instanceof IntertypeFieldDecl ||
		    m instanceof DeclareDecl ||
		    m instanceof PointcutDecl ||
		    m instanceof IntertypeConstructorDecl) {
		    // System.out.println("Cleaning out a node of type "+m.getClass());
		    if (m instanceof IntertypeFieldDecl) {
		    	IntertypeFieldDecl itfd = (IntertypeFieldDecl) m;
		    	ParsedClassType hostType = (ParsedClassType)itfd.host().type();
		    	// hostType.fields().remove(hostType.fieldNamed(itfd.name()));
		    	pct.fields().remove(itfd.fieldInstance());
		    }
			if (m instanceof PointcutDecl) {
				PointcutDecl pcd = (PointcutDecl) m;
				pct.methods().remove(pcd.methodInstance());
			}
			if (m instanceof IntertypeConstructorDecl) {
				// System.out.println("Cleaning out intertype constructor" + m);
				IntertypeConstructorDecl itmd = (IntertypeConstructorDecl) m;
				ParsedClassType hostType = (ParsedClassType) itmd.host().type();
				// hostType.constructors().remove(itmd.constructorInstance());
				pct.constructors().remove(itmd.constructorInstance());
			}
		    // This must be removed
		} else {
			if (m instanceof IntertypeMethodDecl) {
				IntertypeMethodDecl_c itmd = (IntertypeMethodDecl_c) m;
				ParsedClassType hostType = (ParsedClassType) itmd.host().type();
			    // if (!(itmd.host().type().toClass().flags().isInterface()))
				//	hostType.methods().remove(itmd.itMethodInstance);
				if (!itmd.flags().isAbstract()) // || (itmd.host().type().toClass().flags().isInterface()))
					newmembers.add(itmd);
			} else
		    	newmembers.add(m);
			}
	    }
	 
	    return nf.ClassDecl(cd.position(), cd.flags(), cd.name(), cd.superClass(), cd.interfaces(),
				nf.ClassBody(cd.body().position(), newmembers))
		.type(pct);
	}
	return n;
    }

}
