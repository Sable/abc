
package abc.weaving.aspectinfo;

import abc.aspectj.visit.PCStructure;

import soot.*;

/** A weavable class. */
public class AbcClass {
    private String name;
    private SootClass sc;

    public AbcClass(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public SootClass getSootClass() {
	if (sc == null) {
	    sc = Scene.v().getSootClass(name);
	}
	return sc;
    }

    public String toString() {
	return name;
    }

    public void transformName(PCStructure hierarchy) {
	name = hierarchy.transformClassName(name);
    }
}
