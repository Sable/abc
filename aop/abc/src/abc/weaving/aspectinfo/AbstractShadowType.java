package abc.weaving.aspectinfo;

import soot.*;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** A convenient base class for ShadowType classes
 *  @author Ganesh Sittampalam
 *  @date 04-May-04;
 */
public abstract class AbstractShadowType implements ShadowType {
    // FIXME: move this to subclasses(?)
    public ContextValue getThisContextValue(SootMethod method) {
	// FIXME: check if method is static
	return new This();
    }
	
}
