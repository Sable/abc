
package polyglot.ext.aspectj.visit;

import polyglot.ast.*;
import polyglot.visit.*;

import soot.*;
import soot.javaToJimple.*;

import java.util.*;

public class JimplifyVisitor extends NodeVisitor {
    private HashSet classes = new HashSet();
    private SootResolver soot_res = new AspectSootResolver();
    private InitialResolver res = new InitialResolver();

    public Node override(Node n) {
	if (n instanceof ClassDecl) {
	    String cname = ((ClassDecl)n).name();
	    classes.add(cname);
	}
	return null;
    }

    public void finish(Node n) {
	res.setAst(n);

	Iterator ci = classes.iterator();
	while (ci.hasNext()) {
	    String cname = (String)ci.next();
	    SootClass sc = new SootClass(cname);
	    Scene.v().addClass(sc);
	    res.resolveFromJavaFile(sc, soot_res);
	}
    }

    private class AspectSootResolver extends SootResolver {
	public SootClass getResolvedClass(String classname) {
	    if (classes.contains(classname)) {
		SootClass sc = new SootClass(classname);
		Scene.v().addClass(sc);
		res.resolveFromJavaFile(sc, soot_res);
		return sc;
	    }
	    return AspectSootResolver.super.getResolvedClass(classname);
	}
    }
}


