package changes;

import AST.Block;
import AST.Stmt;

public class MoveStmt extends ASTChange {
	
	private Block block;
	private int old_index;
	private int index;
	private Stmt stmt;
	
	public MoveStmt(Block block, Stmt stmt, int index) {
		this.block = block;
		this.old_index = block.getIndexOfChild(stmt);
		this.index = index;
		this.stmt = stmt;
	}

	public String prettyprint() {
		return "move statement "+stmt.dumpString()+" to line"+index;
	}

	public void undo() {
		block.getStmtList().moveChild(index, old_index);
	}

}
