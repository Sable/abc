package arc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** A <code>percflowbelow</code> per clause. */
public class PerCflowBelow extends PerPointcut {
    public PerCflowBelow(Pointcut pc, Position pos) {
	super(pc, pos);
    }
}
