package abc.weaving.matching;

import abc.weaving.aspectinfo.*;
import abc.weaving.residues.WeavingVar;

/** Provides the mapping from named variables in pointcuts
 *  to the weaving position and type
 *  @author Ganesh Sittampalam
 *  @date 04-May-04
 */

public interface WeavingEnv {
    public WeavingVar getWeavingVar(Var v);
    public AbcType getAbcType(Var v);
}
