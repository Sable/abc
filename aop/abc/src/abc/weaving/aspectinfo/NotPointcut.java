package arc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

public class NotPointcut extends AbstractPointcut {
    private Pointcut pc;

    public NotPointcut(Pointcut pc, Position pos) {
	super(pos);
	this.pc = pc;
    }

    public Pointcut getPointcut() {
	return pc;
    }
}
