package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

/** Handler for <code>call</code> shadow pointcut with a method pattern. */
public class MethodCall extends AbstractShadowPointcutHandler {
    private MethodPattern pattern;

    public MethodCall(MethodPattern pattern) {
	this.pattern = pattern;
    }

    public MethodPattern getPattern() {
	return pattern;
    }

    public boolean matchesAt(Stmt stmt) {
	if (stmt==null) return false;
	if (!(stmt instanceof InvokeStmt)) return false;
	SootMethod method = ((InvokeStmt) stmt).getInvokeExpr().getMethod();
	if(getPattern()==null) return true;
	return getPattern().matchesMethod(method);
    }

    public String toString() {
	return "call("+pattern+")";
    }
}
