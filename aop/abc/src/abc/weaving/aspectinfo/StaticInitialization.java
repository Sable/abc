package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>staticinitialization</code> shadow pointcut. */
public class StaticInitialization extends AbstractShadowPointcutHandler {
    private ClassnamePattern pattern;

    public StaticInitialization(ClassnamePattern pattern) {
	this.pattern = pattern;
    }

    public ClassnamePattern getPattern() {
	return pattern;
    }
}
