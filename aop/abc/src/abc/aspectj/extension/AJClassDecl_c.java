/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 * Copyright (C) 2004 oege
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
 * Created on Jun 10, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.aspectj.extension;

import java.util.List;
import java.util.Stack;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;

import polyglot.frontend.Job;

import polyglot.ast.ClassBody;
import polyglot.ast.TypeNode;
import polyglot.ast.Node;
import polyglot.ast.ClassDecl;
import polyglot.ast.MethodDecl;

import polyglot.ext.jl.ast.ClassDecl_c;
import polyglot.types.Flags;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.ParsedClassType;
import polyglot.types.ClassType;
import polyglot.types.Type;
import polyglot.types.MethodInstance;
import polyglot.types.ConstructorInstance;

import polyglot.visit.AmbiguityRemover;
import polyglot.visit.TypeChecker;

import polyglot.util.Position;

import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.MakesAspectMethods;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.AspectType;
import abc.aspectj.visit.AspectMethods;
import abc.aspectj.visit.AJAmbiguityRemover;

/**
 * @author oege
 */

public class AJClassDecl_c extends ClassDecl_c
                           implements MakesAspectMethods
{

	protected boolean superDisambiguated = false;
	protected boolean hierarchyBuilt = false;
	
	/**
	 * @param pos
	 * @param flags
	 * @param name
	 * @param superClass
	 * @param interfaces
	 * @param body
	 */
	public AJClassDecl_c(
		Position pos,
		Flags flags,
		String name,
		TypeNode superClass,
		List interfaces,
		ClassBody body) {
		super(pos, flags, name, superClass, interfaces, body);
	}
	
	public boolean hierarchyBuilt() {
		return hierarchyBuilt;
	}
	
	public void setHierarchyBuilt() {
		hierarchyBuilt=true;
	}
	
	public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
			if (ar.kind() == AmbiguityRemover.SIGNATURES) {
				// make sure that the inStaticContext flag of the class is 
				// correct
				Context ctxt = ar.context();
				this.type().inStaticContext(ctxt.inStaticContext()); 
				addSuperDependencies(this.type(),ar.job());
				return this;
			}
			return super.disambiguate(ar);
	} 
	
	

	public void addSuperDependencies(ClassType ct,Job job) throws SemanticException {        
			Stack s = new Stack();
			Stack w = new Stack();
			Stack ctl = new Stack();
			ctl.add(ct);
			s.push(ctl); w.push(null);
			while (! s.isEmpty()) {	
				
				Stack l = (Stack) s.pop();
				if (l.isEmpty()) {
					w.pop();
				} else {
				
				Type t = (Type) l.pop();
				s.push(l);
				if (w.contains(t))
					throw new SemanticException("Type " + t + " cannot circularly implement or extend itself.",position());
				
				if (t.isClass()) {
					ClassType classt = t.toClass();
					// add a dependency if its a parsed class type.
					if (classt instanceof ParsedClassType) {
						job.extensionInfo().addDependencyToCurrentJob(
										  ((ParsedClassType)classt).fromSource());
					}
                
					// add all the interfaces to the stack.
					Stack newelems = new Stack();
					newelems.addAll(classt.interfaces());
    
					// add the superType to the stack.
					if (classt.superType() != null) {
						Type st = classt.superType();
						newelems.add(st);
					}
					s.push(newelems); w.push(t);
				}
				}
				
			}

		}
	
	protected void disambiguateSuperType(AmbiguityRemover ar) throws SemanticException {
      	if (superDisambiguated)
      		return;
      	superDisambiguated = true;
      	super.disambiguateSuperType(ar);
	}

	public Node typeCheck(TypeChecker tc) throws SemanticException {
		ClassDecl n = (ClassDecl) super.typeCheck(tc);
		if (superClass() != null &&
		     (n.type().toClass().superType() instanceof AspectType) &&
		    !(n.type() instanceof AspectType))
		   throw new SemanticException("A normal class cannot extend an aspect",superClass.position());
		return n;
	}

        public void aspectMethodsEnter(AspectMethods visitor)
        {
                visitor.pushClass();
                visitor.pushContainer(type());
        }

        public Node aspectMethodsLeave(AspectMethods visitor,
                                       AJNodeFactory nf,
                                       AJTypeSystem ts)
        {
                                               
                ClassDecl cd = this;
                List localMethods = visitor.methods();
                visitor.popClass();
                visitor.popContainer();
                

                for (Iterator i = localMethods.iterator(); i.hasNext(); ) {
                        MethodDecl md = (MethodDecl) i.next();
                        cd = this.body(cd.body().addMember(md));
                }

                return cd;
        }
}
