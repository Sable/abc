
package abc.weaving.aspectinfo;

import polyglot.util.Position;

/** A <code>declare soft</code> declaration. */
public class DeclareSoft extends InAspect {
    private AbcType exc;
    private Pointcut pc;

    public DeclareSoft(AbcType exc, Pointcut pc, Aspect aspct, Position pos) {
	super(aspct, pos);
	this.exc = exc;
	this.pc = pc;
    }

    /** Get the softened exception. */
    public AbcType getException() {
	return exc;
    }

    /** Get the pointcut indicating places to soften. */
    public Pointcut getPointcut() {
	return pc;
    }
}
