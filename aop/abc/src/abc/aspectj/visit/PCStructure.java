
package abc.aspectj.visit;

import java.util.*;

import abc.aspectj.ast.*;

import polyglot.types.SemanticException;
import polyglot.types.ClassType;
import polyglot.types.Resolver;

import soot.*;

/** The internal representation of the class hierarchy and the class and
 *  package nesting used by the pattern matcher.
 */
public class PCStructure {
    private static PCStructure v;
    public static PCStructure v() {
	return v;
    }

    Resolver res;
    PCNode root;
    PCNode dummy;
    Map/*<ClassType,PCNode>*/ classes;
    Map/*<String,ClassType>*/ names;

    public PCStructure(Resolver res) {
	this.res = res;
	root = new PCNode(null, null, this);
	dummy = new PCNode(null, null, this);
	classes = new HashMap();
	names = new HashMap();
	v = this;
    }

    public static void reset() {
	v = null;
    }

    private static boolean isNameable(ClassType ct) {
	if (ct.kind() == ClassType.TOP_LEVEL) return true;
	if (ct.kind() == ClassType.MEMBER) return isNameable(ct.outer());
	return false;
    }

    private PCNode insertClass(ClassType ct, boolean weavable) {
	if (classes.containsKey(ct)) {
	    return ((PCNode)classes.get(ct)).updateWeavable(weavable);
	} else {
	    PCNode cn;
	    if (isNameable(ct)) {
		cn = root.insertFullName(ct.fullName(), true, weavable);
	    } else {
		cn = new PCNode(null, null, this).updateWeavable(weavable);
	    }
	    classes.put(ct, cn);
	    return cn;
	}
    }

    private ClassType sootClassToClassType(SootClass sc) {
	boolean debug = abc.main.Debug.v().sootClassToClassType;
	if (debug) System.err.print("To ClassType: "+sc.getName()+" ... ");
	String jvm_name = sc.getName();
	if (names.containsKey(jvm_name)) {
	    if (debug) System.err.println("KNOWN");
	    return (ClassType)names.get(jvm_name);
	} else {
	    try {
		if (debug) System.err.println("LOOKUP");
		ClassType ct = (ClassType)res.find(jvm_name);
		names.put(jvm_name, ct);
		return ct;
	    } catch (SemanticException e) {
		throw new NoSuchElementException("No such class: "+sc);
	    }
	}
    }

    public Collection getClassTypes() {
	return classes.keySet();
    }

    public PCNode getClass(ClassType ct) {
	if (!classes.containsKey(ct)) {
	    throw new NoSuchElementException("No such class: "+ct);
	}
	return (PCNode)classes.get(ct);
    }

    public PCNode getClass(SootClass sc) {
	return getClass(sootClassToClassType(sc));
    }

    public boolean containsClass(ClassType ct) {
	return classes.containsKey(ct);
    }

    public boolean containsClass(SootClass sc) {
	return containsClass(sootClassToClassType(sc));
    }

    public PCNode insertClassAndSuperclasses(ClassType ct, boolean weavable) {
	if (containsClass(ct)) {
	    return getClass(ct).updateWeavable(weavable);
	} else {
	    PCNode pc = insertClass(ct, weavable);

	    ClassType st = (ClassType)ct.superType();
	    if (st != null) {
		PCNode scpc = insertClassAndSuperclasses(st, false);
		pc.addParent(scpc);
	    }
	    Iterator iii = ct.interfaces().iterator();
	    while (iii.hasNext()) {
		ClassType ii = (ClassType)iii.next();
		PCNode iipc = insertClassAndSuperclasses(ii, false);
		pc.addParent(iipc);
	    }
	    return pc;
	}
    }

    public PCNode insertClassAndSuperclasses(SootClass sc, boolean weavable) {
	return insertClassAndSuperclasses(sootClassToClassType(sc), weavable);
    }

    /** Should be called when jimplification is complete.
     *  This ensures that all Soot classes can be used to index into the
     *  hierarchy.
     */
    public void updateWithAllSootClasses() {
	// Fix up all weavable classes first
	Iterator cti = classes.keySet().iterator();
	while (cti.hasNext()) {
	    ClassType ct = (ClassType)cti.next();
	    if (((PCNode)classes.get(ct)).isWeavable()) {
		if (abc.main.Debug.v().sootClassToClassType) {
		    System.err.print("Converting polyglot type: ");
		    ClassType pct = ct;
		    while (pct.outer() != null) {
			System.err.print("inner of ");		    
			pct = pct.outer();
		    }
		    System.err.println(pct);
		}
		SootClass sc = ((RefType)soot.javaToJimple.Util.getSootType(ct)).getSootClass();
		names.put(sc.getName(), ct);
	    }
	}

	// Add all the other ones as well
	Iterator sci = Scene.v().getClasses().iterator();
	while (sci.hasNext()) {
	    SootClass sc = (SootClass)sci.next();
	    insertClassAndSuperclasses(sc, false);
	}
    }

    /*
    public void insertAllSootClassesByName(Collection scns, boolean weavable) {
	Iterator scni = scns.iterator();
	while (scni.hasNext()) {
	    String scn = (String)scni.next();
	    insertSootClass(Scene.v().getSootClass(scn), weavable);
	}
    }
    */

	/*
    public String transformClassName(String class_name) {
	return getClass(class_name).transformedName();
    }
	*/
	/*
    public void declareParent(String child, String parent) throws SemanticException {
	PCNode cn = insertClass(child, false);
	if (!cn.isWeavable()) {
	    throw new SemanticException("The class "+child+" is not weavable");
	}
	PCNode pn = insertClass(parent, false);
	cn.addParent(pn);
    }
	*/
    public Set/*<PCNode>*/ matchName(NamePattern pattern, PCNode context, Set/*<String>*/ classes, Set/*<String>*/ packages) {
	Set/*<PCNode>*/ classes_nodes = new HashSet();
	Iterator ci = classes.iterator();
	while (ci.hasNext()) {
	    String c = (String)ci.next();
	    classes_nodes.add(root.insertFullName(c, true, false));
	}
	Set/*<PCNode>*/ packages_nodes = new HashSet();
	Iterator pi = packages.iterator();
	while (pi.hasNext()) {
	    String p = (String)pi.next();
	    packages_nodes.add(root.insertFullName(p, false, false));
	}

	//System.out.println("Context: "+context);
	Set/*<PCNode>*/ nodes = pattern.match(context, classes_nodes, packages_nodes);
	//System.out.println("Nodes: "+nodes);

	Iterator ni = nodes.iterator();
	while (ni.hasNext()) {
	    PCNode n = (PCNode)ni.next();
	    if (!n.isClass()) {
		ni.remove();
	    }
	}
	//System.out.println("Nodes: "+nodes);
	return nodes;
    }

}
