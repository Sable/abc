package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;

/** Pointcut disjunction. */
public class OrPointcut extends AbstractPointcut {
    private Pointcut pc1;
    private Pointcut pc2;

    public OrPointcut(Pointcut pc1, Pointcut pc2, Position pos) {
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

    public boolean matchesAt(SootClass cls,SootMethod method,Stmt stmt) {
	return pc1.matchesAt(cls,method,stmt) || pc2.matchesAt(cls,method,stmt);
    }
}
