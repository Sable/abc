package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>handler</code> shadow pointcut. */
public class Handler extends AbstractShadowPointcutHandler {
    private ClassnamePattern pattern;

    public Handler(ClassnamePattern pattern) {
	this.pattern = pattern;
    }

    public ClassnamePattern getPattern() {
	return pattern;
    }
}
