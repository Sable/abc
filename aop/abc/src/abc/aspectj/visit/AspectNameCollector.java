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

import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.frontend.Job;
import polyglot.frontend.Source;

import abc.aspectj.ast.*;

import java.util.*;

/** Collects the names of all aspects to make them available
 *  to early phases that need them.
 *  @author Aske Simon Christensen
 */
public class AspectNameCollector extends NodeVisitor {
    private Collection/*<String>*/ aspect_names;

    public AspectNameCollector(Collection/*<String>*/ aspect_names) {
	this.aspect_names = aspect_names;
    }

    public NodeVisitor enter(Node n) {
	if (n instanceof AspectDecl) {
	    String aname = ((AspectDecl)n).type().fullName();
	    aspect_names.add(aname);
        }
	return this;
    }

}
