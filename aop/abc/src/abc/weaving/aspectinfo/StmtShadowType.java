package abc.weaving.aspectinfo;

import soot.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

/** A joinpoint shadow that applies at a statement
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04
 */
public class StmtShadowType implements ShadowType {
    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue,
				     MethodPosition pos) {
	mal.stmtAdvice.add
	    (new StmtAdviceApplication
	     (ad,
	      residue,
	      ((StmtMethodPosition) pos).getStmt()));
    }
}
