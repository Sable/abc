package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** Skeleton implementation of the {@link abc.weaving.aspectinfo.AdviceSpec} interface.
 *  Useful when implementing advice specifications.
 */
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
