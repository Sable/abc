package abc.weaving.aspectinfo;

import soot.*;
import polyglot.util.Position;
import abc.weaving.residues.*;

/** Handler for <code>withincode</code> lexical pointcut with a constructor pattern
 *  @author Ganesh Sittampalam
 *  @date 01-May-04
 */
public class WithinConstructor extends LexicalPointcut {
    private ConstructorPattern pattern;

    public WithinConstructor(ConstructorPattern pattern,Position pos) {
	super(pos);
	this.pattern = pattern;
    }

    public ConstructorPattern getPattern() {
	return pattern;
    }

    protected Residue matchesAt(SootClass cls,SootMethod method) {
	if(!method.getName().equals(SootMethod.constructorName))
	    return null;

	// FIXME: Remove this once pattern is built properly
	if(getPattern()==null) return AlwaysMatch.v;

	if(!getPattern().matchesConstructor(method)) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "withinconstructor("+pattern+")";
    }

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof WithinConstructor) {
	    return pattern.equivalent(((WithinConstructor)otherpc).getPattern());
	} else return false;
    }

}
