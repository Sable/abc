package abc.weaving.aspectinfo;

import soot.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

/** A joinpoint shadow that applies at "initialization"
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04
 */
public class InitializationShadowType extends AbstractShadowType {
    public boolean couldMatch(MethodPosition pos) {
	return pos instanceof WholeMethodPosition;
    }

    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue,
				     MethodPosition pos) {
	if(debugResidues) System.out.println("pos: "+pos.getClass());
	mal.initializationAdvice.add
	    (new InitializationAdviceApplication(ad,residue));
    }
}
