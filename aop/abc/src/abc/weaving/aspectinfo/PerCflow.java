package abc.weaving.aspectinfo;

import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.residues.*;

/** A <code>percflow</code> per clause. */
public class PerCflow extends PerPointcut {
    public PerCflow(Pointcut pc, Position pos) {
	super(pc, pos);
    }

    public String toString() {
	return "percflow("+getPointcut()+")";
    }

    public void registerSetupAdvice(Aspect aspct) {
	GlobalAspectInfo.v().addAdviceDecl
	    (new PerCflowSetup(aspct,getPointcut(),false,getPosition()));
    }

    public Residue matchesAt(Aspect aspct,ShadowMatch sm) {
	return new HasAspect(aspct.getInstanceClass().getSootClass(),null);
    }

    public Residue getAspectInstance(Aspect aspct,ShadowMatch sm) {
	return new AspectOf(aspct.getInstanceClass().getSootClass(),null);
    }
}
