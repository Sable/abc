/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
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

import polyglot.types.ClassType;

import java.util.*;
import java.util.regex.*;

/** A node in the internal representation of the hierarchy and
 *  package structure used by the pattern matcher.
 *  Such a node represents one package, subpackage, class or interface.
 *  @author Aske Simon Christensen
 */
public class PCNode {
    private String name;
    private Map/*<String,PCNode>*/ inners;
    private PCNode outer;
    private Set/*<PCNode>*/ parents;
    private Set/*<PCNode>*/ children;
    private boolean is_class;
    private boolean is_weavable;
    private boolean is_object;
    private PCStructure root;
    private ClassType ct;

    public PCNode(String name, PCNode outer, PCStructure root) {
	this.name = name;
	this.outer = outer;
	this.root = root;
	inners = new HashMap();
	parents = new HashSet();
	children = new HashSet();
	is_class = false;
	is_weavable = false;
	is_object = toString().equals("java.lang.Object");
    }
    
    PCNode setClassType(ClassType ct) {
	this.ct = ct;
	return this;
    }

    ClassType getClassType() {
	return ct;
    }

    PCNode insertFullName(String full_name, boolean cl, boolean weavable) {
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
	    child = new PCNode(head, this, root);
	    inners.put(head, child);
	} else {
	    child = (PCNode)inners.get(head);
	}
	if (rest != null) {
	    return child.insertFullName(rest, cl, weavable);
	} else {
	    child.is_class |= cl;
	    child.is_weavable |= weavable;
	    return child;
	}
    }

    public void addParent(PCNode parent) {
	// Polyglot thinks java.lang.Object is a superclass of itself.
	// We don't want that.
	if (!is_object) {
	    parents.add(parent);
	    parent.children.add(this);
	}
    }

    public boolean isClass() {
	return is_class;
    }

    public boolean isWeavable() {
	return is_weavable;
    }

    public PCNode updateWeavable(boolean weavable) {
	is_weavable |= weavable;
	return this;
    }

    public Set/*<PCNode>*/ getInners() {
	return new HashSet(inners.values());
    }

    public Set/*<PCNode>*/ getParents() {
	return parents;
    }

    public Set/*<PCNode>*/ getChildren() {
	return children;
    }

    public PCStructure getRoot() {
	return root;
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
    /*
    public String transformedName() {
	if (outer != null) {
	    String ps = outer.toString();
	    if (ps.equals("")) {
		return name;
	    } else {
		if (outer.isClass()) {
		    return ps+"$"+name;
		} else {
		    return ps+"."+name;
		}
	    }
	}
	return "";
    }
    */
    public Set/*<PCNode>*/ matchScope(Pattern simple_name_pattern, Set/*<PCNode>*/ classes, Set/*<PCNode>*/ packages) {
	Set this_scope = matchClass(simple_name_pattern);
	Set this_scope_names = new HashSet();
	Iterator tsi = this_scope.iterator();
	while (tsi.hasNext()) {
	    PCNode pc = (PCNode)tsi.next();
	    // Add name to shadow outer classes
	    this_scope_names.add(pc.name);
	}
	if (is_class) {
	    // Match inner classes of outer classes or same package
	    Set outer_scope = outer.matchScope(simple_name_pattern, classes, packages);
	    Iterator osi = outer_scope.iterator();
	    while (osi.hasNext()) {
		PCNode pc = (PCNode)osi.next();
		if (!this_scope_names.contains(pc.name)) {
		    this_scope.add(pc);
		    // Nothing to shadow here
		}
	    }
	} else {
	    // Match specifically imported classes
	    Iterator ci = classes.iterator();
	    while (ci.hasNext()) {
		PCNode c = (PCNode)ci.next();
		if (!this_scope_names.contains(c.name)) {
		    // Add name to list to shadow nonspecifically imported classes
		    this_scope_names.add(c.name);
		    // If it matches the pattern, add it to the list of matches
		    if (simple_name_pattern.matcher(c.name).matches()) {
			this_scope.add(c);
		    }
		}
	    }
	    // Match nonspecifically imported classes
	    Set/*<String>*/ new_names = new HashSet();
	    Iterator pi = packages.iterator();
	    while (pi.hasNext()) {
		PCNode p = (PCNode)pi.next();
		Set/*<PCNode>*/ p_matches = p.matchSpecific(simple_name_pattern);
		Iterator pci = p_matches.iterator();
		while (pci.hasNext()) {
		    PCNode pc = (PCNode)pci.next();
		    if (!this_scope_names.contains(pc.name)) {
			// Nonspecifically imported classes do not shadow each other,
			// but they may shadow toplevel packages
			new_names.add(pc.name);
			// If it matches the pattern, add it to the list of matches
			if (simple_name_pattern.matcher(pc.name).matches()) {
			    this_scope.add(pc);
			}
		    }
		}
	    }
	    this_scope_names.addAll(new_names);

	    // Finally, match toplevel classes and packages
	    Iterator tli = root.root.matchSpecific(simple_name_pattern).iterator();
	    while (tli.hasNext()) {
		PCNode tl = (PCNode)tli.next();
		if (!this_scope_names.contains(tl.name)) {
		    // If it matches the pattern, add it to the list of matches
		    if (simple_name_pattern.matcher(tl.name).matches()) {
			this_scope.add(tl);
		    }
		}
	    }

	}
	if (abc.main.Debug.v().namePatternProcessing)
	    System.out.println(this+".matchScope "+simple_name_pattern.pattern()+": "+this_scope);
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
	    //System.out.println("Parent: "+parent);
	    Set parent_class = parent.matchClass(simple_name_pattern);
	    Iterator osi = parent_class.iterator();
	    while (osi.hasNext()) {
		PCNode pc = (PCNode)osi.next();
		if (!this_class_names.contains(pc.name)) {
		    this_class.add(pc);
		}
	    }
	}
	if (abc.main.Debug.v().namePatternProcessing)
	    System.out.println(this+".matchClass "+simple_name_pattern.pattern()+": "+this_class);
	return this_class;
    }

    public Set/*<PCNode>*/ matchSpecific(Pattern simple_name_pattern) {
	Set matches = new HashSet();
	Iterator ii = inners.keySet().iterator();
	while (ii.hasNext()) {
	    String inner = (String)ii.next();
	    //System.out.print("Matching "+inner+" against "+simple_name_pattern.pattern()+": ");
	    if (simple_name_pattern.matcher(inner).matches()) {
		//System.out.println("true");
		matches.add(inners.get(inner));
	    } else {
		//System.out.println("false");
	    }
	}
	return matches;
    }

}
