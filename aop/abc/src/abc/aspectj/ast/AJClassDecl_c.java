/*
 * Created on Jun 10, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.aspectj.ast;

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

import polyglot.ext.jl.ast.ClassDecl_c;
import polyglot.types.Flags;
import polyglot.types.Context;
import polyglot.types.SemanticException;
import polyglot.types.ParsedClassType;
import polyglot.types.ClassType;
import polyglot.types.Type;

import polyglot.visit.AmbiguityRemover;
import polyglot.visit.TypeChecker;

import polyglot.util.Position;

import abc.aspectj.types.AspectJFlags;

/**
 * @author oege
 */

public class AJClassDecl_c extends ClassDecl_c {

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

	public Node typeCheck(TypeChecker tc) throws SemanticException {
		ClassDecl n = (ClassDecl) super.typeCheck(tc);
		if (superClass() != null &&
		     AspectJFlags.isAspectclass(n.type().toClass().superType().toClass().flags()) &&
		    !AspectJFlags.isAspectclass(n.type().toClass().flags()))
		   throw new SemanticException("A normal class cannot extend an aspect",superClass.position());
		return n;
	}
			
}
