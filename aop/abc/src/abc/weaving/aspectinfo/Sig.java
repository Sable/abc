
package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

import java.util.*;

/** A method or field signature. */
public abstract class Sig extends Syntax {
    public Sig(Position pos) {
	super(pos);
    }

    public abstract int getModifiers();

    public abstract AbcClass getDeclaringClass();

    public abstract String getName();

    public abstract ClassMember getSootMember();

}
