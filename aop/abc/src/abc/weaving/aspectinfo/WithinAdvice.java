package abc.weaving.aspectinfo;

import soot.*;
import polyglot.util.Position;
import abc.weaving.residues.*;

/** Handler for <code>withinadvice</code> lexical pointcut
 *  @author Ganesh Sittampalam
 *  @date 01-May-04
 */
public class WithinAdvice extends LexicalPointcut {

    public WithinAdvice(Position pos) {
	super(pos);
    }

    protected Residue matchesAt(SootClass cls,SootMethod method) {
	// FIXME : check we're in advice!
	return AlwaysMatch.v;
    }

    public String toString() {
	return "withinadvice()";
    }
}
