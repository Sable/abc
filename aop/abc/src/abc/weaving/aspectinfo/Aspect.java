
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** An aspect in the program. */
public class Aspect extends Syntax {
    private AbcClass cl;
    private Per per;

    public Aspect(AbcClass cl, Per per, Position pos) {
	super(pos);
	this.cl = cl;
	this.per = per;
    }

    public AbcClass getInstanceClass() {
	return cl;
    }

    public Per getPer() {
	return per;
    }

    public void setPer(Per per) {
	this.per=per;
    }

    public String getName() {
	return cl.getJvmName();
    }

    public String toString() {
	return "aspect "+getName()+" "+per;
    }

    public boolean equals(Object other) {
	if (!(other instanceof Aspect)) return false;
	Aspect oa = (Aspect)other;
	return cl.equals(oa.cl);
    }

    public int hashCode() {
	return cl.hashCode();
    }
}
