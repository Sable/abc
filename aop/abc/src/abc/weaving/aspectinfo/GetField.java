package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

/** Handler for <code>get</code> shadow pointcut. */
public class GetField extends AbstractShadowPointcutHandler {
    private FieldPattern pattern;

    public GetField(FieldPattern pattern) {
	this.pattern = pattern;
    }

    public FieldPattern getPattern() {
	return pattern;
    }

    public boolean matchesAt(Stmt stmt) {
	if (stmt==null) return false;
	if (!(stmt instanceof AssignStmt)) return false;
	AssignStmt as = (AssignStmt) stmt;
	Value lhs = as.getRightOp();
       	if(!(lhs instanceof FieldRef)) return false;
	FieldRef fr = (FieldRef) lhs;
	return getPattern().matchesField(fr.getField());
    }

    public String toString() {
	return "get("+pattern+")";
    }
}
