package abc.weaving.aspectinfo;

import soot.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

/** A joinpoint shadow that applies at "execution"
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04
 */
public class ExecutionShadowType implements ShadowType {
    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue,
				     MethodPosition pos) {
	mal.stmtAdvice.add
	    (new ExecutionAdviceApplication(ad,residue));
    }
}
