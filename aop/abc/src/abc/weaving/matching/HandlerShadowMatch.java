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

    
    private HandlerShadowMatch(SootMethod container,Stmt stmt,SootClass sootexc) {
	super(container);
	this.stmt=stmt;
	this.sootexc=sootexc;
    }

    public ShadowMatch getEnclosing() {
	if(stmt.hasTag(abc.soot.util.InPreinitializationTag.name)) return this;
	return new ExecutionShadowMatch(container);
    }

    public SootClass getException() {
	return sootexc;
    }

    public static HandlerShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof TrapMethodPosition)) return null;
	Trap trap=((TrapMethodPosition) pos).getTrap();
	Stmt stmt=(Stmt) trap.getHandlerUnit();
	return new HandlerShadowMatch(pos.getContainer(),stmt,trap.getException());
    }

    public AdviceApplication.SJPInfo makeSJPInfo() {
	return new AdviceApplication.SJPInfo
	    ("exception-handler","CatchClauseSignature","makeCatchClauseSig","",stmt);
    }

    public AdviceApplication  doAddAdviceApplication
	(MethodAdviceList mal,AdviceDecl ad,Residue residue) {

	HandlerAdviceApplication aa=new HandlerAdviceApplication(ad,residue,stmt);
        mal.addStmtAdvice(aa);
	return aa;
    }

    public boolean supportsAfter() {
	return false;
    }
}
