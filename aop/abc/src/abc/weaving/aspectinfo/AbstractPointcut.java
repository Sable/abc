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


    /* This should all be removed when all the subclasses implement it */
    private boolean warnedUnimplemented=false;

    public boolean matchesAt(SootClass cls,SootMethod method,Stmt stmt) {
	if(!warnedUnimplemented) {
	    System.out.println("returning false for an unimplemented pointcut "+this.getClass());
	    warnedUnimplemented=true;
	}
	return false;
    }
}
