
package arc.aspectj.visit;

import arc.aspectj.ast.*;

import polyglot.ast.*;
import polyglot.visit.*;

import java.util.*;

public class PatternMatcher {
    private PCStructure hierarchy;
    private Map/*<NamePattern,Set<PCNode>>*/ pattern_matches = new HashMap();

    private Set/*<String>*/ prim_types;

    public PatternMatcher(PCStructure hierarchy) {
	this.hierarchy = hierarchy;

	prim_types = new HashSet();
	prim_types.add("void");
	prim_types.add("char");
	prim_types.add("byte");
	prim_types.add("short");
	prim_types.add("int");
	prim_types.add("long");
	prim_types.add("float");
	prim_types.add("double");
	prim_types.add("boolean");
    }

    public void computeMatches(NamePattern pat, PCNode context, Set/*<String>*/ classes, Set/*<String>*/ packages) {
	Set/*<PCNode>*/ matches = hierarchy.matchName(pat, context, classes, packages);
	pattern_matches.put(pat, matches);
    }

    public Set/*<PCNode>*/ getMatches(NamePattern pat) {
	return (Set)pattern_matches.get(pat);
    }

    public boolean matchesObject(NamePattern pat) {
	PCNode object = hierarchy.insertFullName("java.lang.Object", true, false);
	return getMatches(pat).contains(object);
    }

    public boolean matchesClass(ClassnamePatternExpr pattern, String cl) {
	PCNode cl_node = hierarchy.insertFullName(cl, true, false);
	return pattern.matches(this, cl_node);
    }

    public boolean matchesType(TypePatternExpr pattern, String type) {
	int dim = 0;
	while (type.endsWith("[]")) {
	    dim++;
	    type = type.substring(0, type.length()-2);
	}
	if (prim_types.contains(type)) {
	    if (dim == 0) {
		return pattern.matchesPrimitive(this, type);
	    } else {
		return pattern.matchesPrimitiveArray(this, type, dim);
	    }
	} else {
	    PCNode cl_node = hierarchy.insertFullName(type, true, false);
	    if (dim == 0) {
		return pattern.matchesClass(this, cl_node);
	    } else {
		return pattern.matchesClassArray(this, cl_node, dim);
	    }
	}
    }


}
