package abc.weaving.aspectinfo;

import soot.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

/** A joinpoint shadow that applies at "preinitialization"
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04
 */
public class PreinitializationShadowType extends AbstractShadowType {
    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue,
				     MethodPosition pos) {
	mal.constructorAdvice.add
	    (new PreinitializationAdviceApplication(ad,residue));
    }
}
