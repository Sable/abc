package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** Skeleton implementation of the {@link abc.weaving.aspectinfo.AdviceSpec} interface.
 *  Useful when implementing advice specifications.
 */
public abstract class AbstractAdviceSpec extends Syntax implements AdviceSpec {
    private AbstractAdviceDecl advice;

    public AbstractAdviceSpec(Position pos) {
	super(pos);
    }

    void setAdvice(AbstractAdviceDecl advice) {
	this.advice = advice;
    }

    public AbstractAdviceDecl getAdvice() {
	return advice;
    }

}
