package abc.soot.util;

import java.util.*;
import polyglot.util.InternalCompilerError;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.base.*;

public class AspectJExceptionChecker extends ExceptionChecker {
    public AspectJExceptionChecker(ExceptionCheckerErrorReporter r) {
	super(r);
    }
    
    private Body realBody;

    private static class Context {
	public Context(Body body,Stmt stmt) {
	    this.body=body;
	    this.stmt=stmt;
	}

	public Body body;
	public Stmt stmt;
    }

    private Stack/*<Context>*/ contextStack=new Stack();

    protected void internalTransform(Body b, String phaseName, Map options){
	if(b.getMethod().hasTag(DisableExceptionCheckTag.name)) return;
	realBody=b;
	doCheck(b,b.getUnits().iterator());
	realBody=null;
	if(!contextStack.empty())
	    throw new InternalCompilerError("Uneven use of stack in AspectJExceptionChecker");
    }

    protected void doCheck(Body b,Iterator it) {
        while (it.hasNext()){
            Stmt s = (Stmt)it.next();
	    if(s.hasTag(DisableExceptionCheckTag.name)) {
	    } else if(s.hasTag(RedirectedExceptionSpecTag.name)) {
		RedirectedExceptionSpecTag redir=(RedirectedExceptionSpecTag) s.getTag(RedirectedExceptionSpecTag.name);
		contextStack.push(new Context(b,s));
		doCheck(redir.body,redir.stmts.iterator());
		contextStack.pop();
	    }
            else if (s instanceof ThrowStmt){
                ThrowStmt ts = (ThrowStmt)s;
                checkThrow(b, ts);
            }
            else if (s instanceof InvokeStmt){
                InvokeStmt is = (InvokeStmt)s;
                checkInvoke(b, is);
            }
            else if ((s instanceof AssignStmt) && (((AssignStmt)s).getRightOp() instanceof InvokeExpr)){
                InvokeExpr ie = (InvokeExpr)((AssignStmt)s).getRightOp();
                checkInvokeExpr(b, ie, s);
            }
        }
    }
    
    protected boolean isThrowDeclared(Body b, SootClass throwClass) {
	return super.isThrowDeclared(realBody,throwClass);
    }

    protected boolean isExceptionCaught(Body b, Stmt s, RefType throwType) {
	if(super.isExceptionCaught(b,s,throwType)) return true;
	Iterator contextIt=contextStack.iterator();
	while(contextIt.hasNext()) {
	    Context context=(Context) contextIt.next();
	    if(super.isExceptionCaught(context.body,context.stmt, throwType)) return true;
	}
	return false;
    }

}