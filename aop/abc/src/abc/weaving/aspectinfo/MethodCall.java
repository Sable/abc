package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

import polyglot.util.Position;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Handler for <code>call</code> shadow pointcut with a method pattern.
 *  @author Ganesh Sittampalam
 *  @author Aske Simon Christensen
 */
public class MethodCall extends ShadowPointcut {
    private MethodPattern pattern;

    public MethodCall(MethodPattern pattern,Position pos) {
	super(pos);
	this.pattern = pattern;
    }

    public MethodPattern getPattern() {
	return pattern;
    }

    protected Residue matchesAt(ShadowMatch sm) {
	if(!(sm instanceof MethodCallShadowMatch)) return null;
	MethodCallShadowMatch msm=(MethodCallShadowMatch) sm;

	if(!getPattern().matchesMethodRef(msm.getMethodRef())) return null;

	return AlwaysMatch.v;
    }

    public String toString() {
	return "call("+pattern+")";
    }

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof MethodCall) {
	    return pattern.equivalent(((MethodCall)otherpc).getPattern());
	} else return false;
    }

}
