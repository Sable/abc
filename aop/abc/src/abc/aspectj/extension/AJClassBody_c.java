/* abc - The AspectBench Compiler
 * Copyright (C) 2004 oege
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
 * Created on May 26, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.aspectj.extension;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import polyglot.types.SemanticException;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;

import polyglot.visit.TypeChecker;
import polyglot.visit.AmbiguityRemover;


import polyglot.ast.Node;
import polyglot.ast.ClassDecl;
import polyglot.ast.ClassMember;

import polyglot.ext.jl.ast.ClassBody_c;
import polyglot.util.Position;

import polyglot.types.TypeSystem;
import polyglot.types.MemberInstance;
import polyglot.types.MethodInstance;
import polyglot.types.FieldInstance;
import polyglot.types.Context;

import polyglot.frontend.Job;
import polyglot.frontend.Pass;

import abc.aspectj.types.InterTypeConstructorInstance_c;
import abc.aspectj.types.InterTypeMethodInstance;
import abc.aspectj.types.InterTypeMethodInstance_c;
import abc.aspectj.types.InterTypeFieldInstance_c;
import abc.aspectj.types.InterTypeMemberInstance;
import abc.aspectj.types.AJTypeSystem_c;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.PointcutInstance_c;
import abc.aspectj.types.AJContext;

import abc.aspectj.visit.AspectMethods;
import abc.aspectj.visit.AJAmbiguityRemover;

import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.IntertypeMethodDecl_c;
import abc.aspectj.ast.MakesAspectMethods;

/**
 * @author Oege de Moor
 *
 */
public class AJClassBody_c extends ClassBody_c implements MakesAspectMethods {

	
	public AJClassBody_c(Position pos, List members) {
		super(pos, members);
	}
	
	
	
	protected void duplicateConstructorCheck(TypeChecker tc) throws SemanticException {
		duplicateConstructorCheck(tc.context().currentClass());
	}
	
	static void duplicateConstructorCheck(ClassType type) throws SemanticException {

		 ArrayList l = new ArrayList(type.constructors());

		 for (int i = 0; i < l.size(); i++) {
			 ConstructorInstance ci = (ConstructorInstance) l.get(i);

			 for (int j = i+1; j < l.size(); j++) {
				 ConstructorInstance cj = (ConstructorInstance) l.get(j);

				 if (ci.hasFormals(cj.formalTypes()) && !ITDoks(ci,cj)) {
				 	if (ci instanceof InterTypeMemberInstance)
				 		throw new SemanticException("Duplicate constructor \"" + ci + "\".", ci.position());
				 	else
					 throw new SemanticException("Duplicate constructor \"" + cj + "\".", cj.position());
				 }
			 }
		 }
	 }
	
	private static boolean ITDoks(MemberInstance ci, MemberInstance cj) {
			return ITDok(ci,cj) || ITDok(cj,ci);
	}
	
	private static boolean ITDok(MemberInstance ci, MemberInstance cj) {
		AJTypeSystem_c ts = (AJTypeSystem_c) ci.typeSystem();
		return // a private ITD cannot conflict with anything that's already there
		            ((ci instanceof InterTypeMemberInstance && ci.flags().isPrivate()) &&
		              !(cj instanceof InterTypeMemberInstance) ) ||
				// ok to zap private members with a non-private ITD
				// this has changed in ajc
		          //  ((ci instanceof InterTypeMemberInstance && !ci.flags().isPrivate() &&
		          //  !(cj instanceof InterTypeMemberInstance) && 
		          //  cj.flags().isPrivate())) ||
		        // ok to have two ITDs that cannot see each other 
		          ((ci instanceof InterTypeMemberInstance && 
		            cj instanceof InterTypeMemberInstance && 
		             ! ts.isAccessible(ci,((InterTypeMemberInstance) cj).origin()) &&
		             ! ts.isAccessible(cj,((InterTypeMemberInstance) ci).origin()))) ||
		       // also ok to have a duplicate in an interface
		            (ci instanceof InterTypeMemberInstance && 
		             !(cj instanceof InterTypeMemberInstance) &&
		             ci.container().toClass().flags().isInterface()) ||
		       // and subaspects override their super-aspect
		            (ci instanceof InterTypeMemberInstance &&
		             cj instanceof InterTypeMemberInstance &&
		             ((InterTypeMemberInstance) ci).origin().descendsFrom(((InterTypeMemberInstance) cj).origin()));
	}
	
