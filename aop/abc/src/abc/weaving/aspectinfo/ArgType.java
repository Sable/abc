
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** An argument pattern denoting a specific type. */
public class ArgType extends ArgPattern {
    private AbcType type;

    public ArgType(AbcType type, Position pos) {
	super(pos);
	this.type = type;
    }

    public AbcType getType() {
	return type;
    }

}
