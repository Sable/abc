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


    final protected Residue matchesAt(SootClass cls,SootMethod method) {
	return matchesAt(cls);
    }
    
    protected Residue matchesAt(SootClass cls) {
	if(getPattern().matchesClass(cls)) return AlwaysMatch.v;
	if(cls.hasOuterClass()) return matchesAt(cls.getOuterClass());
	return null;
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
