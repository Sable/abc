
package arc.aspectj.visit;

import polyglot.ast.*;
import polyglot.visit.*;

import java.util.*;

public class HierarchyBuilder extends NodeVisitor {
    private PCStructure hierarchy;

    public HierarchyBuilder(PCStructure hierarchy) {
	this.hierarchy = hierarchy;
    }

    public NodeVisitor enter(Node n) {
	if (n instanceof ClassDecl) {
	    String name = ((ClassDecl)n).type().fullName();
	    hierarchy.insertFullName(name, true, true);
	    return this;
	}
	return this;
    }

}
