package arc.weaving.aspectinfo;

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
}
