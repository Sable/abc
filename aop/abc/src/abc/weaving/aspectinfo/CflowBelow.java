package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>cflowbelow</code> condition pointcut. */
public class CflowBelow extends AbstractConditionPointcutHandler {
    private Pointcut pc;

    public CflowBelow(Pointcut pc) {
	this.pc = pc;
    }

    public Pointcut getPointcut() {
	return pc;
    }
}
