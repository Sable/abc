
package abc.aspectj.ast;

import java.util.List;

import polyglot.ast.ClassBody;
import polyglot.ast.Expr;
import polyglot.ast.New;
import polyglot.ast.TypeNode;
import polyglot.ast.NodeFactory;
import polyglot.ast.Node;
import polyglot.ast.Special;

import polyglot.types.Context;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.SemanticException;
import polyglot.types.ClassType;

import polyglot.visit.AmbiguityRemover;

import abc.aspectj.types.AspectJTypeSystem;
import abc.aspectj.types.AJContext;

import polyglot.ext.jl.ast.New_c;
import polyglot.util.Position;

/**
 * @author oege
 */
public class AJNew_c extends New_c implements New {

	
	public AJNew_c(
		Position pos,
		Expr qualifier,
		TypeNode tn,
		List arguments,
		ClassBody body) {
		super(pos, qualifier, tn, arguments, body);
	}

	public Node disambiguate(AmbiguityRemover ar) throws SemanticException {
		if (ar.kind() != AmbiguityRemover.ALL) {
			return this;
		}

		if (qualifier == null) {
			ClassType ct = tn.type().toClass();

			if (! ct.isMember() || ct.flags().isStatic()) {
				return this;
			}

			// If we're instantiating a non-static member class, add a "this"
			// qualifier.
			NodeFactory nf = ar.nodeFactory();
			TypeSystem ts = ar.typeSystem();
			Context c = ar.context();
			
			// Search for the outer class of the member.  The outer class is
			// not just ct.outer(); it may be a subclass of ct.outer().
			Type outer = null;

			String name = ct.name();
			
			// first search in the normal way
			outer = findOuter(ct, ts, c.currentClass(), outer, name);

			if (outer == null) {
				// give it another try, starting from the ITD host(=target)
				AspectJTypeSystem ajts = (AspectJTypeSystem) ts;
				AJContext ajc = (AJContext) c;
				AspectJNodeFactory ajnf = (AspectJNodeFactory) nf;
				if (ajc.inInterType()) {
					outer = findOuter(ct,ts,ajc.hostClass(), outer, name);
					if (outer == null)
						throw new SemanticException("Could not find non-static member class \"" +
													name + "\".", position());
					Expr q;
					if (outer.equals(ajc.hostClass())) {
						q = ajnf.hostSpecial(position(),Special.THIS,null,ajc.hostClass());
					}
					else {
						q = ajnf.hostSpecial(position(), Special.THIS,
									nf.CanonicalTypeNode(position(),
														 outer),ajc.hostClass());
					}
					return qualifier(q);
				}		
			
				throw new SemanticException("Could not find non-static member class \"" +
											name + "\".", position());
			}

			// Create the qualifier.
			Expr q;

			if (outer.equals(c.currentClass())) {
				q = nf.This(position());
			}
			else {
				q = nf.This(position(),
							nf.CanonicalTypeNode(position(),
												 outer));
			}

			return qualifier(q);
		}

		return this;
	}

	private Type findOuter(
		ClassType ct,
		TypeSystem ts,
		ClassType start,
		Type outer,
		String name) {
		ClassType t = start ;
		
		// We're in one scope too many.
		if (t == anonType) {
			t = t.outer();
		}
		
		while (t != null) {
			try {
				// HACK: PolyJ outer() doesn't work
				t = ts.staticTarget(t).toClass();
				ClassType mt = ts.findMemberClass(t, name, start);
		
				if (ts.equals(mt, ct)) {
					outer = t;
					break;
				}
			}
			catch (SemanticException e) {
			}
		
			t = t.outer();
		}
		return outer;
	}
}
