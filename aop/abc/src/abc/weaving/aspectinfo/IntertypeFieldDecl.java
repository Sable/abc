
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** An intertype field declaration. */
public class IntertypeFieldDecl extends InAspect {
    private FieldSig target;
    private MethodSig init;
    private MethodSig setter;
    private MethodSig getter;

    public IntertypeFieldDecl(FieldSig target, Aspect aspct, MethodSig init, 
    							MethodSig getter, MethodSig setter, Position pos) {
	super(aspct, pos);
	this.target = target;
	this.init = init;
	this.getter = getter;
	this.setter = setter;
    }

    /** Get the field signature that this intertype field declaration
     *  will end up having when it is woven in.
     */
    public FieldSig getTarget() {
	return target;
    }

    /** Get the signature of the method to initialise this field
     * This is a static method of aspect, with one parameter for the
     * "this" of the target class.
     */
    public MethodSig getInit() {
   	return init;
    }
    
    public MethodSig getGetter() {
    	return getter;
    }
    
    public MethodSig getSetter() {
    	return setter;
    }

    public String toString() {
	return "(in aspect "+getAspect().getName()+") "+target+";";
    }
}
