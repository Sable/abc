package abc.weaving.aspectinfo;

import java.util.Hashtable;

import soot.*;

import polyglot.util.Position;

import abc.weaving.residues.*;
import abc.weaving.matching.*;

/** Handler for <code>preinitialization</code> shadow pointcut. */
public class Preinitialization extends ShadowPointcut {
    public Preinitialization(Position pos) {
	super(pos);
    }

    public Residue matchesAt(ShadowMatch sm) {
	if(!(sm instanceof PreinitializationShadowMatch)) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "preinitialization()";
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		if (otherpc instanceof Preinitialization) {
			return true;
		} else return false;
	}

}
