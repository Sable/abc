package abc.weaving.aspectinfo;

import soot.*;

/** A field pattern. */
public interface FieldPattern {
    public boolean matchesField(SootField sf);
    public boolean matchesMethod(SootMethod sf);

    public boolean equivalent(FieldPattern p);

    public abc.aspectj.ast.FieldPattern getPattern();
}
