package abc.weaving.matching;

import soot.*;
import soot.jimple.*;
import soot.tagkit.SourceLnPosTag;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;


/** The results of matching at a method call shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class MethodCallShadowMatch extends StmtShadowMatch {
    
    private SootMethod method;
    
    private MethodCallShadowMatch(SootMethod container,Stmt stmt,SootMethod method) {
	super(container,stmt);
	this.method=method;
    }

    public SootMethod getMethod() {
	return method;
    }

    public static MethodCallShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof StmtMethodPosition)) return null;
	Stmt stmt=((StmtMethodPosition) pos).getStmt();
	SootMethod method=null;

	if (stmt instanceof InvokeStmt) {
	    method = ((InvokeStmt) stmt).getInvokeExpr().getMethod();
	} else if(stmt instanceof AssignStmt) {
	    AssignStmt as = (AssignStmt) stmt;
	    Value rhs = as.getRightOp();
	    if(!(rhs instanceof InvokeExpr)) return null;
	    method=((InvokeExpr) rhs).getMethod();
	} else return null;

	if(method.getName().equals(SootMethod.constructorName)) return null;
	// The next one really ought not to happen...
	if(method.getName().equals(SootMethod.staticInitializerName)) return null;

	return new MethodCallShadowMatch(pos.getContainer(),stmt,method);
    }

    public AdviceApplication.SJPInfo makeSJPInfo() {
	return new AdviceApplication.SJPInfo
	    ("method-call","MethodSignature","makeMethodSig","",stmt);
    }

    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AdviceDecl ad,Residue residue) {

	StmtAdviceApplication aa=new StmtAdviceApplication(ad,residue,stmt);
	mal.addStmtAdvice(aa);
	return aa;
    }
}
