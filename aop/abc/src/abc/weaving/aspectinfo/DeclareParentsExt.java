
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import java.util.*;

/** A <code>declare parents</code> .. <code>extends</code> declaration. */
public class DeclareParentsExt extends DeclareParents {
    AbcClass parent;

    /** Create a <code>declare parents</code> extending a class.
     *  @param classes the classes that should extend the class. A collection of
     *                 {@link abc.weaving.aspectinfo.AbcClass} objects.
     *  @param parent the class to extend.
     */
    public DeclareParentsExt(ClassnamePattern pattern, Collection classes, AbcClass parent, Aspect aspct, Position pos) {
	super(pattern, classes, aspct, pos);
	this.parent = parent;
    }

    /** Get the extended class.
     *  @return the class to be extended.
     */
    public AbcClass getParent() {
	return parent;
    }

    public String toString() {
	return "declare parents: "+getClasses()+" extends "+parent.getJvmName()+";";
    }
}
