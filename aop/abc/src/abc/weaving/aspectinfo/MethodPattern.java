package abc.weaving.aspectinfo;

import soot.*;

/** A method pattern. */
public interface MethodPattern {
    public boolean matchesMethodRef(SootMethodRef smr);

    public boolean equivalent(MethodPattern p);

    public abc.aspectj.ast.MethodPattern getPattern();
}
