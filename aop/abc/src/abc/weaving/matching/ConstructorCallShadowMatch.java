package abc.weaving.matching;

import java.util.*;

import polyglot.util.InternalCompilerError;

import soot.*;
import soot.jimple.*;
import soot.tagkit.Host;

import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.Residue;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.JimpleValue;

/** The results of matching at a new+constructor call shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class ConstructorCallShadowMatch extends StmtShadowMatch {
    private SpecialInvokeExpr invoke;
        
    private ConstructorCallShadowMatch(SootMethod container,Stmt stmt,SpecialInvokeExpr invoke) {
	super(container,stmt);
	
	this.invoke=invoke;
    }

    public SootMethodRef getMethodRef() {
	return invoke.getMethodRef();
    }

    public List/*<SootClass>*/ getExceptions() {
	return invoke.getMethodRef().resolve().getExceptions();
    }

    public static ConstructorCallShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof NewStmtMethodPosition)) return null;
	if(abc.main.Debug.v().traceMatcher) System.err.println("ConstructorCall");

	NewStmtMethodPosition stmtMP=(NewStmtMethodPosition) pos;
	Stmt current=stmtMP.getStmt();
	Stmt next=stmtMP.getNextStmt();

	if(!(current instanceof AssignStmt)) return null;
	AssignStmt as = (AssignStmt) current;
	Value rhs = as.getRightOp();
	if(!(rhs instanceof NewExpr)) return null;

	if(!(next instanceof InvokeStmt)) { 
	    // FIXME : improve this behaviour?
	    throw new InternalCompilerError
		("Didn't find an InvokeStmt after a new: "
		 +pos.getContainer()+" "+current+" "+next);
	}
	InvokeExpr iexpr=((InvokeStmt) next).getInvokeExpr();
	if(!(iexpr instanceof SpecialInvokeExpr)) 
	    throw new InternalCompilerError
		("Invoke statement "+next+" after a new statement "+current+" in method "
		 +pos.getContainer()+" wasn't a special invoke");
	SpecialInvokeExpr siexpr=(SpecialInvokeExpr) (((InvokeStmt) next).getInvokeExpr());
	
	StmtShadowMatch.makeArgumentsUniqueLocals(stmtMP.getContainer(), next);
	
	// We assume the method we just got must be a constructor, because
	// we've already done the moving stuff around thing.
	return new ConstructorCallShadowMatch(pos.getContainer(),current,siexpr);
    }

    public Host getHost() {
	return stmt;
    }

    public SJPInfo makeSJPInfo() {
	return new SJPInfo
	    ( "constructor-call","ConstructorSignature",
	      "makeConstructorSig",SJPInfo.makeConstructorSigData(getMethodRef().resolve()),stmt);
    }

    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AbstractAdviceDecl ad,Residue residue) {

	NewStmtAdviceApplication aa=new NewStmtAdviceApplication(ad,residue,stmt);
	mal.addStmtAdvice(aa);
	return aa;
    }

    public ContextValue getTargetContextValue() {
	return null;
    }

    public ContextValue getReturningContextValue() {
	return new JimpleValue(invoke.getBase());
    }

    public List/*<ContextValue>*/ getArgsContextValues() {
	Iterator argsIt=invoke.getArgs().iterator();
	List ret=new LinkedList();
	while(argsIt.hasNext()) 
	    ret.add(new JimpleValue((Value) argsIt.next()));
	return ret;
    }
}
