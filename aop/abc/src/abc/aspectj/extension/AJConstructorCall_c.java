
package abc.aspectj.extension;

import java.util.List;

import polyglot.ast.ConstructorCall;
import polyglot.ast.Expr;
import polyglot.ast.ConstructorCall.Kind;
import polyglot.ast.Node;

import polyglot.visit.AmbiguityRemover;
import polyglot.types.SemanticException;

import polyglot.ext.jl.ast.ConstructorCall_c;
import polyglot.util.Position;

import abc.aspectj.ast.AspectJNodeFactory;
import abc.aspectj.ast.MakesAspectMethods;
import abc.aspectj.ast.HostConstructorCall_c;
import abc.aspectj.types.AJContext;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.InterTypeConstructorInstance_c;
import abc.aspectj.visit.AspectMethods;

/**
 * @author Oege de Moor
 *
 * constructor calls for intertype declarations
 */
public class AJConstructorCall_c
	extends ConstructorCall_c
	implements ConstructorCall, MakesAspectMethods
{

	
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
		AJTypeSystem ts = (AJTypeSystem) ar.typeSystem();
		if (!ts.refHostOfITD(c,qualifier())) {		       
				// this is an ordinary constructor call
				return super.disambiguate(ar);
		} else {
				// this is a host constructor call
				AspectJNodeFactory nf = (AspectJNodeFactory) ar.nodeFactory();
				HostConstructorCall_c hc = (HostConstructorCall_c) nf.hostConstructorCall(position,kind,qualifier,arguments);
				return hc.disambiguate(ar);
			}
		}

        public void aspectMethodsEnter(AspectMethods visitor)
        {
                // do nothing
        }

        public Node aspectMethodsLeave(AspectMethods visitor, AspectJNodeFactory nf,
                                       AJTypeSystem ts)
        {
                if (constructorInstance() instanceof InterTypeConstructorInstance_c) {
                        InterTypeConstructorInstance_c itcd =
                                (InterTypeConstructorInstance_c) constructorInstance();
                        return itcd.mangledCall(this, nf, ts);
                }
                return this;
        }
}
