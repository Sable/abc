package abc.weaving.weaver;

import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import abc.weaving.weaver.*;
import abc.main.AbcTimer;

/** Saves all method bodies so that they can be unwoven after weaving.
 * @author Ondrej Lhotak
 * @date August 3, 2004
 */

public class Unweaver {
    private static void debug(String message) {
        if (abc.main.Debug.v().unweaver) 
            System.err.println("UNWEAVER ***** " + message);
    }	

    Map savedBodies;

    /** Save Jimple bodies of all weavable classes to be restored later. */
    public void save() {
        savedBodies = new HashMap();
        for( Iterator abcClassIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); abcClassIt.hasNext(); ) {
            final AbcClass abcClass = (AbcClass) abcClassIt.next();
            SootClass cl = abcClass.getSootClass();
            debug( "saving "+cl );
            for( Iterator mIt = cl.getMethods().iterator(); mIt.hasNext(); ) {
                final SootMethod m = (SootMethod) mIt.next();
                if( m.hasActiveBody() ) {
                    debug( "saving body of "+m );
                    savedBodies.put( m, m.getActiveBody().clone() );
                } else {
                    debug( ""+m+" has no active body" );
                }
            }
        }
    }

    /** Restore saved bodies to their original methods. */
    public void restore() {
        for( Iterator mIt = savedBodies.keySet().iterator(); mIt.hasNext(); ) {
            final SootMethod m = (SootMethod) mIt.next();
            debug( "restoring body of "+m );
            m.setActiveBody( (Body) savedBodies.get(m) );
        }
    }
}
