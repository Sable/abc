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

    public void registerSetupAdvice(Aspect aspct) {
	GlobalAspectInfo.v().addAdviceDecl(new PerThisSetup(aspct,getPointcut(),getPosition()));
    }

    public Residue matchesAt(Aspect aspct,ShadowMatch sm) {
	ContextValue thisCV=sm.getThisContextValue();
	if(thisCV==null) return null;
	return new HasAspect(aspct.getInstanceClass().getSootClass(),thisCV);
    }

    public Residue getAspectInstance(Aspect aspct,ShadowMatch sm) {
	ContextValue thisCV=sm.getThisContextValue();
	if(thisCV==null) return null;
	return new AspectOf(aspct.getInstanceClass().getSootClass(),thisCV);
    }
}
