
package arc.weaving.aspectinfo;

import soot.*;

public class Type {
    private String name;
    private soot.Type st;

    public Type(String name) {
	this.name = name;
    }

    public Type(soot.Type st) {
	this.st = st;
    }

    public soot.Type getSootType() {
	if (st == null) {
	    //TODO: Something
	}
	return st;
    }

}
