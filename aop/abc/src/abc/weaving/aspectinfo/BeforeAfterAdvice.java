package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import abc.weaving.matching.*;
import abc.weaving.residues.Residue;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.weaver.WeavingContext;

/** Advice specification for advice that applies both before and after. */
public class BeforeAfterAdvice extends AbstractAdviceSpec implements ThrowingAdvice {
    public BeforeAfterAdvice(Position pos) {
	super(pos);
    }

    public String toString() {
	return "beforeafter";
    }

    public Residue matchesAt(WeavingEnv we,ShadowMatch sm) {
	return sm.supportsBefore() && sm.supportsAfter() 
	    ? AlwaysMatch.v : null;
    }

    // For use with WeavingContext
    public static interface ChoosePhase {
	public void setBefore();
	public void setAfter();
    }

    public RefType getCatchType() {
	return RefType.v("java.lang.Throwable");
    }

    public void bindException(WeavingContext wc,AbstractAdviceDecl ad,Local Exception) {
    }
}

