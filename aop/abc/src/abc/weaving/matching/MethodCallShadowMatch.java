package abc.weaving.matching;

import soot.*;
import soot.jimple.*;
import soot.tagkit.SourceLnPosTag;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.JimpleValue;


/** The results of matching at a method call shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class MethodCallShadowMatch extends StmtShadowMatch {
    
    private SootMethod method;
    private InvokeExpr invoke;
    
    private MethodCallShadowMatch(SootMethod container,Stmt stmt,
				  InvokeExpr invoke,SootMethod method) {
	super(container,stmt);
	this.method=method;
	this.invoke=invoke;
    }

    public SootMethod getMethod() {
	return method;
    }

    public static MethodCallShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof StmtMethodPosition)) return null;
	Stmt stmt=((StmtMethodPosition) pos).getStmt();

	InvokeExpr invoke;

	if (stmt instanceof InvokeStmt) {
	    invoke = ((InvokeStmt) stmt).getInvokeExpr();
	} else if(stmt instanceof AssignStmt) {
	    AssignStmt as = (AssignStmt) stmt;
	    Value rhs = as.getRightOp();
	    if(!(rhs instanceof InvokeExpr)) return null;
	    invoke=(InvokeExpr) rhs;
	} else return null;
	SootMethod method=invoke.getMethod();

	if(method.getName().equals(SootMethod.constructorName)) return null;
	// The next one really ought not to happen...
	if(method.getName().equals(SootMethod.staticInitializerName)) return null;

	return new MethodCallShadowMatch(pos.getContainer(),stmt,invoke,method);
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

    public ContextValue getTargetContextValue() {
	if(invoke instanceof InstanceInvokeExpr) {
	    InstanceInvokeExpr ii=(InstanceInvokeExpr) invoke;
	    return new JimpleValue(ii.getBase());
	} else return null;
    }
}
