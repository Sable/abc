package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

/** A pointcut designator representing ones of "other" types
 *  @author Ganesh Sittampalam
 *  @date 30-Apr-04
 */
public class OtherPointcut extends AbstractPointcut {
    private OtherPointcutHandler handler;

    public OtherPointcut(OtherPointcutHandler handler, Position pos) {
	super(pos);
	this.handler = handler;
    }

    public Residue matchesAt(WeavingEnv env,
			     ShadowType st,
			     SootClass cls,
			     SootMethod method,
			     MethodPosition position) {
	return null;
    }

    public String toString() {
	return handler.toString();
    }
}
