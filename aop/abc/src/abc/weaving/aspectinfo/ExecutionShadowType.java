package abc.weaving.aspectinfo;

import soot.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

/** A joinpoint shadow that applies at "execution"
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04
 */
public class ExecutionShadowType extends AbstractShadowType {
    public boolean couldMatch(MethodPosition pos) {
	return pos instanceof WholeMethodPosition;
    }

    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue,
				     MethodPosition pos) {
	mal.bodyAdvice.add
	    (new ExecutionAdviceApplication(ad,residue));
    }
}
