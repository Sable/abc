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

import polyglot.ast.Disamb;
import polyglot.ast.Receiver;
import polyglot.ast.Special;

import polyglot.types.SemanticException;
import polyglot.types.FieldInstance;
import polyglot.types.ClassType;

import polyglot.ext.jl.ast.Disamb_c;

import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.AJContext;

/**
*  when creating missing field targets, check whether this is a reference
 * to an intertype host.
 * @author Oege de Moor
 */

public class AJDisamb_c extends Disamb_c implements Disamb {
	
	protected Receiver makeMissingFieldTarget(FieldInstance fi) throws SemanticException {
			Receiver r;

			if (fi.flags().isStatic()) {
				r = nf.CanonicalTypeNode(pos, fi.container());
			} else {
				// The field is non-static, so we must prepend with
				// "this", but we need to determine if the "this"
				// should be qualified.  Get the enclosing class which
				// brought the field into scope.  This is different
				// from fi.container().  fi.container() returns a super
				// type of the class we want.
	
			
				
				AJTypeSystem ajts = (AJTypeSystem) ts;
				AJNodeFactory ajnf = (AJNodeFactory) nf;
				// first check whether this is a reference to the host of an ITD
				if (ajts.refHostOfITD((AJContext)c,fi)) {
					AJContext ajc = (AJContext) c;
					ClassType scope = ajc.findFieldScopeInHost(name);
					if (! ts.equals(scope,ajc.hostClass()))
						r = ajnf.hostSpecial(pos,Special.THIS,nf.CanonicalTypeNode(pos,scope),
						                                    ajc.hostClass());
					else
					    r = ajnf.hostSpecial(pos,Special.THIS,null, ((AJContext)c).hostClass());
				} else
				{ 	ClassType scope = c.findFieldScope(name);
					if (! ts.equals(scope, c.currentClass())) {
						r = nf.This(pos, nf.CanonicalTypeNode(pos, scope));
					} else {
						r = nf.This(pos);
					}
				}
			}

			return r;
		}
}
