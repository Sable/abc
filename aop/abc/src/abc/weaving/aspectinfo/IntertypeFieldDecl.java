
package arc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

public class IntertypeFieldDecl extends Syntax {
    private FieldSig target;
    private Aspect aspect;

    public IntertypeFieldDecl(FieldSig target, Aspect aspect, Position pos) {
	super(pos);
	this.target = target;
	this.aspect = aspect;
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
}
