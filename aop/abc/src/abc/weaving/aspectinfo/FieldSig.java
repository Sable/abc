
package arc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** A field signature. */
public class FieldSig extends Syntax {
    private Class cl;
    private Type type;
    private String name;
    private SootField sf;

    public FieldSig(Class cl, Type type, String name, Position pos) {
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

}
