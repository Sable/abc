
package abc.aspectj.visit;

import java.util.*;

import abc.aspectj.ast.*;

import abc.weaving.aspectinfo.AbcFactory;

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

    static {
	reset();
    }

    PCNode root;
    PCNode dummy;
    Map/*<ClassType,PCNode>*/ classes;
    boolean autosootify = false;

    private PCStructure() {
	root = new PCNode(null, null, this);
	dummy = new PCNode(null, null, this);
	classes = new HashMap();
	v = this;
    }

    public static void reset() {
	v = new PCStructure();
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
	    if (autosootify) {
		AbcFactory.classTypeToSootClass(ct);
	    }
	    return cn;
	}
    }

    public Collection getClassTypes() {
	return classes.keySet();
    }

    public PCNode getClass(ClassType ct) {
	if (!classes.containsKey(ct)) {
	    PCNode cn = insertClassAndSuperclasses(ct, false);
	    classes.put(ct, cn);
	    return cn;
	}
	return (PCNode)classes.get(ct);
    }

    public PCNode getClass(SootClass sc) {
	return getClass(AbcFactory.sootClassToClassType(sc));
    }

    public PCNode insertClassAndSuperclasses(ClassType ct, boolean weavable) {
	if (classes.containsKey(ct)) {
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
	return insertClassAndSuperclasses(AbcFactory.sootClassToClassType(sc), weavable);
    }

    public void updateWithAllSootClasses() {
	Iterator cti = getClassTypes().iterator();
	while (cti.hasNext()) {
	    ClassType ct = (ClassType)cti.next();
	    AbcFactory.classTypeToSootClass(ct);
	}

	autosootify = true;

	Iterator sci = Scene.v().resolvedClasses().iterator();
	while (sci.hasNext()) {
	    SootClass sc = (SootClass)sci.next();
	    insertClassAndSuperclasses(sc, false);
	}
    }


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
