package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

/** A joinpoint shadow that applies at an exception handler
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04
 */
public class TrapShadowType implements ShadowType {
    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue,
				     MethodPosition pos) {
	mal.stmtAdvice.add
	    (new StmtAdviceApplication
	     (ad,
	      residue,
	      (Stmt) (((TrapMethodPosition) pos).getTrap().getHandlerUnit())));
	    
    }
}
