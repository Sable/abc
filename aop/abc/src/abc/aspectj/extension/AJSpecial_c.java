
package abc.aspectj.extension;

import polyglot.util.Position;

import polyglot.ast.Special;
import polyglot.ast.TypeNode;
import polyglot.ast.Node;

import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;

import polyglot.visit.TypeChecker;
import polyglot.visit.AmbiguityRemover;

import abc.aspectj.ast.*;
import abc.aspectj.types.AJContext;
import abc.aspectj.types.AJTypeSystem;

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
		AJTypeSystem ts = (AJTypeSystem) ar.typeSystem();
		if (!(ts.refHostOfITD(c,qualifier()))) {		       
			// this is an ordinary special
			return super.disambiguate(ar);
		} else {
			// this is a host special
			AJNodeFactory nf = (AJNodeFactory) ar.nodeFactory();
			HostSpecial_c hs = (HostSpecial_c) nf.hostSpecial(position,kind,qualifier,((AJContext)c).hostClass());
			return hs.type(type()).disambiguate(ar);
		}
	}

	
}
