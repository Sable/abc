package abc.weaving.aspectinfo;

import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.residues.*;

/** A <code>pertarget</code> per clause. */
public class PerTarget extends PerPointcut {
    public PerTarget(Pointcut pc, Position pos) {
	super(pc, pos);
    }

    public String toString() {
	return "pertarget("+getPointcut()+")";
    }

    public void registerSetupAdvice(Aspect aspct) {
	GlobalAspectInfo.v().addAdviceDecl(new PerTargetSetup(aspct,getPointcut(),getPosition()));
    }


    public Residue matchesAt(Aspect aspct,ShadowMatch sm) {
	return new HasAspect(aspct.getInstanceClass().getSootClass(),sm.getTargetContextValue());
    }

    public Residue getAspectInstance(Aspect aspct,ShadowMatch sm) {
	return new AspectOf(aspct.getInstanceClass().getSootClass(),sm.getTargetContextValue());
    }
}
