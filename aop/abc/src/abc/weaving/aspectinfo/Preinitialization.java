package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>preinitialization</code> shadow pointcut. */
public class Preinitialization extends AbstractShadowPointcutHandler {
    private ConstructorPattern pattern;

    public Preinitialization(ConstructorPattern pattern) {
	this.pattern = pattern;
    }

    public ConstructorPattern getPattern() {
	return pattern;
    }
}
