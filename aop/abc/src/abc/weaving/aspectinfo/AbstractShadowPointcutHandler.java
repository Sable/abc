package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

/** Skeleton implementation of the {@link abc.weaving.aspectinfo.ShadowPointcutHandler} interface.
 *  Useful when implementing shadow pointcut handlers.
 */
public abstract class AbstractShadowPointcutHandler implements ShadowPointcutHandler {

    public ShadowType getShadowType() {
	// FIXME
	try {
	    return (ShadowType) Class.forName(getClass().toString()+"Type").newInstance();
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    public boolean matchesAt(SootClass cls,SootMethod method,Stmt stmt) {
	return false;
    }

}
