
package abc.weaving.aspectinfo;

import polyglot.util.Position;

/** A <code>declare parents</code> .. <code>extends</code> declaration. */
public class DeclareParentsExt extends DeclareParents {
    String parent;

    /** Create a <code>declare parents</code> extending a class.
     *  @param the classes that should extend the class.
     *  @param parent the class to extend.
     */
    public DeclareParentsExt(ClassnamePattern classes, String parent, Aspect aspect, Position pos) {
	super(classes, aspect, pos);
	this.parent = parent;
    }

    /** Get the extended class.
     *  @return the class to be extended.
     */
    public String getParent() {
	return parent;
    }

    public String toString() {
	return "declare parents: "+getClasses()+" extends "+parent+";";
    }
}
