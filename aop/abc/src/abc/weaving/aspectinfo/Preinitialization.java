package abc.weaving.aspectinfo;

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

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof Preinitialization) {
	    return true;
	} else return false;
    }

}
