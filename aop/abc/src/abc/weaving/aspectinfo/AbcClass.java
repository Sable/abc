
package abc.weaving.aspectinfo;

import polyglot.types.Type;

import abc.aspectj.visit.PCStructure;

import soot.Scene;
import soot.SootClass;

/** A class handled by the ABC compiler. */
public class AbcClass {
    private String java_name;
    private String jvm_name;
    private Type polyglot_type;
    private SootClass sc;

    AbcClass(Type polyglot_type, String java_name) {
	this.polyglot_type = polyglot_type;
	this.java_name = java_name;
	polyglot_type.toString();
    }

    AbcClass(Type polyglot_type) {
	this.polyglot_type = polyglot_type;
	polyglot_type.toString();
    }

    AbcClass(SootClass sc) {
	this.sc = sc;
	this.jvm_name = sc.getName();
    }

    public Type getPolyglotType() {
	return polyglot_type;
    }

    public String getJavaName() {
	return java_name;
    }

    public String getJvmName() {
	if (jvm_name == null) {
	    //System.err.println(((polyglot.types.ClassType)polyglot_type).fullName());
	    jvm_name = soot.javaToJimple.Util.getSootType(polyglot_type).toString();
	}
	return jvm_name;
    }

    public SootClass getSootClass() {
	if (sc == null) {
	    sc = Scene.v().getSootClass(getJvmName());
	}
	return sc;
    }

    public String toString() {
	return getJvmName();
    }

    public boolean equals(Object other) {
	if (!(other instanceof AbcClass)) return false;
	AbcClass oc = (AbcClass)other;
	if (polyglot_type != null && oc.polyglot_type != null)
	    return polyglot_type.equals(oc.polyglot_type);
	return getSootClass().equals(oc.getSootClass());
    }

    public int hashCode() {
	// No nontrivial valid hashcode possible
	return 0;
    }
}
