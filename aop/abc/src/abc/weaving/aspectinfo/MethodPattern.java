package abc.weaving.aspectinfo;

import soot.*;

/** A method pattern. */
public interface MethodPattern {
    public boolean matchesCall(SootMethodRef smr);

    public boolean matchesExecution(SootMethod sm);

    public boolean equivalent(MethodPattern p);

    public abc.aspectj.ast.MethodPattern getPattern();
}
