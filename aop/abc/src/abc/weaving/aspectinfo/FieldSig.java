
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** A field signature. */
public class FieldSig extends Syntax {
    private AbcClass cl;
    private AbcType type;
    private String name;
    private SootField sf;

    public FieldSig(AbcClass cl, AbcType type, String name, Position pos) {
	super(pos);
	this.cl = cl;
	this.type = type;
	this.name = name;
    }

    public SootField getSootField() {
	if (sf == null) {
	    SootClass sc = cl.getSootClass();
	    soot.Type st = type.getSootType();
	    sf = sc.getField(name, st);
	}
	return sf;
    }

    public String toString() {
	return type+" "+cl+"."+name;
    }
}
