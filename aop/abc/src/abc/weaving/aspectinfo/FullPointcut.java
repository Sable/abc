package abc.weaving.aspectinfo;

import java.util.Hashtable;

import polyglot.util.Position;

import soot.*;

import abc.weaving.residues.*;

/** A pointcut designator representing every join point
 *  @author Ganesh Sittampalam
 */

public class FullPointcut extends LexicalPointcut {

    public FullPointcut(Position pos) {
	super(pos);
    }

    protected Residue matchesAt(SootClass cls,SootMethod method) {
	return AlwaysMatch.v();
    }
    
    public String toString() {
	return "full";
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		if (otherpc instanceof FullPointcut) {
			return true;
		} else return false;
	}

}
