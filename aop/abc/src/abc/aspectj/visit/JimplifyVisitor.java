
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
    private InitialResolver res = SootResolver.v().getInitSourceResolver();
    private Map classToAST = new HashMap();
    private Node currentAST = null;

    public JimplifyVisitor(Collection classes, PCStructure hierarchy) {
	this.classes = classes;
	this.hierarchy = hierarchy;

        List classProviders = new LinkedList();
        classProviders.add( new AbcClassProvider() );
        classProviders.add( new CoffiClassProvider() );
        SourceLocator.v().setClassProviders(classProviders);
    }

    public Node override(Node n) {
	if (n instanceof SourceFile) {
            currentAST = n;
	}
	if (n instanceof ClassDecl) {
	    String cname = ((ClassDecl)n).type().fullName();
	    cname = hierarchy.transformClassName(cname);

            classToAST.put(cname, currentAST);
        }
        /*
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
        */
	return null;
    }

    public void finish(Node n) {
        // NOTE: if you move where the resolveClassAndSupportClasses is
        //   called,  please also move the timer code with it. LJH
	long beforetime = System.currentTimeMillis();
        for( Iterator classNameIt = classToAST.keySet().iterator(); classNameIt.hasNext(); ) {
            final String className = (String) classNameIt.next();
            SootResolver.v().resolveClassAndSupportClasses(className);
        }
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
    private class AbcClassProvider implements ClassProvider {
        public ClassSource find( String className ) {
            if( !classes.contains(className) ) return null;
            return new AbcClassSource(className);
        }
    }

    private class AbcClassSource extends ClassSource {
        AbcClassSource( String className ) {
            super(className);
        }
        public void resolve( SootClass sc ) {
            Node n = (Node) classToAST.get(className);
            if( n == null ) {
                throw new RuntimeException( "Frontend bug: The class "+className+" was listed in the parameter classes to JimplifyVisitor, but there's no AST for it." );
            }
	    res.setAst(n);
	    res.resolveFromJavaFile(sc);
            sc.setApplicationClass();
        }
    }
}


