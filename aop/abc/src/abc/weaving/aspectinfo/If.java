package abc.weaving.aspectinfo;

import soot.*;

import java.util.*;

/** Handler for <code>if</code> condition pointcut. */
public class If extends AbstractOtherPointcutHandler {
    private List/*<Var>*/ vars;
    private MethodSig impl;

    public If(List vars, MethodSig impl) {
	this.vars = vars;
	this.impl = impl;
    }

    /** Get the pointcut variables that should be given as arguments to
     *  the method implementing the <code>if</code> condition.
     *  @return a list of {@link {abc.weaving.aspectinfo.Var} objects.
     */
    public List getVars() {
	return vars;
    }

    /** Get the signature of the method implementing
     *  the <code>if</code> condition.
     */
    public MethodSig getImpl() {
	return impl;
    }
}
