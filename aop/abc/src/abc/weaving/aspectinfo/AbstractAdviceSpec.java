package abc.weaving.aspectinfo;

import polyglot.util.Position;
import polyglot.util.InternalCompilerError;
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

    // FIXME: delegate this properly once we work out the precise rules
    public boolean isAfter() {
	if(this instanceof AfterAdvice 
	   || this instanceof AfterReturningAdvice 
	   || this instanceof AfterThrowingAdvice) return true;
	return false;
    }
}
