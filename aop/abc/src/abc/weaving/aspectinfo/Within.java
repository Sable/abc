package abc.weaving.aspectinfo;

import soot.*;
import abc.weaving.residues.*;

/** Handler for <code>within</code> condition pointcut. */
public class Within extends AbstractLexicalPointcutHandler {
    private ClassnamePattern pattern;

    public Within(ClassnamePattern pattern) {
	this.pattern = pattern;
    }

    public ClassnamePattern getPattern() {
	return pattern;
    }


    public Residue matchesAt(SootClass cls,SootMethod method) {
	// FIXME: Remove this once pattern is built properly
	if(getPattern()==null) return AlwaysMatch.v;

	if(!getPattern().matchesClass(cls)) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "within("+pattern+")";
    }
    
}
