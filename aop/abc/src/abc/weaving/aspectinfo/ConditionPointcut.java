package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;

/** A pointcut designator representing a runtime condition
 *  that must be satisfied for the pointcut to match.
 */
public class ConditionPointcut extends AbstractPointcut {
    private ConditionPointcutHandler handler;

    public ConditionPointcut(ConditionPointcutHandler handler, Position pos) {
	super(pos);
	this.handler = handler;
    }

    public boolean matchesAt(SootClass cls,SootMethod method,Stmt stmt) {
	return handler.matchesAt(cls,method);
    }
}
