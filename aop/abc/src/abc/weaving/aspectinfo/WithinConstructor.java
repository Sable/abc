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
	// FIXME: Remove this once pattern is built properly
	if(getPattern()==null) 
	    return 
		method.getName().equals(SootMethod.constructorName) ?
		AlwaysMatch.v : null;

	if(!getPattern().matchesConstructor(method)) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "withinconstructor("+pattern+")";
    }
}
