package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

/** Handler for <code>set</code> shadow pointcut. */
public class SetField extends AbstractShadowPointcutHandler {
    private FieldPattern pattern;

    public SetField(FieldPattern pattern) {
	this.pattern = pattern;
    }

    public FieldPattern getPattern() {
	return pattern;
    }

    public boolean matchesAt(SootClass cls,SootMethod method,Stmt stmt) {
	if (!(stmt instanceof AssignStmt)) return false;
	AssignStmt as = (AssignStmt) stmt;
	Value lhs = as.getLeftOp();
       	if(!(lhs instanceof FieldRef)) return false;
	return true;
	//	FieldRef fr = (FieldRef) lhs;
	//	return getPattern().matchesField(fr.getField());
    }

    public String toString() {
	return "set("+pattern+")";
    }
}
