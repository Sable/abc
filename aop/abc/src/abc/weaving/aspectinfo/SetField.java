package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>set</code> shadow pointcut. */
public class SetField extends AbstractShadowPointcutHandler {
    private FieldPattern pattern;

    public SetField(FieldPattern pattern) {
	this.pattern = pattern;
    }

    public FieldPattern getPattern() {
	return pattern;
    }
}
