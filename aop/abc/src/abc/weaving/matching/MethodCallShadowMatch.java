package abc.weaving.matching;

import soot.*;
import soot.jimple.*;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;

/** The results of matching at a new+constructor call shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class MethodCallShadowMatch extends ShadowMatch {
    
    private Stmt stmt;
    private SootMethod method;
    
    private MethodCallShadowMatch(Stmt stmt,SootMethod method) {
	this.stmt=stmt;
	this.method=method;
    }

    public SootMethod getMethod() {
	return method;
    }

    public static MethodCallShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof StmtMethodPosition)) return null;
	Stmt stmt=((StmtMethodPosition) pos).getStmt();

	if (stmt instanceof InvokeStmt) {
	    SootMethod method 
		= ((InvokeStmt) stmt).getInvokeExpr().getMethod();
	    return new MethodCallShadowMatch(stmt,method);
	} else if(stmt instanceof AssignStmt) {
	    AssignStmt as = (AssignStmt) stmt;
	    Value rhs = as.getRightOp();
	    if(!(rhs instanceof InvokeExpr)) return null;
	    return new MethodCallShadowMatch(stmt,
					     ((InvokeExpr) rhs).getMethod());
	} else return null;
    }

    public void addAdviceApplication(MethodAdviceList mal,
				     AdviceDecl ad,
				     Residue residue) {
        mal.addStmtAdvice(new StmtAdviceApplication(ad,residue,stmt));
    }
}
