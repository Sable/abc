package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** Advice specification for after returning advice without return variable binding. */
public class AfterReturningAdvice extends AbstractAdviceSpec {
    public AfterReturningAdvice(Position pos) {
	super(pos);
    }
}
