
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** A pattern for a single argument. */
public abstract class ArgPattern extends Syntax {
    public ArgPattern(Position pos) {
	super(pos);
    }

}
