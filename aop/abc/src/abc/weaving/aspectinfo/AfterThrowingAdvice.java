package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.Residue;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.weaver.WeavingContext;

/** Advice specification for after throwing advice without exception variable binding. */
public class AfterThrowingAdvice extends AbstractAdviceSpec {
    public AfterThrowingAdvice(Position pos) {
	super(pos);
    }

    public String toString() {
	return "after throwing";
    }

    public Residue matchesAt(WeavingEnv we,ShadowMatch sm) {
	return sm.supportsAfter() ? AlwaysMatch.v : null;
    }

    public RefType getCatchType() {
	return RefType.v("java.lang.Throwable");
    }

    public void bindException(WeavingContext wc,AbstractAdviceDecl ad,Local Exception) {
    }
}
