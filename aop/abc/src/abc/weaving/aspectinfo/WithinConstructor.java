package abc.weaving.aspectinfo;

import soot.*;
import abc.weaving.residues.*;

/** Handler for <code>withincode</code> condition pointcut with a constructor pattern. */
public class WithinConstructor extends AbstractLexicalPointcutHandler {
    private ConstructorPattern pattern;

    public WithinConstructor(ConstructorPattern pattern) {
	this.pattern = pattern;
    }

    public ConstructorPattern getPattern() {
	return pattern;
    }

    public Residue matchesAt(SootClass cls,SootMethod method) {
	// FIXME: Remove this once pattern is built properly
	if(getPattern()==null) return AlwaysMatch.v;

	if(!getPattern().matchesConstructor(method)) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "withincode("+pattern+")";
    }
}
