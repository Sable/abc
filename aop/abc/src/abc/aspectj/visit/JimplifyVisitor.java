
package abc.aspectj.visit;

import polyglot.ast.*;
import polyglot.visit.*;

import soot.*;
import soot.javaToJimple.*;

import java.util.*;

public class JimplifyVisitor extends NodeVisitor {
    private HashSet classes = new HashSet();
    private SootResolver soot_res = new SootResolver();
    private InitialResolver res = new InitialResolver();

    public Node override(Node n) {
	if (n instanceof ClassDecl) {
	    String cname = ((ClassDecl)n).name();
	    if (Scene.v().containsClass(cname)) {
		throw new RuntimeException("Scene already contained class "+cname);
	    }
	    SootClass sc = new SootClass(cname);
	    Scene.v().addClass(sc);
	    classes.add(cname);
	    System.out.println("Jimplify class: "+cname);
	}
	return null;
    }

    public void finish(Node n) {
	res.setAst(n);

	Iterator ci = classes.iterator();
	while (ci.hasNext()) {
	    String cname = (String)ci.next();
	    res.resolveFromJavaFile(Scene.v().getSootClass(cname), soot_res);
            Scene.v().getSootClass(cname).setApplicationClass();
	    System.out.println("Jimplified class: "+cname);
	}
	soot_res.resolveClassAndSupportClasses("java.lang.Object");
	System.out.println("Jimplification completed");
    }
/*
    private class AspectSootResolver extends SootResolver {
	public SootClass getResolvedClass(String classname) {
	    if (classes.contains(classname)) {
		SootClass sc = new SootClass(classname);
		Scene.v().addClass(sc);
		res.resolveFromJavaFile(sc, soot_res);
		System.out.println("Jimplified class (indirectly): "+classname);
		return sc;
	    }
	    return AspectSootResolver.super.getResolvedClass(classname);
	}
    }
*/
}


