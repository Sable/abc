package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Pointcut conjunction. */
public class AndPointcut extends AbstractPointcut {
    private Pointcut pc1;
    private Pointcut pc2;

    public AndPointcut(Pointcut pc1, Pointcut pc2, Position pos) {
	super(pos);
	this.pc1 = pc1;
	this.pc2 = pc2;
    }

    public Pointcut getLeftPointcut() {
	return pc1;
    }

    public Pointcut getRightPointcut() {
	return pc2;
    }

    public Residue matchesAt(WeavingEnv we,
			     SootClass cls,
			     SootMethod method,
			     ShadowMatch sm) {
	return AndResidue.construct(pc1.matchesAt(we,cls,method,sm),
				    pc2.matchesAt(we,cls,method,sm));
    }
    
    public String toString() {
	return "("+pc1+") && ("+pc2+")";
    }
}
