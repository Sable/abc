package abc.weaving.aspectinfo;

import soot.*;

/** A method pattern. */
public interface MethodPattern {
    public boolean matchesMethod(SootMethod sm);
}
