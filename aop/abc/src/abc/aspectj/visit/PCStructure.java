
package abc.aspectj.visit;

import java.util.*;

import abc.aspectj.ast.*;

import polyglot.types.SemanticException;

import soot.*;

public class PCStructure {
    PCNode root;
    Map/*<String,PCNode>*/ classes;

    public PCStructure() {
	root = new PCNode(null, null, this);
	classes = new HashMap();
    }

    private PCNode insertFullName(String full_name, boolean cl, boolean weavable) {
	//System.out.println("Class in hierarchy ("+hashCode()+"): "+full_name+" "+cl+" "+weavable);
	String java_name = full_name.replace('$','.');
	PCNode cn = root.insertFullName(java_name, cl, weavable);
	classes.put(java_name, cn);
	return cn;
    }

    public PCNode insertClass(String full_name, boolean weavable) {
	if (containsClass(full_name)) {
	    return getClass(full_name).updateWeavable(weavable);
	} else {
	    return insertFullName(full_name, true, weavable);
	}
    }

    public PCNode getClass(String full_name) {
	String java_name = full_name.replace('$','.');
	if (!classes.containsKey(java_name)) {
	    throw new NoSuchElementException("No such class: "+full_name);
	}
	return (PCNode)classes.get(java_name);
    }

    public boolean containsClass(String full_name) {
	String java_name = full_name.replace('$','.');
	return classes.containsKey(java_name);
    }

    public PCNode insertSootClass(SootClass sc, boolean weavable) {
	//System.out.println("Inserting Soot class "+sc.getName());
	String full_name = sc.getName();
	if (containsClass(full_name)) {
	    return getClass(full_name).updateWeavable(weavable);
	} else {
	    PCNode cn = insertClass(sc.getName(), weavable);
	    if (sc.hasSuperclass()) {
		PCNode p = insertSootClass(sc.getSuperclass(), false);
		cn.addParent(p);
	    }
	    Iterator ii = sc.getInterfaces().iterator();
	    while (ii.hasNext()) {
		SootClass i = (SootClass)ii.next();
		PCNode p = insertSootClass(i, false);
		cn.addParent(p);
	    }
	    return cn;
	}
    }

    public void insertAllSootClassesByName(Collection/*<String>*/ scns, boolean weavable) {
	Iterator scni = scns.iterator();
	while (scni.hasNext()) {
	    String scn = (String)scni.next();
	    insertSootClass(Scene.v().getSootClass(scn), weavable);
	}
    }

    public String transformClassName(String class_name) {
	return getClass(class_name).transformedName();
    }

    public void declareParent(String child, String parent) throws SemanticException {
	PCNode cn = insertClass(child, false);
	if (!cn.isWeavable()) {
	    throw new SemanticException("The class "+child+" is not weavable");
	}
	PCNode pn = insertClass(parent, false);
	cn.addParent(pn);
    }

    public Set/*<PCNode>*/ matchName(NamePattern pattern, PCNode context, Set/*<String>*/ classes, Set/*<String>*/ packages) {
	Set/*<PCNode>*/ classes_nodes = new HashSet();
	Iterator ci = classes.iterator();
	while (ci.hasNext()) {
	    String c = (String)ci.next();
	    classes_nodes.add(insertFullName(c, true, false));
	}
	Set/*<PCNode>*/ packages_nodes = new HashSet();
	Iterator pi = packages.iterator();
	while (pi.hasNext()) {
	    String p = (String)pi.next();
	    packages_nodes.add(insertFullName(p, false, false));
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
