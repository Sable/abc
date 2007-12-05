package changes;

import java.util.Collection;

import AST.Block;
import AST.List;

public class InsertBlock extends ASTChange {
	
	private Block block;
	private Collection old_stmts;
	private Collection before;
	private Collection new_block;
	private Collection after;
	
	public InsertBlock(Block block, Collection before, Collection new_block, Collection after) {
		this.block = block;
		this.old_stmts = block.getStmtList().toCollection();
		this.before = before;
		this.new_block= new_block;
		this.after = after;
	}

	public void apply() {
		List new_stmts = List.ofCollection(before);
		new_stmts.add(new Block(List.ofCollection(new_block)));
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
