package abc.weaving.aspectinfo;

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

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof ClassInitialization) {
	    return true;
	} else return false;
    }

}
