
package arc.aspectj.visit;

import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.types.*;

import java.util.*;

public class HierarchyBuilder extends NodeVisitor {
    private PCStructure hierarchy;

    public HierarchyBuilder(PCStructure hierarchy) {
	this.hierarchy = hierarchy;
    }

    public NodeVisitor enter(Node n) {
	if (n instanceof ClassDecl) {
	    String name = ((ClassDecl)n).type().fullName();
	    PCNode pc = hierarchy.insertFullName(name, true, true);
	    setParents(pc, ((ClassDecl)n).type());
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
