
package abc.weaving.aspectinfo;

import polyglot.util.Position;

public abstract class DeclareParents extends InAspect {
    private ClassnamePattern classes;

    public DeclareParents(ClassnamePattern classes, Aspect aspct, Position pos) {
	super(aspct, pos);
	this.classes = classes;
    }

    /** Get the classes whose parents are being declared.
     */
    public ClassnamePattern getClasses() {
	return classes;
    }
}
