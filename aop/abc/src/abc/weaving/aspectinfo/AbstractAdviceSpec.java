package arc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

public abstract class AbstractAdviceSpec extends Syntax implements AdviceSpec {
    private AdviceDecl advice;

    public AbstractAdviceSpec(Position pos) {
	super(pos);
    }

    void setAdvice(AdviceDecl advice) {
	this.advice = advice;
    }

    public AdviceDecl getAdvice() {
	return advice;
    }
}
