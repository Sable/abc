
package arc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

public class Aspect extends Syntax {
    private Class cl;
    private Per per;

    public Aspect(Class cl, Per per, Position pos) {
	super(pos);
	this.cl = cl;
	this.per = per;
    }

    public Class getInstanceClass() {
	return cl;
    }

    public Per getPer() {
	return per;
    }
}
