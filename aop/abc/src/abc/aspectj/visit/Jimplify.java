
package abc.aspectj.visit;

import polyglot.frontend.*;
import polyglot.ast.*;
import polyglot.visit.*;

import soot.*;
import soot.javaToJimple.*;

import java.util.*;

import abc.main.AbcTimer;

public class Jimplify extends AbstractPass {
    private Map class_to_ast;

    private boolean has_been_run = false;

    private InitialResolver res = SootResolver.v().getInitSourceResolver();

    public Jimplify(Pass.ID id, Map class_to_ast) {
	super(id);
	this.class_to_ast = class_to_ast;
    }

    public boolean run() {
	if (!has_been_run) {
	    long beforetime = System.currentTimeMillis();

	    List classProviders = new LinkedList();
	    classProviders.add( new AbcClassProvider() );
	    classProviders.add( new CoffiClassProvider() );
	    SourceLocator.v().setClassProviders(classProviders);

	    // NOTE: if you move where the resolveClassAndSupportClasses is
	    //   called,  please also move the timer code with it. LJH
	    for( Iterator classNameIt = class_to_ast.keySet().iterator(); classNameIt.hasNext(); ) {
	        final String className = (String) classNameIt.next();
		if (abc.main.Debug.v().classResolving)
		    System.err.println("Resolving class "+className);
		SootResolver.v().resolveClassAndSupportClasses(className);
	    }

	    long aftertime = System.currentTimeMillis();
	    AbcTimer.addToSootResolve(aftertime-beforetime);

	    has_been_run = true;
	}
	return true;
    }

    private class AbcClassProvider implements ClassProvider {
        public ClassSource find( String className ) {
            String javaClassName = SourceLocator.v().getSourceForClass(className);
            if( !class_to_ast.containsKey(javaClassName) ) {
                return null;
            }
            return new AbcClassSource(className);
        }
    }

    private class AbcClassSource extends ClassSource {
        AbcClassSource( String className ) {
            super(className);
        }
        public void resolve( SootClass sc ) {
	    if (abc.main.Debug.v().classResolving)
		System.err.println("resolving [from abc AST]: " + className );

            String javaClassName = SourceLocator.v().getSourceForClass(className);
            Node n = (Node) class_to_ast.get(javaClassName);
	    res.setAst(n);
	    res.resolveFromJavaFile(sc);
            sc.setApplicationClass();
        }
    }
}


