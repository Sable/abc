package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>target</code> condition pointcut with a type argument. */
public class TargetType extends AbstractConditionPointcutHandler {
    private Type type;

    public TargetType(Type type) {
	this.type = type;
    }

    /** Get the type that is matched against the target
     *  by this <code>target</code> pointcut.
     */
    public Type getType() {
	return type;
    }

}
