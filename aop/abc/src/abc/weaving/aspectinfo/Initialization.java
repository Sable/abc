package abc.weaving.aspectinfo;

import soot.*;

import polyglot.util.Position;

import abc.weaving.residues.*;
import abc.weaving.matching.*;

/** Handler for <code>initialization</code> shadow pointcut. */
public class Initialization extends ShadowPointcut {

    public Initialization(Position pos) {
	super(pos);
    }

    static private ShadowType shadowType=new InitializationShadowType();
    
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
	return "initialization()";
    }
}
