package abc.weaving.aspectinfo;

import soot.*;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

// FIXME: Currently incomplete; need to do a transformation in the front end,
// add a withinsi dummy pointcut, and
// remove this entire class if the transformation behaviour of
// staticinitialization(Foo) => within(Foo) && withinsi() && execution() is correct

/** Handler for <code>staticinitialization</code> shadow pointcut. */
public class StaticInitialization extends AbstractShadowPointcutHandler {
    private ClassnamePattern pattern;

    public StaticInitialization(ClassnamePattern pattern) {
	this.pattern = pattern;
    }

    public ClassnamePattern getPattern() {
	return pattern;
    }

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
	return "staticinitialization()";
    }
}
