package abc.weaving.aspectinfo;

import soot.*;

/** A constructor pattern. */
public interface ConstructorPattern {
    public boolean matchesConstructorRef(SootMethodRef scr);

    public boolean equivalent(ConstructorPattern p);

    public abc.aspectj.ast.ConstructorPattern getPattern();
}
