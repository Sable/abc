/* abc - The AspectBench Compiler
 * Copyright (C) 2004 oege
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

package abc.aspectj.extension;

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
import polyglot.visit.TypeChecker;

import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.MakesAspectMethods;

import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.AJContext;
import abc.aspectj.types.AspectType;
import abc.aspectj.types.InterTypeConstructorInstance;
import abc.aspectj.types.InterTypeConstructorInstance_c;

import abc.aspectj.visit.AspectMethods;

import polyglot.ext.jl.ast.New_c;
import polyglot.util.Position;

/**
 * @author Oege de Moor
 */
public class AJNew_c extends New_c implements New, MakesAspectMethods {

    protected boolean hierarchyBuilt = false;
    
	public AJNew_c(
		Position pos,
		Expr qualifier,
		TypeNode tn,
		List arguments,
		ClassBody body) {
		super(pos, qualifier, tn, arguments, body);
	}

    public boolean hierarchyBuilt() {
        return hierarchyBuilt;
    }
    
    public void setHierarchyBuilt() {
        hierarchyBuilt=true;
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
				AJTypeSystem ajts = (AJTypeSystem) ts;
				AJContext ajc = (AJContext) c;
				AJNodeFactory ajnf = (AJNodeFactory) nf;
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
	
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		Node n = super.typeCheck(tc);
		if ((tn!=null) && (tn.type() instanceof ClassType) &&
		    (tn.type() instanceof AspectType))
		   throw new SemanticException("Cannot instantiate an aspect with new.");
		return n;
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
                        return itcd.mangledNew(this, nf, ts);
                }
                return this;
        }
}
