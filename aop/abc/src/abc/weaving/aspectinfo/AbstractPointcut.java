package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;

/** Skeleton implementation of the {@link abc.weaving.aspectinfo.Pointcut} interface.
 *  Useful when implementing new kinds of poincuts.
 */
public abstract class AbstractPointcut extends Syntax implements Pointcut {
    public AbstractPointcut(Position pos) {
	super(pos);
    }

    public boolean matchesAt(SootClass cls,SootMethod method,Stmt stmt) {
	return false;
    }
}
