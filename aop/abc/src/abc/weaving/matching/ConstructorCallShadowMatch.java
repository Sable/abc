package abc.weaving.matching;

import java.util.*;

import soot.*;
import soot.jimple.*;

import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.Residue;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.JimpleValue;

/** The results of matching at a new+constructor call shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class ConstructorCallShadowMatch extends StmtShadowMatch {
    private InvokeExpr invoke;
        
    private ConstructorCallShadowMatch(SootMethod container,Stmt stmt,InvokeExpr invoke) {
	super(container,stmt);
	
	this.invoke=invoke;
    }

    public SootMethod getMethod() {
	return invoke.getMethod();
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
	InvokeExpr invoke=((InvokeStmt) next).getInvokeExpr();
	
	StmtShadowMatch.makeArgumentsUniqueLocals(stmtMP.getContainer(), next);
	
	// We assume the method we just got must be a constructor, because
	// we've already done the moving stuff around thing.
	// FIXME: Does this break with arrays?
	return new ConstructorCallShadowMatch(pos.getContainer(),current,invoke);
    }

    public AdviceApplication.SJPInfo makeSJPInfo() {
	// FIXME : dummy details string
	return new AdviceApplication.SJPInfo
	    ( "constructor-call","ConstructorSignature",
	      "makeConstructorSig","1--Test-int:-x:--",stmt);
    }

    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AbstractAdviceDecl ad,Residue residue) {

	NewStmtAdviceApplication aa=new NewStmtAdviceApplication(ad,residue,stmt);
	mal.addStmtAdvice(aa);
	return aa;
    }

    public ContextValue getTargetContextValue() {
	return new JimpleValue(((AssignStmt) stmt).getLeftOp());
    }

    public ContextValue getReturningContextValue() {
	return getTargetContextValue();
    }

    public List/*<ContextValue>*/ getArgsContextValues() {
	Iterator argsIt=invoke.getArgs().iterator();
	List ret=new LinkedList();
	while(argsIt.hasNext()) 
	    ret.add(new JimpleValue((Value) argsIt.next()));
	return ret;
    }
}
