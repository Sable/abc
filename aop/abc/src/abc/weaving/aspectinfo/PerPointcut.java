package abc.weaving.aspectinfo;

import polyglot.util.Position;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.residues.Residue;

/** Base class for a per clause that contains a pointcut. */
public abstract class PerPointcut extends Per {
    private Pointcut pc;

    public PerPointcut(Pointcut pc, Position pos) {
	super(pos);
	this.pc = pc;
    }

    public Pointcut getPointcut() {
	return pc;
    }

    // FIXME once PerCflow and PerCflowBelow are done
    public void registerSetupAdvice(Aspect aspct) {
	throw new RuntimeException("per clause not supported yet: "+this); 
    }

    public Residue matchesAt(Aspect aspct,ShadowMatch sm) { 
	throw new RuntimeException("per clause not supported yet: "+this); 
    }
    public Residue getAspectInstance(Aspect aspct,ShadowMatch sm) { 
	throw new RuntimeException("per clause not supported yet: "+this);
    }

}
