
package arc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

public class IntertypeMethodDecl extends Syntax {
    private MethodSig target;
    private MethodSig impl;
    private Aspect aspect;

    public IntertypeMethodDecl(MethodSig target, MethodSig impl, Aspect aspect, Position pos) {
	super(pos);
	this.target = target;
	this.impl = impl;
	this.aspect = aspect;
    }

    /** Get the method signature that this intertype method declaration
     *  will end up having when it is woven in.
     */
    public MethodSig getTarget() {
	return target;
    }

    /** Get the signature of the placeholder method that contains the
     *  implementation of this intertype method declaration.
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
