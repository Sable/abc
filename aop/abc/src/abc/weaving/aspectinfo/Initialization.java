package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>initialization</code> shadow pointcut. */
public class Initialization extends AbstractShadowPointcutHandler {
    private ConstructorPattern pattern;

    public Initialization(ConstructorPattern pattern) {
	this.pattern = pattern;
    }

    public ConstructorPattern getPattern() {
	return pattern;
    }
}
