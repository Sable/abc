package abc.weaving.aspectinfo;

import soot.*;

import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;

import abc.weaving.residues.Residue;


/** An advice specification. */
public interface AdviceSpec {
    public AbstractAdviceDecl getAdvice();

    public Residue matchesAt(WeavingEnv we,ShadowMatch sm);
}
