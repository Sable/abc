
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import java.util.*;

/** A <code>declare parents</code> .. <code>implements</code> declaration. */
public class DeclareParentsImpl extends DeclareParents {
    List/*<String>*/ interfaces;

    /** Create a <code>declare parents</code> implementing a list of interfaces.
     *  @param the classes that should implement the interfaces.
     *  @param interfaces a list of {@link java.lang.String} objects giving the
     *         interfaces to be implemented.
     */
    public DeclareParentsImpl(ClassnamePattern classes, List interfaces, Aspect aspct, Position pos) {
	super(classes, aspct, pos);
	this.interfaces = interfaces;
    }

    /** Get the list of implemented interfaces.
     *  @return a list of {@link java.lang.String} objects.
     */
    public List/*<String>*/ getInterfaces() {
	return interfaces;
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("declare parents: ");
	sb.append(getClasses());
	sb.append(" implements ");
	Iterator ii = getInterfaces().iterator();
	while (ii.hasNext()) {
	    String i = (String)ii.next();
	    sb.append(i);
	    if (ii.hasNext()) {
		sb.append(", ");
	    }
	}
	sb.append(";");
	return sb.toString();
    }
}
