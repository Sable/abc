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

import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.MakesAspectMethods;
import abc.aspectj.ast.HostConstructorCall_c;
import abc.aspectj.types.AJContext;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.InterTypeConstructorInstance;
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
				AJNodeFactory nf = (AJNodeFactory) ar.nodeFactory();
				HostConstructorCall_c hc = (HostConstructorCall_c) nf.hostConstructorCall(position,kind,qualifier,arguments);
				return hc.del().disambiguate(ar);
			}
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
