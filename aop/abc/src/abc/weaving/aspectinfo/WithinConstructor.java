package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>withincode</code> condition pointcut with a constructor pattern. */
public class WithinConstructor extends AbstractConditionPointcutHandler {
    private ConstructorPattern pattern;

    public WithinConstructor(ConstructorPattern pattern) {
	this.pattern = pattern;
    }

    public ConstructorPattern getPattern() {
	return pattern;
    }

    public boolean matchesAt(SootClass cls,SootMethod method) {
	if(getPattern()==null) return true;
	return getPattern().matchesConstructor(method);
    }

    public String toString() {
	return "withincode("+pattern+")";
    }
}
