package abc.weaving.aspectinfo;

import soot.*;
import abc.weaving.residues.*;

/** Handler for <code>withincode</code> condition pointcut with a method pattern. */
public class WithinMethod extends AbstractLexicalPointcutHandler {
    private MethodPattern pattern;

    public WithinMethod(MethodPattern pattern) {
	this.pattern = pattern;
    }

    public MethodPattern getPattern() {
	return pattern;
    }

    public Residue matchesAt(SootClass cls,SootMethod method) {
	// FIXME: Remove this once pattern is built properly
	if(getPattern()==null) return AlwaysMatch.v;

	if(!getPattern().matchesMethod(method)) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "withincode("+pattern+")";
    }
}
