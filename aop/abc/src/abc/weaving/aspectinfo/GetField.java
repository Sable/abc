package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>get</code> shadow pointcut. */
public class GetField extends AbstractShadowPointcutHandler {
    private FieldPattern pattern;

    public GetField(FieldPattern pattern) {
	this.pattern = pattern;
    }

    public FieldPattern getPattern() {
	return pattern;
    }
}
