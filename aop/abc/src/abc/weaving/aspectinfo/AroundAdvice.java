package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** Advice specification for around advice. */
public class AroundAdvice extends AbstractAdviceSpec {
    private Type rtype;
    private MethodSig proceed;

    public AroundAdvice(Type rtype, MethodSig proceed, Position pos) {
	super(pos);
	this.rtype = rtype;
	this.proceed = proceed;
    }

    public Type getReturnType() {
	return rtype;
    }

    /** get the signature of the dummy placeholder method that is called
     *  as a representation of proceed calls inside this around advice.
     */
    public MethodSig getProceedImpl() {
	return proceed;
    }
}
