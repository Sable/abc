
package abc.aspectj.ast;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import polyglot.ast.Expr;
import polyglot.ast.ConstructorCall.Kind;
import polyglot.ast.Node;

import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.types.Context;
import polyglot.types.ClassType;
import polyglot.types.Type;
import polyglot.types.ConstructorInstance;

import polyglot.visit.TypeChecker;

import abc.aspectj.ast.AspectJNodeFactory;
import abc.aspectj.ast.MakesAspectMethods;
import abc.aspectj.types.AJContext;
import abc.aspectj.types.AspectJTypeSystem;
import abc.aspectj.types.InterTypeConstructorInstance_c;
import abc.aspectj.visit.AspectMethods;

import polyglot.ext.jl.ast.ConstructorCall_c;
import polyglot.util.Position;

/**
 * @author Oege de Moor
 *
 */
public class HostConstructorCall_c extends ConstructorCall_c
                                   implements MakesAspectMethods
{
	public HostConstructorCall_c(
		Position pos,
		Kind kind,
		Expr qualifier,
		List arguments) {
		super(pos, kind, qualifier, arguments);
	}

	/** Override the typecheck for normal constructor calls, replacing currentClass by hostClass */
	public Node typeCheck(TypeChecker tc) throws SemanticException {
	TypeSystem ts = tc.typeSystem();
	AJContext c = (AJContext) tc.context();

	ClassType ct = c.hostClass();

		// The qualifier specifies the enclosing instance of this inner class.
		// The type of the qualifier must be the outer class of this
		// inner class or one of its super types.
		//
		// Example:
		//
		// class Outer {
		//     class Inner { }
		// }
		//
		// class ChildOfInner extends Outer.Inner {
		//     ChildOfInner() { (new Outer()).super(); }
		// }
		if (qualifier != null) {
			if (kind != SUPER) {
				throw new SemanticException("Can only qualify a \"super\"" +
											"constructor invocation.",
											position());
			}


			Type superType = ct.superType();
            
			if (!superType.isClass() || !superType.toClass().isInnerClass() ||
				superType.toClass().inStaticContext()) {
				throw new SemanticException("The class \"" + superType + "\"" +
					" is not an inner class, or was declared in a static " +
					"context; a qualified constructor invocation cannot " +
					"be used.", position());
			}

			Type qt = qualifier.type();

			if (! qt.isClass() || !qt.isSubtype(superType.toClass().outer())) {
				throw new SemanticException("The type of the qualifier " +
					"\"" + qt + "\" does not match the immediately enclosing " +
					"class  of the super class \"" +
					superType.toClass().outer() + "\".", qualifier.position());
			}
		}

	if (kind == SUPER) {
		if (! ct.superType().isClass()) {
			throw new SemanticException("Super type of " + ct +
			" is not a class.", position());
		}

		ct = ct.superType().toClass();
	}

	List argTypes = new LinkedList();

	for (Iterator iter = this.arguments.iterator(); iter.hasNext();) {
		Expr e = (Expr) iter.next();
		argTypes.add(e.type());
	}

	ConstructorInstance ci = ts.findConstructor(ct, argTypes, c.hostClass());

	return constructorInstance(ci);
	}

        public void aspectMethodsEnter(AspectMethods visitor)
        {
                // do nothing
        }

        public Node aspectMethodsLeave(AspectMethods visitor, AspectJNodeFactory nf,
                                       AspectJTypeSystem ts)
        {
                if (constructorInstance() instanceof InterTypeConstructorInstance_c) {
                        InterTypeConstructorInstance_c itcd =
                                (InterTypeConstructorInstance_c) constructorInstance();
                        return itcd.mangledCall(this, nf, ts);
                }

                return this;
        }
}
