package arc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** A pointcut designator representing a set of joinpoint shadows
 *  at which the pointcut will match.
 */
public class ShadowPointcut extends AbstractPointcut {
    private ShadowPointcutHandler handler;

    public ShadowPointcut(ShadowPointcutHandler handler, Position pos) {
	super(pos);
	this.handler = handler;
    }
}
