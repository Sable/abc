package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Pointcut disjunction. */
public class OrPointcut extends AbstractPointcut {
    private Pointcut pc1;
    private Pointcut pc2;

    public OrPointcut(Pointcut pc1, Pointcut pc2, Position pos) {
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
			     ShadowType st,
			     SootClass cls,
			     SootMethod method,
			     MethodPosition pos) {
	return OrResidue.construct(pc1.matchesAt(we,st,cls,method,pos),
				   pc2.matchesAt(we,st,cls,method,pos));
    }

    public String toString() {
	return "("+pc1+") || ("+pc2+")";
    }
}
