package abc.weaving.aspectinfo;

import polyglot.util.*;

import abc.weaving.matching.*;
import abc.weaving.residues.*;
import abc.polyglot.util.ErrorInfoFactory;

/** Abstract base class for all forms of "after" advice
 *  @author Ganesh Sittampalam
 */
public abstract class AbstractAfterAdvice extends AbstractAdviceSpec {
    public AbstractAfterAdvice(Position pos) {
	super(pos);
    }

    public Residue matchesAt(WeavingEnv we,ShadowMatch sm,AbstractAdviceDecl ad) {
	if(sm.supportsAfter()) return AlwaysMatch.v;
	// FIXME: should be a multi-position error
	if(ad instanceof AdviceDecl)
	    abc.main.Main.v().error_queue.enqueue
		(ErrorInfoFactory.newErrorInfo
		 (ErrorInfo.WARNING,
		  sm.joinpointName()+" join points do not support after advice, but some advice from "+ad.errorInfo()
		  +" would otherwise apply here",
		  sm.getContainer(),
		  sm.getHost()));
	      
	return null;
    }

}