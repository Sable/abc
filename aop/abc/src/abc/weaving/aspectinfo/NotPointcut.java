package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;

/** Pointcut negation. */
public class NotPointcut extends AbstractPointcut {
    private Pointcut pc;

    public NotPointcut(Pointcut pc, Position pos) {
	super(pos);
	this.pc = pc;
    }

    public Pointcut getPointcut() {
	return pc;
    }

    public boolean matchesAt(SootClass cls,SootMethod method,Stmt stmt) {
	return !pc.matchesAt(cls,method,stmt);
    }
}
