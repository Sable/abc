package abc.weaving.aspectinfo;

import soot.*;

import abc.weaving.residues.*;
import abc.weaving.matching.*;

/** Handler for <code>preinitialization</code> shadow pointcut. */
public class Preinitialization extends AbstractShadowPointcutHandler {
    static private ShadowType shadowType=new PreinitializationShadowType();
    
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
	return "preinitialization()";
    }

}
