package abc.weaving.aspectinfo;

import soot.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;
import abc.soot.util.LocalGeneratorEx;


/** An advice specification. */
public interface AdviceSpec {
    public AbstractAdviceDecl getAdvice();
    public boolean isAfter();
    public Residue matchesAt(WeavingEnv we,ShadowMatch sm);
    public void weave(SootMethod method,LocalGeneratorEx localgen,AdviceApplication adviceappl);
}
