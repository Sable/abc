
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

import java.util.*;

/** A base class for things that have an aspect associated with them. */
public abstract class InAspect extends Syntax {
    private Aspect aspct;

    public InAspect(Aspect aspct, Position pos) {
	super(pos);
	this.aspct = aspct;
    }

    public Aspect getAspect() {
	return aspct;
    }
}
