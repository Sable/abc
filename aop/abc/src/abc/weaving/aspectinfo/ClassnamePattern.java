package abc.weaving.aspectinfo;

import soot.*;

/** A classname pattern. */
public interface ClassnamePattern {
    public boolean matchesClass(SootClass cl);
}
