package abc.weaving.aspectinfo;

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

    static private ShadowType shadowType=new ExecutionShadowType();
    
    static {
	ShadowPointcut.registerShadowType(shadowType);
    }

    public ShadowType getShadowType() {
	return shadowType;
    }

    protected Residue matchesAt(MethodPosition pos) {
	if(!(pos instanceof WholeMethodPosition)) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "execution()";
    }

}
