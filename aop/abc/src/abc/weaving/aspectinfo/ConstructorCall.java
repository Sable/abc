package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

import polyglot.util.Position;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Handler for <code>call</code> shadow pointcut with a constructor pattern. */
public class ConstructorCall extends ShadowPointcut {
    private ConstructorPattern pattern;

    public ConstructorCall(ConstructorPattern pattern,Position pos) {
	super(pos);
	this.pattern = pattern;
    }

    public ConstructorPattern getPattern() {
	return pattern;
    }

    protected Residue matchesAt(ShadowMatch sm) {
	if(!(sm instanceof ConstructorCallShadowMatch)) return null;
	ConstructorCallShadowMatch csm=(ConstructorCallShadowMatch) sm;

	// FIXME: Hack should be removed when patterns are added
	if(getPattern()==null) return AlwaysMatch.v;
		
	if(!getPattern().matchesConstructor(csm.getMethod())) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "constructorcall("+pattern+")";
    }

}
