package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.Residue;
import abc.weaving.residues.AlwaysMatch;


/** Advice specification for after returning advice without return variable binding. */
public class AfterReturningAdvice extends AbstractAdviceSpec {
    public AfterReturningAdvice(Position pos) {
	super(pos);
    }

    public String toString() {
	return "after returning";
    }

    public Residue matchesAt(WeavingEnv we,ShadowMatch sm) {
	return sm.supportsAfter() ? AlwaysMatch.v : null;
    }
}
