package abc.weaving.residues;

import soot.*;
import abc.weaving.matching.ShadowMatch;

public class JoinPointInfo extends ContextValue {

    private ShadowMatch sm;

    public JoinPointInfo(ShadowMatch sm) {
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
