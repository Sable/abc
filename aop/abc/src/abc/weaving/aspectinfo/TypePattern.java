package abc.weaving.aspectinfo;

import soot.*;

/** A type pattern. */
public interface TypePattern {
    public boolean matchesType(Type cl);

    public abc.aspectj.ast.TypePatternExpr getPattern();

    public boolean equivalent(TypePattern p);
}
