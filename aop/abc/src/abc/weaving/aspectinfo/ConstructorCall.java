package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>call</code> shadow pointcut with a constructor pattern. */
public class ConstructorCall extends AbstractShadowPointcutHandler {
    private ConstructorPattern pattern;

    public ConstructorCall(ConstructorPattern pattern) {
	this.pattern = pattern;
    }

    public ConstructorPattern getPattern() {
	return pattern;
    }
}
