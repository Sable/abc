package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** Advice specification for before advice. */
public class BeforeAdvice extends AbstractAdviceSpec {
    public BeforeAdvice(Position pos) {
	super(pos);
    }

    public String toString() {
	return "before";
    }
}
