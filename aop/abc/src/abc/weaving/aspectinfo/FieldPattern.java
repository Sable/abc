package abc.weaving.aspectinfo;

import soot.*;

/** A field pattern. */
public interface FieldPattern {
    public boolean matchesFieldRef(SootFieldRef sfr);
    public boolean matchesMethod(SootMethod sf);

    public boolean equivalent(FieldPattern p);

    public abc.aspectj.ast.FieldPattern getPattern();
}
