package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>this</code> condition pointcut with a type argument. */
public class ThisType extends AbstractConditionPointcutHandler {
    private AbcType type;

    public ThisType(AbcType type) {
	this.type = type;
    }

    /** Get the type that is matched against <code>this</code>
     *  by this <code>this</code> pointcut.
     */
    public AbcType getType() {
	return type;
    }

}
