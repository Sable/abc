package abc.weaving.residues;

import soot.*;

/** The base class defining a value to be extracted from the context
 *  @author Ganesh Sittampalam
 *  @date 30-Apr-04
 */ 

public abstract class ContextValue {
    /** Force subclasses to implement toString */
    public abstract String toString();

    public abstract Type getSootType();

    /** get a soot value corresponding to this contextvalue */
    public abstract Value getSootValue();
}
