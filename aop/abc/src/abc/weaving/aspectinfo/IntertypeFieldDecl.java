
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** An intertype field declaration. */
public class IntertypeFieldDecl extends Syntax {
    private FieldSig target;
    private Aspect aspect;
    private MethodSig init;

    public IntertypeFieldDecl(FieldSig target, Aspect aspect, MethodSig init, Position pos) {
	super(pos);
	this.target = target;
	this.aspect = aspect;
	this.init = init;
    }

    /** Get the field signature that this intertype field declaration
     *  will end up having when it is woven in.
     */
    public FieldSig getTarget() {
	return target;
    }

    /** Get the aspect containing this intertype field declaration.
     */
    public Aspect getAspect() {
	return aspect;
    }
    
    /** Get the signature of the method to initialise this field
     * This is a static method of aspect, with one parameter for the
     * "this" of the target class.
     */
    public MethodSig getInit() {
   	return init;
    }

    public String toString() {
	return "(in aspect "+aspect.getInstanceClass().getName()+") "+target+";";
    }
}
