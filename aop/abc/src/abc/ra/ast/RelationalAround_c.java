/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
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
package abc.ra.ast;

import java.util.ArrayList;
import java.util.List;

import polyglot.ast.TypeNode;
import polyglot.lex.Identifier;
import polyglot.util.Position;
import abc.aspectj.ast.Around_c;

/**
 * A relational around advice-spec. This is similar to a usual around advice-spec but holds additional
 * proceed-variables.
 *
 * @author Eric Bodden
 */
public class RelationalAround_c extends Around_c implements RelationalAround {

	protected final List<String> proceedIdentifiers;

	public RelationalAround_c(Position pos, TypeNode returnType, List formals, List<Identifier> proceedIdentifiers) {
		super(pos, returnType, formals);
		this.proceedIdentifiers = new ArrayList<String>();
		for (Identifier ident : proceedIdentifiers) {
			this.proceedIdentifiers.add(ident.getIdentifier());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> proceedVars() {
		return proceedIdentifiers;
	}
	
}
