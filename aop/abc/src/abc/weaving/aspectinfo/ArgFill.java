
package abc.weaving.aspectinfo;

import java.util.*;

import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.*;


/** An argument pattern denoting any number of arbitrary arguments. */
public class ArgFill extends ArgPattern {
    public ArgFill(Position pos) {
	super(pos);
    }

    public Residue matchesAt(WeavingEnv we,ContextValue cv) {
	throw new RuntimeException("Internal error: ArgFill should be special-cased in an args() list");
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

    public void getFreeVars(Set/*<Var>*/ result) {}

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.ArgPattern#equivalent(abc.weaving.aspectinfo.ArgPattern, java.util.Hashtable)
	 */
	public boolean equivalent(ArgPattern p, Hashtable renaming) {
		if (p instanceof ArgFill) {
			return true;
		} else return false;
	}

}
