/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
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
package abc.ra.visit;

import abc.tm.ast.Regex;
import abc.tm.ast.RegexSymbol_c;
import abc.tm.ast.TMNodeFactory;
import polyglot.ast.Node;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;

/**
 * Visitor that replaces all symbols s in a regular expression with
 * a conjunction (r s) of some fixed regular expression r.
 * 
 * @author Eric Bodden
 */
public class RegexShuffle extends NodeVisitor {
	
	protected final Regex insert;
	protected final TMNodeFactory nf;

	public RegexShuffle(Regex insert, TMNodeFactory nf) {
		this.insert = insert;
		this.nf = nf;				
	}
	
	@Override
	public Node leave(Node old, Node n, NodeVisitor v) {
		if(n instanceof RegexSymbol_c) {
			n = nf.RegexConjunction(Position.COMPILER_GENERATED, insert, (Regex) n);
		}		
		return super.leave(old, n, v);
	}

}
