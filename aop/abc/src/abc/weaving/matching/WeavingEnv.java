package abc.weaving.matching;

import abc.weaving.aspectinfo.*;
import abc.weaving.residues.WeavingVar;

/** Provides the mapping from named variables in pointcuts
 *  to the weaving position and type. It is generated
 *  from the advice declaration, and used during pointcut
 *  matching to check the declared type of pointcut variables
 *  and to construct residues which bind values to these
 *  variables.
 *  @author Ganesh Sittampalam
 */
public interface WeavingEnv {

    /** Return the weaving variable corresponding to the given named
     *  pointcut variable
     *  @param v The pointcut variable
     *  @author Ganesh Sittampalam
     */
    public WeavingVar getWeavingVar(Var v);

    /** Return the declared type of the given named pointcut variable
     *  @param v The pointcut variable
     *  @author Ganesh Sittampalam
     */
    public AbcType getAbcType(Var v);
}
