package abc.weaving.aspectinfo;

import soot.*;

/** Skeleton implementation of the {@link abc.weaving.aspectinfo.ConditionPointcutHandler} interface.
 *  Useful when implementing condition pointcut handlers.
 */
public abstract class AbstractConditionPointcutHandler implements ConditionPointcutHandler {
    /* remove this once all deriving classes implement it */
    public boolean matchesAt(SootClass cls,SootMethod method) {
	System.out.println("Returning false for unimplemented condition pointcut type "+this.getClass());
	return false;
    }
}
