package abc.weaving.matching;

import soot.jimple.*;
import soot.SootMethod;

/** Specifies matching at a particular new statement
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04                                  
 */


public class NewStmtMethodPosition extends MethodPosition {
    private Stmt stmt;
    private Stmt nextStmt;
    
    public NewStmtMethodPosition(SootMethod container,Stmt stmt,Stmt nextStmt) {
	super(container);
	this.stmt=stmt;
	this.nextStmt=nextStmt;
    }

    public Stmt getStmt() {
	return stmt;
    }

    public Stmt getNextStmt() {
	return nextStmt;
    }

}
