
package abc.weaving.aspectinfo;

import java.util.*;

import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** An argument pattern denoting any type. */
public class ArgAny extends ArgPattern {
    public ArgAny(Position pos) {
	super(pos);
    }

    public Residue matchesAt(WeavingEnv we,ContextValue cv) {
	return AlwaysMatch.v;
    }

    public String toString() {
	return "*";
    }

    public Var substituteForPointcutFormal
	(Hashtable/*<String,Var>*/ renameEnv,
	 Hashtable/*<String,AbcType>*/ typeEnv,
	 Formal formal,
	 List/*<Formal>*/ newLocals,
	 List /*<CastPointcutVar>*/ newCasts,
	 Position pos) {

	String name=Pointcut.freshVar();
	Var v=new Var(name,pos);
	
	newLocals.add(new Formal(formal.getType(),name,pos));

	return v;
    }

    public void getFreeVars(Set/*<Var>*/ result) {}

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.ArgPattern#equivalent(abc.weaving.aspectinfo.ArgPattern, java.util.Hashtable)
	 */
	public boolean equivalent(ArgPattern p, Hashtable renaming) {
		if (p instanceof ArgAny) {
			//FIXME ArgAny.equivalent(ArgType, ren) returns true; is this OK?
			System.out.println(p);
			return true;
		} else return false;
	}

}
