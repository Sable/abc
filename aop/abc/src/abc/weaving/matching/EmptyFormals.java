package abc.weaving.matching;

import polyglot.util.InternalCompilerError;
import abc.weaving.aspectinfo.*;
import abc.weaving.residues.*;

/** An empty weaving environment, for use in contexts where named pointcut
 *  variables aren't supported.
 *  @author Ganesh Sittampalam
 */

public class EmptyFormals implements WeavingEnv {
    public WeavingVar getWeavingVar(Var v) {
	throw new InternalCompilerError
	    ("Undefined variable "+v.getName()+" escaped frontend",v.getPosition());
    }

    public AbcType getAbcType(Var v) {
	throw new InternalCompilerError
	    ("Undefined variable "+v.getName()+" escaped frontend",v.getPosition());
    }
}
