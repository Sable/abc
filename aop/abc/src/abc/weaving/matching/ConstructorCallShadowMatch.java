package abc.weaving.matching;

import soot.*;
import soot.jimple.*;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;

/** The results of matching at a new+constructor call shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class ConstructorCallShadowMatch extends StmtShadowMatch {
    
    private SootMethod method;
    
    private ConstructorCallShadowMatch(SootMethod container,Stmt stmt,SootMethod method) {
	super(container,stmt);
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
	// We assume the method we just got must be a constructor, because
	// we've already done the moving stuff around thing.
	// FIXME: Does this break with arrays?
	return new ConstructorCallShadowMatch(pos.getContainer(),current,method);
    }

    public AdviceApplication.SJPInfo makeSJPInfo() {
	return new AdviceApplication.SJPInfo
	    ( "constructor-call","ConstructorSignature","makeConstructorSig","",stmt);
    }

    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AdviceDecl ad,Residue residue) {

	NewStmtAdviceApplication aa=new NewStmtAdviceApplication(ad,residue,stmt);
	mal.addStmtAdvice(aa);
	return aa;
    }
}
