package abc.weaving.matching;

import soot.jimple.*;

/** Specifies matching at a particular new statement
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04                                  
 */

public class NewStmtMethodPosition extends StmtMethodPosition {
    private Stmt nextStmt;
    
    public NewStmtMethodPosition(Stmt stmt,Stmt nextStmt) {
	super(stmt);
	this.nextStmt=nextStmt;
    }

    public Stmt getNextStmt() {
	return nextStmt;
    }

}
