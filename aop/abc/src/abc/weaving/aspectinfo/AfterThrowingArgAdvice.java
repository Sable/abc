package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.Residue;
import abc.weaving.residues.AlwaysMatch;

/** Advice specification for after throwing advice with exception variable binding. */
public class AfterThrowingArgAdvice extends AbstractAdviceSpec {
    private Formal formal;

    public AfterThrowingArgAdvice(Formal formal, Position pos) {
	super(pos);
	this.formal = formal;
    }

    public Formal getFormal() {
	return formal;
    }

    public String toString() {
	return "after throwing arg";
    }


    public Residue matchesAt(WeavingEnv we,ShadowMatch sm) {
	if(!sm.supportsAfter()) return null;
	// Bind the exception
	return AlwaysMatch.v;
    }
}
