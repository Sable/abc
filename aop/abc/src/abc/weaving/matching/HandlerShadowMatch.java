package abc.weaving.matching;

import soot.*;
import soot.jimple.*;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;

/** The results of matching at a handler shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class HandlerShadowMatch extends ShadowMatch {
    
    private Stmt stmt;
    private SootClass sootexc;
    
    private HandlerShadowMatch(Stmt stmt,SootClass sootexc) {
	this.stmt=stmt;
	this.sootexc=sootexc;
    }

    public SootClass getException() {
	return sootexc;
    }

    public static HandlerShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof TrapMethodPosition)) return null;
	Trap trap=((TrapMethodPosition) pos).getTrap();
	Stmt stmt=(Stmt) trap.getHandlerUnit();
	return new HandlerShadowMatch(stmt,trap.getException());
    }

    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue) {
        mal.addStmtAdvice(new HandlerAdviceApplication(ad,residue,stmt));
    }
}
