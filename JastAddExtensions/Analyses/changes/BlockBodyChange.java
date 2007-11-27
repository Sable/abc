package changes;

import java.util.Collection;

import AST.Block;
import AST.List;

public class BlockBodyChange extends ASTChange {
	
	private Block block;
	private Collection old_stmts;
	private Collection new_stmts;
	
	public BlockBodyChange(Block block, Collection stmts) {
		this.block = block;
		this.old_stmts = block.getStmtList().toCollection();
		this.new_stmts = stmts;
	}

	public void apply() {
		block.setStmtList(List.ofCollection(new_stmts));
	}

	public String prettyprint() {
		return "change block body";
	}

	public void undo() {
		block.setStmtList(List.ofCollection(old_stmts));
	}

}
