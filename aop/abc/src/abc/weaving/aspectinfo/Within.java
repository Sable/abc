package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>within</code> condition pointcut. */
public class Within extends AbstractConditionPointcutHandler {
    private ClassnamePattern pattern;

    public Within(ClassnamePattern pattern) {
	this.pattern = pattern;
    }

    public ClassnamePattern getPattern() {
	return pattern;
    }


    public boolean matchesAt(SootClass cls,SootMethod method) {
	if(getPattern()==null) return true;
	return getPattern().matchesClass(cls);
    }

    public String toString() {
	return "within("+pattern+")";
    }
    
}
