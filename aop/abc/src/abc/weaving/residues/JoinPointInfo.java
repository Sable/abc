package abc.weaving.residues;

import polyglot.util.InternalCompilerError;
import soot.*;
import soot.jimple.Jimple;
import abc.weaving.matching.AdviceApplication;

public class JoinPointInfo extends ContextValue {

    private abc.weaving.matching.ShadowMatch sm;

    public JoinPointInfo(abc.weaving.matching.ShadowMatch sm) {
	this.sm=sm;
    }

    public String toString() {
	return "thisJoinPoint";
    }

    public Type getSootType() {
	return RefType.v("org.aspectj.lang.JoinPoint");
    }

    public Value getSootValue() {
	return sm.sp.getThisJoinPoint();
    }
}