	protected void duplicateFieldCheck(TypeChecker tc) throws SemanticException {
		duplicateFieldCheck(tc.context().currentClass());
	}
	
	static void duplicateFieldCheck(ClassType type) throws SemanticException {
		  ArrayList l = new ArrayList(type.fields());

		  for (int i = 0; i < l.size(); i++) {
			  FieldInstance fi = (FieldInstance) l.get(i);

			  for (int j = i+1; j < l.size(); j++) {
				  FieldInstance fj = (FieldInstance) l.get(j);

				  if (fi.name().equals(fj.name()) && !ITDoks(fi,fj)) {
				  	if (fi instanceof InterTypeMemberInstance)
				  		throw new SemanticException("Duplicate field \"" + fi + "\".", fi.position());
				  	else
					  throw new SemanticException("Duplicate field \"" + fj + "\".", fj.position());
				  }
			  }
		  }
	  }
	  

	protected void duplicateMethodCheck(TypeChecker tc) throws SemanticException {
		duplicateMethodCheck(tc.context().currentClass());
	}
	
	static void duplicateMethodCheck(ClassType type) throws SemanticException {

		ArrayList l = new ArrayList(type.methods());

		for (int i = 0; i < l.size(); i++) {
			MethodInstance mi = (MethodInstance) l.get(i);

			for (int j = i+1; j < l.size(); j++) {
				MethodInstance mj = (MethodInstance) l.get(j);

				if (mi.isSameMethod(mj) && !ITDoks(mi,mj)) {
					if (mi instanceof InterTypeMemberInstance) {
						InterTypeMethodInstance itmi = (InterTypeMethodInstance) mi;
						if (mj instanceof InterTypeMemberInstance) {
							InterTypeMethodInstance itmj = (InterTypeMethodInstance) mj;
						    throw new SemanticException("Duplicate method \"" + mi.signature() + "\" introduced by " +
						                "aspects \""+itmi.origin() + "\" and \"" + itmj.origin() + 
                                        "\" into class \"" + mi.container() + "\".", mi.position());
						}
						throw new SemanticException("Duplicate method \"" + mi.signature() + "\" introduced by " +
						                "aspect \""+itmi.origin() +"\" into class \""+
						                mi.container() + "\", which already contains that method.", 
						                mi.position());
					}
					if (mj instanceof InterTypeMemberInstance) {
						InterTypeMethodInstance itmj = (InterTypeMethodInstance) mj;
						throw new SemanticException("Duplicate method \"" + mj.signature() + "\" introduced by " +
										"aspect \""+itmj.origin() +"\" into class \""+
										mj.container() + "\", which already contains that method.", 
										mj.position());
					}
					else if (mi instanceof PointcutInstance_c)
					    throw new SemanticException("Duplicate "+mj+
                                   " in class \""+mj.container() +"\".",mj.position());
					else throw new SemanticException("Duplicate method \"" + mj.signature() + "\" in class \"" +
						                            mi.container() + "\".", mj.position());
				}
			}
		}
	}
	
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		Node n = super.typeCheck(tc);
		IntertypeMethodDecl_c.intertypeMethodChecks(tc.context().currentClass());
		return n;
	}
	
	public static void checkDuplicates(ClassType ct) throws SemanticException{
		duplicateConstructorCheck(ct);
		duplicateFieldCheck(ct);
		duplicateMethodCheck(ct);
	}
	
	public void aspectMethodsEnter(AspectMethods visitor)
	{
		AJContext c = (AJContext) visitor.context();
		ClassType ct = c.currentClassScope();
		if (c.inAdvice()) {
			  for (Iterator mets = ct.methods().iterator(); mets.hasNext(); ) {
				  MethodInstance mi = (MethodInstance) mets.next();
				  visitor.advice().localMethod(mi);
			  }
			  for (Iterator cons = ct.constructors().iterator(); cons.hasNext(); ) {
				  ConstructorInstance ci = (ConstructorInstance) cons.next();
				  visitor.advice().localMethod(ci);
			  }
		  }
	}

	
	public Node aspectMethodsLeave(AspectMethods visitor,
										  AJNodeFactory nf,
										  AJTypeSystem ts)
   {              
		   return this;
   }
}
