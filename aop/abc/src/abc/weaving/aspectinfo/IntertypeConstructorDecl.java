
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** An intertype constructor declaration. */
public class IntertypeConstructorDecl extends Syntax {
    private MethodSig target;
    private MethodSig impl;
    private Aspect aspect;

    public IntertypeConstructorDecl(MethodSig target, MethodSig impl, Aspect aspect, Position pos) {
	super(pos);
	this.target = target;
	this.impl = impl;
	this.aspect = aspect;
    }

    /** Get the method signature that this intertype constructor declaration
     *  will end up having when it is woven in.
     */
    public MethodSig getTarget() {
	return target;
    }

    /** Get the signature of the placeholder method that contains the
     *  implementation of this intertype constructor declaration.
     */
    public MethodSig getImpl() {
	return impl;
    }

    /** Get the aspect containing this intertype constructor declaration.
     */
    public Aspect getAspect() {
	return aspect;
    }
}
