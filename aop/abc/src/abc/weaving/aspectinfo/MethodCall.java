package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>call</code> shadow pointcut with a method pattern. */
public class MethodCall extends AbstractShadowPointcutHandler {
    private MethodPattern pattern;

    public MethodCall(MethodPattern pattern) {
	this.pattern = pattern;
    }

    public MethodPattern getPattern() {
	return pattern;
    }
}
