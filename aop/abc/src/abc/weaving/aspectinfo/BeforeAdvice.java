package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.Residue;
import abc.weaving.residues.AlwaysMatch;

/** Advice specification for before advice. */
public class BeforeAdvice extends AbstractAdviceSpec {
    public BeforeAdvice(Position pos) {
	super(pos);
    }

    public String toString() {
	return "before";
    }

    public Residue matchesAt(WeavingEnv we,ShadowMatch sm) {
	return sm.supportsBefore() ? AlwaysMatch.v : null;
    }
}
