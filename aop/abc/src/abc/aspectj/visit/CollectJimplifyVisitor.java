
package abc.aspectj.visit;

import polyglot.ast.*;
import polyglot.visit.*;

import soot.*;
import soot.javaToJimple.*;

import java.util.*;

public class CollectJimplifyVisitor extends NodeVisitor {
    private Collection classes;

    public CollectJimplifyVisitor(Collection classes) {
	this.classes = classes;
    }

    public Node override(Node n) {
	if (n instanceof ClassDecl) {
	    String cname = ((ClassDecl)n).name();
	    if (Scene.v().containsClass(cname)) {
		throw new RuntimeException("Scene already contained class "+cname);
	    }
	    SootClass sc = new SootClass(cname);
	    Scene.v().addClass(sc);
	    classes.add(cname);
	    // System.out.println("Jimplify class: "+cname);
	}
	return null;
    }

}


