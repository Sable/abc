
package abc.aspectj.ast;

import polyglot.util.Position;

import polyglot.ast.Special;
import polyglot.ast.TypeNode;
import polyglot.ast.Node;

import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;

import polyglot.visit.TypeChecker;
import polyglot.visit.AmbiguityRemover;

import abc.aspectj.types.AJContext;

import polyglot.ext.jl.ast.Special_c;

/**
 * @author Oege de Moor
 *
 * disambiguate this and super in intertype method declarations
 */
public class AJSpecial_c extends Special_c implements Special {
	
	public AJSpecial_c(Position pos, Special.Kind kind, TypeNode qualifier) {
	   super(pos,kind,qualifier);
	}
	
	/** Disambiguate the expression. */
	public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
		AJContext c = (AJContext) ar.context();
		if (!c.inInterType() 
		    || (c.nested() && qualifier() == null)
		    || (c.inInterType() && qualifier() != null && 
		    	c.currentClass().hasEnclosingInstance(qualifier.type().toClass()))) {		       
			// this is an ordinary special
			System.out.println("ordinary");
			return this;
		} else {
			// this is a host special
			AspectJNodeFactory nf = (AspectJNodeFactory) ar.nodeFactory();
			HostSpecial_c hs = (HostSpecial_c) nf.hostSpecial(position,kind,qualifier);
			return hs.type(type());
		}
	}

	
}
