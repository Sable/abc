package abc.weaving.aspectinfo;

import java.util.Hashtable;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

/** A pointcut designator representing ones of "other" types
 *  Delete when nothing uses it any more
 *  @author Ganesh Sittampalam
 *  @date 30-Apr-04
 */
public class OtherPointcut extends Pointcut {
    private OtherPointcutHandler handler;

    public OtherPointcut(OtherPointcutHandler handler, Position pos) {
	super(pos);
	this.handler = handler;
    }

    public Residue matchesAt(WeavingEnv env,
			     SootClass cls,
			     SootMethod method,
			     ShadowMatch sm) {
	return null; // throw an exception?
    }

    public String toString() {
	return handler.toString();
    }

    protected Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context) {
	return this;
    }
}
