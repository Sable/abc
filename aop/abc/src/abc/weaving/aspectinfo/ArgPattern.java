
package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import soot.*;

/** A pattern for a single argument. */
public abstract class ArgPattern extends Syntax {
    public ArgPattern(Position pos) {
	super(pos);
    }


    public abstract Var substituteForPointcutFormal
	(Hashtable/*<String,Var>*/ renameEnv,
	 Hashtable/*<String,AbcType>*/ typeEnv,
	 Formal formal,
	 List/*<Formal>*/ newLocals,
	 List /*<CastPointcutVar>*/ newCasts,
	 Position pos);

}
