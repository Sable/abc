
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import java.util.*;

/** A declare precedence declaration */
public class DeclarePrecedence extends Syntax {
    private Aspect aspect;
    private List/*<ClassnamePattern>*/ patterns;

    /** Create a new <code>declare precedence</code>.
     *  @param patterns a list of {@link abc.weaving.aspectinfo.ClassnamePattern} objects.
     */
    public DeclarePrecedence(List patterns, Aspect aspect, Position pos) {
	super(pos);
	this.aspect = aspect;
	this.patterns = patterns;
    }

    /** Get the aspect containing this <code>declare precedence</code>.
     */
    public Aspect getAspect() {
	return aspect;
    }

    /** Get the patterns matching the aspects to be ordered.
     *  @return a list of {@link abc.weaving.aspectinfo.ClassnamePattern} objects.
     */
    public List/*<ClassnamePattern>*/ getPatterns() {
	return patterns;
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("declare precedence: ");
	Iterator cpi = patterns.iterator();
	while (cpi.hasNext()) {
	    ClassnamePattern cp = (ClassnamePattern)cpi.next();
	    sb.append(cp);
	    if (cpi.hasNext()) {
		sb.append(", ");
	    }
	}
	sb.append(";");
	return sb.toString();
    }
}
