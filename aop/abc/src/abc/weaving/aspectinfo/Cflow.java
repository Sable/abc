package abc.weaving.aspectinfo;

import soot.*;

/** Handler for <code>cflow</code> condition pointcut. */
public class Cflow extends AbstractConditionPointcutHandler {
    private Pointcut pc;

    public Cflow(Pointcut pc) {
	this.pc = pc;
    }

    public Pointcut getPointcut() {
	return pc;
    }
}
