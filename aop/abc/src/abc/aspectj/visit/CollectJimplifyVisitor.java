
package abc.aspectj.visit;

import polyglot.ast.*;
import polyglot.visit.*;

import soot.*;
import soot.javaToJimple.*;

import java.util.*;

public class CollectJimplifyVisitor extends NodeVisitor {
    private Collection classes;
    private PCStructure hierarchy;

    public CollectJimplifyVisitor(Collection classes, PCStructure hierarchy) {
	this.classes = classes;
	this.hierarchy = hierarchy;
    }

    public Node override(Node n) {
        /*
	if (n instanceof ClassDecl) {
	    String cname = ((ClassDecl)n).type().fullName();
	    cname = hierarchy.transformClassName(cname);
	    if (Scene.v().containsClass(cname)) {
		throw new RuntimeException("Scene already contained class "+cname);
	    }
	    SootClass sc = new SootClass(cname);
	    Scene.v().addClass(sc);
	    classes.add(cname);
	    // System.out.println("Jimplify class: "+cname);
	}
        */
	return null;
    }

}


