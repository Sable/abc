package abc.weaving.aspectinfo;

import soot.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;
import abc.soot.util.LocalGeneratorEx;


/** An advice specification. */
public interface AdviceSpec {
    
    /** Is this advice spec for "after" advice? 
     *  This affects the precedence of the advice.
     */
    public boolean isAfter();
    public Residue matchesAt(WeavingEnv we,ShadowMatch sm);

    /** Weave a specific advice application into the given method
     *  using the given local generator. The AdviceSpec is used to
     *  dispatch to the correct weaving method for the advice type.
     *  @author Ganesh Sittampalam
     */
    public void weave(SootMethod method,
		      LocalGeneratorEx localgen,
		      AdviceApplication adviceappl);
}
