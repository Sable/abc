package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** A <code>percflow</code> per clause. */
public class PerCflow extends PerPointcut {
    public PerCflow(Pointcut pc, Position pos) {
	super(pc, pos);
    }
}
