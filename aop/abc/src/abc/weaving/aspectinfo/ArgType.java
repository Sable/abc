
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

/** An argument pattern denoting a specific type. */
public class ArgType extends ArgAny {
    private AbcType type;

    public ArgType(AbcType type, Position pos) {
	super(pos);
	this.type = type;
    }

    public AbcType getType() {
	return type;
    }

    // inherit substituteForPointcutFormal from ArgAny;
    // as far as I can tell the rules about what is
    // permitted make doing a dynamic type test completely
    // pointless anyway

}
