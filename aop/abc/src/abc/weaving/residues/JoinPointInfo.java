package abc.weaving.residues;

import soot.*;
import soot.util.Chain;
import soot.jimple.Stmt;
import abc.weaving.matching.ShadowMatch;
import abc.soot.util.LocalGeneratorEx;

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

    public Stmt doInit(LocalGeneratorEx lg,Chain units,Stmt begin) {
	return sm.sp.lazyInitThisJoinPoint(lg,units,begin);
    }
}
