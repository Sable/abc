package abc.weaving.matching;

import soot.jimple.*;

/** Specifies matching at a particular new statement
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04                                  
 */

// FIXME: Can't do this, it makes things think they are StmtMethodPositions
// when they aren't
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
