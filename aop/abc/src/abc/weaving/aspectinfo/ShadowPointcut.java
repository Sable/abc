package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;

import java.util.*;

import polyglot.util.Position;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

/** A pointcut designator representing a set of joinpoint shadows
 *  at which the pointcut will match.
 */
public abstract class ShadowPointcut extends AbstractPointcut {
    public final Residue matchesAt(WeavingEnv env,
				   SootClass cls,
				   SootMethod method,
				   ShadowMatch sm) {
	return matchesAt(sm);
    }

    public ShadowPointcut(Position pos) {
	super(pos);
    }

    /** Shadow pointcuts just need to know the ShadowMatch */
    protected abstract Residue matchesAt(ShadowMatch sm);

}
