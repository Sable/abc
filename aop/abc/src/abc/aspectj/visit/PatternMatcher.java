
package abc.aspectj.visit;

import abc.aspectj.ast.*;

import polyglot.ast.*;
import polyglot.visit.*;

import java.util.*;

import soot.*;

public class PatternMatcher {
    private PCStructure hierarchy;
    private Map/*<NamePattern,Set<PCNode>>*/ pattern_matches = new HashMap();

    private Set/*<String>*/ prim_types;

    private static PatternMatcher instance;

    public static PatternMatcher v() {
	return instance;
    }

    private PatternMatcher(PCStructure hierarchy) {
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

    public static PatternMatcher create(PCStructure hierarchy) {
	instance = new PatternMatcher(hierarchy);
	return instance;
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

    public boolean matchesModifiers(List /*<ModifierPattern>*/ mods, soot.ClassMember thing) {
	// FIXME
	return true;
    }




    public abc.weaving.aspectinfo.ClassnamePattern makeAIClassnamePattern(ClassnamePatternExpr pattern) {
	return new AIClassnamePattern(pattern);
    }

    private class AIClassnamePattern implements abc.weaving.aspectinfo.ClassnamePattern {
	ClassnamePatternExpr pattern;

	public AIClassnamePattern(ClassnamePatternExpr pattern) {
	    this.pattern = pattern;
	}

	public boolean matchesClass(SootClass sc) {
	    return PatternMatcher.this.matchesClass(pattern, sc.toString());
	}

	public String toString() {
	    return pattern.toString();
	}
    }

    public abc.weaving.aspectinfo.TypePattern makeAITypePattern(TypePatternExpr pattern) {
	return new AITypePattern(pattern);
    }

    private class AITypePattern implements abc.weaving.aspectinfo.TypePattern {
	TypePatternExpr pattern;

	public AITypePattern(TypePatternExpr pattern) {
	    this.pattern = pattern;
	}

	public boolean matchesType(Type t) {
	    return PatternMatcher.this.matchesType(pattern, t.toString());
	}

	public String toString() {
	    return pattern.toString();
	}
    }

    public abc.weaving.aspectinfo.MethodPattern makeAIMethodPattern(MethodPattern pattern) {
	if(abc.main.Debug.v.matcherWarnUnimplemented)
	    System.err.println("FIXME: Making an incomplete method pattern");
	return new AIMethodPattern(pattern);
    }

    private class AIMethodPattern implements abc.weaving.aspectinfo.MethodPattern {
	MethodPattern pattern;
	public AIMethodPattern(MethodPattern pattern) {
	    this.pattern=pattern;
	}
	public boolean matchesMethod(SootMethod method) {
	    if(!matchesModifiers(pattern.getModifiers(),method)) return false;
	    if(!matchesType(pattern.getType(),method.getReturnType().toString())) return false;
	    if(!matchesClass(pattern.getName().base(),method.getDeclaringClass().toString())) return false;
	    if(!pattern.getName().name().getPattern().matcher(method.getName()).matches()) return false;
	    // FIXME: need to match arguments and throws
	    return true;
	}
	public String toString() {
	    return pattern.toString();
	}
    }

    public abc.weaving.aspectinfo.FieldPattern makeAIFieldPattern(List modifiers,
								  TypePatternExpr type,
								  ClassnamePatternExpr clpat,
								  SimpleNamePattern name) {
	return new AIFieldPattern(modifiers, type, clpat, name);
    }


    private class AIFieldPattern implements abc.weaving.aspectinfo.FieldPattern {
	List/*<ModifierPattern>*/ modifiers;
	TypePatternExpr type;
	ClassnamePatternExpr clpat;
	SimpleNamePattern name;

	public AIFieldPattern(List modifiers, TypePatternExpr type, ClassnamePatternExpr clpat, SimpleNamePattern name) {
	    this.modifiers = modifiers;
	    this.type = type;
	    this.clpat = clpat;
	    this.name = name;
	}

	public boolean matchesField(SootField sf) {
	    return
		matchesModifiers(modifiers, sf) &&
		matchesType(type, sf.getType().toString()) &&
		matchesClass(clpat, sf.getDeclaringClass().toString()) &&
		name.getPattern().matcher(sf.getName()).matches();
	}

	public String toString() {
	    return modifiers.toString()+" "+type.toString()+" "+clpat.toString()+" "+name.toString();
	}
    }

    public abc.weaving.aspectinfo.ConstructorPattern makeAIConstructorPattern(ConstructorPattern pattern) {
	if(abc.main.Debug.v.matcherWarnUnimplemented)
	    System.err.println("FIXME: Making a null contructor pattern");
	return null;
    }



}
