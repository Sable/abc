package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;

/** A pointcut designator representing a set of joinpoint shadows
 *  at which the pointcut will match.
 */
public class ShadowPointcut extends AbstractPointcut {
    private ShadowPointcutHandler handler;

    public ShadowPointcut(ShadowPointcutHandler handler, Position pos) {
	super(pos);
	this.handler = handler;
    }

    public boolean matchesAt(SootClass cls,SootMethod method,Stmt stmt) {
	return handler.matchesAt(cls,method,stmt);
    }

    public String toString() {
	return handler.toString();
    }
}
