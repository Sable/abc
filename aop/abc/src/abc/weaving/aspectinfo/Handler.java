package abc.weaving.aspectinfo;

import soot.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Handler for <code>handler</code> shadow pointcut. */
public class Handler extends AbstractShadowPointcutHandler {
    private ClassnamePattern pattern;

    public Handler(ClassnamePattern pattern) {
	this.pattern = pattern;
    }

    public ClassnamePattern getPattern() {
	return pattern;
    }

    static private ShadowType shadowType=new TrapShadowType();
    
    static {
	AbstractShadowPointcutHandler.registerShadowType(shadowType);
    }

    public ShadowType getShadowType() {
	return shadowType;
    }

    public Residue matchesAt(MethodPosition position) {
	if(!(position instanceof TrapMethodPosition)) return null;
	Trap trap=((TrapMethodPosition) position).getTrap();

	// FIXME: Hack should be removed when patterns are added
	if(getPattern()==null) return AlwaysMatch.v;

	if(!getPattern().matchesClass(trap.getException())) return null;
	return AlwaysMatch.v;

    }

}
