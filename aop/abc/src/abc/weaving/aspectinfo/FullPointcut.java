package abc.weaving.aspectinfo;

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

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof FullPointcut) {
	    return true;
	} else return false;
    }

}
