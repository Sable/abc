package abc.weaving.aspectinfo;

import soot.*;

import abc.weaving.residues.*;
import abc.weaving.matching.*;

// FIXME: Currently incomplete; need to do a transformation in the front end,
// add a withinadvice dummy pointcut, and
// remove this entire class if the transformation behaviour of
// adviceexecution() => withinadvice() && execution() is correct

/** Handler for <code>adviceexecution</code> shadow pointcut. */
public class AdviceExecution extends AbstractShadowPointcutHandler {

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
	return "adviceexecution()";
    }
}
