
package arc.aspectj.visit;

import java.util.*;
import java.util.regex.*;

public class PCNode {
    private String name;
    private Map/*<String,PCNode>*/ inners;
    private PCNode outer;
    private Set/*<PCNode>*/ parents;
    private Set/*<PCNode>*/ children;
    boolean is_class;

    public PCNode(String name, PCNode outer) {
	this.name = name;
	this.outer = outer;
	inners = new HashMap();
	parents = new HashSet();
	children = new HashSet();
	is_class = false;
    }
    
    public PCNode insertFullName(String full_name) {
	int dotpos = full_name.indexOf('.');
	String head;
	String rest;
	if (dotpos == -1) {
	    head = full_name;
	    rest = null;
	} else {
	    head = full_name.substring(0,dotpos);
	    rest = full_name.substring(dotpos+1);
	}
	PCNode child;
	if (!inners.containsKey(head)) {
	    child = new PCNode(head, this);
	    inners.put(head, child);
	} else {
	    child = (PCNode)inners.get(head);
	}
	if (rest != null) {
	    return child.insertFullName(rest);
	} else {
	    child.is_class = true;
	    return child;
	}
    }

    public void addParent(PCNode parent) {
	parents.add(parent);
	parent.children.add(this);
    }

    public boolean isClass() {
	return is_class;
    }

    public Set/*<PCNode>*/ getInners() {
	return new HashSet(inners.values());
    }

    public Set/*<PCNode>*/ getParents() {
	return parents;
    }

    public String toString() {
	if (outer != null) {
	    String ps = outer.toString();
	    if (ps.equals("")) {
		return name;
	    } else {
		return ps+"."+name;
	    }
	}
	return "";
    }

    public Set/*<PCNode>*/ matchScope(Pattern simple_name_pattern) {
	Set this_scope = matchClass(simple_name_pattern);
	Set this_scope_names = new HashSet();
	Iterator tsi = this_scope.iterator();
	while (tsi.hasNext()) {
	    PCNode pc = (PCNode)tsi.next();
	    this_scope_names.add(pc.name);
	}
	if (outer != null) {
	    Set outer_scope = outer.matchScope(simple_name_pattern);
	    Iterator osi = outer_scope.iterator();
	    while (osi.hasNext()) {
		PCNode pc = (PCNode)osi.next();
		if (!this_scope_names.contains(pc.name)) {
		    this_scope.add(pc);
		}
	    }
	}
	return this_scope;
    }

    public Set/*<PCNode>*/ matchClass(Pattern simple_name_pattern) {
	Set this_class = matchSpecific(simple_name_pattern);
	Set this_class_names = new HashSet();
	Iterator tsi = this_class.iterator();
	while (tsi.hasNext()) {
	    PCNode pc = (PCNode)tsi.next();
	    this_class_names.add(pc.name);
	}
	Iterator pi = parents.iterator();
	while (pi.hasNext()) {
	    PCNode parent = (PCNode)pi.next();
	    Set parent_class = parent.matchClass(simple_name_pattern);
	    Iterator osi = parent_class.iterator();
	    while (osi.hasNext()) {
		PCNode pc = (PCNode)osi.next();
		if (!this_class_names.contains(pc.name)) {
		    this_class.add(pc);
		}
	    }
	}
	return this_class;
    }

    public Set/*<PCNode>*/ matchSpecific(Pattern simple_name_pattern) {
	Set matches = new HashSet();
	Iterator ii = inners.keySet().iterator();
	while (ii.hasNext()) {
	    String inner = (String)ii.next();
	    if (simple_name_pattern.matcher(inner).matches()) {
		matches.add(inners.get(inner));
	    }
	}
	return matches;
    }

}
