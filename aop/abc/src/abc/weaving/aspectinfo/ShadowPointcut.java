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
				   ShadowType st,
				   SootClass cls,
				   SootMethod method,
				   MethodPosition position) {
	return st==getShadowType() ? matchesAt(position) : null;
    }

    public ShadowPointcut(Position pos) {
	super(pos);
    }

    // Keep a record of what class is what shadow type?
    static private List/*<ShadowType>*/ allShadowTypes=new LinkedList();

    /** All classes that implement a new shadow type should call this in 
	their static initializer */
    static public void registerShadowType(ShadowType st) {
	allShadowTypes.add(st);
    }

    static public Iterator shadowTypesIterator() {
	return allShadowTypes.iterator();
    }

    public abstract ShadowType getShadowType();

    /** Shadow pointcuts just need to know the position for matching */
    protected abstract Residue matchesAt(MethodPosition position);

}
