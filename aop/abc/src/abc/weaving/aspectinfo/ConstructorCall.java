package abc.weaving.aspectinfo;

import java.util.Hashtable;

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

	if(!getPattern().matchesConstructorRef(csm.getMethodRef())) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "constructorcall("+pattern+")";
    }

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof ConstructorCall) {
	    return pattern.equivalent(((ConstructorCall)otherpc).getPattern());
	} else return false;
    }



	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		if (otherpc instanceof ConstructorCall) {
			return pattern.equivalent(((ConstructorCall)otherpc).getPattern());
		} else return false;
	}

}
