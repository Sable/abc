
package arc.aspectj.visit;

import java.util.*;
import java.util.regex.*;

import arc.aspectj.ast.*;

import polyglot.types.SemanticException;

public class PCStructure {
    PCNode root;

    public PCStructure() {
	root = new PCNode(null, null, this);
    }

    public PCNode insertFullName(String full_name, boolean cl, boolean weavable) {
	//System.out.println("Class in hierarchy ("+hashCode()+"): "+full_name+" "+cl+" "+weavable);
	return root.insertFullName(full_name, cl, weavable);
    }

    public void declareParent(String child, String parent) throws SemanticException {
	PCNode cn = root.insertFullName(child, true, false);
	if (!cn.isWeavable()) {
	    throw new SemanticException("The class "+child+" is not weavable");
	}
	PCNode pn = root.insertFullName(parent, true, false);
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
	    if (!n.isClass() || !n.isWeavable()) {
		ni.remove();
	    }
	}
	//System.out.println("Nodes: "+nodes);
	return nodes;
    }

    public static Pattern compilePattern(String name_pat) {
	return Pattern.compile(name_pat.replaceAll("\\*", ".*"));
    }

}
