package abc.weaving.matching;

import soot.jimple.*;

/** Specifies matching at a particular statement
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04                                  
 */

public class StmtMethodPosition implements MethodPosition {
    private Stmt stmt;
    
    public StmtMethodPosition(Stmt stmt) {
	this.stmt=stmt;
    }

    public Stmt getStmt() {
	return stmt;
    }

}
