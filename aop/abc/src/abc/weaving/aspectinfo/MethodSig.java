
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

import java.util.*;

/** A method signature. */
public class MethodSig extends Syntax {
    private AbcClass cl;
    private AbcType rtype;
    private String name;
    private List/*<AbcType>*/ params;
    private SootMethod sm;

    /** Create a method signature.
     *  @param params a list of {@link abc.weaving.aspectinfo.Type} objects
     */
    public MethodSig(AbcClass cl, AbcType rtype, String name, List params, Position pos) {
	super(pos);
	this.cl = cl;
	this.rtype = rtype;
	this.name = name;
	this.params = params;
    }

    public SootMethod getSootMethod() {
	if (sm == null) {
	    SootClass sc = cl.getSootClass();
	    soot.Type srt = rtype.getSootType();
	    List spt = new ArrayList();
	    Iterator pi = params.iterator();
	    while (pi.hasNext()) {
		AbcType t = (AbcType)pi.next();
		spt.add(t.getSootType());
	    }
	    sm = sc.getMethod(name, spt);
	}
	return sm;
    }
}
