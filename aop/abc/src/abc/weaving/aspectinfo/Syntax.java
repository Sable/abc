
package arc.weaving.aspectinfo;

import polyglot.util.Position;

public class Syntax {
    private Position pos;

    public Syntax(Position pos) {
	this.pos = pos;
    }

    public Position getPosition() {
	return pos;
    }
}
