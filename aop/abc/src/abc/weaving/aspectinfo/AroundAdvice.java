package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.Residue;
import abc.weaving.residues.AlwaysMatch;

/** Advice specification for around advice. */
public class AroundAdvice extends AbstractAdviceSpec {
    private AbcType rtype;
    private MethodSig proceed;

    public AroundAdvice(AbcType rtype, MethodSig proceed, Position pos) {
	super(pos);
	this.rtype = rtype;
	this.proceed = proceed;
    }

    public AbcType getReturnType() {
	return rtype;
    }

    /** get the signature of the dummy placeholder method that is called
     *  as a representation of proceed calls inside this around advice.
     */
    public MethodSig getProceedImpl() {
	return proceed;
    }

    public String toString() {
	return rtype+" around";
    }

    public Residue matchesAt(WeavingEnv we,ShadowMatch sm) {
	return sm.supportsAround() ? AlwaysMatch.v : null;
    }
}
