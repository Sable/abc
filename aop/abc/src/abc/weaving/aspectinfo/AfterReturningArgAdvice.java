package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** Advice specification for after returning advice with return variable binding. */
public class AfterReturningArgAdvice extends AbstractAdviceSpec {
    private Formal formal;

    public AfterReturningArgAdvice(Formal formal, Position pos) {
	super(pos);
	this.formal = formal;
    }

    public Formal getFormal() {
	return formal;
    }

    public String toString() {
	return "after returning arg";
    }
}
