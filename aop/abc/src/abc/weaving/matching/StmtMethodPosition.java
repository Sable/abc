package abc.weaving.matching;

import soot.jimple.*;
import soot.*;

/** Specifies matching at a particular statement
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04                                  
 */

public class StmtMethodPosition extends MethodPosition {
    private Stmt stmt;

    public StmtMethodPosition(SootMethod container,Stmt stmt) {
	super(container);
	this.stmt=stmt;
    }

    public Stmt getStmt() {
	return stmt;
    }

}
