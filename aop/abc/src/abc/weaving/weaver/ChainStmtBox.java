package abc.weaving.weaver;

import soot.jimple.Stmt;
import soot.util.Chain;

/** A container class for a Chain of Stmts generated for a cflow operation.
 *  Singles out a single stmt for use in later control-flow analyses (this
 *  is only required for push/pop and isValid). The stmt chosen does not 
 *  matter, though in isValid care should be taken to select an instruction
 *  that lies on the main control flow from entry. 
 */
public class ChainStmtBox {
	private Chain chain;
	private Stmt stmt;
	public Chain getChain() { return chain; }
	public Stmt getStmt() { return stmt; }
	
	public ChainStmtBox(Chain chain) { this.chain = chain; }
	public ChainStmtBox(Chain chain, Stmt stmt)
	{ this.chain = chain; this.stmt = stmt; }
}

