package abc.weaving.aspectinfo;

import soot.*;

import polyglot.util.Position;

import abc.weaving.residues.*;
import abc.weaving.matching.*;

/** Handler for <code>initialization</code> shadow pointcut. */
public class InterfaceInitialization extends ShadowPointcut {
    private ClassnamePattern pattern;

    public InterfaceInitialization(ClassnamePattern pattern,Position pos) {
	super(pos);
	this.pattern=pattern;
    }

    public ClassnamePattern getPattern() {
	return pattern;
    }


    protected Residue matchesAt(ShadowMatch sm) {
	if(!(sm instanceof InterfaceInitializationShadowMatch)) return null;
	InterfaceInitializationShadowMatch ism=(InterfaceInitializationShadowMatch) sm;
	if(!(getPattern().matchesClass(ism.getInterface()))) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "interfaceinitialization()";
    }

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof InterfaceInitialization) {
	    return pattern.equivalent(((InterfaceInitialization)otherpc).getPattern());
	} else return false;
    }

}