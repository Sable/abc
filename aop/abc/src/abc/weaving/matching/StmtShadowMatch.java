package abc.weaving.matching;

import soot.SootMethod;
import soot.jimple.Stmt;
import abc.weaving.residues.ContextValue;

/** A "stmt" shadow match
 *  @author Ganesh Sittampalam
 */

public abstract class StmtShadowMatch extends ShadowMatch {
    protected Stmt stmt;

    protected StmtShadowMatch(SootMethod container,Stmt stmt) {
	super(container);
	this.stmt=stmt;
    }

    public ShadowMatch getEnclosing() {
	if(stmt.hasTag(abc.soot.util.InPreinitializationTag.name)) return this;
	return new ExecutionShadowMatch(container);
    }

}
