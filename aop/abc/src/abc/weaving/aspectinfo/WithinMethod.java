package abc.weaving.aspectinfo;

import soot.*;
import polyglot.util.Position;
import abc.weaving.residues.*;

/** Handler for <code>withincode</code> lexical pointcut with a method pattern. */
public class WithinMethod extends LexicalPointcut {
    private MethodPattern pattern;

    public WithinMethod(MethodPattern pattern,Position pos) {
	super(pos);
	this.pattern = pattern;
    }

    public MethodPattern getPattern() {
	return pattern;
    }

    protected Residue matchesAt(SootClass cls,SootMethod method) {
	if(method.getName().equals(SootMethod.constructorName) ||
	   method.getName().equals(SootMethod.staticInitializerName) ||
	   MethodCategory.adviceBody(method))
	    return null;

	// FIXME: Remove this once pattern is built properly
	if(getPattern()==null) return AlwaysMatch.v;

	if(!getPattern().matchesMethod(method)) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "withinmethod("+pattern+")";
    }

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof WithinMethod) {
	    return pattern.equivalent(((WithinMethod)otherpc).getPattern());
	} else return false;
    }

}
