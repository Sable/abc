
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** An advice declaration. */
public class AdviceDecl extends Syntax {
    private AdviceSpec spec;
    private Pointcut pc;
    private MethodSig impl;
    private Aspect aspect;

    public AdviceDecl(AdviceSpec spec, Pointcut pc, MethodSig impl, Aspect aspect, Position pos) {
	super(pos);
	this.spec = spec;
	this.pc = pc;
	this.impl = impl;
	this.aspect = aspect;

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

    /** Get the signature of the placeholder method that contains the
     *  body of this advice.
     */
    public MethodSig getImpl() {
	return impl;
    }

    /** Get the aspect containing this intertype method declaration.
     */
    public Aspect getAspect() {
	return aspect;
    }
}
