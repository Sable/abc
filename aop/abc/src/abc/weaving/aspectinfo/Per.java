package abc.weaving.aspectinfo;

import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** A per clause. */
public abstract class Per extends Syntax {
    public Per(Position pos) {
	super(pos);
    }

    public abstract String toString();

    /** Register any advice declarations required to setup the aspect instances */
    public abstract void registerSetupAdvice(Aspect aspct);

    // These are separate because we want to check for the aspect first (if appropriate), 
    // but bind the local last. They are residues because in the case of proper per-advice,
    // we need shadow-specific stuff like the target.
    public abstract Residue matchesAt(Aspect aspct,ShadowMatch sm);
    public abstract Residue getAspectInstance(Aspect aspct,ShadowMatch sm);
}
