package abc.weaving.aspectinfo;

import soot.*;

import polyglot.util.Position;

import abc.weaving.residues.*;
import abc.weaving.matching.*;

/** Handler for <code>initialization</code> shadow pointcut. */
public class Initialization extends ShadowPointcut {

    public Initialization(Position pos) {
	super(pos);
    }

    protected Residue matchesAt(ShadowMatch sm) {
	if(!(sm instanceof InitializationShadowMatch)) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "initialization()";
    }

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof Initialization) {
	    return true;
	} else return false;
    }

}
