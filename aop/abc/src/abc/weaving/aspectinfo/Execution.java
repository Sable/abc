package abc.weaving.aspectinfo;

import java.util.Hashtable;

import soot.*;
import soot.jimple.*;

import polyglot.util.Position;

import abc.weaving.residues.*;
import abc.weaving.matching.*;

/** Handler for <code>execution</code> shadow pointcut with a method pattern. */
public class Execution extends ShadowPointcut {
    public Execution(Position pos) {
	super(pos);
    }

    protected Residue matchesAt(ShadowMatch sm) {
	if(!(sm instanceof ExecutionShadowMatch)) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "execution()";
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		if (otherpc instanceof Execution) {
			return true;
		} else return false;
	}

}
