package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>withincode</code> condition pointcut with a method pattern. */
public class WithinMethod extends AbstractConditionPointcutHandler {
    private MethodPattern pattern;

    public WithinMethod(MethodPattern pattern) {
	this.pattern = pattern;
    }

    public MethodPattern getPattern() {
	return pattern;
    }
}
