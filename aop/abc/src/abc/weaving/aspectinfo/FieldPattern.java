package abc.weaving.aspectinfo;

import soot.*;

/** A field pattern. */
public interface FieldPattern {
    public boolean matchesField(SootField sf);
}
