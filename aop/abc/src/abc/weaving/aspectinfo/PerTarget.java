package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** A <code>pertarget</code> per clause. */
public class PerTarget extends PerPointcut {
    public PerTarget(Pointcut pc, Position pos) {
	super(pc, pos);
    }
}
