package abc.weaving.aspectinfo;

import soot.*;

/** Handler for an instance of a specific kind of condition pointcut. */
public interface ConditionPointcutHandler {
    /** Checks whether a pointcut matches at this class+method */
    public abstract boolean matchesAt(SootClass cls,SootMethod method);
}
