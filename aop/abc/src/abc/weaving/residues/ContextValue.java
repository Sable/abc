package abc.weaving.residues;

import soot.Value;
import soot.SootMethod;
import abc.weaving.weaver.LocalGeneratorEx;

/** The interface defining a value to be extracted from the context
 *  @author Ganesh Sittampalam
 *  @date 30-Apr-04
 */ 

public interface ContextValue {
    /** get the soot immediate value corresponding to this contextvalue */
    public Value getSootValue(SootMethod method,LocalGeneratorEx localgen);
}
