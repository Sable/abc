package abc.weaving.matching;

import polyglot.util.InternalCompilerError;
import abc.weaving.aspectinfo.*;
import abc.weaving.residues.*;

/** Handle named pointcut variables in contexts where they aren't supported
 *  @author Ganesh Sittampalam
 *  @date 04-May-04
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
