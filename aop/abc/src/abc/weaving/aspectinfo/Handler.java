package abc.weaving.aspectinfo;

import soot.*;

import polyglot.util.Position;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Handler for <code>handler</code> shadow pointcut. */
public class Handler extends ShadowPointcut {
    private ClassnamePattern pattern;

    public Handler(ClassnamePattern pattern,Position pos) {
	super(pos);
	this.pattern = pattern;
    }

    public ClassnamePattern getPattern() {
	return pattern;
    }

    protected Residue matchesAt(ShadowMatch sm) {
	if(!(sm instanceof HandlerShadowMatch)) return null;
	SootClass exc=((HandlerShadowMatch) sm).getException();

	// FIXME: Hack should be removed when patterns are added
	if(getPattern()==null) return AlwaysMatch.v;

	if(!getPattern().matchesClass(exc)) return null;
	return AlwaysMatch.v;

    }

    public String toString() {
	return "handler("+pattern+")";
    }

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof Handler) {
	    return pattern.equivalent(((Handler)otherpc).getPattern());
	} else return false;
    }

}
