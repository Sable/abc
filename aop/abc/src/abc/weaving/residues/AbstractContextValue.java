package abc.weaving.residues;

import soot.Value;
import soot.SootMethod;
import abc.soot.util.LocalGeneratorEx;

/** A convenient base class for context values
 *  @author Ganesh Sittampalam
 *  @date 30-Apr-04
 */ 

public abstract class AbstractContextValue implements ContextValue {
    /** Force subclasses to implement toString */
    public abstract String toString();

    public Value getSootValue(SootMethod method,LocalGeneratorEx localgen) {
	throw new RuntimeException("context value not implemented: "+this);
    }
}
