package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

/** A pointcut designator.
 *  @author Ganesh Sittampalam
 *  @date 28-Apr-04
 */
public interface Pointcut {
    /** Given a statement + context, do we match? 
     *  If stmt is null, then shadow is the entire execution
     */
    public Residue matchesAt(WeavingEnv env,
			     SootClass cls,
			     SootMethod method,
			     ShadowMatch sm);
}
