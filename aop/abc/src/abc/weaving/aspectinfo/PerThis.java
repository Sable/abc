package arc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** A <code>perthis</code> per clause. */
public class PerThis extends PerPointcut {
    public PerThis(Pointcut pc, Position pos) {
	super(pc, pos);
    }
}
