package abc.weaving.matching;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.JimpleValue;
import abc.weaving.residues.Residue;

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
	if(abc.main.Debug.v().traceMatcher) System.err.println("MethodCall");

	Stmt stmt=((StmtMethodPosition) pos).getStmt();

	InvokeExpr invoke;

	if (stmt instanceof InvokeStmt) {
	    InvokeStmt istmt=(InvokeStmt) stmt;
	    invoke=istmt.getInvokeExpr();
	} else if(stmt instanceof AssignStmt) {
	    AssignStmt as = (AssignStmt) stmt;
	    Value rhs = as.getRightOp();
	    if(!(rhs instanceof InvokeExpr)) return null;
	    invoke=(InvokeExpr) rhs;
	} else return null;
	SootMethod method=invoke.getMethod();

	if(!MethodCategory.weaveCalls(method)) return null;

	if(method.getName().equals(SootMethod.constructorName)) return null;
	// The next one really ought not to happen...
	if(method.getName().equals(SootMethod.staticInitializerName)) return null;

	if(abc.main.Debug.v().traceMatcher) System.err.print("Restructuring...");
	// Eagerly restructure non-constructor InvokeStmts to AssignStmts, 
	// because it saves us from having to fix up the AdviceApplications later
	// We may wish to improve this behaviour later.
	if(stmt instanceof InvokeStmt && !(method.getReturnType() instanceof VoidType))
	    stmt=Restructure.getEquivAssignStmt(pos.getContainer(),(InvokeStmt) stmt);

	if(abc.main.Debug.v().traceMatcher) System.err.print("args -> unique locals...");
	StmtShadowMatch.makeArgumentsUniqueLocals(((StmtMethodPosition) pos).getContainer(), stmt);
	if(abc.main.Debug.v().traceMatcher) System.err.println("done");

	return new MethodCallShadowMatch(pos.getContainer(),stmt,invoke,method);
    }

    public SJPInfo makeSJPInfo() {
	return new SJPInfo
	    ("method-call","MethodSignature","makeMethodSig",
	     SJPInfo.makeMethodSigData(method),stmt);
    }

    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AbstractAdviceDecl ad,Residue residue) {

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
    
    public List/*<ContextValue>*/ getArgsContextValues() {
	Iterator argsIt=invoke.getArgs().iterator();
	List ret=new LinkedList();
	while(argsIt.hasNext()) 
	    ret.add(new JimpleValue((Value) argsIt.next()));
	return ret;
    }

}
