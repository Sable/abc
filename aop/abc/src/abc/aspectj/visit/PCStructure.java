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

import java.util.*;

import abc.aspectj.ast.*;

import abc.weaving.aspectinfo.AbcFactory;

import polyglot.types.SemanticException;
import polyglot.types.ClassType;
import polyglot.types.Resolver;
import polyglot.util.InternalCompilerError;

import soot.*;

/** The internal representation of the class hierarchy and
 *  package structure used by the pattern matcher.
 *  @author Aske Simon Christensen
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
		cn = root.insertFullName(ct.fullName(), true, weavable).setClassType(ct);
	    } else {
		cn = new PCNode(null, null, this).updateWeavable(weavable).setClassType(ct);
	    }
	    classes.put(ct, cn);
	    if (autosootify) {
		AbcFactory.classTypeToSootClass(ct);
	    }
	    return cn;
	}
    }

    public Collection getClassTypes() {
	// Return all the classes in topological order
	List/*<ClassType>*/ ctlist = new ArrayList();
	LinkedList/*<PCNode>*/ queue = new LinkedList();
	Map/*<PCNode,Integer>*/ nparents = new HashMap();
	Iterator cti = classes.keySet().iterator();
	while (cti.hasNext()) {
	    ClassType ct = (ClassType)cti.next();
	    PCNode cn = getClass(ct);
	    int np = cn.getParents().size();
	    if (np == 0) {
		queue.addLast(cn);
	    } else {
		nparents.put(cn, new Integer(np));
	    }
	}
	
	while (!queue.isEmpty()) {
	    PCNode cn = (PCNode)queue.removeFirst();
	    ctlist.add(cn.getClassType());
	    Iterator chi = cn.getChildren().iterator();
	    while (chi.hasNext()) {
		PCNode ch = (PCNode)chi.next();
		int np = ((Integer)nparents.get(ch)).intValue();
		if (np == 1) {
		    nparents.remove(ch);
		    queue.addLast(ch);
		} else {
		    nparents.put(ch, new Integer(np-1));
		}
	    }
	}
	if (!nparents.isEmpty()) {
	    throw new InternalCompilerError("Error in topological sort -- cyclic hierarchy");
	}

	return ctlist;
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

	Iterator sci = Scene.v().getClasses(SootClass.HIERARCHY).iterator();
	while (sci.hasNext()) {
	    SootClass sc = (SootClass)sci.next();
            if(!sc.hasTag("SyntheticTag")) {
                // Synthetic classes should be ignored
                insertClassAndSuperclasses(sc, false);
            }
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
