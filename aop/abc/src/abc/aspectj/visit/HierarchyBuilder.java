
package abc.aspectj.visit;

import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.types.*;

import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.AbcClass;

import java.util.*;

public class HierarchyBuilder extends NodeVisitor {
    private PCStructure hierarchy;
    private Collection weavable_classes;

    public HierarchyBuilder(PCStructure hierarchy, Collection weavable_classes) {
	this.hierarchy = hierarchy;
	this.weavable_classes = weavable_classes;
    }

    public NodeVisitor enter(Node n) {
	if (n instanceof ClassDecl) {
	    String name = ((ClassDecl)n).type().fullName();
	    System.out.println("Weavable class: "+name);
	    PCNode pc = hierarchy.insertFullName(name, true, true);
	    setParents(pc, ((ClassDecl)n).type());
	    weavable_classes.add(name);
	    GlobalAspectInfo.v().addClass(new AbcClass(name));
	    return this;
	}
	return this;
    }

    private void setParents(PCNode pc, ClassType t) {
	ClassType st = (ClassType)t.superType();
	if (st != null) {
	    PCNode scpc = hierarchy.insertFullName(st.fullName(), true, false);
	    pc.addParent(scpc);
	    setParents(scpc, (ClassType)st);
	}
	Iterator iii = t.interfaces().iterator();
	while (iii.hasNext()) {
	    ClassType ii = (ClassType)iii.next();
	    PCNode iipc = hierarchy.insertFullName(ii.fullName(), true, false);
	    pc.addParent(iipc);
	    setParents(iipc, (ClassType)ii);
	}

    }

}
