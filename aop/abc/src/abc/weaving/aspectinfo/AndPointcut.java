package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** Pointcut conjunction. */
public class AndPointcut extends AbstractPointcut {
    private Pointcut pc1;
    private Pointcut pc2;

    public AndPointcut(Pointcut pc1, Pointcut pc2, Position pos) {
	super(pos);
	this.pc1 = pc1;
	this.pc2 = pc2;
    }

    public Pointcut getLeftPointcut() {
	return pc1;
    }

    public Pointcut getRightPointcut() {
	return pc2;
    }
}
