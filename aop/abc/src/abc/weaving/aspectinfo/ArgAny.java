
package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import soot.*;

/** An argument pattern denoting any type. */
public class ArgAny extends ArgPattern {
    public ArgAny(Position pos) {
	super(pos);
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

}
