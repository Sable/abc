
package abc.aspectj.visit;

import polyglot.ast.*;
import polyglot.visit.*;

import soot.*;
import soot.javaToJimple.*;

import java.util.*;

import abc.main.AbcTimer;

public class JimplifyVisitor extends NodeVisitor {
    private Collection classes;
    private PCStructure hierarchy;
    private SootResolver soot_res =  SootResolver.v();
    private InitialResolver res = SootResolver.v().getInitSourceResolver();

    public JimplifyVisitor(Collection classes, PCStructure hierarchy) {
	this.classes = classes;
	this.hierarchy = hierarchy;
    }

    public Node override(Node n) {
	if (n instanceof SourceFile) {
	    res.setAst(n);
	}
	if (n instanceof ClassDecl) {
	    String cname = ((ClassDecl)n).type().fullName();
	    cname = hierarchy.transformClassName(cname);
	    res.resolveFromJavaFile(Scene.v().getSootClass(cname), soot_res);
            Scene.v().getSootClass(cname).setApplicationClass();
	    // System.out.println("Jimplified class: "+cname);
	}
	return null;
    }

    public void finish(Node n) {
        // NOTE: if you move where the resolveClassAndSupportClasses is
        //   called,  please also move the timer code with it. LJH
	long beforetime = System.currentTimeMillis();
	// FIXME: I think we can do better than this????
	soot_res.resolveClassAndSupportClasses("java.lang.Object");
	long aftertime = System.currentTimeMillis();
	AbcTimer.addToSootResolve(aftertime-beforetime);
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


