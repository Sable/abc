
package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

/** A pointcut designator.
 *  @author Ganesh Sittampalam
 *  @date 28-Apr-04
 */
public interface Pointcut {
    /** Given a statement + context, do we match? */
    public boolean matchesAt(SootClass cls,SootMethod method,Stmt stmt);
}
