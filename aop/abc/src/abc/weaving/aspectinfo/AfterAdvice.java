package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.Residue;
import abc.weaving.residues.AlwaysMatch;

/** Advice specification for after advice. */
public class AfterAdvice extends AbstractAdviceSpec {
    public AfterAdvice(Position pos) {
	super(pos);
    }

    public String toString() {
	return "after";
    }

    public Residue matchesAt(WeavingEnv we,ShadowMatch sm) {
	return sm.supportsAfter() ? AlwaysMatch.v : null;
    }
}
