/*
 * Created on Jul 6, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.aspectj.extension;

import java.util.List;

import polyglot.ast.Block;
import polyglot.ext.jl.ast.ConstructorDecl_c;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;
import polyglot.ast.Node;

import abc.aspectj.ast.HostConstructorCall_c;
import abc.aspectj.types.AspectType;


/**
 * @author oege
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AJConstructorDecl_c extends ConstructorDecl_c {

	
	public AJConstructorDecl_c(
		Position pos,
		Flags flags,
		String name,
		List formals,
		List throwTypes,
		Block body) {
		super(pos, flags, name, formals, throwTypes, body);
		
	}

	public Node typeCheck(TypeChecker tc) throws SemanticException {
		Node n = super.typeCheck(tc);
		if ((constructorInstance().container() instanceof AspectType) &&
		    constructorInstance().formalTypes().size() > 0)
		    throw new SemanticException("Aspects can only have nullary constructors",position());
		return n;
	}
}
