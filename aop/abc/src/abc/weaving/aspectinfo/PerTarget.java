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

    public void registerSetupAdvice(Aspect aspect) {
	GlobalAspectInfo.v().addAdviceDecl(new PerTargetSetup(aspect,getPointcut(),getPosition()));
    }


    public Residue matchesAt(Aspect aspect,ShadowMatch sm) {
	return new HasAspect(aspect.getInstanceClass().getSootClass(),sm.getTargetContextValue());
    }

    public Residue getAspectInstance(Aspect aspect,ShadowMatch sm) {
	return new AspectOf(aspect.getInstanceClass().getSootClass(),sm.getTargetContextValue());
    }
}
