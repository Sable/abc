
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** A field signature. */
public class FieldSig extends Syntax {
    private int mod;
    private AbcClass cl;
    private AbcType type;
    private String name;
    private SootField sf;

    public FieldSig(int mod, AbcClass cl, AbcType type, String name, Position pos) {
	super(pos);
	this.mod = mod;
	this.cl = cl;
	this.type = type;
	this.name = name;
    }

    public int getModifiers() {
	return mod;
    }

    public AbcClass getDeclaringClass() {
	return cl;
    }

    public AbcType getType() {
	return type;
    }

    public String getName() {
	return name;
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
	return Modifier.toString(mod)+" "+type+" "+cl+"."+name;
    }

    public boolean equals(Object other) {
	if (!(other instanceof FieldSig)) return false;
	FieldSig os = (FieldSig)other;
	return
	    mod == os.mod &&
	    cl.equals(os.cl) &&
	    type.equals(os.type) &&
	    name.equals(os.name);
    }
}
