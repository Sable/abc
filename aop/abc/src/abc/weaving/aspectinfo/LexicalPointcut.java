package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

/** A pointcut designator representing a condition on the 
 *  lexical context
 *  @author Ganesh Sittampalam
 *  @date 30-Apr-04
 */
public abstract class LexicalPointcut extends AbstractPointcut {
    public LexicalPointcut(Position pos) {
	super(pos);
    }

    public final Residue matchesAt(WeavingEnv env,
				   ShadowType st,
				   SootClass cls,
				   SootMethod method,
				   MethodPosition position) {
	return matchesAt(cls,method);
    }

    /** Do we match at a particular class and method? */
    protected abstract Residue matchesAt(SootClass cls,
					 SootMethod method);

}
