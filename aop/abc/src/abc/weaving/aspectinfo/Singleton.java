package abc.weaving.aspectinfo;

import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.residues.*;


/** A <code>singleton</code> per clause. */
public class Singleton extends Per {
    public Singleton(Position pos) {
	super(pos);
    }

    public String toString() {
	return "issingleton";
    }

    public void registerSetupAdvice(Aspect aspct) {}

    public Residue matchesAt(Aspect aspct,ShadowMatch sm) {
	return AlwaysMatch.v;
    }

    public Residue getAspectInstance(Aspect aspct,ShadowMatch sm) {
	return new AspectOf(aspct.getInstanceClass().getSootClass(),null);
    }
}
