package abc.weaving.aspectinfo;

import soot.*;
import polyglot.util.Position;
import abc.weaving.residues.*;

/** Handler for <code>withinstaticinitializer</code> lexical pointcut
 *  @author Ganesh Sittampalam
 *  @date 01-May-04
 */
public class WithinStaticInitializer extends LexicalPointcut {

    public WithinStaticInitializer(Position pos) {
	super(pos);
    }

    protected Residue matchesAt(SootClass cls,SootMethod method) {
	if(!method.isStatic()) return null;
	if(!method.getName().equals(SootMethod.staticInitializerName)) 
	    return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "withinstaticinitializer()";
    }
}
