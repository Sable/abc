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

	// Eagerly restructure non-constructor InvokeStmts to AssignStmts, 
	// because it saves us from having to fix up the AdviceApplications later
	// We may wish to improve this behaviour later.
	if(stmt instanceof InvokeStmt && !(method.getReturnType() instanceof VoidType))
	    stmt=Restructure.getEquivAssignStmt(pos.getContainer(),(InvokeStmt) stmt);

	makeArgumentsUniqueLocals(((StmtMethodPosition) pos).getContainer(), stmt);

	return new MethodCallShadowMatch(pos.getContainer(),stmt,invoke,method);
    }

	/**
	 * Lazily replaces the arguments of the invokeExpr of stmt with
	 * unique locals and inserts assignment statements before stmt,
	 * assigning the original values to the locals.
	 * Needed for around().
	 * @param method
	 * @param stmt
	 */
	private static void makeArgumentsUniqueLocals(SootMethod method, Stmt stmt) {
		InvokeExpr invokeEx=stmt.getInvokeExpr();
		boolean bDoModify=false;
		{
			Set args=new HashSet(); 
			Iterator it=invokeEx.getArgs().iterator();
			while (it.hasNext()) {
				Value val=(Value)it.next();
				if (!(val instanceof Local)) {
					bDoModify=true;
					break;
				} else {
					if (args.contains(val)) {
						bDoModify=true;
						break;
					}
					args.add(val);
				}
			}
		}
		if (bDoModify) {
			Body body=method.getActiveBody();
			Chain statements=body.getUnits().getNonPatchingChain();
			LocalGeneratorEx lg=new LocalGeneratorEx(body);
			for (int i=0; i<invokeEx.getArgCount(); i++) {
				Value val=invokeEx.getArg(i);
				Local l=lg.generateLocal(invokeEx.getMethod().getParameterType(i),
					"uniqueArgLocal");
				AssignStmt as=Jimple.v().newAssignStmt(l,val);
				statements.insertBefore(as, stmt);
				invokeEx.getArgBox(i).setValue(l);
			}
		}
	}

    public AdviceApplication.SJPInfo makeSJPInfo() {
	// FIXME: dummy string
	return new AdviceApplication.SJPInfo
	    ("method-call","MethodSignature","makeMethodSig",
	     "1-println-java.io.PrintStream-java.lang.String:-arg0:--void-",stmt);
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
