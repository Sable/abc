package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** Advice specification for after throwing advice with exception variable binding. */
public class AfterThrowingArgAdvice extends AbstractAdviceSpec {
    public AfterThrowingArgAdvice(Position pos) {
	super(pos);
    }
}
