package abc.weaving.matching;

import soot.jimple.*;

/** Specifies matching at a particular statement
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04                                  
 */

public class StmtMethodPosition implements MethodPosition {
    private Stmt stmt;
    private Stmt nextStmt;
    
    public StmtMethodPosition(Stmt stmt,Stmt nextStmt) {
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
