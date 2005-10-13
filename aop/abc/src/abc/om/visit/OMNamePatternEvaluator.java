/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Neil Ongkingco
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
 * Created on Sep 5, 2005
 *
 */
package abc.om.visit;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import polyglot.ast.ClassDecl;
import polyglot.ast.Import;
import polyglot.ast.Node;
import polyglot.ast.SourceFile;
import polyglot.types.ClassType;
import polyglot.types.ParsedClassType;
import polyglot.visit.NodeVisitor;
import abc.aspectj.visit.ContainsNamePattern;
import abc.aspectj.visit.NamePatternEvaluator;
import abc.aspectj.visit.PCNode;
import abc.aspectj.visit.PCStructure;
import abc.om.ast.ModuleDecl;

/**
 * @author Neil Ongkingco
 *
 */
public class OMNamePatternEvaluator extends NamePatternEvaluator {

    public OMNamePatternEvaluator(abc.aspectj.ExtensionInfo ext_info) {
        super(ext_info);
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
	//When in a module declaration, match the pointcuts in a dummy context.
	if (n instanceof ModuleDecl) {
	    PCStructure pcStruct = PCStructure.v();
	    context = new PCNode(null, null, pcStruct);
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
