package abc.weaving.aspectinfo;

import soot.*;
import polyglot.util.Position;
import abc.weaving.residues.*;

/** Handler for <code>within</code> condition pointcut. */
public class Within extends LexicalPointcut {
    private ClassnamePattern pattern;

    public Within(ClassnamePattern pattern,Position pos) {
	super(pos);
	this.pattern = pattern;
    }

    public ClassnamePattern getPattern() {
	return pattern;
    }


    protected Residue matchesAt(SootClass cls,SootMethod method) {
	// FIXME: Remove this once pattern is built properly
	if(getPattern()==null) return AlwaysMatch.v;

	if(!getPattern().matchesClass(cls)) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "within("+pattern+")";
    }

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof Within) {
	    return pattern.equivalent(((Within)otherpc).getPattern());
	} else return false;
    }
    
}
