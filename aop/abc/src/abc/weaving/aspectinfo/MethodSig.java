
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

import java.util.*;

/** A method signature. */
public class MethodSig extends Syntax {
    private int mod;
    private AbcClass cl;
    private AbcType rtype;
    private String name;
    private List/*<Formal>*/ formals;
    private List/*<String>*/ exc;
    private SootMethod sm;
    private List/*<SootClass>*/ sexc;

    /** Create a method signature.
     *  @param formals a list of {@link abc.weaving.aspectinfo.Formal} objects
     *  @param exc a list of {@link java.lang.String} objects
     */
    public MethodSig(int mod, AbcClass cl, AbcType rtype, String name, List formals, List exc, Position pos) {
	super(pos);
	this.mod = mod;
	this.cl = cl;
	this.rtype = rtype;
	this.name = name;
	this.formals = formals;
	this.exc = exc;
    }

    public int getModifiers() {
	return mod;
    }

    public AbcClass getDeclaringClass() {
	return cl;
    }

    public AbcType getReturnType() {
	return rtype;
    }

    public String getName() {
	return name;
    }

    /** Get the formals of the method.
     *  @return a list of {@link abc.weaving.aspectinfo.Formal} objects.
     */
    public List getFormals() {
	return formals;
    }

    /** Get the exceptions thrown by the method.
     *  @return a list of {@link soot.SootClass} objects.
     */
    public List getExceptions() {
	if (sexc == null) {
	    sexc = new ArrayList();
	    Iterator ei = exc.iterator();
	    while (ei.hasNext()) {
		String e = (String)ei.next();
		sexc.add(Scene.v().getSootClass(e));
	    }
	}
	return sexc;
    }

    public SootMethod getSootMethod() {
	if (sm == null) {
	    SootClass sc = cl.getSootClass();
	    soot.Type srt = rtype.getSootType();
	    List spt = new ArrayList();
	    Iterator fi = formals.iterator();
	    while (fi.hasNext()) {
		Formal f = (Formal)fi.next();
		spt.add(f.getType().getSootType());
	    }
	    sm = sc.getMethod(name, spt);
	}
	return sm;
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append(Modifier.toString(mod));
	sb.append(" ");
	sb.append(rtype);
	sb.append(" ");
	sb.append(cl);
	sb.append(".");
	sb.append(name);
	sb.append("(");
	Iterator fi = formals.iterator();
	while (fi.hasNext()) {
	    Formal f = (Formal)fi.next();
	    sb.append(f.getType());
	    sb.append(" ");
	    sb.append(f.getName());
	    if (fi.hasNext()) {
		sb.append(", ");
	    }
	}
	sb.append(")");
	if (exc.size() > 0) {
	    sb.append(" throws ");
	    Iterator ei = exc.iterator();
	    while (ei.hasNext()) {
		String e = (String)ei.next();
		sb.append(e);
		if (ei.hasNext()) {
		    sb.append(", ");
		}
	    }
	}
	return sb.toString();
    }
}
