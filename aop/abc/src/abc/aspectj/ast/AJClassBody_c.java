/*
 * Created on May 26, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.aspectj.ast;

import java.util.List;
import java.util.ArrayList;

import polyglot.types.SemanticException;
import polyglot.types.ClassType;
import polyglot.types.ConstructorInstance;

import polyglot.visit.TypeChecker;


import polyglot.ext.jl.ast.ClassBody_c;
import polyglot.util.Position;

import polyglot.types.TypeSystem;
import polyglot.types.MemberInstance;
import polyglot.types.MethodInstance;
import polyglot.types.FieldInstance;

import abc.aspectj.types.InterTypeConstructorInstance_c;
import abc.aspectj.types.InterTypeMemberInstance;

/**
 * @author oege
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AJClassBody_c extends ClassBody_c {

	
	public AJClassBody_c(Position pos, List members) {
		super(pos, members);
	}
	
	protected void duplicateConstructorCheck(TypeChecker tc) throws SemanticException {
		 ClassType type = tc.context().currentClass();

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
	
	private boolean ITDoks(MemberInstance ci, MemberInstance cj) {
			return ITDok(ci,cj) || ITDok(cj,ci);
	}
	
	private boolean ITDok(MemberInstance ci, MemberInstance cj) {
		return // ok to zap private members with a non-private ITD
		            ((ci instanceof InterTypeMemberInstance && !ci.flags().isPrivate() &&
		            !(cj instanceof InterTypeMemberInstance) && 
		            cj.flags().isPrivate())) ||
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
		  ClassType type = tc.context().currentClass();

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
		ClassType type = tc.context().currentClass();
		TypeSystem ts = tc.typeSystem();

		ArrayList l = new ArrayList(type.methods());

		for (int i = 0; i < l.size(); i++) {
			MethodInstance mi = (MethodInstance) l.get(i);

			for (int j = i+1; j < l.size(); j++) {
				MethodInstance mj = (MethodInstance) l.get(j);

				if (isSameMethod(ts, mi, mj) && !ITDoks(mi,mj)) {
					if (mi instanceof InterTypeMemberInstance)
						throw new SemanticException("Duplicate method \"" + mi + "\".", mi.position());
					else
						throw new SemanticException("Duplicate method \"" + mj + "\".", mj.position());
				}
			}
		}
	}
	
	
}
