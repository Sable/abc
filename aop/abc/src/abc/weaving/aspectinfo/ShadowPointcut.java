package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.MethodPosition;
import abc.weaving.residues.Residue;

/** A pointcut designator representing a set of joinpoint shadows
 *  at which the pointcut will match.
 */
public class ShadowPointcut extends AbstractPointcut {
    private ShadowPointcutHandler handler;

    public ShadowPointcut(ShadowPointcutHandler handler, Position pos) {
	super(pos);
	this.handler = handler;
    }

    public Residue matchesAt(ShadowType st,SootClass cls,SootMethod method,MethodPosition position) {
	return st==handler.getShadowType() ? handler.matchesAt(position) : null;
    }

    public String toString() {
	return handler.toString();
    }
}
