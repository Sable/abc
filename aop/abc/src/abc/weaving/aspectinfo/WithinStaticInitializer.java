package abc.weaving.aspectinfo;

import java.util.Hashtable;

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

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof WithinStaticInitializer) {
	    return true;
	} else return false;
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		if (otherpc instanceof WithinStaticInitializer) {
			return true;
		} else return false;
	}

}
