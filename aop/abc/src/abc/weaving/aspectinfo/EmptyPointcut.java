package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

import abc.weaving.residues.*;

/** A pointcut designator representing no join points
 *  at all
 *  @author Ganesh Sittampalam
 *  @date 30-Apr-04
 */

public class EmptyPointcut extends LexicalPointcut {

    public EmptyPointcut(Position pos) {
	super(pos);
    }

    protected Residue matchesAt(SootClass cls,SootMethod method) {
	return NeverMatch.v;
    }
    
    public String toString() {
	return "empty";
    }

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof EmptyPointcut) {
	    return true;
	} else return false;
    }

}
