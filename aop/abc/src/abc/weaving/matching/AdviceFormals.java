package abc.weaving.matching;

import abc.weaving.aspectinfo.*;
import abc.weaving.residues.*;

/** Handle named pointcut variables corresponding to advice formals
 *  @author Ganesh Sittampalam
 *  @date 04-May-04
 */

public class AdviceFormals implements WeavingEnv {
    // FIXME: need to abstract away from AdviceDecl to handle cflow with binders etc
    private AdviceDecl ad;

    public AdviceFormals(AdviceDecl ad) {
	this.ad=ad;
    }

    public WeavingVar getWeavingVar(Var v) {
	return new AdviceFormal
	    (ad.getFormalIndex(v.getName()),
	     ad.getFormalType(v.getName()).getSootType());
    }

    public AbcType getAbcType(Var v) {
	return ad.getFormalType(v.getName());
    }
}
