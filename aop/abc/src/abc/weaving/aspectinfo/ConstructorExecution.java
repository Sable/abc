package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>execution</code> shadow pointcut with a constructor pattern. */
public class ConstructorExecution extends AbstractShadowPointcutHandler {
    private ConstructorPattern pattern;

    public ConstructorExecution(ConstructorPattern pattern) {
	this.pattern = pattern;
    }

    public ConstructorPattern getPattern() {
	return pattern;
    }
}
