package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** Advice specification for after returning advice with return variable binding. */
public class AfterReturningArgAdvice extends AbstractAdviceSpec {
    public AfterReturningArgAdvice(Position pos) {
	super(pos);
    }
}
