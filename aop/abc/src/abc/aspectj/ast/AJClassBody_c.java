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

import abc.aspectj.types.InterTypeConstructorInstance_c;

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
					 throw new SemanticException("Duplicate constructor \"" + cj + "\".", cj.position());
				 }
			 }
		 }
	 }

	private boolean ITDok(ConstructorInstance ci, ConstructorInstance cj) {
		return (ci instanceof InterTypeConstructorInstance_c && !ci.flags().isPrivate() &&
		       !(cj instanceof InterTypeConstructorInstance_c) && cj.flags().isPrivate());
	}
	
	private boolean ITDoks(ConstructorInstance ci, ConstructorInstance cj) {
		return ITDok(ci,cj) || ITDok(cj,ci);
	}
}
