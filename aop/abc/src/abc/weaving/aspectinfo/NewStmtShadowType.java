package abc.weaving.aspectinfo;

import soot.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

/** A joinpoint shadow that applies at a "new" statement
 *  (and the constructor call after it)
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04
 */
public class NewStmtShadowType extends AbstractShadowType {
    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue,
				     MethodPosition pos) {
	mal.stmtAdvice.add
	    (new NewStmtAdviceApplication
	     (ad,
	      residue,
	      ((NewStmtMethodPosition) pos).getStmt()));
    }
}
