
package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import soot.*;

/** An argument pattern denoting any number of arbitrary arguments. */
public class ArgFill extends ArgPattern {
    public ArgFill(Position pos) {
	super(pos);
    }

    public Var substituteForPointcutFormal
	(Hashtable/*<String,Var>*/ renameEnv,
	 Hashtable/*<String,AbcType>*/ typeEnv,
	 Formal formal,
	 List/*<Formal>*/ newLocals,
	 List /*<CastPointcutVar>*/ newCasts,
	 Position pos) {
	throw new RuntimeException("Internal error: Shouldn't have a ArgFill in a PointcutRef");
    }

}
