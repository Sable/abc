package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>target</code> condition pointcut with a variable argument. */
public class TargetVar extends AbstractConditionPointcutHandler {
    private Var var;

    public TargetVar(Var var) {
	this.var = var;
    }

    /** Get the pointcut variable that is bound by this
     *  <code>target</code> pointcut.
     */
    public Var getVar() {
	return var;
    }

}
