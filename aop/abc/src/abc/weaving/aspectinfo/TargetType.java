package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>target</code> condition pointcut with a type argument. */
public class TargetType extends AbstractOtherPointcutHandler {
    private AbcType type;

    public TargetType(AbcType type) {
	this.type = type;
    }

    /** Get the type that is matched against the target
     *  by this <code>target</code> pointcut.
     */
    public AbcType getType() {
	return type;
    }

}
