
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import java.util.*;

/** A <code>declare parents</code> .. <code>implements</code> declaration. */
public class DeclareParentsImpl extends DeclareParents {
    List/*<AbcClass>*/ interfaces;

    /** Create a <code>declare parents</code> implementing a list of interfaces.
     *  @param classes the classes that should implement the interfaces. A collection of
     *                 {@link abc.weaving.aspectinfo.AbcClass} objects.
     *  @param interfaces a list of {@link abc.weaving.aspectinfo.AbcClass} objects giving the
     *         interfaces to be implemented.
     */
    public DeclareParentsImpl(ClassnamePattern pattern, Collection classes, List interfaces, Aspect aspct, Position pos) {
	super(pattern, classes, aspct, pos);
	this.interfaces = interfaces;
    }

    /** Get the list of implemented interfaces.
     *  @return a list of {@link abc.weaving.aspectinfo.AbcClass} objects.
     */
    public List/*<AbcClass>*/ getInterfaces() {
	return interfaces;
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("declare parents: ");
	sb.append(getClasses());
	sb.append(" implements ");
	Iterator ii = getInterfaces().iterator();
	while (ii.hasNext()) {
	    AbcClass i = (AbcClass)ii.next();
	    sb.append(i.getJvmName());
	    if (ii.hasNext()) {
		sb.append(", ");
	    }
	}
	sb.append(";");
	return sb.toString();
    }
}
