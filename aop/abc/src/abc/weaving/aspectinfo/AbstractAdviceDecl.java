package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

import java.util.*;

/** The base class for any kind of 'advice' declaration 
 *  @author Ganesh Sittampalam
 */
public abstract class AbstractAdviceDecl extends Syntax {
    protected AdviceSpec spec;
    protected Pointcut pc;

    protected AbstractAdviceDecl(AdviceSpec spec,Pointcut pc,
				 List/*<Formal>*/ formals,Position pos) {
	super(pos);
	this.spec=spec;

	this.pc=Pointcut.normalize(pc,formals);

	if (spec instanceof AbstractAdviceSpec) {
	    ((AbstractAdviceSpec)spec).setAdvice(this);
	}
    }

    public AdviceSpec getAdviceSpec() {
	return spec;
    }

    public Pointcut getPointcut() {
	return pc;
    }
}
