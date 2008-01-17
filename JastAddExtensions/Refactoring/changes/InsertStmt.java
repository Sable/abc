package changes;

import AST.Block;
import AST.Stmt;

public class InsertStmt extends changes.ASTChange {
	
	private Block block;
	private int index;
	private Stmt stmt;
	
	public InsertStmt(Block block, int index, Stmt stmt) {
		this.block = block;
		this.index = index;
		this.stmt = stmt;
	}

	public String prettyprint() {
		return "insert statement "+stmt.dumpString()+" into block at line"+index;
	}

	public void undo() {
		block.getStmtList().removeChild(index);
	}

}
