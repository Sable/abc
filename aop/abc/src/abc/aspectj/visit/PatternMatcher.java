
package abc.aspectj.visit;

import abc.aspectj.ast.*;

import polyglot.ast.*;
import polyglot.visit.*;

import java.util.*;
import java.util.regex.*;

import abc.weaving.aspectinfo.MethodCategory;

import soot.*;

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

    public void updateWithAllSootClasses() {
	Iterator sci = Scene.v().getClasses().iterator();
	while (sci.hasNext()) {
	    SootClass sc = (SootClass)sci.next();
	    hierarchy.insertClassAndSuperclasses(sc, false);
	}
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

    public boolean matchesClassWithMethod(ClassnamePatternExpr pattern, SootClass base_sc, String name, List parameterTypes, Type returnType) {
	Set seen = new HashSet();
	LinkedList worklist = new LinkedList();
	worklist.add(base_sc);
	while (!worklist.isEmpty()) {
	    SootClass sc = (SootClass)worklist.removeFirst();
	    if (!seen.contains(sc)) {
		//System.err.println(sc+": "+matchesClass(pattern, sc)+" ");
		//Iterator mi = sc.getMethods().iterator();
		//while (mi.hasNext()) {
		//    SootMethod m = (SootMethod)mi.next();
		//    System.err.println(m);
		//}
		if (sc.declaresMethod(name, parameterTypes, returnType) && matchesClass(pattern, sc)) {
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

    public boolean matchesType(TypePatternExpr pattern, String type) {
	//System.out.println("Matching type pattern "+pattern+" on "+pattern.position()+" to "+type+"...");
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

    public boolean matchesModifiers(List /*<ModifierPattern>*/ modps, soot.ClassMember thing) {
	int thing_mods = thing.getModifiers();
	Iterator modpi = modps.iterator();
	while (modpi.hasNext()) {
	    ModifierPattern modp = (ModifierPattern)modpi.next();
	    int mods = soot.javaToJimple.Util.getModifier(modp.modifier());
	    if (modp.positive()) {
		if ((mods & thing_mods) == 0) return false;
	    } else {
		if ((mods & thing_mods) != 0) return false;
	    }
	}
	return true;
    }

    public boolean matchesFormals(List/*<FormalPattern>*/ fpats, soot.SootMethod method) {
	LinkedList/*<soot.Type>*/ ftypes = new LinkedList(method.getParameterTypes());
	int skip_first = MethodCategory.getSkipFirst(method);
	int skip_last = MethodCategory.getSkipLast(method);
	while (skip_first-- > 0) ftypes.removeFirst();
	while (skip_last-- > 0) ftypes.removeLast();
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
	    boolean matches = PatternMatcher.this.matchesType(pattern, t.toString());
	    if (abc.main.Debug.v().patternMatches) {
		System.err.println("Matching type pattern "+pattern+" against "+t+": "+matches);
	    }
	    return matches;
	}

	public String toString() {
	    return pattern.toString();
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

	public boolean matchesMethod(SootMethod method) {
	    String name = MethodCategory.getName(method);
	    SootClass realcl = MethodCategory.getClass(method);
	    LinkedList/*<soot.Type>*/ ftypes = new LinkedList(method.getParameterTypes());
	    int skip_first = MethodCategory.getSkipFirst(method);
	    int skip_last = MethodCategory.getSkipLast(method);
	    while (skip_first-- > 0) ftypes.removeFirst();
	    while (skip_last-- > 0) ftypes.removeLast();
	    boolean matches =
		matchesModifiers(pattern.getModifiers(), method) &&
		matchesType(pattern.getType(), method.getReturnType().toString()) &&
		pattern.getName().name().getPattern().matcher(name).matches() &&
		matchesFormals(pattern.getFormals(), method) &&
		matchesThrows(pattern.getThrowspats(), method.getExceptions()) &&
		matchesClassWithMethod(pattern.getName().base(), realcl, name, ftypes, method.getReturnType());
	    if (abc.main.Debug.v().patternMatches) {
		System.err.println("Matching method pattern "+pattern+" against "+method+": "+matches);
	    }
	    return matches;
	}
	public String toString() {
	    return pattern.toString();
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

	public boolean matchesField(SootField sf) {
	    boolean matches =
		matchesModifiers(pattern.getModifiers(), sf) &&
		matchesType(pattern.getType(), sf.getType().toString()) &&
		matchesClass(pattern.getName().base(), sf.getDeclaringClass()) &&
		pattern.getName().name().getPattern().matcher(sf.getName()).matches();
	    if (abc.main.Debug.v().patternMatches) {
		System.err.println("Matching field pattern "+pattern+" against "+sf+": "+matches);
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
	    return matchesField(sf);
	}

	public String toString() {
	    return pattern.toString();
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

	public boolean matchesConstructor(SootMethod method) {
	    boolean matches =
		matchesModifiers(pattern.getModifiers(), method) &&
		matchesClass(pattern.getName().base(), method.getDeclaringClass()) &&
		matchesFormals(pattern.getFormals(), method) &&
		matchesThrows(pattern.getThrowspats(), method.getExceptions());
	    if (abc.main.Debug.v().patternMatches) {
		System.err.println("Matching constructor pattern "+pattern+" against "+method+": "+matches);
	    }
	    return matches;
	}
	public String toString() {
	    return pattern.toString();
	}
    }



}
