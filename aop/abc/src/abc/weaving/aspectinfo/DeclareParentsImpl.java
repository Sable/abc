
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
    public DeclareParentsImpl(ClassnamePattern classes, List interfaces, Aspect aspect, Position pos) {
	super(classes, aspect, pos);
	this.interfaces = interfaces;
    }

    /** Get the list of implemented interfaces.
     *  @return a list of {@link java.lang.String} objects.
     */
    public List/*<String>*/ getInterfaces() {
	return interfaces;
    }
}
