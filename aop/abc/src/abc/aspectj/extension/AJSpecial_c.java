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
 *  disambiguate this and super in intertype method declarations
 * @author Oege de Moor
 *
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
			return hs.type(type()).del().disambiguate(ar);
		}
	}

	
}
