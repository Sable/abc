package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

import abc.weaving.residues.*;
import abc.weaving.matching.*;

/** Handler for <code>execution</code> shadow pointcut with a method pattern. */
public class Execution extends AbstractShadowPointcutHandler {

    static private ShadowType shadowType=new ExecutionShadowType();
    
    static {
	AbstractShadowPointcutHandler.registerShadowType(shadowType);
    }

    public ShadowType getShadowType() {
	return shadowType;
    }

    public Residue matchesAt(MethodPosition pos) {
	if(!(pos instanceof WholeMethodPosition)) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "execution()";
    }

}
