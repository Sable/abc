package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>this</code> condition pointcut with a variable argument. */
public class ThisVar extends AbstractOtherPointcutHandler {
    private Var var;

    public ThisVar(Var var) {
	this.var = var;
    }

    /** Get the pointcut variable that is bound by this
     *  <code>this</code> pointcut.
     */
    public Var getVar() {
	return var;
    }

}
