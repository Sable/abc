package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>execution</code> shadow pointcut with a method pattern. */
public class MethodExecution extends AbstractShadowPointcutHandler {
    private MethodPattern pattern;

    public MethodExecution(MethodPattern pattern) {
	this.pattern = pattern;
    }

    public MethodPattern getPattern() {
	return pattern;
    }
}
