package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

/** Handler for an instance of a specific kind of shadow pointcut.
 *  Each joinpoint shadow will have one shadow type, so there will be
 *  exactly one implementation of {@link abc.weaving.aspectinfo.ShadowType}
 *  for each implementation of {@link abc.weaving.aspectinfo.ShadowPointcutHandler}.
 *  @author Ganesh Sittampalam
 *  @date 28-Apr-04
 */
public interface ShadowPointcutHandler {
    public ShadowType getShadowType();

    /** Given the current statement return false if we 
	don't match at the current shadow, and true if we do
	If stmt is null, shadow is the entire execution
     */
    public boolean matchesAt(Stmt current);
}
