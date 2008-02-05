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

import java.util.HashSet;
import java.util.Set;

import polyglot.ast.Node;
import polyglot.visit.NodeVisitor;
import abc.tm.ast.Regex;
import abc.tm.ast.RegexSymbol_c;

/**
 * Visitor that collects the names of all symbols used in a {@link Regex}.
 *
 * @author Eric Bodden
 */
public class SymbolCollector extends NodeVisitor {

	protected Set<String> symbolNames;
	
	public SymbolCollector() {
		this.symbolNames = new HashSet<String>();
	}
	
	public Node leave(Node old, Node n, NodeVisitor v) {
		if(n instanceof RegexSymbol_c) {
			String name = ((RegexSymbol_c)n).name();
			symbolNames.add(name);
		}
		return super.leave(old, n, v);
	}
	
	public Set<String> getSymbolNames() {
		return symbolNames;
	}
	
}
