
package abc.aspectj.visit;

import abc.aspectj.ast.*;

import polyglot.types.*;
import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.util.*;

import java.util.*;

public class NamePatternEvaluator extends HaltingVisitor {
    protected Set/*<String>*/ classes;
    protected Set/*<String>*/ packages;
    protected PCNode context;

    protected abc.aspectj.ExtensionInfo ext_info;

    private Set/*<ParsedClassType>*/ seen_classes = new HashSet();

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
		String name = ((ClassDecl)n).type().fullName();
		context = ext_info.hierarchy.getClass(name);
		seen_classes.add(ct);
	    }
	    return this;
	}
	if (n instanceof NamePattern) {
	    //Position p = n.position();
	    //System.out.println("Evaluating name pattern on "+p.file()+":"+p.line());
	    ext_info.pattern_matcher.computeMatches((NamePattern)n, context, classes, packages);
	    return bypass(n);
	}
	return this;
    }

}

