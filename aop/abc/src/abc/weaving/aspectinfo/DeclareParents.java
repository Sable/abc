
package abc.weaving.aspectinfo;

import polyglot.util.Position;

public abstract class DeclareParents extends Syntax {
    private Aspect aspect;
    private ClassnamePattern classes;

    public DeclareParents(ClassnamePattern classes, Aspect aspect, Position pos) {
	super(pos);
	this.aspect = aspect;
	this.classes = classes;
    }

    /** Get the aspect containing this <code>declare parents</code>.
     */
    public Aspect getAspect() {
	return aspect;
    }

    /** Get the classes whose parents are being declared.
     */
    public ClassnamePattern getClasses() {
	return classes;
    }
}
