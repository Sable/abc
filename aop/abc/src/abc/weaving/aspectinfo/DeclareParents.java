
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import java.util.*;

public abstract class DeclareParents extends InAspect {
    private ClassnamePattern pattern;
    private Collection/*<AbcClass>*/ classes;

    /** Make a <code>declare parents</code> declaration.
     *  @param classes a collection of {@link abc.weaving.aspectinfo.AbcClass} objects.
     */
    public DeclareParents(ClassnamePattern pattern, Collection classes, Aspect aspct, Position pos) {
	super(aspct, pos);
	this.pattern = pattern;
	this.classes = classes;
    }

    public ClassnamePattern getPattern() {
	return pattern;
    }

    /** Get the classes whose parents are being declared.
     *  @return a collection of {@link abc.weaving.aspectinfo.AbcClass} objects.
     */
    public Collection getClasses() {
	return classes;
    }
}
