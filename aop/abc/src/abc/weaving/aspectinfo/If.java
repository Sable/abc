package arc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>if</code> condition pointcut. */
public class If extends AbstractConditionPointcutHandler {
    private Var[] vars;
    private MethodSig impl;

    public If(Var[] vars, MethodSig impl) {
	this.vars = vars;
	this.impl = impl;
    }

    /** Get the pointcut variables that should be given as arguments to
     *  the method implementing the <code>if</code> condition.
     */
    public Var[] getVars() {
	return vars;
    }

    /** Get the signature of the method implementing
     *  the <code>if</code> condition.
     */
    public MethodSig getImpl() {
	return impl;
    }
}
