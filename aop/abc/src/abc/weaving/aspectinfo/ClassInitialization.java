package abc.weaving.aspectinfo;

import java.util.Hashtable;

import soot.*;

import polyglot.util.Position;

import abc.weaving.residues.*;
import abc.weaving.matching.*;

/** Handler for <code>initialization</code> shadow pointcut. */
public class ClassInitialization extends ShadowPointcut {

    public ClassInitialization(Position pos) {
	super(pos);
    }

    protected Residue matchesAt(ShadowMatch sm) {
	if(!(sm instanceof ClassInitializationShadowMatch)) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "classinitialization()";
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		if (otherpc instanceof ClassInitialization) {
			return true;
		} else return false;
	}

}
