
package abc.weaving.aspectinfo;

import polyglot.util.Position;

/** Base class for anything that corresponds to some syntax in the program. */
public class Syntax {
    private Position pos;

    public Syntax(Position pos) {
	this.pos = pos;
    }

    public Position getPosition() {
	return pos;
    }
}
