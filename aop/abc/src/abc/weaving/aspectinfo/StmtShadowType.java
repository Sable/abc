package abc.weaving.aspectinfo;

import soot.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

/** A joinpoint shadow that applies at a statement
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04
 */
public class StmtShadowType extends AbstractShadowType {
    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue,
				     MethodPosition pos) {
	System.out.println("pos: "+pos.getClass());
	mal.stmtAdvice.add
	    (new StmtAdviceApplication
	     (ad,
	      residue,
	      ((StmtMethodPosition) pos).getStmt()));
    }
}
