package abc.weaving.matching;

import abc.weaving.aspectinfo.*;
import abc.weaving.residues.*;

/** Handle named pointcut variables corresponding to advice formals
 *  @author Ganesh Sittampalam
 *  @date 04-May-04
 */

public class AdviceFormals implements WeavingEnv {
    private AdviceDecl ad;

    public AdviceFormals(AdviceDecl ad) {
	this.ad=ad;
    }

    public WeavingVar getWeavingVar(Var v) {
	return new AdviceFormal(ad.getFormalIndex(v.getName()));
    }

    public AbcType getAbcType(Var v) {
	return ad.getFormalType(v.getName());
    }
}
