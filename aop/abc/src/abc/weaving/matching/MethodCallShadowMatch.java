package abc.weaving.matching;

import soot.*;
import soot.jimple.*;
import soot.tagkit.SourceLnPosTag;

import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.residues.Residue;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.JimpleValue;
import abc.soot.util.Restructure;

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

	// Eagerly restructure non-constructor InvokeStmts to AssignStmts, 
	// because it saves us from having to fix up the AdviceApplications later
	// We may wish to improve this behaviour later.
	if (stmt instanceof InvokeStmt) {

	    InvokeStmt istmt=(InvokeStmt) stmt;
	    SootMethod m=istmt.getInvokeExpr().getMethod();

	    if(!(m.getReturnType() instanceof VoidType) && 
	       !(m.getName().equals(SootMethod.constructorName)))

		stmt=Restructure.getEquivAssignStmt(pos.getContainer(),istmt);

	    //	    invoke = ((InvokeStmt) stmt).getInvokeExpr();
	}
	if(stmt instanceof AssignStmt) {
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

    public ContextValue getReturningContextValue() {
	if(method.getReturnType() instanceof VoidType)
	    return super.getReturningContextValue();  // null value

	// This shouldn't get triggered as long as we are eagerly restructuring
	// in the matcher above
	if(stmt instanceof InvokeStmt) 
	    stmt=Restructure.getEquivAssignStmt(container,(InvokeStmt) stmt);

	AssignStmt astmt=(AssignStmt) stmt;

	return new JimpleValue(astmt.getLeftOp());
    }
}
