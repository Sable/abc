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

/*
 * Created on Jul 6, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.aspectj.extension;

import java.util.List;

import polyglot.ast.Block;
import polyglot.ext.jl.ast.ConstructorDecl_c;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.TypeChecker;
import polyglot.ast.Node;

import abc.aspectj.ast.HostConstructorCall_c;
import abc.aspectj.types.AspectType;
import abc.aspectj.visit.AJAmbiguityRemover;

/**
 * @author Oege de Moor
 *
 */
public class AJConstructorDecl_c extends ConstructorDecl_c {

	
	public AJConstructorDecl_c(
		Position pos,
		Flags flags,
		String name,
		List formals,
		List throwTypes,
		Block body) {
		super(pos, flags, name, formals, throwTypes, body);
		
	}
	 public NodeVisitor disambiguateEnter(AmbiguityRemover ar) throws SemanticException {
        if (ar.kind() == AmbiguityRemover.SUPER || ar instanceof AJAmbiguityRemover) {
            return ar.bypassChildren(this);
        }
        else if (ar.kind() == AmbiguityRemover.SIGNATURES) {
            if (body != null) {
                return ar.bypass(body);
            }
        }

        return ar;
    }
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		Node n = super.typeCheck(tc);
		if ((constructorInstance().container() instanceof AspectType) &&
		    constructorInstance().formalTypes().size() > 0)
		    throw new SemanticException("Aspects can only have nullary constructors",position());
		return n;
	}
}
