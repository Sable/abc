package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>this</code> condition pointcut with a type argument. */
public class ThisType extends AbstractConditionPointcutHandler {
    private Type type;

    public ThisType(Type type) {
	this.type = type;
    }

    /** Get the type that is matched against <code>this</code>
     *  by this <code>this</code> pointcut.
     */
    public Type getType() {
	return type;
    }

}
