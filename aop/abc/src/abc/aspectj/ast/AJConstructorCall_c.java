
package abc.aspectj.ast;

import java.util.List;

import polyglot.ast.ConstructorCall;
import polyglot.ast.Expr;
import polyglot.ast.ConstructorCall.Kind;
import polyglot.ast.Node;

import polyglot.visit.AmbiguityRemover;
import polyglot.types.SemanticException;

import polyglot.ext.jl.ast.ConstructorCall_c;
import polyglot.util.Position;

import abc.aspectj.types.AJContext;
import abc.aspectj.types.AspectJTypeSystem;

/**
 * @author Oege de Moor
 *
 * constructor calls for intertype declarations
 */
public class AJConstructorCall_c
	extends ConstructorCall_c
	implements ConstructorCall {

	
	public AJConstructorCall_c(
		Position pos,
		Kind kind,
		Expr qualifier,
		List arguments) {
		super(pos, kind, qualifier, arguments);
	}
	
	/** Disambiguate the expression. */
	public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
		AJContext c = (AJContext) ar.context();
		AspectJTypeSystem ts = (AspectJTypeSystem) ar.typeSystem();
		if (!ts.refHostOfITD(c,qualifier(),null)) {		       
				// this is an ordinary constructor call
				return super.disambiguate(ar);
		} else {
				// this is a host constructor call
				AspectJNodeFactory nf = (AspectJNodeFactory) ar.nodeFactory();
				HostConstructorCall_c hc = (HostConstructorCall_c) nf.hostConstructorCall(position,kind,qualifier,arguments);
				return hc.disambiguate(ar);
			}
		}

}
