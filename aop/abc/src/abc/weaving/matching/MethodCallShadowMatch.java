package abc.weaving.matching;

import java.util.*;

import soot.*;
import soot.jimple.*;
import soot.tagkit.Host;
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
    
    private SootMethodRef methodref;
    private InvokeExpr invoke;
    
    private MethodCallShadowMatch(SootMethod container,Stmt stmt,
				  InvokeExpr invoke,SootMethodRef methodref) {
	super(container,stmt);
	this.methodref=methodref;
	this.invoke=invoke;
    }

    public SootMethodRef getMethodRef() {
	return methodref;
    }

    public List/*<SootClass>*/ getExceptions() {
	return methodref.resolve().getExceptions();
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
	SootMethodRef methodref=invoke.getMethodRef();

	if(!MethodCategory.weaveCalls(methodref)) return null;

	if(methodref.name().equals(SootMethod.constructorName)) return null;
	// The next one really ought not to happen...
	if(methodref.name().equals(SootMethod.staticInitializerName)) return null;

	if(abc.main.Debug.v().ajcCompliance) {
	    // eliminate super calls, following the specification for such
	    // calls described in 'invokespecial' in the JVM spec

	    SootMethod method=methodref.resolve();

	    // We already know it is not a <init>
	    if(invoke instanceof SpecialInvokeExpr && 
	       !method.isPrivate() && 
	       // this check should be redundant
	       !method.isStatic()) {
		SootClass declaringclass=method.getDeclaringClass();
		SootClass currentclass=pos.getContainer().getDeclaringClass();
		// FIXME: temporary until Soot gets fixed
		Scene.v().releaseActiveHierarchy();
		if(Scene.v().getActiveHierarchy()
		   .isClassSubclassOf(currentclass,declaringclass)) {
		    // Assume ACC_SUPER was set since we have no way of checking
		    // and it's only not set by legacy compilers anyway
		    return null;
		}
	    }
	}

	if(abc.main.Debug.v().traceMatcher) System.err.print("Restructuring...");
	// Eagerly restructure non-constructor InvokeStmts to AssignStmts, 
	// because it saves us from having to fix up the AdviceApplications later
	// We may wish to improve this behaviour later.
	if(stmt instanceof InvokeStmt && !(methodref.returnType() instanceof VoidType))
	    stmt=Restructure.getEquivAssignStmt(pos.getContainer(),(InvokeStmt) stmt);

	if(abc.main.Debug.v().traceMatcher) System.err.print("args -> unique locals...");
	StmtShadowMatch.makeArgumentsUniqueLocals(((StmtMethodPosition) pos).getContainer(), stmt);
	if(abc.main.Debug.v().traceMatcher) System.err.println("done");

	return new MethodCallShadowMatch(pos.getContainer(),stmt,invoke,methodref);
    }

    public Host getHost() {
	return stmt;
    }

    public SJPInfo makeSJPInfo() {
	return new SJPInfo
	    ("method-call","MethodSignature","makeMethodSig",
	     SJPInfo.makeMethodSigData(methodref.resolve()),stmt);
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
	if(methodref.returnType() instanceof VoidType)
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

    public String joinpointName() {
	return "method call";
    }
}
