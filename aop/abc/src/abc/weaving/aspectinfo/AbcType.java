
package abc.weaving.aspectinfo;

import soot.*;

/** A Java type. */
public class AbcType {
    private soot.Type st;

    public AbcType(soot.Type st) {
	this.st = st;
    }

    public soot.Type getSootType() {
	return st;
    }

    public String toString() {
	return st.toString();
    }
}
