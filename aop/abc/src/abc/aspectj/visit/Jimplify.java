/* Abc - The AspectBench Compiler
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL; 
 * if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.aspectj.visit;

import polyglot.frontend.*;
import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.util.InternalCompilerError;

import soot.*;
import soot.javaToJimple.*;

import java.util.*;

import abc.main.AbcTimer;

public class Jimplify extends OncePass {
    private Map class_to_ast;

    public Jimplify(Pass.ID id, Map class_to_ast) {
	super(id);
	this.class_to_ast = class_to_ast;
    }

    public void once() {
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
	    SootResolver.v().resolveClass(className, SootClass.BODIES);
	}

	long aftertime = System.currentTimeMillis();
	AbcTimer.addToSootResolve(aftertime-beforetime);
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
        public List resolve( SootClass sc ) {
            List ret;
	    try {
		if (abc.main.Debug.v().classResolving)
		    System.err.println("resolving [from abc AST]: " + className );

		String javaClassName = SourceLocator.v().getSourceForClass(className);
		Node n = (Node) class_to_ast.get(javaClassName);
		InitialResolver.v().setAst(n);
		InitialResolver.v().resolveAST();
        ret = InitialResolver.v().resolveFromJavaFile(sc);
		sc.setApplicationClass();
	    } catch(InternalCompilerError e) {
		throw new InternalCompilerError(e.message()+" while resolving "+sc.getName(),
						e.position(),
						e.getCause());
	    } catch(Throwable e) {
		throw new InternalCompilerError("exception while resolving "+sc.getName(),e);
	    }
            return ret;
        }
    }
}
