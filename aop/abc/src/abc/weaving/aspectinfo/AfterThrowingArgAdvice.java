package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** Advice specification for after throwing advice with exception variable binding. */
public class AfterThrowingArgAdvice extends AbstractAdviceSpec {
    private Formal formal;

    public AfterThrowingArgAdvice(Formal formal, Position pos) {
	super(pos);
	this.formal = formal;
    }

    public Formal getFormal() {
	return formal;
    }

    public String toString() {
	return "after throwing arg";
    }
}
