
package abc.weaving.aspectinfo;

import soot.*;

/** A Java type. */
public class AbcType {
    private String name;
    private soot.Type st;

    public AbcType(String name) {
	this.name = name;
    }

    public AbcType(soot.Type st) {
	this.st = st;
    }

    public soot.Type getSootType() {
	if (st == null) {
	    //TODO: Something
	}
	return st;
    }

}
