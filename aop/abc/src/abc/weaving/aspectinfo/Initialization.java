package abc.weaving.aspectinfo;

import soot.*;

import abc.weaving.residues.*;
import abc.weaving.matching.*;

/** Handler for <code>initialization</code> shadow pointcut. */
public class Initialization extends AbstractShadowPointcutHandler {
    static private ShadowType shadowType=new InitializationShadowType();
    
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
	return "initialization()";
    }
}
