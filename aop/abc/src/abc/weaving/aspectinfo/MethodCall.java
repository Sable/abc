package abc.weaving.aspectinfo;

import soot.*;
import soot.jimple.*;

import polyglot.util.Position;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Handler for <code>call</code> shadow pointcut with a method pattern. */
public class MethodCall extends ShadowPointcut {
    private MethodPattern pattern;

    public MethodCall(MethodPattern pattern,Position pos) {
	super(pos);
	this.pattern = pattern;
    }

    public MethodPattern getPattern() {
	return pattern;
    }

    static private ShadowType shadowType=new StmtShadowType();
    
    static public void registerShadowType() {
	ShadowPointcut.registerShadowType(shadowType);
    }

    public ShadowType getShadowType() {
	return shadowType;
    }

    protected Residue matchesAt(MethodPosition position) {
	if(!(position instanceof StmtMethodPosition)) return null;
	Stmt stmt=((StmtMethodPosition) position).getStmt();

	// FIXME: Hack should be removed when patterns are added
	if(getPattern()==null) return AlwaysMatch.v;

	if (stmt instanceof InvokeStmt) {
	    SootMethod method = ((InvokeStmt) stmt).getInvokeExpr().getMethod();
	    if(!getPattern().matchesMethod(method)) return null;
	    return AlwaysMatch.v;
	} else if(stmt instanceof AssignStmt) {
	    AssignStmt as = (AssignStmt) stmt;
	    Value rhs = as.getRightOp();
	    if(!(rhs instanceof InvokeExpr)) return null;
	    if(!getPattern().matchesMethod(((InvokeExpr) rhs).getMethod())) 
		return null;
	    return AlwaysMatch.v;
	} else return null;

    }

    public String toString() {
	return "call("+pattern+")";
    }
}
