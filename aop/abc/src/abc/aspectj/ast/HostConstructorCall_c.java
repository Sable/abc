/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

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

import abc.aspectj.types.AJContext;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.InterTypeConstructorInstance;
import abc.aspectj.types.InterTypeConstructorInstance_c;
import abc.aspectj.visit.AspectMethods;

import polyglot.ext.jl.ast.ConstructorCall_c;
import polyglot.util.Position;

/**
 * In an intertype declaration, when a constructor call refers to the host 
 * rather than the containing aspect, it is disambiguated to a HostConstructorCall.
 * 
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
	Type superType = ct.superType();

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
		if (! superType.isClass()) {
			throw new SemanticException("Super type of " + ct +
			" is not a class.", position());
		}

		// If the super class is an inner class (i.e., has an enclosing
	   // instance of its container class), then either a qualifier 
	   // must be provided, or ct must have an enclosing instance of the
	   // super class's container class, or a subclass thereof.
	   if (qualifier == null && superType.isClass() && superType.toClass().isInnerClass()) {
		   ClassType superContainer = superType.toClass().outer();
		   // ct needs an enclosing instance of superContainer, 
		   // or a subclass of superContainer.
		   ClassType e = ct;
    
		   while (e != null) {
			   if (e.isSubtype(superContainer) && ct.hasEnclosingInstance(e)) {
				   break; 
			   }
			   e = e.outer();
		   }
    
		   if (e == null) {
			   throw new SemanticException(ct + " must have an enclosing instance" +
				   " that is a subtype of " + superContainer, position());
		   }               
		   if (e == ct) {
			   throw new SemanticException(ct + " is a subtype of " + superContainer + 
				   "; an enclosing instance that is a subtype of " + superContainer +
				   " must be specified in the super constructor call.", position());
		   }
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

        public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
                                       AJTypeSystem ts)
        {
                if (constructorInstance() instanceof InterTypeConstructorInstance) {
                        InterTypeConstructorInstance itcd =
                                (InterTypeConstructorInstance) constructorInstance();
                        return itcd.mangledCall(this, nf, ts);
                }

                return this;
        }
}
