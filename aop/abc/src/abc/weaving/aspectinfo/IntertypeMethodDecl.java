
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** An intertype method declaration. */
public class IntertypeMethodDecl extends InAspect {
    private MethodSig target;
    private MethodSig impl;

    public IntertypeMethodDecl(MethodSig target, MethodSig impl, Aspect aspct, Position pos) {
	super(aspct, pos);
	this.target = target;
	this.impl = impl;
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

    public String toString() {
	return "(in aspect "+getAspect().getName()+") "+target+" { ... }";
    }
}
