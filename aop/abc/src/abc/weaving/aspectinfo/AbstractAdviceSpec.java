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

    public static int getPrecedence(AdviceSpec a,AdviceSpec b) {
	int aprec=getPrecNum(a),bprec=getPrecNum(b);
	if(aprec>bprec) return GlobalAspectInfo.PRECEDENCE_FIRST;
	if(aprec<bprec) return GlobalAspectInfo.PRECEDENCE_SECOND;
	return GlobalAspectInfo.PRECEDENCE_NONE;
    }

    private static int getPrecNum(AdviceSpec s) {
	if(s instanceof BeforeAdvice) return 1;
	else if(s instanceof AfterAdvice 
		|| s instanceof AfterReturningAdvice 
		|| s instanceof AfterThrowingAdvice) return 0;
	else if(s instanceof AroundAdvice) return 1;
	else if(s instanceof BeforeAfterAdvice) return 1;
	else throw new InternalCompilerError("Unknown advice spec "+s.getClass());
    }
}
