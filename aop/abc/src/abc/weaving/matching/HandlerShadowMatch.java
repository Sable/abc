package abc.weaving.matching;

import java.util.*;

import soot.*;
import soot.jimple.*;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.*;

/** The results of matching at a handler shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class HandlerShadowMatch extends StmtShadowMatch {
    
    private SootClass sootexc;

    
    private HandlerShadowMatch(SootMethod container,Stmt stmt,SootClass sootexc) {
	super(container,stmt);
	this.sootexc=sootexc;
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
	// FIXME: dummy string
	return new AdviceApplication.SJPInfo
	    ("exception-handler","CatchClauseSignature","makeCatchClauseSig",
	     "0--Test-java.lang.Exception-<missing>-",stmt);
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

    public ContextValue getTargetContextValue() {
	return null;
    }

    public List/*<ContextValue>*/ getArgsContextValues() {
	ArrayList ret=new ArrayList(1);
	ret.add(new JimpleValue(((IdentityStmt) stmt).getLeftOp()));
	return ret;
    }
}
