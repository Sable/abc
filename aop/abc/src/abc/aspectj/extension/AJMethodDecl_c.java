/*
 * Created on Oct 7, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.aspectj.extension;

import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.TypeNode;
import polyglot.ext.jl.ast.MethodDecl_c;
import polyglot.types.Flags;
import polyglot.util.Position;
import polyglot.visit.*;
import polyglot.types.*;

import abc.aspectj.visit.AJAmbiguityRemover;

/**
 * @author oege
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AJMethodDecl_c extends MethodDecl_c {

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @param arg6
	 */
	public AJMethodDecl_c(
		Position arg0,
		Flags arg1,
		TypeNode arg2,
		String arg3,
		List arg4,
		List arg5,
		Block arg6) {
		super(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
		// TODO Auto-generated constructor stub
	}
	
	 public NodeVisitor disambiguateEnter(AmbiguityRemover ar) throws SemanticException {
			if (ar.kind() == AmbiguityRemover.SUPER || ar instanceof AJAmbiguityRemover) {
				return ar.bypassChildren(this);
			}
			else if (ar.kind() == AmbiguityRemover.SIGNATURES) {
				if (body != null) {
					return ar.bypass(body);
				}
			}

			return ar;
		} 
}
