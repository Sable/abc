/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
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

package abc.aspectj.visit;

import abc.aspectj.ast.*;

import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.util.*;

import java.util.*;

/** Produces debug output telling which classes are matched by each name pattern.
 *  @author Aske Simon Christensen
 */
public class PatternTester extends NodeVisitor {
    protected abc.aspectj.ExtensionInfo ext_info;

    public PatternTester(abc.aspectj.ExtensionInfo ext_info) {
	this.ext_info = ext_info;
    }

    public Node override(Node n) {
	if (n instanceof ContainsNamePattern) {
	    NamePattern pat = ((ContainsNamePattern)n).getNamePattern();
	    Position p = pat.position();
	    System.out.println("The name pattern "+pat+" on "+p+" matches these names:");
	    Set matches = ext_info.pattern_matcher.getMatches(pat);
	    Iterator mi = matches.iterator();
	    while (mi.hasNext()) {
		PCNode m = (PCNode)mi.next();
		System.out.println(m);
	    }
	    return n;
	}
	return null;
    }

}
