
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** An argument pattern denoting any number of arbitrary arguments. */
public class ArgFill extends ArgPattern {
    public ArgFill(Position pos) {
	super(pos);
    }

}
