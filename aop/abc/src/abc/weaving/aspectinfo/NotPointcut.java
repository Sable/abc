package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.MethodPosition;
import abc.weaving.residues.*;

/** Pointcut negation. */
public class NotPointcut extends AbstractPointcut {
    private Pointcut pc;

    public NotPointcut(Pointcut pc, Position pos) {
	super(pos);
	this.pc = pc;
    }

    public Pointcut getPointcut() {
	return pc;
    }

    public Residue matchesAt(ShadowType st,
			     SootClass cls,
			     SootMethod method,
			     MethodPosition pos) {
	return NotResidue.construct(pc.matchesAt(st,cls,method,pos));
    }
}
