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

import polyglot.types.*;
import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.util.*;

import java.util.*;

/** Evaluate all name patterns and store the list of classes matched.
 *  @author Aske Simon Christensen
 */
public class NamePatternEvaluator extends HaltingVisitor {
    protected Set/*<String>*/ classes;
    protected Set/*<String>*/ packages;
    protected PCNode context;

    protected abc.aspectj.ExtensionInfo ext_info;

    protected Set/*<ParsedClassType>*/ seen_classes = new HashSet();

    public NamePatternEvaluator(abc.aspectj.ExtensionInfo ext_info) {
	this.ext_info = ext_info;
    }

    public NodeVisitor enter(Node n) {
	if (n instanceof SourceFile) {
	    classes = new HashSet();
	    packages = new HashSet();
	    Iterator ii = ((SourceFile)n).imports().iterator();
	    while (ii.hasNext()) {
		Import i = (Import)ii.next();
		if (i.kind() == Import.CLASS) {
		    classes.add(i.name());
		}
		if (i.kind() == Import.PACKAGE) {
		    packages.add(i.name());
		}
	    }
	    // Always implicitly import java.lang
	    packages.add("java.lang");
	    return this;
	}
	if (n instanceof ClassDecl) {
	    ParsedClassType ct = ((ClassDecl)n).type();
	    if (ct.kind() == ClassType.TOP_LEVEL ||
		(ct.kind() == ClassType.MEMBER && seen_classes.contains(ct.container()))) {
		context = ext_info.hierarchy.getClass(ct);
		seen_classes.add(ct);
	    }
	    return this;
	}
	if (n instanceof ContainsNamePattern) {
	    //Position p = n.position();
	    //System.out.println("Evaluating name pattern on "+p.file()+":"+p.line());
	    ext_info.pattern_matcher.computeMatches(((ContainsNamePattern)n).getNamePattern(),
						    context, classes, packages);
	    return bypass(n);
	}
	return this;
    }

}
