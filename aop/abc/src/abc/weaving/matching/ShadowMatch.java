package abc.weaving.matching;

import soot.*;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.*;

/** The results of matching at a particular shadow type
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public abstract class ShadowMatch {
    /** Add a new advice application to the appropriate bit of a 
	method advice list */
    public abstract void addAdviceApplication(MethodAdviceList mal,
					      AdviceDecl ad,
					      Residue residue);

    // FIXME: move this to subclasses(?)
    public ContextValue getThisContextValue(SootMethod method) {
        return method.isStatic() ? null : new This();
    }
}
