package abc.weaving.aspectinfo;

import soot.*;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** A category of joinpoint shadows.
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04
 */
public interface ShadowType {
    /** Could a given MethodPosition match here? */
    public boolean couldMatch(MethodPosition pos);

    /** Add a new advice application to the appropriate bit of a method advice list */
    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue,
				     MethodPosition pos);

    public ContextValue getThisContextValue(SootMethod method);
}
