package abc.weaving.aspectinfo;

import soot.*;

import java.util.*;

/** Handler for a pointcut reference. */
public class PointcutRef extends AbstractOtherPointcutHandler {
    private Object decl_key;
    private Map/*<Object,PointcutDecl>*/ decl_map;
    private PointcutDecl decl;
    private List/*<ArgPattern>*/ args;

    /** Create an <code>args</code> pointcut.
     *  @param decl_key an object that can later be resolved into the pointcut declaration.
     *  @param decl_map a map from {@link java.lang.Object} to {@link abc.weaving.aspectinfo.PointcutDecl}.
     *  @param args a list of {@link abc.weaving.aspectinfo.ArgPattern} objects
     */
    public PointcutRef(Object decl_key, Map decl_map, List args) {
	this.decl_key = decl_key;
	this.decl_map = decl_map;
	this.args = args;
    }

    /** Create an <code>args</code> pointcut.
     *  @param decl the pointcut declaration.
     *  @param args a list of {@link abc.weaving.aspectinfo.ArgPattern} objects
     */
    public PointcutRef(PointcutDecl decl, List args) {
	this.decl = decl;
	this.args = args;
    }

    public PointcutDecl getDecl() {
	if (decl == null) {
	    decl = (PointcutDecl) decl_map.get(decl_key);
	    decl_key = null;
	    decl_map = null;
	}
	return decl;
    }

    /** Get the list of argument patterns.
     *  @return a list of {@link abc.weaving.aspectinfo.ArgPattern} objects
     */
    public List getArgs() {
	return args;
    }

    public String toString() {
	return getDecl().getName()+"(...)";
    }
}
