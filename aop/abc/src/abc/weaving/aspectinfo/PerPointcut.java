package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** Base class for a per clause that contains a pointcut. */
public abstract class PerPointcut extends AbstractPer {
    private Pointcut pc;

    public PerPointcut(Pointcut pc, Position pos) {
	super(pos);
	this.pc = pc;
    }

    public Pointcut getPointcut() {
	return pc;
    }
}
