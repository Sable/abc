
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

import java.util.*;

/** A method signature. */
public class MethodSig extends Sig {
    private int mod;
    private AbcClass cl;
    private AbcType rtype;
    private String name;
    private List/*<Formal>*/ formals;
    private List/*<AbcClass>*/ exc;
    private SootMethod sm;
    private List/*<SootClass>*/ sexc;

    /** Create a method signature.
     *  @param formals a list of {@link abc.weaving.aspectinfo.Formal} objects
     *  @param exc a list of {@link abc.weaving.aspectinfo.AbcClass} objects
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
		AbcClass e = (AbcClass)ei.next();
		sexc.add(e.getSootClass());
	    }
	}
	return sexc;
    }

    public ClassMember getSootMember() {
	return getSootMethod();
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
		AbcClass e = (AbcClass)ei.next();
		sb.append(e);
		if (ei.hasNext()) {
		    sb.append(", ");
		}
	    }
	}
	return sb.toString();
    }

    /** Checks whether the two method signatures refer to the same method. */
    public boolean equals(Object other) {
	if (!(other instanceof MethodSig)) return false;
	MethodSig os = (MethodSig)other;
	//System.err.print("Comparing "+this+" against "+os+": ");
	boolean result =
	    cl.equals(os.cl) &&
	    rtype.equals(os.rtype) &&
	    name.equals(os.name) &&
	    formals.equals(os.formals);
	//System.err.println(result);
	return result;
    }

    public int hashCode() {
	return cl.hashCode()+rtype.hashCode()+name.hashCode()+formals.hashCode();
    }
}
