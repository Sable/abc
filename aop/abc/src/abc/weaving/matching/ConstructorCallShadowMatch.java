package abc.weaving.matching;

import soot.*;
import soot.jimple.*;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;

/** The results of matching at a new+constructor call shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class ConstructorCallShadowMatch extends ShadowMatch {
    
    private Stmt stmt;
    private SootMethod method;
    
    private ConstructorCallShadowMatch(Stmt stmt,SootMethod method) {
	this.stmt=stmt;
	this.method=method;
    }

    public SootMethod getMethod() {
	return method;
    }

    public static ConstructorCallShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof NewStmtMethodPosition)) return null;

	NewStmtMethodPosition stmtMP=(NewStmtMethodPosition) pos;
	Stmt current=stmtMP.getStmt();
	Stmt next=stmtMP.getNextStmt();

	if(!(current instanceof AssignStmt)) return null;
	AssignStmt as = (AssignStmt) current;
	Value rhs = as.getRightOp();
	if(!(rhs instanceof NewExpr)) return null;

	if(!(next instanceof InvokeStmt)) { 
	    // FIXME : improve this behaviour
	    throw new Error
		("INTERNAL ERROR: Didn't find an InvokeStmt after a new");
	}
	SootMethod method=((InvokeStmt) next).getInvokeExpr().getMethod();
	return new ConstructorCallShadowMatch(current,method);
    }

    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue) {
	AdviceApplication.SJPInfo sjpInfo
	    = new AdviceApplication.SJPInfo("constructor-call","makeConstructorSig",null,-1,-1);
        mal.addStmtAdvice(new NewStmtAdviceApplication(ad,residue,sjpInfo,stmt));
    }
}
