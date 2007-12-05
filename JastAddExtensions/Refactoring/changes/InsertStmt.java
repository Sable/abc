package changes;

import java.util.Collection;

import AST.Block;
import AST.List;
import AST.Stmt;

public class InsertStmt extends ASTChange {
	
	private Block block;
	private Collection old_stmts;
	private Collection before;
	private Stmt new_stmt;
	private Collection after;
	
	public InsertStmt(Block block, Collection before, Stmt new_stmt, Collection after) {
		this.block = block;
		this.old_stmts = block.getStmtList().toCollection();
		this.before = before;
		this.new_stmt= new_stmt;
		this.after = after;
	}

	public void apply() {
		List new_stmts = List.ofCollection(before);
		new_stmts.add(new_stmt);
		new_stmts.addAll(after);
		block.setStmtList(new_stmts);
	}

	public String prettyprint() {
		return "change block body";
	}

	public void undo() {
		block.setStmtList(List.ofCollection(old_stmts));
	}

}
