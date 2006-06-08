/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Damien Sereni
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

import java.util.*;
import java.util.regex.*;

import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.aspectinfo.AbcFactory;

import soot.*;

/** Encapsulates the pattern matching code.
 *  Contains means for matching classname patterns, type patterns,
 *  method patterns, field patterns and constructor patterns.
 *  @author Aske Simon Christensen
 *  @author Damien Sereni
 */
public class PatternMatcher {
    private PCStructure hierarchy;
    private Map/*<NamePattern,Set<PCNode>>*/ pattern_matches = new HashMap();
    private Map/*<NamePattern,Set<String>>*/ pattern_classes = new HashMap();
    private Map/*<NamePattern,Set<String>>*/ pattern_packages = new HashMap();
    private Map/*<NamePattern,PCNode>*/ pattern_context = new HashMap();

    private Set/*<String>*/ prim_types;

    private Map/*<String,Pattern>*/ name_pattern_cache = new HashMap();

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

    public Pattern compileNamePattern(String name_pat) {
	if (name_pattern_cache.containsKey(name_pat)) {
	    return (Pattern)name_pattern_cache.get(name_pat);
	}
	String pat;
	// Make sure that the pattern never matches a pure integer name
	if (name_pat.equals("*")) {
	    pat = "[^0-9].*";
	} else if (name_pat.startsWith("*")) {
	    String pat_start;
	    char after_star = name_pat.charAt(1);
	    if (after_star >= '0' && after_star <= '9') {
		pat_start = "[^0-9].*";
	    } else {
		pat_start = "([^0-9].*)?";
	    }
	    pat = pat_start + name_pat.substring(1).replaceAll("\\*", ".*");
	} else {
	    char first = name_pat.charAt(0);
	    if (first >= '0' && first <= '9') {
		pat = "[a&&b]"; // The nonmatching pattern. Any better way to do it?
	    } else {
		pat = name_pat.replaceAll("\\*", ".*");
	    }
	}
	if (abc.main.Debug.v().namePatternMatches)
	    System.err.println("Compiling the name pattern component "+name_pat+" into "+pat);
	pat = pat.replaceAll("\\$","\\\\\\$");
	Pattern p = Pattern.compile("^"+pat+"$");
	name_pattern_cache.put(name_pat, p);
	return p;
    }

    public void computeMatches(NamePattern pat, PCNode context, Set/*<String>*/ classes, Set/*<String>*/ packages) {
	if (abc.main.Debug.v().namePatternMatches)
	    System.err.println("Evaluating the name pattern "+pat+" ("+pat.getClass()+") in context "+context+" on "+pat.position());
	pattern_classes.put(pat, classes);
	pattern_packages.put(pat, packages);
	pattern_context.put(pat, context);
	pattern_matches.put(pat, hierarchy.matchName(pat, context, classes, packages));
    }

    /** Should be called when jimplification is complete.
     *  This ensures that all Soot classes can be used to index into the
     *  hierarchy.
     */
    public void updateWithAllSootClasses() {
	PCStructure.v().updateWithAllSootClasses();
    }

    public void recomputeAllMatches() {
	Iterator pati = pattern_matches.keySet().iterator();
	while (pati.hasNext()) {
	    NamePattern pat = (NamePattern)pati.next();
	    PCNode context = (PCNode)pattern_context.get(pat);
	    Set classes = (Set)pattern_classes.get(pat);
	    Set packages = (Set)pattern_packages.get(pat);
	    //System.out.print("Recomputing pattern "+pat+"...");
	    pattern_matches.put(pat, hierarchy.matchName(pat, context, classes, packages));
	    //System.out.println("DONE");
	}
    }

    Set getMatches(NamePattern pat) {
	if (!pattern_matches.containsKey(pat)) {
	    throw new RuntimeException("Unknown name pattern: "+pat+" ("+pat.getClass()+") at "+pat.position());
	}
	return (Set)pattern_matches.get(pat);
    }

