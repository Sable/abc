
package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

/** A pointcut designator. */
public interface Pointcut {
    /** quick hack, needs to change **/
    public boolean matchesAt(SootClass cls,SootMethod method,Stmt stmt);
}
