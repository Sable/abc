package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

import polyglot.util.Position;

import abc.weaving.residues.*;
import abc.weaving.matching.*;

/** Handler for <code>execution</code> shadow pointcut with a method pattern. */
public class Execution extends ShadowPointcut {
    public Execution(Position pos) {
	super(pos);
    }

    protected Residue matchesAt(ShadowMatch sm) {
	if(!(sm instanceof ExecutionShadowMatch)) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "execution()";
    }

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof Execution) {
	    return true;
	} else return false;
    }

}
