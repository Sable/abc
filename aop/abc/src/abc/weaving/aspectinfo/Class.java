
package arc.weaving.aspectinfo;

import soot.*;

/** A weavable class. */
public class Class {
    private String name;
    private SootClass sc;

    public Class(String name) {
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
}
