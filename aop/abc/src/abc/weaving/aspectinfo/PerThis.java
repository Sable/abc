package abc.weaving.aspectinfo;

import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.residues.*;

/** A <code>perthis</code> per clause. */
public class PerThis extends PerPointcut {
    public PerThis(Pointcut pc, Position pos) {
	super(pc, pos);
    }

    public String toString() {
	return "perthis("+getPointcut()+")";
    }

    public void registerSetupAdvice(Aspect aspect) {
	GlobalAspectInfo.v().addAdviceDecl(new PerThisSetup(aspect,getPointcut(),getPosition()));
    }

    public Residue matchesAt(Aspect aspect,ShadowMatch sm) {
	return new HasAspect(aspect.getInstanceClass().getSootClass(),sm.getThisContextValue());
    }

    public Residue getAspectInstance(Aspect aspect,ShadowMatch sm) {
	return new AspectOf(aspect.getInstanceClass().getSootClass(),sm.getThisContextValue());
    }
}