    public boolean matchesName(NamePattern pat, PCNode name) {
	//System.out.print("Matching pattern "+pat+"...");
	boolean res = getMatches(pat).contains(name);
	//System.out.println("DONE");
	return res;
    }

    public boolean matchesObject(NamePattern pat) {
	PCNode object = hierarchy.getClass(Scene.v().getSootClass("java.lang.Object"));
	return matchesName(pat, object);
    }

    public boolean matchesClass(ClassnamePatternExpr pattern, SootClass sc) {
	PCNode cl_node = hierarchy.getClass(sc);
	return pattern.matches(this, cl_node);
    }

    private boolean containsMethod(SootClass sc, String name, List parameterTypes, Type returnType, boolean isStatic) {
	// FIXME: This is rather inefficient!
	try {
	    if (sc.declaresMethod(name, parameterTypes, returnType)) {
		return true;
	    }
	    Scene.v().makeMethodRef(sc, name, parameterTypes, returnType, isStatic).resolve();
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }

    private boolean containsField(SootClass sc, String name, Type type, boolean isStatic) {
	// FIXME: This is rather inefficient!
	try {
	    if (sc.declaresField(name, type)) {
		return true;
	    }
	    Scene.v().makeFieldRef(sc, name, type, isStatic).resolve();
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }

    private boolean matchesClassWithMethodMatching(ClassnamePatternExpr pattern, SootClass base_sc, String name, List parameterTypes, Type returnType, boolean isStatic) {
	Set seen = new HashSet();
	LinkedList worklist = new LinkedList();
	worklist.add(base_sc);
	while (!worklist.isEmpty()) {
	    SootClass sc = (SootClass)worklist.removeFirst();
	    if (!seen.contains(sc)) {
		if (matchesClass(pattern, sc) && containsMethod(sc, name, parameterTypes, returnType, isStatic)) {
		    return true;
		}
		seen.add(sc);
		if (sc.hasSuperclass()) {
		    worklist.add(sc.getSuperclass());
		}
		Iterator ini = sc.getInterfaces().iterator();
		while (ini.hasNext()) {
		    SootClass in = (SootClass)ini.next();
		    worklist.add(in);
		}
	    }
	}
	return false;
    }

    private boolean matchesClassSubclassOf(ClassnamePatternExpr pattern, SootClass base_sc, SootClass super_sc) {
	FastHierarchy h = Scene.v().getFastHierarchy();
	Set seen = new HashSet();
	LinkedList worklist = new LinkedList();
	worklist.add(base_sc);
	while (!worklist.isEmpty()) {
	    SootClass sc = (SootClass)worklist.removeFirst();
	    if (!seen.contains(sc)) {
		if (h.canStoreType(sc.getType(), super_sc.getType()) && matchesClass(pattern, sc)) {
		    return true;
		}
		seen.add(sc);
		if (sc.hasSuperclass()) {
		    worklist.add(sc.getSuperclass());
		}
		Iterator ini = sc.getInterfaces().iterator();
		while (ini.hasNext()) {
		    SootClass in = (SootClass)ini.next();
		    worklist.add(in);
		}
	    }
	}
	return false;
    }
    /*
    private boolean matchesClassWithFieldResolvingTo(ClassnamePatternExpr pattern, SootClass base_sc, SootField field) {
	Set seen = new HashSet();
	LinkedList worklist = new LinkedList();
	worklist.add(base_sc);
	while (!worklist.isEmpty()) {
	    SootClass sc = (SootClass)worklist.removeFirst();
	    if (!seen.contains(sc)) {
		if (matchesClass(pattern, sc) && containsField(sc, field.getName(), field.getType(), field.isStatic())) {
		    SootFieldRef sfr = Scene.v().makeFieldRef(sc, field.getName(), field.getType(), field.isStatic());
		    if (sfr.resolve().equals(field)) {
			return true;
		    }
		}
		seen.add(sc);
		if (sc.hasSuperclass()) {
		    worklist.add(sc.getSuperclass());
		}
		Iterator ini = sc.getInterfaces().iterator();
		while (ini.hasNext()) {
		    SootClass in = (SootClass)ini.next();
		    worklist.add(in);
		}
	    }
	}
	return false;
    }
    */
    public boolean matchesType(TypePatternExpr pattern, String type) {
	// System.out.println("Matching type pattern "+pattern+" on "+pattern.position()+" to "+type+"...");
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
		
	    PCNode cl_node = hierarchy.getClass(Scene.v().getSootClass(type));
	    if (dim == 0) {
		return pattern.matchesClass(this, cl_node);
	    } else {
		return pattern.matchesClassArray(this, cl_node, dim);
	    }
	}
    }

    public boolean matchesModifiers(List /*<ModifierPattern>*/ modps, int mods) {
	Iterator modpi = modps.iterator();
	while (modpi.hasNext()) {
	    ModifierPattern modp = (ModifierPattern)modpi.next();
	    int pmods = AbcFactory.modifiers(modp.modifier());
	    if (modp.positive()) {
		if ((pmods & mods) == 0) return false;
	    } else {
		if ((pmods & mods) != 0) return false;
	    }
	}
	return true;
    }

    public boolean matchesFormals(List/*<FormalPattern>*/ fpats, List/*<soot.Type>*/ ftypes) {
	return matchesFormals(fpats, 0, ftypes, 0);
    }

    private boolean matchesFormals(List/*<FormalPattern>*/ fpats, int fpi, List/*<soot.Type>*/ ftypes, int fti) {
	// FIXME: BRUTE FORCE MATCHING. DO SOMETHING MORE CLEVER!
	while (fpi < fpats.size()) {
	    FormalPattern fp = (FormalPattern)fpats.get(fpi);
	    if (fp instanceof TypeFormalPattern) {
		if (fti >= ftypes.size()) return false;
		TypePatternExpr pat = ((TypeFormalPattern)fp).expr();
		soot.Type ft = (soot.Type)ftypes.get(fti);
		if (!matchesType(pat, ft.toString())) return false;
	    } else {
		// DOTDOT
		while (fti <= ftypes.size()) {
		    if (matchesFormals(fpats, fpi+1, ftypes, fti)) return true;
		    fti++;
		}
		return false;
	    }
	    fpi++;
	    fti++;
	}
	return fti == ftypes.size();
    }

    private boolean matchesThrows(List/*<ThrowsPattern>*/ tpats, List/*<soot.SootClass>*/ excs) {
	Iterator tpati = tpats.iterator();
	while (tpati.hasNext()) {
	    ThrowsPattern tpat = (ThrowsPattern)tpati.next();
	    ClassnamePatternExpr cnp = tpat.type();
	    boolean matches = false;
	    Iterator ei = excs.iterator();
	    while (ei.hasNext() && !matches) {
		soot.SootClass e = (soot.SootClass)ei.next();
		if (matchesClass(cnp, e)) matches = true;
	    }
	    if (matches != tpat.positive()) return false;
	}
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

	public ClassnamePatternExpr getPattern() {
	    return pattern;
	}

	public boolean matchesClass(SootClass sc) {
	    boolean matches = PatternMatcher.this.matchesClass(pattern, sc);
	    if (abc.main.Debug.v().patternMatches) {
		System.err.println("Matching classname pattern "+pattern+" against "+sc+": "+matches);
	    }
	    return matches;
	}

	public String toString() {
	    return pattern.toString();
	}

	public boolean equivalent(abc.weaving.aspectinfo.ClassnamePattern otherpat) {
	    return pattern.equivalent(otherpat.getPattern());
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

	public TypePatternExpr getPattern() {
	    return pattern;
	}

	public boolean matchesType(Type t) {
	    boolean matches = PatternMatcher.this.matchesType(pattern, t.toString());
	    if (abc.main.Debug.v().patternMatches) {
		System.err.println("Matching type pattern "+pattern+" against "+t+": "+matches);
	    }
	    return matches;
	}

	public String toString() {
	    return pattern.toString();
	}

	public boolean equivalent(abc.weaving.aspectinfo.TypePattern p) {
	    return pattern.equivalent(p.getPattern());
	}

    }

    public abc.weaving.aspectinfo.MethodPattern makeAIMethodPattern(MethodPattern pattern) {
	return new AIMethodPattern(pattern);
    }

    private class AIMethodPattern implements abc.weaving.aspectinfo.MethodPattern {
	MethodPattern pattern;

	public AIMethodPattern(MethodPattern pattern) {
	    this.pattern = pattern;
	}

	public MethodPattern getPattern() {
	    return pattern;
	}

	public boolean matchesExecution(SootMethod method) {
	    int mods = MethodCategory.getModifiers(method);
	    String name = MethodCategory.getName(method);
	    SootClass realcl = MethodCategory.getClass(method);
	    LinkedList/*<soot.Type>*/ ftypes = new LinkedList(method.getParameterTypes());
	    int skip_first = MethodCategory.getSkipFirst(method);
	    int skip_last = MethodCategory.getSkipLast(method);
	    //System.out.println("Real name: "+name+" "+skip_first+" "+skip_last);
	    while (skip_first-- > 0) ftypes.removeFirst();
	    while (skip_last-- > 0) ftypes.removeLast();
	    boolean matches =
		matchesType(pattern.getType(), method.getReturnType().toString()) &&
		pattern.getName().name().getPattern().matcher(name).matches() &&
		matchesFormals(pattern.getFormals(), ftypes) &&
		matchesModifiers(pattern.getModifiers(), mods) &&
		matchesThrows(pattern.getThrowspats(), method.getExceptions());

	    if (Modifier.isStatic(mods)) {
		matches = matches && matchesClass(pattern.getName().base(), realcl);
	    } else {
		matches = matches && (matchesClass(pattern.getName().base(), realcl) ||
				      matchesClassWithMethodMatching(pattern.getName().base(),
								     realcl,
								     name,
								     ftypes,
								     method.getReturnType(),
								     false));
	    }
	    if (abc.main.Debug.v().patternMatches) {
		System.err.println("Matching method execution pattern "+pattern+" against ("+mods+" "+realcl+"."+name+") "+method+": "+matches);
	    }
	    return matches;
	}

	public boolean matchesCall(SootMethodRef methodref) {
	    SootMethod method = methodref.resolve();
	    boolean matches =
		matchesType(pattern.getType(), method.getReturnType().toString()) &&
		pattern.getName().name().getPattern().matcher(MethodCategory.getName(method)).matches() &&
		matchesFormals(pattern.getFormals(), method.getParameterTypes()) &&
		matchesModifiers(pattern.getModifiers(), method.getModifiers()) &&
		matchesThrows(pattern.getThrowspats(), method.getExceptions());
	    if (Modifier.isStatic(method.getModifiers())) {
		matches = matches && matchesClass(pattern.getName().base(), method.getDeclaringClass());
	    } else {
		matches = matches && matchesClassWithMethodMatching(pattern.getName().base(),
								    methodref.declaringClass(),
								    method.getName(),
								    method.getParameterTypes(),
								    method.getReturnType(),
								    false);
	    }
	    if (abc.main.Debug.v().patternMatches) {
		System.err.println("Matching method call pattern "+pattern+" against "+methodref+": "+matches);
	    }
	    return matches;
	}

	public String toString() {
	    return pattern.toString();
	}

	public boolean equivalent(abc.weaving.aspectinfo.MethodPattern otherpat) {
	    return pattern.equivalent(otherpat.getPattern());
	}
    }

    public abc.weaving.aspectinfo.FieldPattern makeAIFieldPattern(FieldPattern pattern) {
	return new AIFieldPattern(pattern);
    }

    private class AIFieldPattern implements abc.weaving.aspectinfo.FieldPattern {
	FieldPattern pattern;

	public AIFieldPattern(FieldPattern pattern) {
	    this.pattern = pattern;
	}

	public FieldPattern getPattern() {
	    return pattern;
	}

	public boolean matchesFieldRef(SootFieldRef sfr) {
	    int mods = MethodCategory.getModifiers(sfr);
	    String name = MethodCategory.getName(sfr);
	    SootClass realcl = MethodCategory.getClass(sfr);
	    SootFieldRef realfr = Scene.v().makeFieldRef(realcl, name, sfr.type(), Modifier.isStatic(mods));
	    boolean matches =
		matchesType(pattern.getType(), sfr.type().toString()) &&
		pattern.getName().name().getPattern().matcher(name).matches() &&
		matchesModifiers(pattern.getModifiers(), mods) &&
		(matchesClass(pattern.getName().base(), realcl) ||
		 (containsField(realcl, name, sfr.type(), Modifier.isStatic(mods)) &&
		  matchesClassSubclassOf(pattern.getName().base(), realcl, realfr.resolve().getDeclaringClass())));
	    if (abc.main.Debug.v().patternMatches) {
		System.err.println("Matching field pattern "+pattern+" against "+sfr+": "+matches);
	    }
	    return matches;
	}

	public boolean matchesMethod(SootMethod sm) {
	    int cat = MethodCategory.getCategory(sm);
	    if (!(cat == MethodCategory.ACCESSOR_GET || cat == MethodCategory.ACCESSOR_SET)) {
		return false;
	    }
	    String name = MethodCategory.getName(sm);
	    SootClass realcl = MethodCategory.getClass(sm);
	    //FIXME: This will not work for inner classes
	    SootField sf = realcl.getField(name);
	    return matchesFieldRef(sf.makeRef());
	}

	public String toString() {
	    return pattern.toString();
	}

	public boolean equivalent(abc.weaving.aspectinfo.FieldPattern otherpat) {
	    return pattern.equivalent(otherpat.getPattern());
	}
    }

    public abc.weaving.aspectinfo.ConstructorPattern makeAIConstructorPattern(ConstructorPattern pattern) {
	// Assumes that name is <init>
	return new AIConstructorPattern(pattern);
    }

    private class AIConstructorPattern implements abc.weaving.aspectinfo.ConstructorPattern {
	ConstructorPattern pattern;

	public AIConstructorPattern(ConstructorPattern pattern) {
	    this.pattern = pattern;
	}

	public ConstructorPattern getPattern() {
	    return pattern;
	}

	public boolean matchesConstructor(SootMethod method) {
	    int mods = MethodCategory.getModifiers(method);
	    SootClass realcl = MethodCategory.getClass(method);
	    LinkedList/*<soot.Type>*/ ftypes = new LinkedList(method.getParameterTypes());
	    int skip_first = MethodCategory.getSkipFirst(method);
	    int skip_last = MethodCategory.getSkipLast(method);
	    //System.out.println("Real name: "+name+" "+skip_first+" "+skip_last);
	    while (skip_first-- > 0) ftypes.removeFirst();
	    while (skip_last-- > 0) ftypes.removeLast();
	    boolean matches =
		matchesModifiers(pattern.getModifiers(), mods) &&
		matchesClass(pattern.getName().base(), realcl) &&
		matchesFormals(pattern.getFormals(), ftypes) &&
		matchesThrows(pattern.getThrowspats(), method.getExceptions());
	    if (abc.main.Debug.v().patternMatches) {
		System.err.println("Matching constructor pattern "+pattern+" against "+method+": "+matches);
	    }
	    return matches;
	}
	public String toString() {
	    return pattern.toString();
	}

	public boolean equivalent(abc.weaving.aspectinfo.ConstructorPattern otherpat) {
	    return pattern.equivalent(otherpat.getPattern());
	}
    }



}
