package arc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** Advice specification for after throwing advice without exception variable binding. */
public class AfterThrowingAdvice extends AbstractAdviceSpec {
    public AfterThrowingAdvice(Position pos) {
	super(pos);
    }
}
