package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.Residue;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.weaver.WeavingContext;

// FIXME: replace ThrowingAdvice interface with just embedding AfterReturning and AfterThrowing 
// in After when we delegate the weaving code appropriate

/** Advice specification for after throwing advice without exception variable binding. */
public class AfterThrowingAdvice extends AbstractAdviceSpec implements ThrowingAdvice {
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
