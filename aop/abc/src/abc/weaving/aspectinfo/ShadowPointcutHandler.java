package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.MethodPosition;
import abc.weaving.residues.Residue;

/** Handler for an instance of a specific kind of shadow pointcut.
 *  Each joinpoint shadow will have one shadow type, so there will be
 *  exactly one instance of {@link abc.weaving.aspectinfo.ShadowType}
 *  for each implementation of {@link abc.weaving.aspectinfo.ShadowPointcutHandler}.
 *  @author Ganesh Sittampalam
 *  @date 28-Apr-04
 */
public interface ShadowPointcutHandler {
    /** Get the ShadowType instance that corresponds to this shadow type */
    public ShadowType getShadowType();

    /** Given the method position return null if we 
     *  don't match, and residue if we do. Checking that
     *  we are at the right shadow type has already been
     *  done for us.
     */
    public Residue matchesAt(MethodPosition position);
}
